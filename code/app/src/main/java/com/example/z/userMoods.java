package com.example.z;

public enum userMoods {
    SELECT,
    ANGER,
    CONFUSION,
    DISGUST,
    FEAR,
    HAPPINESS,
    SADNESS,
    SHAME,
    SURPRISE;
    // used to reformat genre
    @Override
    public String toString() {
        String reformatGenre = name().toLowerCase().replace("_", " ");
        return reformatGenre.substring(0, 1).toUpperCase() + reformatGenre.substring(1);
    }
}
