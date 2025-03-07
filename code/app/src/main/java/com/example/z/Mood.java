package com.example.z;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

public class Mood implements Serializable {

    private int id;
    private int ownerId;
    private EmotionalStates emotionalState;
    private String trigger;
    private String socialSituation;
    private Date createdAt;
    private Map location;
    private String description;

    public Mood(int id, int ownerId, EmotionalStates emotionalState, String trigger,
                String socialSituation, Date createdAt, Map location, String description) {
        this.id = id;
        this.ownerId = ownerId;
        this.emotionalState = emotionalState;
        this.trigger = trigger;
        this.socialSituation = socialSituation;
        this.createdAt = createdAt;
        this.location = location;
        this.description = description;
    }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public EmotionalStates getEmotionalState() {
        return emotionalState;
    }

    public void setEmotionalState(EmotionalStates emotionalState) {
        this.emotionalState = emotionalState;
    }

    public String getTrigger() {
        return trigger;
    }

    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }

    public String getSocialSituation() {
        return socialSituation;
    }

    public void setSocialSituation(String socialSituation) {
        this.socialSituation = socialSituation;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Map getLocation() {
        return location;
    }

    public void setLocation(Map location) {
        this.location = location;
    }

}
