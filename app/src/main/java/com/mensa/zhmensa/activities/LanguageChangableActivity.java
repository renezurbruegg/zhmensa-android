package com.mensa.zhmensa.activities;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

import com.mensa.zhmensa.services.LocaleManager;

/**
 * Base activity.
 * Needed to be able to change language. Changes the base context when it is attached
 */
public abstract class LanguageChangableActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(new LocaleManager(base).setLocale(base));
    }

}
