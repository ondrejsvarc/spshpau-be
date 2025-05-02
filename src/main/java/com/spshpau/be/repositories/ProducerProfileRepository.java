package com.spshpau.be.repositories;

import com.spshpau.be.model.ProducerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProducerProfileRepository extends JpaRepository<ProducerProfile, UUID> {
    Optional<ProducerProfile> findById(UUID userId);
}
