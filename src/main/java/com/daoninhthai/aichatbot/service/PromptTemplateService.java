package com.daoninhthai.aichatbot.service;

import com.daoninhthai.aichatbot.dto.request.PromptTemplateRequest;
import com.daoninhthai.aichatbot.dto.response.PromptTemplateResponse;
import com.daoninhthai.aichatbot.entity.PromptTemplate;
import com.daoninhthai.aichatbot.exception.ResourceNotFoundException;
import com.daoninhthai.aichatbot.repository.PromptTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Service for managing prompt templates: CRUD, search, rendering with variable substitution.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PromptTemplateService {

    private final PromptTemplateRepository templateRepository;

    @Transactional
    public PromptTemplateResponse createTemplate(PromptTemplateRequest request, String userId) {
        PromptTemplate template = PromptTemplate.builder()
                .name(request.getName())
                .description(request.getDescription())
                .content(request.getContent())
                .category(request.getCategory())
                .variables(request.getVariables())
                .isPublic(request.isPublic())
                .createdBy(userId)
                .build();

        template = templateRepository.save(template);
        log.info("Prompt template created: {} by user {}", template.getId(), userId);
        return PromptTemplateResponse.from(template);
    }

    @Transactional(readOnly = true)
    public PromptTemplateResponse getTemplate(Long id) {
        PromptTemplate template = findById(id);
        return PromptTemplateResponse.from(template);
    }

    @Transactional(readOnly = true)
    public List<PromptTemplateResponse> getAllTemplates() {
        return templateRepository.findAll().stream()
                .map(PromptTemplateResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PromptTemplateResponse> getTemplatesByCategory(String category) {
        return templateRepository.findByCategory(category).stream()
                .map(PromptTemplateResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PromptTemplateResponse> searchTemplates(String query) {
        return templateRepository.searchTemplates(query).stream()
                .map(PromptTemplateResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PromptTemplateResponse> getPopularTemplates(int limit) {
        return templateRepository.findPopularTemplates(PageRequest.of(0, limit)).stream()
                .map(PromptTemplateResponse::from)
                .toList();
    }

    @Transactional
    public PromptTemplateResponse updateTemplate(Long id, PromptTemplateRequest request) {
        PromptTemplate template = findById(id);
        template.setName(request.getName());
        template.setDescription(request.getDescription());
        template.setContent(request.getContent());
        template.setCategory(request.getCategory());
        template.setVariables(request.getVariables());
        template.setPublic(request.isPublic());

        template = templateRepository.save(template);
        return PromptTemplateResponse.from(template);
    }

    @Transactional
    public void deleteTemplate(Long id) {
        PromptTemplate template = findById(id);
        templateRepository.delete(template);
        log.info("Prompt template deleted: {}", id);
    }

    /**
     * Render a template by substituting {{variable}} placeholders with provided values.
     */
    @Transactional
    public String renderTemplate(Long id, Map<String, String> variables) {
        PromptTemplate template = findById(id);

        String rendered = template.getContent();
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            rendered = rendered.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }

        // Increment usage counter
        template.incrementUsage();
        templateRepository.save(template);

        log.debug("Template {} rendered with {} variables", id, variables.size());
        return rendered;
    }

    private PromptTemplate findById(Long id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PromptTemplate", id));
    }
}
