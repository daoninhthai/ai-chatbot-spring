package com.daoninhthai.aichatbot.repository;

import com.daoninhthai.aichatbot.entity.PromptTemplate;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PromptTemplateRepository extends JpaRepository<PromptTemplate, Long> {

    List<PromptTemplate> findByCategory(String category);

    List<PromptTemplate> findByIsPublicTrue();

    List<PromptTemplate> findByCreatedBy(String createdBy);

    @Query("SELECT pt FROM PromptTemplate pt WHERE pt.isPublic = true OR pt.createdBy = :userId ORDER BY pt.usageCount DESC")
    List<PromptTemplate> findAccessibleTemplates(String userId);

    @Query("SELECT pt FROM PromptTemplate pt ORDER BY pt.usageCount DESC")
    List<PromptTemplate> findPopularTemplates(Pageable pageable);

    @Query("SELECT pt FROM PromptTemplate pt WHERE LOWER(pt.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(pt.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<PromptTemplate> searchTemplates(String query);

    List<PromptTemplate> findByCategoryAndIsPublicTrue(String category);
}
