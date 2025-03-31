package com.example.z.mood;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

/**
 * Adapter for displaying a grid of emojis in an emoji picker.
 * This adapter provides emoji images to a GridView.
 */
public class EmojiAdapter extends BaseAdapter {
    private Context context;
    private int[] emojiList;

    /**
     * Constructor for EmojiAdapter.
     *
     * @param context   The activity context.
     * @param emojiList An array of emoji resource IDs to be displayed.
     */
    public EmojiAdapter(Context context, int[] emojiList) {
        this.context = context;
        this.emojiList = emojiList;
    }

    /**
     * Returns the number of emojis in the dataset.
     *
     * @return The size of the emoji list.
     */
    @Override
    public int getCount() {
        return emojiList.length;
    }

    /**
     * Returns the emoji resource at the specified position.
     *
     * @param position The position of the item in the dataset.
     * @return The emoji resource ID.
     */
    @Override
    public Object getItem(int position) {
        return emojiList[position];
    }

    /**
     * Returns the item ID at the specified position.
     * In this case, it simply returns the position.
     *
     * @param position The position of the item.
     * @return The item ID (same as position).
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Creates or updates a view for an emoji in the GridView.
     *
     * @param position    The position of the item in the dataset.
     * @param displayView The recycled view, if available.
     * @param parent      The parent view that the new view will be attached to.
     * @return A View displaying the emoji at the given position.
     */
    @Override
    public View getView(int position, View displayView, ViewGroup parent) {
        ImageView imageView;

        if (displayView == null) {
            imageView = new ImageView(context);
            imageView.setLayoutParams(new GridView.LayoutParams(96, 91)); // Sets size of each emoji
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP); // Ensures proper scaling
        } else {
            imageView = (ImageView) displayView;
        }

        imageView.setImageResource(emojiList[position]); // Set emoji image
        return imageView;
    }
}

