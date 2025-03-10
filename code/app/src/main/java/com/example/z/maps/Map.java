package com.example.z.maps;



// https://developers.google.com/maps/documentation/android-sdk/map#maps_android_map_fragment-java
// https://androidknowledge.com/google-maps-android-studio/
import java.util.ArrayList;
import java.util.List;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a map that holds mood events and users.
 * This class provides methods to store and manage mood events and user data.
 * This class is still a work in progress for part 4 so things may change.
 */
public class Map {

    private List<Object> moodEvents;
    private List<Object> users;

    /**
     * Constructs a new Map object with empty lists for mood events and users.
     */
    public Map() {
        this.moodEvents = new ArrayList<>();
        this.users = new ArrayList<>();
    }

    /**
     * Retrieves the list of mood events stored in the map.
     *
     * @return A list of mood events.
     */
    public List<Object> getMoodEvents() {
        return moodEvents;
    }

    /**
     * Sets the mood events list with a new collection of events.
     *
     * @param moodEvents A list of mood events to set.
     */
    public void setMoodEvents(List<Object> moodEvents) {
        this.moodEvents = moodEvents;
    }

    /**
     * Retrieves the list of users associated with the map.
     *
     * @return A list of users.
     */
    public List<Object> getUsers() {
        return users;
    }

    /**
     * Sets the list of users associated with the map.
     *
     * @param users A list of users to set.
     */
    public void setUsers(List<Object> users) {
        this.users = users;
    }

    /**
     * Adds a mood event to the list of mood events.
     *
     * @param event The mood event to be added.
     */
    public void addMoodEvent(Object event) {
        moodEvents.add(event);
    }

    /**
     * Adds a user to the list of users.
     *
     * @param user The user to be added.
     */
    public void addUser(Object user) {
        users.add(user);
    }
}

