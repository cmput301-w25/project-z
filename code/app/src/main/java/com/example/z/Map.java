package com.example.z;



// https://developers.google.com/maps/documentation/android-sdk/map#maps_android_map_fragment-java
// https://androidknowledge.com/google-maps-android-studio/
import java.util.ArrayList;
import java.util.List;

public class Map {

    private List<Object> moodEvents;
    private List<Object> users;

    public Map() {
        this.moodEvents = new ArrayList<>();
        this.users = new ArrayList<>();
    }

    public List<Object> getMoodEvents() {
        return moodEvents;
    }

    public void setMoodEvents(List<Object> moodEvents) {
        this.moodEvents = moodEvents;
    }
    public List<Object> getUsers() {
        return users;
    }

    public void setUsers(List<Object> users) {
        this.users = users;
    }

    public void addMoodEvent(Object event) {
        moodEvents.add(event);
    }

    public void addUser(Object user) {
        users.add(user);
    }
}
