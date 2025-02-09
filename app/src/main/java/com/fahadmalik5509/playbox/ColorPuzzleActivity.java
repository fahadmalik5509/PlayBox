package com.fahadmalik5509.playbox;

import static com.fahadmalik5509.playbox.ActivityUtils.*;
import com.fahadmalik5509.playbox.databinding.ColorpuzzleLayoutBinding;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class ColorPuzzleActivity extends AppCompatActivity {

    ColorpuzzleLayoutBinding vb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vb = ColorpuzzleLayoutBinding.inflate(getLayoutInflater());
        setContentView(vb.getRoot());

    }
}
