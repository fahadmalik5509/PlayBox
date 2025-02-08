package com.fahadmalik5509.playbox;

import static com.fahadmalik5509.playbox.ActivityUtils.*;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;

import com.fahadmalik5509.playbox.databinding.ColorpuzzleLayoutBinding;
import com.fahadmalik5509.playbox.databinding.HomeLayoutBinding;

import androidx.appcompat.app.AppCompatActivity;



public class ColorPuzzleActivity extends AppCompatActivity {

    ColorpuzzleLayoutBinding vb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vb = ColorpuzzleLayoutBinding.inflate(getLayoutInflater());
        setContentView(vb.getRoot());

    }
}
