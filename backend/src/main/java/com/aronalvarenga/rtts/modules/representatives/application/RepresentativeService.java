package com.aronalvarenga.rtts.modules.representatives.application;

import com.aronalvarenga.rtts.identity.domain.UserAccount;
import com.aronalvarenga.rtts.identity.domain.UserAccountRepository;
import com.aronalvarenga.rtts.identity.domain.UserRole;
import com.aronalvarenga.rtts.media.CloudinaryService;
import com.aronalvarenga.rtts.modules.representatives.domain.Representative;
import com.aronalvarenga.rtts.modules.representatives.domain.RepresentativeRepository;
import com.aronalvarenga.rtts.modules.representatives.domain.RepresentativeStatus;
import com.aronalvarenga.rtts.modules.representatives.web.RepresentativeRequest;
import com.aronalvarenga.rtts.modules.representatives.web.UpdateRepresentativeProfileRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RepresentativeService {

    private final RepresentativeRepository representativeRepository;
    private final UserAccountRepository userAccountRepository;
    private final CloudinaryService cloudinaryService;

    public RepresentativeService(
            RepresentativeRepository representativeRepository,
            UserAccountRepository userAccountRepository,
            CloudinaryService cloudinaryService
    ) {
        this.representativeRepository = representativeRepository;
        this.userAccountRepository = userAccountRepository;
        this.cloudinaryService = cloudinaryService;
    }

    @Transactional(readOnly = true)
    public List<Representative> list() {
        return representativeRepository.findAll();
    }

    @Transactional
    public Representative create(RepresentativeRequest request) {
        return representativeRepository.save(new Representative(request.fullName(), request.email()));
    }

    @Transactional(readOnly = true)
    public Representative getOwnProfile(String username) {
        UserAccount user = userAccountRepository.findByUsername(username)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
        if (user.getRole() != UserRole.REPRESENTATIVE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only representatives can access this profile");
        }
        return representativeRepository.findByUserId(user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Representative profile not found"));
    }

    @Transactional
    public UserAccount updateOwnProfile(
            String username,
            UpdateRepresentativeProfileRequest request,
            MultipartFile image
    ) {
        UserAccount user = userAccountRepository.findByUsername(username)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
        if (user.getRole() != UserRole.REPRESENTATIVE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only representatives can update this profile");
        }
        Representative representative = representativeRepository.findByUserId(user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Representative profile not found"));

        Map<?, ?> uploadedImage = null;
        try {
            if (image != null) {
                uploadedImage = cloudinaryService.uploadProfileImage(image);
            }

            String fullName = request.fullName().trim();
            representative.setFullName(fullName);
            user.setDisplayName(fullName);
            if (uploadedImage != null) {
                user.setProfileImageUrl(requiredUploadValue(uploadedImage, "secure_url"));
            }

            representativeRepository.save(representative);
            return userAccountRepository.save(user);
        } catch (IOException exception) {
            cleanupUploadedImage(uploadedImage, exception);
            throw new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "Unable to upload representative profile image",
                exception
            );
        } catch (RuntimeException exception) {
            cleanupUploadedImage(uploadedImage, exception);
            throw exception;
        }
    }

    @Transactional
    public Representative markTrainingRequested(UUID representativeId) {
        Representative representative = getOrThrow(representativeId);
        representative.setStatus(RepresentativeStatus.IN_TRAINING);
        return representativeRepository.save(representative);
    }

    @Transactional
    public Representative markTrained(UUID representativeId) {
        Representative representative = getOrThrow(representativeId);
        representative.setStatus(RepresentativeStatus.TRAINED);
        return representativeRepository.save(representative);
    }

    @Transactional
    public Representative markAgent(UUID representativeId) {
        Representative representative = getOrThrow(representativeId);
        if (representative.getStatus() != RepresentativeStatus.TRAINED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only trained representatives can be delegated as agent");
        }
        return representative;
    }

    @Transactional(readOnly = true)
    public Representative get(UUID representativeId) {
        return getOrThrow(representativeId);
    }

    private Representative getOrThrow(UUID representativeId) {
        return representativeRepository.findById(representativeId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Representative not found"));
    }

    private String requiredUploadValue(Map<?, ?> upload, String key) {
        Object value = upload.get(key);
        if (!(value instanceof String stringValue) || stringValue.isBlank()) {
            throw new IllegalStateException("Cloudinary upload response is missing " + key);
        }
        return stringValue;
    }

    private void cleanupUploadedImage(Map<?, ?> upload, Exception originalException) {
        if (upload == null || !(upload.get("public_id") instanceof String publicId) || publicId.isBlank()) {
            return;
        }
        try {
            cloudinaryService.delete(publicId, "image");
        } catch (IOException cleanupException) {
            originalException.addSuppressed(cleanupException);
        }
    }
}
