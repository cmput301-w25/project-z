package com.example.z.utils;

/**
 * Represents different emotional states a user can have.
 */
public enum userMoods {
    /**
     * Default selection (no mood selected).
     */
    SELECT,

    /**
     * User is feeling anger.
     */
    ANGER,

    /**
     * User is feeling confusion.
     */
    CONFUSION,

    /**
     * User is feeling disgust.
     */
    DISGUST,

    /**
     * User is feeling fear.
     */
    FEAR,

    /**
     * User is feeling happiness.
     */
    HAPPINESS,

    /**
     * User is feeling sadness.
     */
    SADNESS,

    /**
     * User is feeling shame.
     */
    SHAME,

    /**
     * User is feeling surprise.
     */
    SURPRISE;

    /**
     * Returns a formatted string representation of the mood.
     * Converts the enum name to lowercase and replaces underscores with spaces.
     *
     * @return The formatted mood name as a user-friendly string.
     */
    @Override
    public String toString() {
        return name().toLowerCase().replace("_", " ");
    }
}

