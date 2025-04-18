package com.fahadmalik5509.playbox.gpamanager;

import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.playSoundAndVibrate;

import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.fahadmalik5509.playbox.R;
import com.fahadmalik5509.playbox.databinding.SubjectLayoutBinding;
import com.fahadmalik5509.playbox.miscellaneous.BaseActivity;
import java.util.ArrayList;
import java.util.List;

public class SubjectActivity extends BaseActivity {

    SubjectLayoutBinding vb;
    private SubjectAdapter adapter;
    private final List<Subject> subjectList = new ArrayList<>();;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vb = SubjectLayoutBinding.inflate(getLayoutInflater());
        setContentView(vb.getRoot());

        // Set up the RecyclerView
        vb.subjectRV.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SubjectAdapter(
                subjectList,
                (pos, name, totalMarks, marksGained, creditHours) -> {
            Subject s = subjectList.get(pos);
            s.setSubjectName(name);
            s.setTotalMarks(totalMarks);
            s.setMarksGained(marksGained);
            s.setCreditHours(creditHours);
            new Thread(() -> {
                if (s.getSubjectId() <= 0) {
                    long id = db.subjectDao().insertSubject(s);
                    s.setSubjectId((int) id);
                } else {
                    db.subjectDao().updateSubject(s);
                }
            }).start();
        },
            // Delete
            pos -> {
                Subject s = subjectList.get(pos);
                new Thread(() -> db.subjectDao().deleteSubject(s)).start();
                runOnUiThread(() -> {
                    subjectList.remove(pos);
                    adapter.notifyItemRemoved(pos);
                    if (pos < subjectList.size()) {
                        adapter.notifyItemRangeChanged(pos, subjectList.size() - pos);
                    }
                });
            });
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
        return SemesterActivity.class;
    }
}
