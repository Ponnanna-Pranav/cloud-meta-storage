package com.cloudstorage.backend.controller;

import com.cloudstorage.backend.service.FileService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/share")
@CrossOrigin(origins = "*")
public class ShareController {

    private final FileService fileService;

    public ShareController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/{token}/view")
    public Map<String, Object> view(@PathVariable String token) {
        return fileService.viewSharedFile(token);
    }

    @GetMapping("/{token}/download")
    public Map<String, Object> download(@PathVariable String token) {
        return fileService.downloadSharedFile(token);
    }
}
