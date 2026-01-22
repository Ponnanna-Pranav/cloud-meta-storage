package com.cloudstorage.backend.service;

import com.cloudstorage.backend.model.Share;
import com.cloudstorage.backend.repository.ShareRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class ShareService {

    private final ShareRepository shareRepository;
    private final SupabaseStorageService storageService;

    public ShareService(
            ShareRepository shareRepository,
            SupabaseStorageService storageService
    ) {
        this.shareRepository = shareRepository;
        this.storageService = storageService;
    }

    /**
     * ✅ ACCESS SHARED FILE
     */
    public Map<String, Object> accessSharedFile(String token) {

        Share share = shareRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid share link"));

        if (share.getExpiresAt() != null &&
                share.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Share link expired");
        }

        return storageService.createSignedDownloadUrl(
                share.getFile().getStorageKey()
        );
    }
}
