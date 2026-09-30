package com.aronalvarenga.rtts.modules.representative.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepresentativeTrainingMaterialProgressRepository
    extends JpaRepository<RepresentativeTrainingMaterialProgress, UUID> {

    List<RepresentativeTrainingMaterialProgress> findByRepresentativeIdAndTrainingId(
        UUID representativeId,
        UUID trainingId);

    boolean existsByRepresentativeIdAndMaterialId(UUID representativeId, UUID materialId);
}
