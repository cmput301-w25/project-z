package com.example.z.mood;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.z.utils.ImgUtil;
import com.example.z.views.ProfileActivity;
import com.example.z.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
/**
 * Adapter for displaying a list of Mood objects in a RecyclerView.
 * This adapter binds mood data to the UI elements inside a mood card item.
 *
 *  Outstanding issues:
 *      - None
 */
public class MoodArrayAdapter extends RecyclerView.Adapter<MoodArrayAdapter.MoodViewHolder> {
    private List<Mood> moodList;
    private Context context;

    /**
     * Constructor for MoodArrayAdapter.
     *
     * @param context  The activity context.
     * @param moodList The list of moods to be displayed.
     */
    public MoodArrayAdapter(Context context, List<Mood> moodList) {
        this.context = context;
        this.moodList = moodList;
    }

    /**
     * Creates new ViewHolder instances for the RecyclerView.
     *
     * @param parent   The parent ViewGroup.
     * @param viewType The type of the view.
     * @return A new MoodViewHolder instance.
     */
    @NonNull
    @Override
    public MoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_mood_card, parent, false);
        return new MoodViewHolder(view);
    }

    /**
     * Binds mood data to the UI elements in the ViewHolder.
     *
     * @param holder   The ViewHolder to bind data to.
     * @param position The position of the item in the dataset.
     */
    @Override
    public void onBindViewHolder(@NonNull MoodViewHolder holder, int position) {
        Mood mood = moodList.get(position);

        // Ensure views are not null before setting text
        if (holder.moodText != null) {
            holder.moodText.setText(String.format("%s is feeling %s", mood.getUsername(), mood.getEmotionalState()));
        }

        if (holder.descriptionText != null) {
            holder.descriptionText.setText(mood.getDescription());
        }

        if (holder.moodTag != null) {
            holder.moodTag.setText(String.format("#%s", mood.getTrigger()));
        }

       /* if (holder.imgMood != null && mood.getImg() != null) {
            ImgUtil.displayBase64Image(mood.getImg(), holder.imgMood);
//            Glide.with(this.context).load(mood.getImg().toString()).into(holder.imgMood);
        }
/*/
        
        Date moodDate = mood.getCreatedAt();
        if (moodDate != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy - HH:mm", Locale.getDefault());
            holder.dateText.setText(dateFormat.format(moodDate));
        } else {
            holder.dateText.setText("No Date"); // Display fallback text if date is null
        }

        // Handle post click to open the mood details dialog
        holder.itemView.setOnClickListener(v -> {
            ViewMoodDialogFragment moodDialog = new ViewMoodDialogFragment(mood);
            moodDialog.show(((ProfileActivity) context).getSupportFragmentManager(), "MoodDetailsDialog");
        });

        // Allow editing only if the mood belongs to the current user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && mood.getUserId().equals(currentUser.getUid())) {
            holder.itemView.setOnLongClickListener(v -> {
                showEditMoodDialog(mood);
                return true;
            });
        }
    }

    /**
     * Returns the total number of moods in the dataset.
     *
     * @return The size of the mood list.
     */
    @Override
    public int getItemCount() {
        return moodList.size();
    }

    /**
     * Displays the EditMoodFragment dialog for editing a mood.
     *
     * @param mood The mood object to be edited.
     */
    private void showEditMoodDialog(Mood mood) {
        EditMoodFragment editFragment = EditMoodFragment.newInstance(mood);
        editFragment.setMoodUpdatedListener(() -> notifyDataSetChanged());

        FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
        editFragment.show(fragmentManager, "EditMoodFragment");
    }

    /**
     * ViewHolder class for holding mood item views.
     */
    public static class MoodViewHolder extends RecyclerView.ViewHolder {
        TextView moodText, descriptionText, dateText, moodTag;
        ImageView imgMood;

        /**
         * Constructor for MoodViewHolder.
         *
         * @param itemView The item view containing the UI elements.
         */
        public MoodViewHolder(@NonNull View itemView) {
            super(itemView);
            moodText = itemView.findViewById(R.id.tvMoodTitle);
            descriptionText = itemView.findViewById(R.id.tvMoodDescription);
            moodTag = itemView.findViewById(R.id.tvMoodTag);
            dateText = itemView.findViewById(R.id.tvMoodDate);
            imgMood = itemView.findViewById(R.id.imgMood);
        }
    }
}



