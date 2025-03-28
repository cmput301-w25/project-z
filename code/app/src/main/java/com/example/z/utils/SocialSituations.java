package com.example.z.utils;

/**
 * Represents different social situations a user may be in when recording their mood.
 *
 * Outstanding issues:
 *      - None
 */
public enum SocialSituations {
    /**
     * Default selection (no situation selected).
     */
    SELECT,

    /**
     * User is alone.
     */
    ALONE,

    /**
     * User is with one other person.
     */
    WITH_A_PERSON,

    /**
     * User is with several people.
     */
    WITH_SEVERAL_PEOPLE,

    /**
     * User is in a large crowd.
     */
    WITH_A_CROWD;

    /**
     * Returns a formatted string representation of the social situation.
     * Converts the enum name to lowercase and replaces underscores with spaces.
     *
     * @return The formatted social situation name as a user-friendly string.
     */
    @Override
    public String toString() {
        return name().toLowerCase().replace("_", " ");
    }
}

