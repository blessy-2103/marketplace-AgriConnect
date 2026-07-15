package com.agriconnect.repository;

import com.agriconnect.model.AuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AuthTokenRepository extends JpaRepository<AuthToken, Long> {

    @Query("SELECT at FROM AuthToken at JOIN FETCH at.user WHERE at.token = :token")
    Optional<AuthToken> findByToken(@Param("token") String token);

    void deleteByToken(String token);
}