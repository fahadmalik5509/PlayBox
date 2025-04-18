package com.fahadmalik5509.playbox.gpamanager;

import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.playSoundAndVibrate;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.toggleVisibility;

import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.fahadmalik5509.playbox.R;
import com.fahadmalik5509.playbox.databinding.SubjectLayoutBinding;
import com.fahadmalik5509.playbox.miscellaneous.BaseActivity;

import java.util.ArrayList;
import java.util.List;

public class SubjectActivity extends BaseActivity {

    private final int MAX_SUBJECTS = 16;

    private SubjectLayoutBinding vb;
    private SubjectAdapter adapter;
    private final List<Subject> subjectList = new ArrayList<>();
    private int semesterId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vb = SubjectLayoutBinding.inflate(getLayoutInflater());
        setContentView(vb.getRoot());

        semesterId = getIntent().getIntExtra("semester_id", -1);
        if (semesterId == -1) {
            finish();
            return;
        }

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
                pos -> {
                    Subject s = subjectList.get(pos);
                    new Thread(() -> db.subjectDao().deleteSubject(s)).start();
                    runOnUiThread(() -> {
                        subjectList.remove(pos);
                        adapter.notifyItemRemoved(pos);
                        if (pos < subjectList.size()) {
                            adapter.notifyItemRangeChanged(pos, subjectList.size() - pos);
                        }
                        // Re-enable the button if under limit
                        if (subjectList.size() < MAX_SUBJECTS) {
                            vb.addSubjectB.setEnabled(true);
                            vb.addSubjectB.setAlpha(1f);
                        }
                    });
                });
        vb.subjectRV.setAdapter(adapter);

        // Load existing subjects for this semester
        loadSubjectsForSemester();
    }

    private void loadSubjectsForSemester() {
        new Thread(() -> {
            List<Subject> list = db.subjectDao().getSubjectsForSemester(semesterId);
            runOnUiThread(() -> {
                subjectList.clear();
                subjectList.addAll(list);
                adapter.notifyDataSetChanged();

                if (subjectList.size() >= MAX_SUBJECTS) {
                    vb.addSubjectB.setEnabled(false);
                    vb.addSubjectB.setAlpha(0.4f);
                }
            });
        }).start();
    }

    public void handleAddSubject(View view) {
        if (subjectList.size() >= MAX_SUBJECTS) return;

        playSoundAndVibrate(R.raw.sound_ui, true, 50);

        // ⚠️ Assign semesterId when creating the subject
        Subject newSubject = new Subject("", 0, 0, 0, semesterId);

        new Thread(() -> {
            long id = db.subjectDao().insertSubject(newSubject);
            newSubject.setSubjectId((int) id);

            runOnUiThread(() -> {
                subjectList.add(newSubject);
                adapter.notifyItemInserted(subjectList.size() - 1);
                vb.subjectRV.smoothScrollToPosition(subjectList.size() - 1);

                if (subjectList.size() >= MAX_SUBJECTS) {
                    toggleVisibility(false, vb.addSubjectB);
                }
            });
        }).start();
    }

    @Override
    protected Class<?> getBackDestination() {
        return SemesterActivity.class;
    }
}
