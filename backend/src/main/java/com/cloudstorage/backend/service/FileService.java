package com.cloudstorage.backend.service;

import com.cloudstorage.backend.model.*;
import com.cloudstorage.backend.repository.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class FileService {

    private final SupabaseStorageService storageService;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final FolderRepository folderRepository;
    private final ShareRepository shareRepository;

    public FileService(
            SupabaseStorageService storageService,
            FileRepository fileRepository,
            UserRepository userRepository,
            FolderRepository folderRepository,
            ShareRepository shareRepository
    ) {
        this.storageService = storageService;
        this.fileRepository = fileRepository;
        this.userRepository = userRepository;
        this.folderRepository = folderRepository;
        this.shareRepository = shareRepository;
    }

    // =========================================================
    // 🔼 UPLOAD (CONTROLLER COMPATIBLE)
    // =========================================================
        public void uploadMultipart(MultipartFile file, UUID folderId, User user) throws Exception {

        Folder folder = null;
        if (folderId != null) {
                folder = folderRepository.findById(folderId)
                        .orElseThrow(() -> new RuntimeException("Folder not found"));
        }

        String storageKey = uploadToStorage(
                file.getBytes(),
                file.getOriginalFilename(),
                file.getContentType()
        );

        FileEntity entity = new FileEntity();
        // ❌ NO setId() EVER
        entity.setName(file.getOriginalFilename());
        entity.setType(file.getContentType());
        entity.setSize(file.getSize());
        entity.setStorageKey(storageKey);
        entity.setOwner(user);
        entity.setFolder(folder);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setDeleted(false);

        fileRepository.save(entity);   // Hibernate generates ID
        }


    // =========================================================
    // REAL STORAGE UPLOAD
    // =========================================================
    public String uploadToStorage(byte[] bytes, String name, String type) {
        String safeName = sanitize(name);
        return storageService.upload(bytes, safeName, type);
    }

  

    // =========================================================
    // LIST FILES
    // =========================================================
    public List<Map<String, Object>> listFiles(String email, UUID folderId) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<FileEntity> files = folderId == null
                ? fileRepository.findByOwnerAndFolderIsNullAndDeletedFalse(user)
                : fileRepository.findByOwnerAndFolderAndDeletedFalse(
                        user,
                        folderRepository.findById(folderId)
                                .orElseThrow(() -> new RuntimeException("Folder not found"))
                );

        List<Map<String, Object>> result = new ArrayList<>();
        for (FileEntity f : files) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", f.getId());
            map.put("name", f.getName());
            map.put("size", f.getSize());
            map.put("createdAt", f.getCreatedAt());
            result.add(map);
        }
        return result;
    }

    // =========================================================
    // VIEW
    // =========================================================
    public Map<String, Object> viewFile(UUID fileId, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        FileEntity file = fileRepository
                .findByIdAndOwnerAndDeletedFalse(fileId, user)
                .orElseThrow(() -> new RuntimeException("File not found"));

        return storageService.createSignedViewUrl(file.getStorageKey());
    }

    // =========================================================
    // DOWNLOAD
    // =========================================================
    public Map<String, Object> downloadFile(UUID fileId, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        FileEntity file = fileRepository
                .findByIdAndOwnerAndDeletedFalse(fileId, user)
                .orElseThrow(() -> new RuntimeException("File not found"));

        return storageService.createSignedDownloadUrl(file.getStorageKey());
    }

    // =========================================================
    // FOLDER ZIP
    // =========================================================
    public byte[] downloadFolderZip(UUID folderId, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("Folder not found"));

        List<FileEntity> files =
                fileRepository.findByOwnerAndFolderAndDeletedFalse(user, folder);

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ZipOutputStream zip = new ZipOutputStream(baos);

            for (FileEntity f : files) {
                zip.putNextEntry(new ZipEntry(f.getName()));
                zip.write(storageService.downloadFileBytes(f.getStorageKey()));
                zip.closeEntry();
            }

            zip.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("ZIP failed", e);
        }
    }

    // =========================================================
    // SOFT DELETE
    // =========================================================
    public void deleteFile(UUID fileId, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        FileEntity file = fileRepository
                .findByIdAndOwner(fileId, user)
                .orElseThrow(() -> new RuntimeException("File not found"));

        if (file.isDeleted()) return;

        file.setDeleted(true);
        file.setDeletedAt(LocalDateTime.now());
        file.setOriginalFolder(file.getFolder());
        file.setFolder(null);

        fileRepository.save(file);
    }

    // =========================================================
    // RESTORE
    // =========================================================
    public void restoreFile(UUID fileId, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        FileEntity file = fileRepository
                .findByIdAndOwner(fileId, user)
                .orElseThrow(() -> new RuntimeException("File not found"));

        file.setDeleted(false);
        file.setDeletedAt(null);
        file.setFolder(file.getOriginalFolder());
        file.setOriginalFolder(null);

        fileRepository.save(file);
    }

    // =========================================================
    // LIST TRASH
    // =========================================================
    public List<Map<String, Object>> listTrash(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<FileEntity> files =
                fileRepository.findByOwnerAndDeletedTrue(user);

        List<Map<String, Object>> response = new ArrayList<>();
        for (FileEntity f : files) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", f.getId());
            map.put("name", f.getName());
            map.put("size", f.getSize());
            map.put("deletedAt", f.getDeletedAt());
            response.add(map);
        }
        return response;
    }

    // =========================================================
    // PERMANENT DELETE
    // =========================================================
    public void permanentlyDelete(UUID fileId, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        FileEntity file = fileRepository
                .findByIdAndOwner(fileId, user)
                .orElseThrow(() -> new RuntimeException("File not found"));

        storageService.deleteFromStorage(file.getStorageKey());
        fileRepository.delete(file);
    }

    // =========================================================
    // EMPTY TRASH
    // =========================================================
    public void emptyTrash(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<FileEntity> trashed =
                fileRepository.findByOwnerAndDeletedTrue(user);

        for (FileEntity f : trashed) {
            storageService.deleteFromStorage(f.getStorageKey());
            fileRepository.delete(f);
        }
    }

    // =========================================================
    // AUTO CLEAN (30 DAYS)
    // =========================================================
    @Scheduled(cron = "0 0 3 * * ?")
    public void autoCleanTrash() {

        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);

        List<FileEntity> expired =
                fileRepository.findByDeletedTrueAndDeletedAtBefore(cutoff);

        for (FileEntity f : expired) {
            storageService.deleteFromStorage(f.getStorageKey());
            fileRepository.delete(f);
        }
    }

    // =========================================================
    // SHARED VIEW
    // =========================================================
    public Map<String, Object> viewSharedFile(String token) {

        Share share = shareRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid link"));

        if (share.getExpiresAt() != null &&
                share.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Link expired");
        }

        return storageService.createSignedViewUrl(
                share.getFile().getStorageKey()
        );
    }

    // =========================================================
    // SHARED DOWNLOAD
    // =========================================================
    public Map<String, Object> downloadSharedFile(String token) {

        Share share = shareRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid link"));

        return storageService.createSignedDownloadUrl(
                share.getFile().getStorageKey()
        );
    }

    // =========================================================
    // SANITIZER
    // =========================================================
    private String sanitize(String name) {
        return name.replaceAll("[^a-zA-Z0-9\\.\\-_]", "_");
    }
}
