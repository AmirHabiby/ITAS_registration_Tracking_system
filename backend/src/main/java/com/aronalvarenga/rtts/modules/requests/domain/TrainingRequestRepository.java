package com.aronalvarenga.rtts.modules.requests.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainingRequestRepository extends JpaRepository<TrainingRequestEntity, UUID> {
    List<TrainingRequestEntity> findByRepresentativeIdOrderByRequestedAtDesc(UUID representativeId);
    List<TrainingRequestEntity> findByApprovedByDelegatorIdOrRejectedByDelegatorIdOrderByRequestedAtDesc(UUID approvedByDelegatorId, UUID rejectedByDelegatorId);
    List<TrainingRequestEntity> findByStatusOrderByRequestedAtDesc(TrainingRequestStatus status);
    long countByRepresentativeIdAndStatus(UUID representativeId, TrainingRequestStatus status);
    long countByApprovedByDelegatorId(UUID approvedByDelegatorId);
    long countByRejectedByDelegatorId(UUID rejectedByDelegatorId);
    long countByStatus(TrainingRequestStatus status);
}
