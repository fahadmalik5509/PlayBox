package com.fahadmalik5509.playbox.gpamanager;

import static com.fahadmalik5509.playbox.gpamanager.GPACalculator.getSemesterGPAStringFromSubjects;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.playSoundAndVibrate;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fahadmalik5509.playbox.R;
import com.fahadmalik5509.playbox.databinding.SemesterItemLayoutBinding;

import java.util.List;

public class SemesterAdapter extends RecyclerView.Adapter<SemesterAdapter.SemesterViewHolder> {

    public interface OnSemesterClickListener {
        void onSemesterClick(int semesterId);
    }

    private final List<Semester> semesterList;
    private final OnSemesterClickListener clickListener;
    private final Context context;

    public SemesterAdapter(Context context, List<Semester> semesterList,
                           OnSemesterClickListener clickListener) {
        this.context = context;
        this.semesterList = semesterList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public SemesterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        SemesterItemLayoutBinding vb = SemesterItemLayoutBinding.inflate(inflater, parent, false);
        return new SemesterViewHolder(vb);
    }

    @Override
    public void onBindViewHolder(@NonNull SemesterViewHolder holder, int position) {
        Semester currentSemester = semesterList.get(position);
        int semesterId = currentSemester.getSemesterId();

        AppDatabase db = AppDatabase.getInstance(holder.itemView.getContext());

        new Thread(() -> {
            List<Subject> subjects = db.subjectDao().getSubjectsForSemester(semesterId);

            // Use context.runOnUiThread to update the UI on the main thread
            ((Activity) context).runOnUiThread(() -> {
                String semesterGPA = getSemesterGPAStringFromSubjects(subjects);

                holder.vb.semesterNameTV.setText(currentSemester.getSemesterName());
                holder.vb.semesterGPATV.setText(semesterGPA);

                holder.itemView.setOnClickListener(v -> {
                    playSoundAndVibrate(R.raw.sound_ui, true, 50);
                    clickListener.onSemesterClick(currentSemester.getSemesterId());
                });
            });
        }).start();

        holder.itemView.setOnClickListener(v -> {
            playSoundAndVibrate(R.raw.sound_ui, true, 50);
            clickListener.onSemesterClick(currentSemester.getSemesterId());
        });
    }

    @Override
    public int getItemCount() {
        return semesterList.size();
    }

    static class SemesterViewHolder extends RecyclerView.ViewHolder {
        final SemesterItemLayoutBinding vb;

        SemesterViewHolder(SemesterItemLayoutBinding vb) {
            super(vb.getRoot());
            this.vb = vb;
        }
    }
}