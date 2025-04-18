package com.fahadmalik5509.playbox.gpamanager;

import static com.fahadmalik5509.playbox.gpamanager.GPACalculator.calculateGpa;
import static com.fahadmalik5509.playbox.gpamanager.GPACalculator.getSubjectGPAStringFromPercentage;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.playSoundAndVibrate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fahadmalik5509.playbox.R;
import com.fahadmalik5509.playbox.databinding.SubjectItemLayoutBinding;
import java.util.List;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder> {

    public interface OnSaveClickListener {
        void onSaveClick(int position, String name, int totalMarks, int marksGained, int creditHours);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }

    private final List<Subject> subjectsList;
    private final OnSaveClickListener saveListener;
    private final OnDeleteClickListener deleteListener;


    public SubjectAdapter(List<Subject> subjectsList,
                          SubjectAdapter.OnSaveClickListener saveListener,
                          SubjectAdapter.OnDeleteClickListener deleteListener) {
        this.subjectsList = subjectsList;
        this.saveListener = saveListener;
        this.deleteListener = deleteListener;
    }

    @Override
    public int getItemCount() {
        return subjectsList.size();
    }

    @NonNull
    @Override
    public SubjectViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate your subject layout (e.g., R.layout.subject_card_layout)
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        SubjectItemLayoutBinding vb = SubjectItemLayoutBinding.inflate(inflater, parent, false);
        return new SubjectViewHolder(vb);
    }

    @Override
    public void onBindViewHolder(SubjectViewHolder holder, int pos) {
        Subject currentSubject = subjectsList.get(pos);

        holder.vb.subjectIndexTV.setText(String.valueOf(pos + 1));
        holder.vb.subjectNameET.setText(currentSubject.getSubjectName());
        holder.vb.subjectTotalMarksET.setText(String.valueOf(currentSubject.getTotalMarks()));
        holder.vb.subjectMarksGainedET.setText(String.valueOf(currentSubject.getMarksGained()));
        holder.vb.creditHoursET.setText(String.valueOf(currentSubject.getCreditHours()));
        holder.vb.subjectGPATV.setText(getSubjectGPAStringFromPercentage(currentSubject.getMarksGained()));

        holder.vb.subjectEditIV.setOnClickListener(v -> {
            if (holder.vb.subjectSaveB.getVisibility() == View.VISIBLE) return;

            playSoundAndVibrate(R.raw.sound_edit, true, 50);
            holder.vb.subjectNameET.setEnabled(true);
            holder.vb.subjectTotalMarksET.setEnabled(true);
            holder.vb.subjectMarksGainedET.setEnabled(true);
            holder.vb.creditHoursET.setEnabled(true);
            holder.vb.subjectSaveB.setVisibility(View.VISIBLE);
        });

        holder.vb.subjectSaveB.setOnClickListener(v -> {
            playSoundAndVibrate(R.raw.sound_ui, true, 50);
            holder.vb.subjectNameET.setEnabled(false);
            holder.vb.subjectTotalMarksET.setEnabled(false);
            holder.vb.subjectMarksGainedET.setEnabled(false);
            holder.vb.creditHoursET.setEnabled(false);
            holder.vb.subjectSaveB.setVisibility(View.GONE);

            saveListener.onSaveClick(pos, holder.vb.subjectNameET.getText().toString(), Integer.parseInt(holder.vb.subjectTotalMarksET.getText().toString()),
                    Integer.parseInt(holder.vb.subjectMarksGainedET.getText().toString()), Integer.parseInt(holder.vb.creditHoursET.getText().toString())
                    );

            holder.vb.subjectGPATV.setText(getSubjectGPAStringFromPercentage(currentSubject.getMarksGained()));
        });

        holder.vb.subjectDeleteIV.setOnClickListener(v -> {
            playSoundAndVibrate(R.raw.sound_delete, true, 50);
            deleteListener.onDeleteClick(pos);
        });


        // Common focus listener for playing a sound when focused
        View.OnFocusChangeListener focusListener = (v, hasFocus) -> {
            if (hasFocus) {
                playSoundAndVibrate(R.raw.sound_ui, true, 50);
            }
        };

        holder.vb.subjectNameET.setOnFocusChangeListener(focusListener);
        holder.vb.subjectTotalMarksET.setOnFocusChangeListener(focusListener);
        holder.vb.subjectMarksGainedET.setOnFocusChangeListener(focusListener);
        holder.vb.creditHoursET.setOnFocusChangeListener(focusListener);
    }

    public static class SubjectViewHolder extends RecyclerView.ViewHolder {
        SubjectItemLayoutBinding vb;

        public SubjectViewHolder(SubjectItemLayoutBinding vb) {

            super(vb.getRoot());
            this.vb = vb;
        }
    }
}