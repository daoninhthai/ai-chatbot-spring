package com.daoninhthai.aichatbot.service;

import com.daoninhthai.aichatbot.dto.FileAnalysisRequest;
import com.daoninhthai.aichatbot.dto.FileAnalysisResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for analyzing uploaded files and URLs using AI.
 * Supports PDF, DOCX, TXT, images, code files, and web URLs.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileAnalysisService {

    private final ChatClient chatClient;
    private final TextExtractionService textExtractionService;

    /**
     * Analyze a document by extracting its text and sending it to the AI with context.
     */
    public FileAnalysisResponse analyzeDocument(MultipartFile file, FileAnalysisRequest request) {
        long startTime = System.currentTimeMillis();

        String extractedText = textExtractionService.extractText(file);
        Map<String, Object> metadata = textExtractionService.extractMetadata(file);

        String prompt = buildAnalysisPrompt(request, extractedText);

        String analysis = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        long processingTime = System.currentTimeMillis() - startTime;

        return FileAnalysisResponse.builder()
                .analysis(analysis)
                .extractedText(truncateText(extractedText, 2000))
                .confidence(calculateConfidence(extractedText))
                .metadata(metadata)
                .fileName(file.getOriginalFilename())
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .analysisType(request.getAnalysisType() != null
                        ? request.getAnalysisType().name() : "SUMMARIZE")
                .processingTimeMs(processingTime)
                .build();
    }

    /**
     * Analyze code content specifically.
     */
    public FileAnalysisResponse analyzeCode(MultipartFile file, String question) {
        long startTime = System.currentTimeMillis();
        String code = textExtractionService.extractText(file);
        Map<String, Object> metadata = textExtractionService.extractMetadata(file);

        String prompt = String.format("""
                Analyze the following source code and answer the question.

                Code:
                ```
                %s
                ```

                Question: %s

                Provide a detailed analysis including:
                1. Code structure and purpose
                2. Key patterns and practices used
                3. Potential improvements or issues
                4. Answer to the specific question
                """, code, question != null ? question : "What does this code do?");

        String analysis = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        return FileAnalysisResponse.builder()
                .analysis(analysis)
                .extractedText(truncateText(code, 2000))
                .confidence(0.85)
                .metadata(metadata)
                .fileName(file.getOriginalFilename())
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .analysisType("CODE_ANALYSIS")
                .processingTimeMs(System.currentTimeMillis() - startTime)
                .build();
    }

    private String buildAnalysisPrompt(FileAnalysisRequest request, String extractedText) {
        FileAnalysisRequest.AnalysisType type = request.getAnalysisType() != null
                ? request.getAnalysisType()
                : FileAnalysisRequest.AnalysisType.SUMMARIZE;

        String instruction = switch (type) {
            case SUMMARIZE -> "Provide a comprehensive summary of the following document. " +
                    "Highlight key points, main arguments, and conclusions.";
            case EXTRACT -> "Extract all key information, data points, names, dates, " +
                    "and important facts from the following document.";
            case QA -> "Based on the following document, answer this question: " +
                    (request.getQuestion() != null ? request.getQuestion() : "What is this document about?");
            case TRANSLATE -> "Translate the following document to " +
                    (request.getTargetLanguage() != null ? request.getTargetLanguage() : "English") + ".";
        };

        return instruction + "\n\nDocument content:\n" + extractedText;
    }

    private double calculateConfidence(String text) {
        if (text == null || text.isEmpty()) return 0.0;
        if (text.length() < 50) return 0.3;
        if (text.length() < 200) return 0.6;
        return 0.9;
    }

    /**
     * Analyze content from a URL by extracting text using Tika and sending to AI.
     */
    public FileAnalysisResponse analyzeUrl(String url, String question) {
        long startTime = System.currentTimeMillis();
        log.info("Analyzing URL: {}", url);

        try {
            UrlResource resource = new UrlResource(new URL(url));
            TikaDocumentReader reader = new TikaDocumentReader(resource);
            var documents = reader.get();
            String extractedText = documents.stream()
                    .map(org.springframework.ai.document.Document::getContent)
                    .collect(Collectors.joining("\n\n"));

            String prompt = String.format("""
                    Analyze the following content extracted from URL: %s

                    Content:
                    %s

                    Question: %s

                    Provide a detailed analysis addressing the question.
                    """, url, truncateText(extractedText, 8000),
                    question != null ? question : "What is this content about?");

            String analysis = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("sourceUrl", url);
            metadata.put("extractedLength", extractedText.length());

            return FileAnalysisResponse.builder()
                    .analysis(analysis)
                    .extractedText(truncateText(extractedText, 2000))
                    .confidence(calculateConfidence(extractedText))
                    .metadata(metadata)
                    .fileName(url)
                    .contentType("text/html")
                    .fileSize(extractedText.length())
                    .analysisType("URL_ANALYSIS")
                    .processingTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        } catch (MalformedURLException e) {
            log.error("Invalid URL: {}", url, e);
            throw new RuntimeException("Invalid URL: " + url, e);
        }
    }

    /**
     * Analyze a document with a specific question (simplified interface for MultipartFile + question).
     */
    public FileAnalysisResponse analyzeDocumentWithQuestion(MultipartFile file, String question) {
        FileAnalysisRequest request = FileAnalysisRequest.builder()
                .question(question)
                .analysisType(FileAnalysisRequest.AnalysisType.QA)
                .build();
        return analyzeDocument(file, request);
    }

    private String truncateText(String text, int maxLength) {
        if (text == null) return null;
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength) + "... [truncated]";
    }
}
