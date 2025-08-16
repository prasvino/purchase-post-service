package com.app.platform.repo;

import com.app.platform.entity.Platform;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PlatformRepository extends JpaRepository<Platform, UUID> {}
