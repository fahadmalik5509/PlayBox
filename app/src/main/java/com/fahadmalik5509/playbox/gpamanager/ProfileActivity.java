// ProfileActivity.java
package com.fahadmalik5509.playbox.gpamanager;

import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.playSoundAndVibrate;

import android.content.Intent;
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
                },
                // Click to open semesters
                this::openSemestersForProfile
        );
        vb.profileRV.setAdapter(adapter);

        // Load all profiles from DB
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
        Profile p = new Profile("");
        profileList.add(p);
        int lastPos = profileList.size() - 1;
        adapter.notifyItemInserted(lastPos);
        vb.profileRV.scrollToPosition(lastPos);

        new Thread(() -> {
            long id = db.profileDao().insertProfile(p);
            p.setProfileId((int) id);
        }).start();
    }

    private void openSemestersForProfile(int profileId) {
        Intent intent = new Intent(this, SemesterActivity.class);
        intent.putExtra("profile_id", profileId);
        startActivity(intent);
    }

    @Override
    protected Class<?> getBackDestination() {
        return ToolsActivity.class;
    }
}
