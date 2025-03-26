package com.fahadmalik5509.playbox.cgpacalculator;

import android.os.Bundle;

import com.fahadmalik5509.playbox.databinding.CgpaLayoutBinding;
import com.fahadmalik5509.playbox.miscellaneous.BaseActivity;
import com.fahadmalik5509.playbox.miscellaneous.ToolsActivity;

public class CgpaActivity extends BaseActivity {

    CgpaLayoutBinding vb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vb = CgpaLayoutBinding.inflate(getLayoutInflater());
        setContentView(vb.getRoot());

    }

    @Override
    protected Class<?> getBackDestination() {
        return ToolsActivity.class;
    }
}
