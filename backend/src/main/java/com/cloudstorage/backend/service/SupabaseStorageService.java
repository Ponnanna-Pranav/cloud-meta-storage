package com.cloudstorage.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

@Service
public class SupabaseStorageService {

    private final WebClient webClient;
    private final String supabaseUrl;

    @Value("${supabase.bucket}")
    private String bucket;

    public SupabaseStorageService(
            @Value("${supabase.url}") String supabaseUrl,
            @Value("${supabase.service.key}") String serviceKey
    ) {
        this.supabaseUrl = supabaseUrl;
        this.webClient = WebClient.builder()
                .baseUrl(supabaseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + serviceKey)
                .defaultHeader("apikey", serviceKey)
                .build();
    }

    // =========================
    // 🔼 REAL UPLOAD (NO SIGNED URL)
    // =========================
    public String upload(byte[] bytes, String filename, String contentType) {

        String storageKey = UUID.randomUUID() + "-" + filename;

        webClient.put()
                .uri("/storage/v1/object/" + bucket + "/" + storageKey)
                .contentType(MediaType.parseMediaType(contentType))
                .bodyValue(bytes)
                .retrieve()
                .toBodilessEntity()
                .block();

        return storageKey;
    }

    // =========================
    // ⬇️ DOWNLOAD
    // =========================
    public Map<String, Object> createSignedDownloadUrl(String storageKey) {

        Map<String, Object> response = webClient.post()
                .uri("/storage/v1/object/sign/" + bucket + "/" + storageKey)
                .bodyValue(Map.of("expiresIn", 3600))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        String disposition = URLEncoder.encode("attachment", StandardCharsets.UTF_8);

        String downloadUrl =
                supabaseUrl +
                        "/storage/v1" +
                        response.get("signedURL") +
                        "&response-content-disposition=" +
                        disposition;

        return Map.of("downloadUrl", downloadUrl);
    }

    // =========================
    // 👁 VIEW
    // =========================
    public Map<String, Object> createSignedViewUrl(String storageKey) {

        Map<String, Object> response = webClient.post()
                .uri("/storage/v1/object/sign/" + bucket + "/" + storageKey)
                .bodyValue(Map.of("expiresIn", 3600))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        return Map.of(
                "viewUrl",
                supabaseUrl + "/storage/v1" + response.get("signedURL")
        );
    }

    // =========================
    // ❌ DELETE
        // =========================
        public void deleteFromStorage(String storageKey) {

        webClient.delete()
                .uri("/storage/v1/object/" + bucket + "/" + storageKey)
                .retrieve()
                .toBodilessEntity()
                .block();
        }



    // =========================
    // 📂 RAW BYTES (ZIP)
    // =========================
    public byte[] downloadFileBytes(String storageKey) {

        return webClient.get()
                .uri("/storage/v1/object/" + bucket + "/" + storageKey)
                .retrieve()
                .bodyToMono(byte[].class)
                .block();
    }
}
