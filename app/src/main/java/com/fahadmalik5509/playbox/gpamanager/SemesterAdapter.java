package com.fahadmalik5509.playbox.gpamanager;

import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.playSoundAndVibrate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fahadmalik5509.playbox.R;
import com.fahadmalik5509.playbox.databinding.SemesterItemLayoutBinding;
import java.util.ArrayList;

public class SemesterAdapter extends RecyclerView.Adapter<SemesterAdapter.SemesterViewHolder>{

    private final ArrayList<Semester> semesterList;

    public SemesterAdapter(ArrayList<Semester> semesterList) {
        this.semesterList = semesterList;
    }

    @Override
    public int getItemCount() {
        return semesterList.size();
    }

    @NonNull
    @Override
    public SemesterAdapter.SemesterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        SemesterItemLayoutBinding vb = SemesterItemLayoutBinding.inflate(inflater, parent, false);
        return new SemesterAdapter.SemesterViewHolder(vb);
    }

    @Override
    public void onBindViewHolder(@NonNull SemesterAdapter.SemesterViewHolder holder, int position) {

        Semester currentSemester = semesterList.get(position);

    }

    // A helper to add a new semester
    public void addSemester(Semester newSemester) {
        semesterList.add(newSemester);
        // Notify the adapter that a new item is inserted at the end
        notifyItemInserted(semesterList.size() - 1);
    }

    // Remove semester based on its position
    public void removeSemester(int position) {
        if (position < semesterList.size() && position >= 0) {
            semesterList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, semesterList.size()); // refresh positions
        }
    }

    public static class SemesterViewHolder extends RecyclerView.ViewHolder {
        SemesterItemLayoutBinding vb;

        public SemesterViewHolder(SemesterItemLayoutBinding vb) {

            super(vb.getRoot());
            this.vb = vb;
        }
    }
}
