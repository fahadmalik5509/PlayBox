// SemesterActivity.java
package com.fahadmalik5509.playbox.gpamanager;

import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.playSoundAndVibrate;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.toggleVisibility;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.fahadmalik5509.playbox.R;
import com.fahadmalik5509.playbox.databinding.SemesterLayoutBinding;
import com.fahadmalik5509.playbox.miscellaneous.BaseActivity;

import java.util.ArrayList;
import java.util.List;

public class SemesterActivity extends BaseActivity {

    private final int MAX_SEMESTERS = 12;

    private SemesterLayoutBinding vb;
    private final List<Semester> semesterList = new ArrayList<>();
    private SemesterAdapter adapter;
    private int profileId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vb = SemesterLayoutBinding.inflate(getLayoutInflater());
        setContentView(vb.getRoot());

        // 1) Retrieve the profileId from the Intent
        profileId = getIntent().getIntExtra("profile_id", -1);
        if (profileId == -1) {
            finish();  // nothing to show
            return;
        }



        // 2) Setup RecyclerView
        vb.semesterRV.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SemesterAdapter(semesterList, this::openSubjectsForSemester);
        vb.semesterRV.setAdapter(adapter);

        // 3) Load existing semesters for this profile
        loadSemestersForProfile();
    }

    private void loadSemestersForProfile() {
        new Thread(() -> {
            List<Semester> list = db.semesterDao().getSemestersForProfile(profileId);
            runOnUiThread(() -> {
                semesterList.clear();
                semesterList.addAll(list);
                adapter.notifyDataSetChanged();

                // disable add button if already MAX_SEMESTERS
                if (semesterList.size() >= MAX_SEMESTERS) {
                    toggleVisibility(false, vb.addSemesterB);
                }
            });
        }).start();
    }

    /** Called by the "+" button in your layout */
    public void handleAddSemester(View v) {
        if (semesterList.size() >= MAX_SEMESTERS) return;

        playSoundAndVibrate(R.raw.sound_ui, true, 50);

        int nextNumber = semesterList.size() + 1;
        String name = "Semester " + nextNumber;
        Semester newSemester = new Semester(name, profileId);

        new Thread(() -> {
            long id = db.semesterDao().insertSemester(newSemester);
            newSemester.setSemesterId((int) id);

            runOnUiThread(() -> {
                semesterList.add(newSemester);
                adapter.notifyItemInserted(semesterList.size() - 1);
                vb.semesterRV.smoothScrollToPosition(semesterList.size() - 1);

                if (semesterList.size() >= MAX_SEMESTERS) {
                    vb.addSemesterB.setEnabled(false);
                    vb.addSemesterB.setAlpha(0.4f);
                }
            });
        }).start();
    }

    private void openSubjectsForSemester(int semesterId) {
        Intent intent = new Intent(this, SubjectActivity.class);
        intent.putExtra("semester_id", semesterId);
        startActivity(intent);
    }

    @Override
    protected Class<?> getBackDestination() {
        return ProfileActivity.class;
    }
}
