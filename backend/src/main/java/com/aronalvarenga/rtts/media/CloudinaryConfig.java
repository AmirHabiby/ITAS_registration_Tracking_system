package com.aronalvarenga.rtts.media;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {

        String cloudName =
                System.getenv("CLOUDINARY_CLOUD_NAME");

        String apiKey =
                System.getenv("CLOUDINARY_API_KEY");

        String apiSecret =
                System.getenv("CLOUDINARY_API_SECRET");

        if (cloudName == null || apiKey == null
                || apiSecret == null) {
            throw new IllegalStateException(
                "Cloudinary environment variables are missing"
            );
        }

        return new Cloudinary(
            ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
            )
        );
    }
}