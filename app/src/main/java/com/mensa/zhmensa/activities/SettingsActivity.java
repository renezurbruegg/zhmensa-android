package com.mensa.zhmensa.activities;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import com.mensa.zhmensa.R;
import com.mensa.zhmensa.component.fragments.SettingsFragment;
import com.mensa.zhmensa.services.MensaManager;

/**
 * Activity that only displays the Settings fragment.
 * If closed, reset state of mensa manager to trigger a reload.
 */
public class SettingsActivity extends LanguageChangableActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SettingsFragment frag = new SettingsFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, frag)
                .commit();
        getSupportActionBar().setTitle(getString(R.string.settings));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            MensaManager.clearState();
            return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

}