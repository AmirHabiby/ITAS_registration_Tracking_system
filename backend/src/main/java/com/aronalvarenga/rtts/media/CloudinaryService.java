package com.aronalvarenga.rtts.media;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

@Service
public class CloudinaryService {

    private static final Set<String> PROFILE_IMAGE_CONTENT_TYPES =
        Set.of("image/jpeg", "image/png", "image/webp");
    private static final long MAX_PROFILE_IMAGE_SIZE = 5 * 1024 * 1024;

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public Map<?, ?> upload(
            MultipartFile file,
            String folder
    ) throws IOException {

        String contentType = file.getContentType();

        String resourceType;

        if (contentType != null
                && contentType.startsWith("video/")) {
            resourceType = "video";
        } else if (contentType != null
                && contentType.startsWith("image/")) {
            resourceType = "image";
        } else {
            resourceType = "raw";
        }

        Object result = cloudinary.uploader().upload(
            file.getBytes(),
            ObjectUtils.asMap(
                "resource_type", resourceType,
                "folder", folder,
                "use_filename", true,
                "unique_filename", true,
                "overwrite", false
            )
        );
        if (result instanceof Map<?, ?> uploadResult) {
            return uploadResult;
        }
        throw new IOException("Cloudinary returned an invalid upload response");
    }

    public Map<?, ?> uploadProfileImage(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please select an image");
        }
        String contentType = file.getContentType();
        if (contentType == null || !PROFILE_IMAGE_CONTENT_TYPES.contains(contentType)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Profile image must be a JPEG, PNG, or WebP image"
            );
        }
        if (file.getSize() > MAX_PROFILE_IMAGE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Profile image must be 5 MB or smaller");
        }

        return upload(file, "rtts/profile-images");
    }

    public void delete(String publicId, String resourceType) throws IOException {
        if (!"image".equals(resourceType) && !"video".equals(resourceType) && !"raw".equals(resourceType)) {
            throw new IOException("Unsupported Cloudinary resource type: " + resourceType);
        }

        Object result = cloudinary.uploader().destroy(
            publicId,
            ObjectUtils.asMap(
                "resource_type", resourceType,
                "invalidate", true
            )
        );
        if (!(result instanceof Map<?, ?> deleteResponse)) {
            throw new IOException("Cloudinary returned an invalid delete response");
        }
        Object deleteResult = deleteResponse.get("result");
        if (!"ok".equals(deleteResult) && !"not found".equals(deleteResult)) {
            throw new IOException("Cloudinary did not delete the uploaded training material");
        }
    }
}