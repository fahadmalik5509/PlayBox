package com.fahadmalik5509.playbox.gpamanager;

import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.changeActivity;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.playSoundAndVibrate;

import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.fahadmalik5509.playbox.R;
import com.fahadmalik5509.playbox.databinding.SemesterLayoutBinding;
import com.fahadmalik5509.playbox.miscellaneous.BaseActivity;

import java.util.ArrayList;
import java.util.List;

public class SemesterActivity extends BaseActivity {

    SemesterLayoutBinding vb;
    private SemesterAdapter adapter;
    private final List<Semester> semesterList = new ArrayList<>();;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vb = SemesterLayoutBinding.inflate(getLayoutInflater());
        setContentView(vb.getRoot());
        // Set up the RecyclerView
        vb.semesterRV.setLayoutManager(new LinearLayoutManager(this));

        adapter = new SemesterAdapter(semesterList);
        vb.semesterRV.setAdapter(adapter);
    }

    public void handleAddSemester(View view) {

        playSoundAndVibrate(R.raw.sound_ui, true, 50);
        //vb.plusLAV.playAnimation();
        // Create a new subject with default or empty values
        Semester newSemester = new Semester("", 1);
        adapter.addSemester(newSemester);
        // Optionally scroll to the newly added item
        vb.semesterRV.smoothScrollToPosition(semesterList.size() - 1);
    }

    public void goToSubjects(View view) {
        playSoundAndVibrate(R.raw.sound_ui, true, 50);
        changeActivity(this, SubjectActivity.class);
    }

    @Override
    protected Class<?> getBackDestination() {
        return ProfileActivity.class;
    }
}
