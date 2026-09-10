package com.aronalvarenga.rtts.modules.trainings.domain;

import com.aronalvarenga.rtts.shared.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "training_materials")
public class TrainingMaterial extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "training_id", nullable = false)
    private UUID trainingId;

    @Column(name = "uploaded_by_user_id", nullable = false)
    private UUID uploadedByUserId;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(name = "material_type", nullable = false, length = 40)
    private String materialType;

    @Column(name = "file_url", nullable = false, length = 1000)
    private String fileUrl;

    @Column(name = "cloudinary_public_id", length = 255)
    private String cloudinaryPublicId;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    protected TrainingMaterial() {
    }

    public TrainingMaterial(UUID trainingId, UUID uploadedByUserId, String title, String description,
                            String materialType, String fileUrl, String cloudinaryPublicId, Long fileSizeBytes) {
        this.trainingId = trainingId;
        this.uploadedByUserId = uploadedByUserId;
        this.title = title;
        this.description = description;
        this.materialType = materialType;
        this.fileUrl = fileUrl;
        this.cloudinaryPublicId = cloudinaryPublicId;
        this.fileSizeBytes = fileSizeBytes;
    }

    public UUID getId() { return id; }
    public UUID getTrainingId() { return trainingId; }
    public UUID getUploadedByUserId() { return uploadedByUserId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getMaterialType() { return materialType; }
    public String getFileUrl() { return fileUrl; }
    public String getCloudinaryPublicId() { return cloudinaryPublicId; }
    public Long getFileSizeBytes() { return fileSizeBytes; }
    public void setId(UUID id) { this.id = id; }
    public void setTrainingId(UUID trainingId) { this.trainingId = trainingId; }
    public void setUploadedByUserId(UUID uploadedByUserId) { this.uploadedByUserId = uploadedByUserId; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setMaterialType(String materialType) { this.materialType = materialType; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public void setCloudinaryPublicId(String cloudinaryPublicId) { this.cloudinaryPublicId = cloudinaryPublicId; }
    public void setFileSizeBytes(Long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }
}
