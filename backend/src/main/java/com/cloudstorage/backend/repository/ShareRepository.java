package com.cloudstorage.backend.repository;

import com.cloudstorage.backend.model.Share;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ShareRepository extends JpaRepository<Share, UUID> {

    Optional<Share> findByToken(String token);
}
