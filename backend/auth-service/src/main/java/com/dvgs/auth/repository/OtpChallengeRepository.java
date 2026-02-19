package com.dvgs.auth.repository;

import com.dvgs.auth.domain.OtpChallenge;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OtpChallengeRepository extends JpaRepository<OtpChallenge, UUID> {
    Optional<OtpChallenge> findTopByPhoneNumberOrderByCreatedAtDesc(String phoneNumber);
    void deleteByExpiresAtBefore(Instant cutoff);
    long countByPhoneNumberAndCreatedAtAfter(String phoneNumber, Instant createdAfter);
}
