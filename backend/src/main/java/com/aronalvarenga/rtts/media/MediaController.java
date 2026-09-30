package com.aronalvarenga.rtts.media;

import com.aronalvarenga.rtts.identity.domain.UserAccount;
import com.aronalvarenga.rtts.identity.domain.UserAccountRepository;
import com.aronalvarenga.rtts.identity.domain.UserRole;
import com.aronalvarenga.rtts.modules.institutes.domain.TrainingInstituteRepository;
import com.aronalvarenga.rtts.modules.trainings.domain.Training;
import com.aronalvarenga.rtts.modules.trainings.domain.TrainingAccessType;
import com.aronalvarenga.rtts.modules.trainings.domain.TrainingMaterial;
import com.aronalvarenga.rtts.modules.trainings.domain.TrainingMaterialRepository;
import com.aronalvarenga.rtts.modules.trainings.domain.TrainingRepository;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/media")
public class MediaController {

    private final CloudinaryService cloudinaryService;
    private final TrainingRepository trainingRepository;
    private final TrainingMaterialRepository trainingMaterialRepository;
    private final UserAccountRepository userAccountRepository;
    private final TrainingInstituteRepository trainingInstituteRepository;

    public MediaController(
            CloudinaryService cloudinaryService,
            TrainingRepository trainingRepository,
            TrainingMaterialRepository trainingMaterialRepository,
            UserAccountRepository userAccountRepository,
            TrainingInstituteRepository trainingInstituteRepository
    ) {
        this.cloudinaryService = cloudinaryService;
        this.trainingRepository = trainingRepository;
        this.trainingMaterialRepository = trainingMaterialRepository;
        this.userAccountRepository = userAccountRepository;
        this.trainingInstituteRepository = trainingInstituteRepository;
    }

    @PostMapping(
        value = "/upload",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public MediaUploadResponse upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("trainingId") UUID trainingId,
            @RequestParam("weekNumber") int weekNumber,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description,
            @AuthenticationPrincipal Jwt jwt
    ) throws IOException {

        if (file.isEmpty()) {
            throw new IllegalArgumentException(
                "Please select a file"
            );
        }
        if (weekNumber < 1) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Week number must be positive");
        }

        UserAccount uploader = userAccountRepository.findByUsername(jwt.getSubject())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
        Training training = trainingRepository.findById(trainingId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Training not found"));

        authorizeUpload(uploader, training);

        Map<?, ?> result = cloudinaryService.upload(
            file,
            "rtts/training-materials"
        );

        String materialTitle = title == null || title.isBlank()
            ? file.getOriginalFilename()
            : title;
        String resourceType = (String) result.get("resource_type");
        TrainingMaterial material = new TrainingMaterial(
            training.getId(),
            uploader.getId(),
            materialTitle,
            description,
            resourceType,
            (String) result.get("secure_url"),
            (String) result.get("public_id"),
            ((Number) result.get("bytes")).longValue(),
            weekNumber);
        trainingMaterialRepository.save(material);

        return new MediaUploadResponse(
            (String) result.get("public_id"),
            (String) result.get("url"),
            (String) result.get("secure_url"),
            (String) result.get("resource_type"),
            (String) result.get("format"),
            ((Number) result.get("bytes")).longValue(),
            file.getOriginalFilename()
        );
    }

    private void authorizeUpload(UserAccount uploader, Training training) {
        if (training.getAccessType() == TrainingAccessType.PRIVATE) {
            boolean isOwningInstituteUser = uploader.getRole() == UserRole.TRAINING_INSTITUTE
                && trainingInstituteRepository.findByUserId(uploader.getId())
                    .map(institute -> institute.getId().equals(training.getTrainingInstituteProfileId()))
                    .orElse(false);
            if (!isOwningInstituteUser) {
                throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only the owning training institute can upload private training materials");
            }
            return;
        }

        if (uploader.getRole() != UserRole.SYSTEM_ADMIN) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Only a system administrator can upload public or staff training materials");
        }
    }
}