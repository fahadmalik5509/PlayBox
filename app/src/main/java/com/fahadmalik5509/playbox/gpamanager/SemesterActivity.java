package com.fahadmalik5509.playbox.gpamanager;

import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.changeActivity;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.playSoundAndVibrate;

import android.os.Bundle;
import android.view.View;

import com.fahadmalik5509.playbox.R;
import com.fahadmalik5509.playbox.databinding.SemesterLayoutBinding;
import com.fahadmalik5509.playbox.miscellaneous.BaseActivity;

public class SemesterActivity extends BaseActivity {

    SemesterLayoutBinding vb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vb = SemesterLayoutBinding.inflate(getLayoutInflater());
        setContentView(vb.getRoot());

    }

    public void handleSemesterButton(View view) {
        playSoundAndVibrate(R.raw.sound_ui, true, 50);
        changeActivity(this, SubjectActivity.class);
    }

    @Override
    protected Class<?> getBackDestination() {
        return ProfileActivity.class;
    }
}
