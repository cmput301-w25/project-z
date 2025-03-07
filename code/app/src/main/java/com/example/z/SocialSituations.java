package com.example.z;

public enum SocialSituations {
    SELECT,
    ALONE,
    WITH_A_PERSON,
    WITH_SEVERAL_PEOPLE,
    WITH_A_CROWD;

    @Override
    public String toString() {
        String reformatGenre = name().toLowerCase().replace("_", " ");
        String[] separate = reformatGenre.split(" ");
        for (int i = 0; i < separate.length; i++) {
            if (separate[i].length() >= 3) {
                separate[i] = separate[i].substring(0, 1).toUpperCase() + separate[i].substring(1);
            }
        }
        return String.join(" ", separate);
    }
}
