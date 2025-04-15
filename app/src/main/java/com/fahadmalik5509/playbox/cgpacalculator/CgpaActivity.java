package com.fahadmalik5509.playbox.cgpacalculator;

import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.playSoundAndVibrate;

import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.fahadmalik5509.playbox.R;
import com.fahadmalik5509.playbox.databinding.CgpaLayoutBinding;
import com.fahadmalik5509.playbox.miscellaneous.BaseActivity;
import com.fahadmalik5509.playbox.miscellaneous.ToolsActivity;

import java.util.ArrayList;

public class CgpaActivity extends BaseActivity {

    CgpaLayoutBinding vb;
    private SubjectAdapter adapter;
    private ArrayList<Subject> subjectList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vb = CgpaLayoutBinding.inflate(getLayoutInflater());
        setContentView(vb.getRoot());
        // Initialize your list (this could be empty or pre-populated)
        subjectList = new ArrayList<>();

        // Set up the RecyclerView
        vb.subjectRV.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SubjectAdapter(subjectList);
        vb.subjectRV.setAdapter(adapter);

    }

    public void handleAddSubject(View view) {

        playSoundAndVibrate(R.raw.sound_ui, true, 50);
        //vb.plusLAV.playAnimation();
        // Create a new subject with default or empty values
        Subject newSubject = new Subject("", 0, 0, 0);
        adapter.addSubject(newSubject);
        // Optionally scroll to the newly added item
        vb.subjectRV.smoothScrollToPosition(subjectList.size() - 1);
    }

    @Override
    protected Class<?> getBackDestination() {
        return ToolsActivity.class;
    }
}
