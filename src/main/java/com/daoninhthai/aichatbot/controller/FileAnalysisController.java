package com.daoninhthai.aichatbot.controller;

import com.daoninhthai.aichatbot.dto.FileAnalysisRequest;
import com.daoninhthai.aichatbot.dto.FileAnalysisResponse;
import com.daoninhthai.aichatbot.service.FileAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/analyze")
@RequiredArgsConstructor
@Tag(name = "File Analysis", description = "AI-powered file analysis supporting multiple formats")
public class FileAnalysisController {

    private final FileAnalysisService fileAnalysisService;

    @PostMapping(value = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Analyze an uploaded file (PDF, DOCX, TXT, images, code)")
    public ResponseEntity<FileAnalysisResponse> analyzeFile(
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "question", required = false) String question,
            @RequestParam(value = "analysisType", defaultValue = "SUMMARIZE") String analysisType) {

        FileAnalysisRequest request = FileAnalysisRequest.builder()
                .question(question)
                .analysisType(FileAnalysisRequest.AnalysisType.valueOf(analysisType.toUpperCase()))
                .build();

        return ResponseEntity.ok(fileAnalysisService.analyzeDocument(file, request));
    }

    @PostMapping(value = "/code", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Analyze a code file with AI assistance")
    public ResponseEntity<FileAnalysisResponse> analyzeCode(
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "question", required = false) String question) {
        return ResponseEntity.ok(fileAnalysisService.analyzeCode(file, question));
    }

    @PostMapping("/url")
    @Operation(summary = "Analyze content from a URL (web pages, online documents)")
    public ResponseEntity<FileAnalysisResponse> analyzeUrl(
            @RequestParam("url") String url,
            @RequestParam(value = "question", required = false) String question) {
        return ResponseEntity.ok(fileAnalysisService.analyzeUrl(url, question));
    }
}
