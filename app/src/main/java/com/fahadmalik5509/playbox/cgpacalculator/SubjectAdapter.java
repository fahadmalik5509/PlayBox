package com.fahadmalik5509.playbox.cgpacalculator;

import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.playSoundAndVibrate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fahadmalik5509.playbox.R;

import java.util.ArrayList;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder> {

    private final ArrayList<Subject> subjectList;

    public SubjectAdapter(ArrayList<Subject> subjectList) {
        this.subjectList = subjectList;

    }


    @Override
    public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate your subject layout (e.g., R.layout.subject_card_layout)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.subject_layout, parent, false);
        return new SubjectViewHolder(view);

    }

    @Override
    public void onBindViewHolder(SubjectViewHolder holder, int position) {
        Subject currentSubject = subjectList.get(position);
        // Bind the data (example for the subject name)
        holder.subjectNameET.setText(currentSubject.getSubjectName());
        // Bind more fields as needed—for marks, total marks, credit hours, etc.

        // Set delete button listener
        holder.deleteIV.setOnClickListener(v -> {
            playSoundAndVibrate(R.raw.sound_delete, true, 50);
            // Remove the item at this position
            removeSubject(holder.getAdapterPosition());
        });

        holder.editIV.setOnClickListener(v -> {
            if(holder.saveB.getVisibility() == View.VISIBLE) return;

            playSoundAndVibrate(R.raw.sound_edit, true, 50);
            holder.subjectNameET.setEnabled(true);
            holder.totalMarksET.setEnabled(true);
            holder.marksObtainedET.setEnabled(true);
            holder.creditHoursET.setEnabled(true);
            holder.saveB.setVisibility(View.VISIBLE);
        });

        holder.saveB.setOnClickListener(v -> {
            playSoundAndVibrate(R.raw.sound_ui, true, 50);
            holder.subjectNameET.setEnabled(false);
            holder.totalMarksET.setEnabled(false);
            holder.marksObtainedET.setEnabled(false);
            holder.creditHoursET.setEnabled(false);
            holder.saveB.setVisibility(View.GONE);
        });

        View.OnFocusChangeListener focusListener = (v, hasFocus) -> {
            if (hasFocus) {
                playSoundAndVibrate(R.raw.sound_ui, true, 50);
            }
        };

        holder.subjectNameET.setOnFocusChangeListener(focusListener);
        holder.totalMarksET.setOnFocusChangeListener(focusListener);
        holder.marksObtainedET.setOnFocusChangeListener(focusListener);
        holder.creditHoursET.setOnFocusChangeListener(focusListener);
    }

    @Override
    public int getItemCount() {
        return subjectList.size();
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

    class SubjectViewHolder extends RecyclerView.ViewHolder {
        // Define the views (using findViewById or view binding if available)
        EditText subjectNameET;
        EditText totalMarksET;
        EditText marksObtainedET;
        EditText creditHoursET;
        TextView gpaTV;
        ImageView deleteIV, editIV;
        Button saveB;

        public SubjectViewHolder(View itemView) {
            super(itemView);
            subjectNameET = itemView.findViewById(R.id.subjectNameTV);
            totalMarksET = itemView.findViewById(R.id.totalMarksET);
            marksObtainedET = itemView.findViewById(R.id.marksObtainedET);
            creditHoursET = itemView.findViewById(R.id.creditHoursET);
            gpaTV = itemView.findViewById(R.id.gpaTV);
            deleteIV = itemView.findViewById(R.id.deleteIV);
            editIV = itemView.findViewById(R.id.editIV);
            saveB = itemView.findViewById(R.id.saveB);
        }
    }
}
