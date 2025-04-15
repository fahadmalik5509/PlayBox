package com.fahadmalik5509.playbox.gpamanager;

import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.playSoundAndVibrate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fahadmalik5509.playbox.R;
import com.fahadmalik5509.playbox.databinding.ProfileItemLayoutBinding;
import java.util.ArrayList;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder>{

    private final ArrayList<Profile> profileList;

    public ProfileAdapter(ArrayList<Profile> profileList) {
        this.profileList = profileList;
    }

    @Override
    public int getItemCount() {
        return profileList.size();
    }

    @NonNull
    @Override
    public ProfileAdapter.ProfileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ProfileItemLayoutBinding vb = ProfileItemLayoutBinding.inflate(inflater, parent, false);
        return new ProfileAdapter.ProfileViewHolder(vb);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileAdapter.ProfileViewHolder holder, int position) {

        holder.vb.profileDeleteIV.setOnClickListener(v -> {
            playSoundAndVibrate(R.raw.sound_delete, true, 50);

            if (position != RecyclerView.NO_POSITION) {
                removeProfile(position);
            }
        });

        holder.vb.profileEditIV.setOnClickListener(v -> {
            if (holder.vb.profileSaveB.getVisibility() == View.VISIBLE) return;

            playSoundAndVibrate(R.raw.sound_edit, true, 50);
            holder.vb.profileItemLayout.setClickable(false);
            holder.vb.profileItemLayout.setFocusable(false);

            holder.vb.profileNameET.setEnabled(true);
            holder.vb.profileNameET.setClickable(true);
            holder.vb.profileNameET.setFocusable(true);

            holder.vb.profileSaveB.setVisibility(View.VISIBLE);
        });

        holder.vb.profileSaveB.setOnClickListener(v -> {
            playSoundAndVibrate(R.raw.sound_ui, true, 50);

            holder.vb.profileItemLayout.setClickable(true);
            holder.vb.profileItemLayout.setFocusable(true);

            holder.vb.profileNameET.setEnabled(false);
            holder.vb.profileNameET.setClickable(false);
            holder.vb.profileNameET.setFocusable(false);
            holder.vb.profileSaveB.setVisibility(View.GONE);
        });
    }

    // A helper to add a new profile
    public void addProfile(Profile newProfile) {
        profileList.add(newProfile);
        // Notify the adapter that a new item is inserted at the end
        notifyItemInserted(profileList.size() - 1);
    }

    // Remove profile based on its position
    public void removeProfile(int position) {
        if (position < profileList.size() && position >= 0) {
            profileList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, profileList.size()); // refresh positions
        }
    }

    public static class ProfileViewHolder extends RecyclerView.ViewHolder {
        ProfileItemLayoutBinding vb;

        public ProfileViewHolder(ProfileItemLayoutBinding vb) {

            super(vb.getRoot());
            this.vb = vb;
        }
    }
}
