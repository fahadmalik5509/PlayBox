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
import java.util.List;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder> {

    public interface OnSaveClickListener {
        void onSaveClick(int position, String name);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }

    private final List<Profile> profileList;
    private final OnSaveClickListener saveListener;
    private final OnDeleteClickListener deleteListener;

    public ProfileAdapter(List<Profile> profileList,
                          OnSaveClickListener saveListener,
                          OnDeleteClickListener deleteListener) {
        this.profileList = profileList;
        this.saveListener = saveListener;
        this.deleteListener = deleteListener;
    }

    @NonNull @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ProfileItemLayoutBinding vb = ProfileItemLayoutBinding
                .inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ProfileViewHolder(vb);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int pos) {
        Profile p = profileList.get(pos);
        holder.vb.profileNameET.setText(p.getProfileName());

        holder.vb.profileEditIV.setOnClickListener(v -> {
            playSoundAndVibrate(R.raw.sound_edit, true, 50);
            holder.vb.profileNameET.setEnabled(true);
            holder.vb.profileSaveB.setVisibility(View.VISIBLE);
        });

        holder.vb.profileSaveB.setOnClickListener(v -> {
            playSoundAndVibrate(R.raw.sound_ui, true, 50);
            holder.vb.profileNameET.setEnabled(false);
            holder.vb.profileSaveB.setVisibility(View.GONE);
            saveListener.onSaveClick(pos, holder.vb.profileNameET.getText().toString());
        });

        holder.vb.profileDeleteIV.setOnClickListener(v -> {
            playSoundAndVibrate(R.raw.sound_delete, true, 50);
            deleteListener.onDeleteClick(pos);
        });
    }

    @Override public int getItemCount() { return profileList.size(); }

    static class ProfileViewHolder extends RecyclerView.ViewHolder {
        final ProfileItemLayoutBinding vb;
        ProfileViewHolder(ProfileItemLayoutBinding vb) {
            super(vb.getRoot());
            this.vb = vb;
        }
    }
}
