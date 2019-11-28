package com.mrkai.estick;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportFragmentManager().addOnBackStackChangedListener(this);

        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);

        boolean saved = prefs.getBoolean("Saved",false);

        if (savedInstanceState == null)
            if (saved)
                getSupportFragmentManager().beginTransaction().add(R.id.fragment, new TerminalFragment(), "devices").commit();
            else
                getSupportFragmentManager().beginTransaction().add(R.id.fragment, new Registeration(), "devices").commit();

        else
            onBackStackChanged();
    }

    @Override
    public void onBackStackChanged() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(getSupportFragmentManager().getBackStackEntryCount() > 0);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
