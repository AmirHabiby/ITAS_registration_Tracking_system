package com.aronalvarenga.rtts.media;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

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