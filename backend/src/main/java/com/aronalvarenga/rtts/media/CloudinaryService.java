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

    public Map upload(
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

        return cloudinary.uploader().upload(
            file.getBytes(),
            ObjectUtils.asMap(
                "resource_type", resourceType,
                "folder", folder,
                "use_filename", true,
                "unique_filename", true,
                "overwrite", false
            )
        );
    }
}