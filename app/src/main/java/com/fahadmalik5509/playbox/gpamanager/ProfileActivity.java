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


public class ProfileActivity extends BaseActivity {

    ProfileLayoutBinding vb;
    private ProfileAdapter adapter;
    private ArrayList<Profile> profileList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vb = ProfileLayoutBinding.inflate(getLayoutInflater());
        setContentView(vb.getRoot());

        profileList = new ArrayList<>();

        // Set up the RecyclerView
        vb.profileRV.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProfileAdapter(profileList);
        vb.profileRV.setAdapter(adapter);
    }

    public void handleAddProfile(View view) {

        playSoundAndVibrate(R.raw.sound_ui, true, 50);
        //vb.plusLAV.playAnimation();
        // Create a new subject with default or empty values
        Profile newProfile = new Profile("");
        adapter.addProfile(newProfile);
        // Optionally scroll to the newly added item
        vb.profileRV.smoothScrollToPosition(profileList.size() - 1);
    }

    public void goToSemester(View view) {
        playSoundAndVibrate(R.raw.sound_ui, true, 50);
        changeActivity(this, SemesterActivity.class);
    }

    @Override
    protected Class<?> getBackDestination() { return ToolsActivity.class; }
}
