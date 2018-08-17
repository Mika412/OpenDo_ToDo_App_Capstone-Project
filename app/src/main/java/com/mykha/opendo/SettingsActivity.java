package com.mykha.opendo;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;


public class SettingsActivity extends AppCompatActivity {


    @BindView(R.id.toolbar)
    Toolbar toolbar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences preferences = getSharedPreferences(Const.PREFS_NAME, MODE_PRIVATE);
        boolean useDarkTheme = preferences.getBoolean(Const.PREF_DARK_THEME, false);

        if(useDarkTheme) {
            setTheme(R.style.PreferenceThemeOverlayDark);
        }

        getFragmentManager().beginTransaction().replace(android.R.id.content, new GeneralPreferenceFragment()).commit();
        getLayoutInflater().inflate(R.layout.toolbar, (ViewGroup)findViewById(android.R.id.content));

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

//        setContentView(R.layout.activity_login);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {

        Activity currentActivity;
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);

            currentActivity = getActivity();

            setHasOptionsMenu(true);
            bindThemeSwitcherToValue(findPreference("theme_key"));
            bindSwitchToValue(findPreference("reminders_enabled"));
        }


        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("some_key", "String data");
                getActivity().setResult(Activity.RESULT_OK, resultIntent);
                getActivity().finish();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        private final Preference.OnPreferenceChangeListener sBindThemeToValueListener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                String stringValue = value.toString();

                if (preference instanceof ListPreference) {
                    // For list preferences, look up the correct display value in
                    // the preference's 'entries' list.
                    ListPreference listPreference = (ListPreference) preference;
                    int index = listPreference.findIndexOfValue(stringValue);

                    // Set the summary to reflect the new value.
                    preference.setSummary(
                            index >= 0
                                    ? listPreference.getEntries()[index]
                                    : null);
                    if(index == 0)
                        toggleTheme(false);
                    else
                        toggleTheme(true);
                }
                return true;
            }
        };

        private final Preference.OnPreferenceChangeListener sBindSwitchToValueListener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {

                if (preference instanceof SwitchPreference) {
                    if(!((Boolean) value))
                        deleteAllReminders();
                }
                return true;
            }
        };

        private void deleteAllReminders() {
            SharedPreferences sharedPreferences = getContext().getSharedPreferences(Const.PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor sharedEditor = sharedPreferences.edit();
            List<String> allReminders = new ArrayList<>();
            Map<String, ?> allEntries = sharedPreferences.getAll();
            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                String possibleName = entry.getKey();
                if(possibleName.contains(Const.KeyHeaderPref)){
                    allReminders.add(possibleName.replace(Const.KeyHeaderPref, ""));
                }
            }

            for (String key : allReminders) {

                Bundle bundle = new Bundle();
                bundle.putString("listKey", sharedPreferences.getString(Const.ListIdHeaderPref, "").replace(Const.ListIdHeaderPref, ""));
                bundle.putString("listName", sharedPreferences.getString(Const.ListNameHeaderPref, "").replace(Const.ListNameHeaderPref, ""));
                bundle.putString("taskId", sharedPreferences.getString(Const.KeyHeaderPref, "").replace(Const.KeyHeaderPref, ""));
                bundle.putString("taskName", sharedPreferences.getString(Const.KeyNamePref, "").replace(Const.KeyNamePref, ""));

                NotificationUtil.deleteReminder(getContext().getApplicationContext(), NotificationUtil.getNotification(getContext(), bundle), key, sharedPreferences);

                //Delete all shared preferences for this todo key
                sharedEditor.remove(Const.ListIdHeaderPref + key);
                sharedEditor.remove(Const.ListNameHeaderPref + key);
                sharedEditor.remove(Const.KeyHeaderPref + key);
                sharedEditor.remove(Const.KeyNamePref + key);
                sharedEditor.remove(Const.ReminderTimePref + key);
                sharedEditor.remove(Const.ReminderDonePref + key);
                sharedEditor.apply();
            }
        }

        private void bindThemeSwitcherToValue(Preference preference) {
            // Set the listener to watch for value changes.
            preference.setOnPreferenceChangeListener(sBindThemeToValueListener);

            // Trigger the listener immediately with the preference's
            // current value.
            sBindThemeToValueListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getString(preference.getKey(), ""));

        }
        private void bindSwitchToValue(Preference preference) {
            // Set the listener to watch for value changes.
            preference.setOnPreferenceChangeListener(sBindSwitchToValueListener);

            // Trigger the listener immediately with the preference's
            // current value.
            sBindSwitchToValueListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getBoolean(preference.getKey(), true));

        }

        private void toggleTheme(boolean darkTheme) {
            SharedPreferences sp = currentActivity.getSharedPreferences(Const.PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            if(sp.getBoolean(Const.PREF_DARK_THEME, false) != darkTheme) {
                editor.putBoolean(Const.PREF_DARK_THEME, darkTheme);
                editor.apply();
                getActivity().setTheme(R.style.PreferenceThemeOverlayDark);
                getActivity().recreate();
            }
        }
    }
}
