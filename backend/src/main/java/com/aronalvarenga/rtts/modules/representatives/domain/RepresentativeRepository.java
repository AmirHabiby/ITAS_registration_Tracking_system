package com.aronalvarenga.rtts.modules.representatives.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepresentativeRepository extends JpaRepository<Representative, UUID> {
	long countByStatus(RepresentativeStatus status);
	List<Representative> findByStatusOrderByFullNameAsc(RepresentativeStatus status);
	java.util.Optional<Representative> findByUserId(UUID userId);
}
