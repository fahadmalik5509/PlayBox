package com.fahadmalik5509.playbox.gpamanager;

import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.changeActivity;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.playSoundAndVibrate;

import android.os.Bundle;
import android.view.View;

import com.fahadmalik5509.playbox.R;
import com.fahadmalik5509.playbox.databinding.ProfileLayoutBinding;
import com.fahadmalik5509.playbox.miscellaneous.BaseActivity;
import com.fahadmalik5509.playbox.miscellaneous.ToolsActivity;


public class ProfileActivity extends BaseActivity {

    ProfileLayoutBinding vb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vb = ProfileLayoutBinding.inflate(getLayoutInflater());
        setContentView(vb.getRoot());

    }

    public void handleProfileButton(View view) {
        playSoundAndVibrate(R.raw.sound_ui, true, 50);
        changeActivity(this, SemesterActivity.class);
    }

    @Override
    protected Class<?> getBackDestination() { return ToolsActivity.class; }
}
