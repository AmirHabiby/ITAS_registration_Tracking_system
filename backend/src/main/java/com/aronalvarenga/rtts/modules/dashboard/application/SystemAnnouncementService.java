package com.aronalvarenga.rtts.modules.dashboard.application;

import com.aronalvarenga.rtts.modules.dashboard.domain.SystemAnnouncement;
import com.aronalvarenga.rtts.modules.dashboard.domain.SystemAnnouncementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SystemAnnouncementService {

    private final SystemAnnouncementRepository announcementRepository;

    public SystemAnnouncementService(SystemAnnouncementRepository announcementRepository) {
        this.announcementRepository = announcementRepository;
    }

    @Transactional(readOnly = true)
    public String getAnnouncement() {
        return announcementRepository.findById(SystemAnnouncement.SINGLETON_ID)
            .map(SystemAnnouncement::getText)
            .orElse("");
    }

    @Transactional
    public String updateAnnouncement(String text) {
        SystemAnnouncement announcement = announcementRepository.findById(SystemAnnouncement.SINGLETON_ID)
            .orElseGet(() -> new SystemAnnouncement(text));
        announcement.updateText(text);
        return announcementRepository.save(announcement).getText();
    }
}
