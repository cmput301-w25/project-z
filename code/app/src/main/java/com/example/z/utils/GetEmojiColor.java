package com.example.z.utils;

import android.graphics.Color;

import java.util.HashMap;
import java.util.Map;

public class GetEmojiColor {

    private static final Map<String, Integer> getMoodColor = new HashMap<>();

    static {
        getMoodColor.put("anger", Color.parseColor("#f94144"));
        getMoodColor.put("confusion", Color.parseColor("#c77dff"));
        getMoodColor.put("disgust", Color.parseColor("#90be6d"));
        getMoodColor.put("fear", Color.parseColor("#577590"));
        getMoodColor.put("happiness", Color.parseColor("#fdc500"));
        getMoodColor.put("sadness", Color.parseColor("#277da1"));
        getMoodColor.put("shame", Color.parseColor("#4d908e"));
        getMoodColor.put("surprise", Color.parseColor("#ff7096"));
    }

    public static int getEmojiColor(String mood) {
        return getMoodColor.getOrDefault(mood.toLowerCase(), Color.parseColor("#001219"));
    }

}
