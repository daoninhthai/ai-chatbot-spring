package com.daoninhthai.aichatbot.service.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.function.Function;

/**
 * URL summary tool that takes a URL and returns a placeholder summary.
 * In production, this would fetch and analyze the URL content.
 */
@Slf4j
@Component
public class UrlSummaryTool implements Function<String, String> {

    @Override
    public String apply(String url) {
        log.debug("URL summary tool invoked for: {}", url);
        try {
            URI uri = URI.create(url.trim());
            String host = uri.getHost();
            String path = uri.getPath();

            return String.format(
                    "Summary of %s:\n" +
                    "- Domain: %s\n" +
                    "- Path: %s\n" +
                    "- Note: Full content fetching would be performed in production. " +
                    "This is a placeholder summary for the URL: %s",
                    url, host, path, url);
        } catch (Exception e) {
            log.error("Failed to process URL: {}", url, e);
            return "Error: Invalid URL provided - " + url;
        }
    }
}
