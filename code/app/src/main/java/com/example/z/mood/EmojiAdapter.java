package com.example.z.mood;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class EmojiAdapter extends BaseAdapter {
    private Context context;
    private int[] emojiList;

    public EmojiAdapter(Context context, int[] emojiList) {
        this.context = context;
        this.emojiList = emojiList;
    }

    @Override
    public int getCount() {
        return emojiList.length;
    }

    @Override
    public Object getItem(int position) {
        return emojiList[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View displayView, ViewGroup parent) {
        ImageView imageView;
        if (displayView == null) {
            imageView = new ImageView(context);
            imageView.setLayoutParams(new GridView.LayoutParams(96, 91));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
        else {
            imageView = (ImageView) displayView;
        }
        imageView.setImageResource(emojiList[position]);
        return imageView;
    }
}
