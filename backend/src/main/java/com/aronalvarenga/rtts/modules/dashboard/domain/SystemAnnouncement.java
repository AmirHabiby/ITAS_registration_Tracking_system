package com.aronalvarenga.rtts.modules.dashboard.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "system_announcement")
public class SystemAnnouncement {

    public static final long SINGLETON_ID = 1L;

    @Id
    private Long id;

    @Column(name = "announcement_text", nullable = false, columnDefinition = "TEXT")
    private String text;

    protected SystemAnnouncement() {
    }

    public SystemAnnouncement(String text) {
        this.id = SINGLETON_ID;
        updateText(text);
    }

    public void updateText(String text) {
        if (text == null) {
            throw new IllegalArgumentException("Announcement text is required");
        }
        this.text = text;
    }

    public Long getId() {
        return id;
    }

    public String getText() {
        return text;
    }
}
