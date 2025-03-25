package com.example.z.utils;

import com.example.z.R;

import java.util.HashMap;
import java.util.Map;

public class GetEmoji {

    private static final Map<String, Integer> getEmoji = new HashMap<>();

    static {
        getEmoji.put("anger_1", R.drawable.anger_1);
        getEmoji.put("anger_2", R.drawable.anger_2);
        getEmoji.put("anger_3", R.drawable.anger_3);
        getEmoji.put("anger_4", R.drawable.anger_4);
        getEmoji.put("disgust_1", R.drawable.disgust_1);
        getEmoji.put("disgust_2", R.drawable.disgust_2);
        getEmoji.put("fear_1", R.drawable.fear_1);
        getEmoji.put("fear_2", R.drawable.fear_2);
        getEmoji.put("fear_3", R.drawable.fear_3);
        getEmoji.put("happy_1", R.drawable.happy_1);
        getEmoji.put("happy_2", R.drawable.happy_2);
        getEmoji.put("happy_3", R.drawable.happy_3);
        getEmoji.put("happy_4", R.drawable.happy_4);
        getEmoji.put("happy_5", R.drawable.happy_5);
        getEmoji.put("love_1", R.drawable.love_1);
        getEmoji.put("love_2", R.drawable.love_2);
        getEmoji.put("money_1", R.drawable.money_1);
        getEmoji.put("sad_1", R.drawable.sad_1);
        getEmoji.put("sad_2", R.drawable.sad_2);
        getEmoji.put("sad_3", R.drawable.sad_3);
        getEmoji.put("sad_4", R.drawable.sad_4);
        getEmoji.put("shock_1", R.drawable.shock_1);
        getEmoji.put("shock_2", R.drawable.shock_2);
        getEmoji.put("singing_1", R.drawable.singing_1);
        getEmoji.put("sleepy_1", R.drawable.sleepy_1);
        getEmoji.put("smirk_1", R.drawable.smirk_1);
        getEmoji.put("smooch_1", R.drawable.smooch_1);
        getEmoji.put("squiggle_1", R.drawable.squiggle_1);
        getEmoji.put("squiggle_2", R.drawable.squiggle_2);
        getEmoji.put("wink_1", R.drawable.wink_1);
    }

    public static int getEmojiPosition(String emoji) {
        return getEmoji.getOrDefault(emoji, 0);
    }

    public static int[] getEmojiList() {
        int[] emoji = new int[getEmoji.size()];
        int index = 0;

        for (int emojiPosition : getEmoji.values()) {
            emoji[index++] = emojiPosition;
        }
        return emoji;
    }

}
