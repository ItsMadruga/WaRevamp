package its.madruga.warevamp.app.ui.fragments.core;

import static android.content.Context.MODE_WORLD_READABLE;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

import its.madruga.warevamp.BuildConfig;
import its.madruga.warevamp.broadcast.senders.ModuleSender;

public class BasePreferenceActivity extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    @SuppressLint("WorldReadableFiles")
    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        getPreferenceManager().setSharedPreferencesMode(MODE_WORLD_READABLE);
        getPreferenceManager().setSharedPreferencesName(BuildConfig.APPLICATION_ID + "_preferences");
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String s) {
        ModuleSender.sendNeedsReboot("Preference changed");
    }

    public ActionBar getSupportActionBar() {
        if (getActivity() == null) return null;
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    }
}
