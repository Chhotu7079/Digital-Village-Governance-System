package com.dvgs.auth.repository;

import com.dvgs.auth.domain.RefreshToken;
import com.dvgs.auth.domain.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByToken(String token);
    long deleteByUser(User user);
    long deleteByUserAndDeviceId(User user, String deviceId);
}
