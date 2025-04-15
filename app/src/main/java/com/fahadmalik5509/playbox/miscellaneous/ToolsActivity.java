package com.fahadmalik5509.playbox.miscellaneous;

import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.changeActivity;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.playSoundAndVibrate;

import android.os.Bundle;
import android.view.View;

import com.fahadmalik5509.playbox.R;
import com.fahadmalik5509.playbox.gpamanager.SubjectActivity;
import com.fahadmalik5509.playbox.databinding.ToolsLayoutBinding;

public class ToolsActivity extends BaseActivity {

    ToolsLayoutBinding vb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vb = ToolsLayoutBinding.inflate(getLayoutInflater());
        setContentView(vb.getRoot());

    }

    public void handleGPAManagerButtonClick(View view) {
        playSoundAndVibrate(R.raw.sound_ui, true, 50);
        changeActivity(this, SubjectActivity.class);
    }

    @Override
    protected Class<?> getBackDestination() {
        return HomeActivity.class;
    }
}
