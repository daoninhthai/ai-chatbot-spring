package com.daoninhthai.aichatbot.service;

import com.daoninhthai.aichatbot.dto.response.DocumentResponse;
import com.daoninhthai.aichatbot.entity.Document;
import com.daoninhthai.aichatbot.entity.User;
import com.daoninhthai.aichatbot.exception.BadRequestException;
import com.daoninhthai.aichatbot.exception.ResourceNotFoundException;
import com.daoninhthai.aichatbot.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final VectorStore vectorStore;

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain",
            "text/markdown",
            "text/csv"
    );

    @Transactional
    public DocumentResponse uploadDocument(MultipartFile file, User user) {
        validateFile(file);

        Document document = Document.builder()
                .user(user)
                .fileName(file.getOriginalFilename())
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .status(Document.ProcessingStatus.PROCESSING)
                .build();
        document = documentRepository.save(document);

        // Process asynchronously so the upload returns immediately
        processDocumentAsync(document.getId(), file.getResource());

        return DocumentResponse.from(document);
    }

    @Async
    public void processDocumentAsync(Long documentId, Resource fileResource) {
        try {
            log.info("Processing document: {}", documentId);

            // Read document using Apache Tika (handles PDF, DOCX, TXT, etc.)
            DocumentReader reader = new TikaDocumentReader(fileResource);
            List<org.springframework.ai.document.Document> rawDocs = reader.get();

            // Split into chunks for better embedding quality
            TokenTextSplitter splitter = new TokenTextSplitter(
                    800,    // default chunk size in tokens
                    350,    // overlap to maintain context between chunks
                    5,      // min chunk size
                    10000,  // max chunk size
                    true    // keep separator
            );
            List<org.springframework.ai.document.Document> chunks = splitter.apply(rawDocs);

            // Add metadata to each chunk for filtering
            chunks.forEach(chunk -> {
                chunk.getMetadata().put("documentId", documentId.toString());
            });

            // Store embeddings in pgvector
            vectorStore.add(chunks);

            // Update document status
            Document document = documentRepository.findById(documentId).orElseThrow();
            document.setStatus(Document.ProcessingStatus.INDEXED);
            document.setChunkCount(chunks.size());
            documentRepository.save(document);

            log.info("Document {} processed: {} chunks indexed", documentId, chunks.size());

        } catch (Exception e) {
            log.error("Failed to process document {}: {}", documentId, e.getMessage());
            documentRepository.findById(documentId).ifPresent(doc -> {
                doc.setStatus(Document.ProcessingStatus.FAILED);
                doc.setErrorMessage(e.getMessage());
                documentRepository.save(doc);
            });
        }
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> getUserDocuments(User user) {
        return documentRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(DocumentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public DocumentResponse getDocument(Long id, User user) {
        Document document = documentRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Document", id));
        return DocumentResponse.from(document);
    }

    @Transactional
    public void deleteDocument(Long id, User user) {
        Document document = documentRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Document", id));

        // Remove vectors from the vector store
        try {
            vectorStore.delete(List.of("documentId == '" + id + "'"));
        } catch (Exception e) {
            log.warn("Could not delete vectors for document {}: {}", id, e.getMessage());
        }

        documentRepository.delete(document);
        log.info("Document deleted: {}", id);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }
        if (file.getContentType() == null || !ALLOWED_TYPES.contains(file.getContentType())) {
            throw new BadRequestException("File type not supported. Allowed: PDF, DOCX, TXT, MD, CSV");
        }
        if (file.getSize() > 20 * 1024 * 1024) {
            throw new BadRequestException("File size exceeds 20MB limit");
        }
    }
}
