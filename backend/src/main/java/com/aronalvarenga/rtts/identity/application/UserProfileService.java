package com.aronalvarenga.rtts.identity.application;

import com.aronalvarenga.rtts.identity.domain.UserAccount;
import com.aronalvarenga.rtts.identity.domain.UserAccountRepository;
import com.aronalvarenga.rtts.identity.domain.UserRole;
import com.aronalvarenga.rtts.media.CloudinaryService;
import com.aronalvarenga.rtts.modules.delegator.domain.DelegatorProfileRepository;
import com.aronalvarenga.rtts.modules.firm.domain.FirmAdminProfileRepository;
import com.aronalvarenga.rtts.modules.representatives.domain.RepresentativeRepository;
import com.aronalvarenga.rtts.identity.web.UpdateUserProfileRequest;
import java.io.IOException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserProfileService {

    private final UserAccountRepository userAccountRepository;
    private final RepresentativeRepository representativeRepository;
    private final DelegatorProfileRepository delegatorProfileRepository;
    private final FirmAdminProfileRepository firmAdminProfileRepository;
    private final CloudinaryService cloudinaryService;

    public UserProfileService(
            UserAccountRepository userAccountRepository,
            RepresentativeRepository representativeRepository,
            DelegatorProfileRepository delegatorProfileRepository,
            FirmAdminProfileRepository firmAdminProfileRepository,
            CloudinaryService cloudinaryService
    ) {
        this.userAccountRepository = userAccountRepository;
        this.representativeRepository = representativeRepository;
        this.delegatorProfileRepository = delegatorProfileRepository;
        this.firmAdminProfileRepository = firmAdminProfileRepository;
        this.cloudinaryService = cloudinaryService;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse current(String username) {
        UserAccount user = findEditableUser(username);
        String fullName = switch (user.getRole()) {
            case REPRESENTATIVE -> representativeRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Representative profile not found"))
                .getFullName();
            case DELEGATOR -> delegatorProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Delegator profile not found"))
                .getFullName();
            case FIRM_ADMIN -> firmAdminProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Firm admin profile not found"))
                .getFullName();
            default -> user.getDisplayName();
        };
        return new UserProfileResponse(fullName);
    }

    @Transactional
    public UserAccount update(String username, UpdateUserProfileRequest request, MultipartFile image) {
        UserAccount user = findEditableUser(username);
        UserRole role = user.getRole();

        Map<?, ?> uploadedImage = null;
        try {
            if (image != null) {
                uploadedImage = cloudinaryService.uploadProfileImage(image);
            }

            String fullName = request.fullName().trim();
            switch (role) {
                case REPRESENTATIVE -> representativeRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Representative profile not found"))
                    .setFullName(fullName);
                case DELEGATOR -> delegatorProfileRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Delegator profile not found"))
                    .setFullName(fullName);
                case FIRM_ADMIN -> firmAdminProfileRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Firm admin profile not found"))
                    .setFullName(fullName);
                default -> {
                }
            }
            user.setDisplayName(fullName);
            if (uploadedImage != null) {
                user.setProfileImageUrl(requiredUploadValue(uploadedImage, "secure_url"));
            }
            return userAccountRepository.save(user);
        } catch (IOException exception) {
            cleanupUploadedImage(uploadedImage, exception);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unable to upload profile image", exception);
        } catch (RuntimeException exception) {
            cleanupUploadedImage(uploadedImage, exception);
            throw exception;
        }
    }

    private String requiredUploadValue(Map<?, ?> upload, String key) {
        Object value = upload.get(key);
        if (!(value instanceof String stringValue) || stringValue.isBlank()) {
            throw new IllegalStateException("Cloudinary upload response is missing " + key);
        }
        return stringValue;
    }

    private UserAccount findEditableUser(String username) {
        UserAccount user = userAccountRepository.findByUsername(username)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
        UserRole role = user.getRole();
        if (role != UserRole.SYSTEM_ADMIN
                && role != UserRole.REPRESENTATIVE
                && role != UserRole.DELEGATOR
                && role != UserRole.TRAINING_INSTITUTE
                && role != UserRole.FIRM_ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This account cannot update its profile");
        }
        return user;
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
