package com.mensa.zhmensa.component.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.google.android.material.snackbar.Snackbar;
import com.mensa.zhmensa.R;
import com.mensa.zhmensa.services.LocaleManager;
import com.mensa.zhmensa.services.MensaManager;

import java.util.HashSet;

public class SettingsFragment extends PreferenceFragmentCompat {

    @SuppressWarnings("HardCodedStringLiteral")
    private static final String LANGUAGE_PREFERENCE = "language_preference";

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings_view);


        Preference button = findPreference(getString(R.string.myDummyButton));

        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                //code for what you want it to do
                PreferenceManager.getDefaultSharedPreferences(getContext())
                        .edit()
                        .putStringSet(MensaManager.DELETED_MENUS_STORE_ID, new HashSet<String>())
                        .apply();
                Snackbar.make(getView(), getString(R.string.msg_menus_deleted), Snackbar.LENGTH_SHORT).show();
                return true;
            }
        });

        findPreference(getString(R.string.myClearCacheButton)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                MensaManager.clearCache(getContext());
                return true;
            }
        });

        findPreference(LANGUAGE_PREFERENCE).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                new LocaleManager(getContext()).setNewLocale(getContext(), newValue.toString());;
                getActivity().finish();
                startActivity(getActivity().getIntent());
                return true;
            }
        });

    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Nothing
    }

}