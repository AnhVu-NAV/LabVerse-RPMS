package com.prm392.g5.labverse;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.prm392.g5.labverse.activity.MyLibraryActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Immediately redirect to the library screen and finish this activity
        startActivity(new Intent(this, MyLibraryActivity.class));
        finish();
    }
}