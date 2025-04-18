package com.fahadmalik5509.playbox.gpamanager;

import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.changeActivity;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.playSoundAndVibrate;

import android.os.Bundle;
import android.view.View;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.fahadmalik5509.playbox.R;
import com.fahadmalik5509.playbox.databinding.ProfileLayoutBinding;
import com.fahadmalik5509.playbox.miscellaneous.BaseActivity;
import com.fahadmalik5509.playbox.miscellaneous.ToolsActivity;
import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends BaseActivity {

    private ProfileLayoutBinding vb;
    private ProfileAdapter adapter;
    private final List<Profile> profileList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vb = ProfileLayoutBinding.inflate(getLayoutInflater());
        setContentView(vb.getRoot());

        vb.profileRV.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProfileAdapter(
                profileList,
                // Save (insert/update)
                (pos, name) -> {
                    Profile p = profileList.get(pos);
                    p.setProfileName(name);
                    new Thread(() -> {
                        if (p.getProfileId() <= 0) {
                            long id = db.profileDao().insertProfile(p);
                            p.setProfileId((int) id);
                        } else {
                            db.profileDao().updateProfile(p);
                        }
                    }).start();
                },
                // Delete
                pos -> {
                    Profile p = profileList.get(pos);
                    new Thread(() -> db.profileDao().deleteProfile(p)).start();
                    runOnUiThread(() -> {
                        profileList.remove(pos);
                        adapter.notifyItemRemoved(pos);
                        if (pos < profileList.size()) {
                            adapter.notifyItemRangeChanged(pos, profileList.size() - pos);
                        }
                    });
                }
        );
        vb.profileRV.setAdapter(adapter);

        // Load existing profiles from DB (newest appear at bottom)
        new Thread(() -> {
            List<Profile> all = db.profileDao().getAllProfiles();
            runOnUiThread(() -> {
                profileList.clear();
                profileList.addAll(all);
                adapter.notifyDataSetChanged();
            });
        }).start();
    }

    public void handleAddProfile(View v) {
        playSoundAndVibrate(R.raw.sound_ui, true, 50);
        // Create and display a new blank profile at the bottom
        Profile p = new Profile("");
        profileList.add(p);
        int lastPos = profileList.size() - 1;
        adapter.notifyItemInserted(lastPos);
        vb.profileRV.scrollToPosition(lastPos);

        // Persist in background
        new Thread(() -> {
            long id = db.profileDao().insertProfile(p);
            p.setProfileId((int) id);
        }).start();
    }

    public void goToSemester(View v) {
        playSoundAndVibrate(R.raw.sound_ui, true, 50);
        changeActivity(this, SemesterActivity.class);
    }

    @Override
    protected Class<?> getBackDestination() {
        return ToolsActivity.class;
    }
}
