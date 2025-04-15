package com.fahadmalik5509.playbox.gpamanager;

import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.playSoundAndVibrate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fahadmalik5509.playbox.R;
import com.fahadmalik5509.playbox.databinding.SubjectItemLayoutBinding;
import java.util.ArrayList;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder> {

    private final ArrayList<Subject> subjectList;

    public SubjectAdapter(ArrayList<Subject> subjectList) {
        this.subjectList = subjectList;

    }

    @Override
    public int getItemCount() {
        return subjectList.size();
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
    public void onBindViewHolder(SubjectViewHolder holder, int position) {
        Subject currentSubject = subjectList.get(position);

        // Set the index (displaying one-based index)
        holder.vb.indexTV.setText(String.valueOf(position + 1));

        String subjectNameStr = currentSubject.getSubjectName();
        String totalMarksStr = String.valueOf(currentSubject.getTotalMarks());
        String marksGainedStr = String.valueOf(currentSubject.getMarksGained());
        String creditHoursStr = String.valueOf(currentSubject.getCreditHours());

        // Bind the subject name and other fields as needed
        if (!holder.vb.subjectNameET.getText().toString().equals(subjectNameStr) && subjectNameStr.isEmpty())
            holder.vb.subjectNameET.setText(currentSubject.getSubjectName());
        if (!holder.vb.totalMarksET.getText().toString().equals(totalMarksStr) && currentSubject.getTotalMarks() != 0)
            holder.vb.totalMarksET.setText(totalMarksStr);
        if (!holder.vb.marksGainedET.getText().toString().equals(marksGainedStr) && currentSubject.getMarksGained() != 0)
            holder.vb.marksGainedET.setText(marksGainedStr);
        if (!holder.vb.creditHoursET.getText().toString().equals(creditHoursStr) && currentSubject.getCreditHours() != 0)
            holder.vb.creditHoursET.setText(creditHoursStr);



        holder.vb.deleteIV.setOnClickListener(v -> {
            playSoundAndVibrate(R.raw.sound_delete, true, 50);

            if (position != RecyclerView.NO_POSITION) {
                removeSubject(position);
            }
        });


        holder.vb.editIV.setOnClickListener(v -> {
            if (holder.vb.saveB.getVisibility() == View.VISIBLE) return;

            playSoundAndVibrate(R.raw.sound_edit, true, 50);
            holder.vb.subjectNameET.setEnabled(true);
            holder.vb.totalMarksET.setEnabled(true);
            holder.vb.marksGainedET.setEnabled(true);
            holder.vb.creditHoursET.setEnabled(true);
            holder.vb.saveB.setVisibility(View.VISIBLE);
        });

        holder.vb.saveB.setOnClickListener(v -> {
            playSoundAndVibrate(R.raw.sound_ui, true, 50);
            holder.vb.subjectNameET.setEnabled(false);
            holder.vb.totalMarksET.setEnabled(false);
            holder.vb.marksGainedET.setEnabled(false);
            holder.vb.creditHoursET.setEnabled(false);
            holder.vb.saveB.setVisibility(View.GONE);
        });

        // Common focus listener for playing a sound when focused
        View.OnFocusChangeListener focusListener = (v, hasFocus) -> {
            if (hasFocus) {
                playSoundAndVibrate(R.raw.sound_ui, true, 50);
            }
        };

        holder.vb.subjectNameET.setOnFocusChangeListener(focusListener);
        holder.vb.totalMarksET.setOnFocusChangeListener(focusListener);
        holder.vb.marksGainedET.setOnFocusChangeListener(focusListener);
        holder.vb.creditHoursET.setOnFocusChangeListener(focusListener);
    }


    // A helper to add a new subject
    public void addSubject(Subject newSubject) {
        subjectList.add(newSubject);
        // Notify the adapter that a new item is inserted at the end
        notifyItemInserted(subjectList.size() - 1);
    }

    // Remove subject based on its position
    public void removeSubject(int position) {
        if (position < subjectList.size() && position >= 0) {
            subjectList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, subjectList.size()); // refresh positions
        }
    }

    public static class SubjectViewHolder extends RecyclerView.ViewHolder {
        SubjectItemLayoutBinding vb;

        public SubjectViewHolder(SubjectItemLayoutBinding vb) {

            super(vb.getRoot());
            this.vb = vb;
        }
    }
}
