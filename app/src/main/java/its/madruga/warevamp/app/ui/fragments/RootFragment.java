package its.madruga.warevamp.app.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import its.madruga.warevamp.App;
import its.madruga.warevamp.R;
import its.madruga.warevamp.app.core.XposedChecker;
import its.madruga.warevamp.broadcast.senders.ModuleSender;
import its.madruga.warevamp.databinding.RootFragmentBinding;
import its.madruga.warevamp.app.ui.activitys.AboutActivity;
import its.madruga.warevamp.app.ui.fragments.core.BaseFragment;
import its.madruga.warevamp.app.ui.fragments.core.BasePreferenceActivity;

public class RootFragment extends BaseFragment {
    RootFragmentBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = RootFragmentBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getChildFragmentManager().beginTransaction().replace(binding.container.getId(), new RootPreference()).commit();
    }

    public static class RootPreference extends BasePreferenceActivity {
        @Override
        public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
            if (!XposedChecker.isActive()) {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
                builder.setTitle(R.string.module_status);
                builder.setMessage(App.getInstance().getString(R.string.module_status) + "  " + App.getInstance().getString(R.string.module_disable));
                builder.setPositiveButton(android.R.string.ok, (dialogInterface, i) -> dialogInterface.dismiss());
                builder.create().show();

                return;
            }
            super.onCreatePreferences(savedInstanceState, rootKey);
            setPreferencesFromResource(R.xml.root_fragment, rootKey);

            findPreference("about").setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent();
                intent.setClass(requireContext(), AboutActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
            });
        }
        @Override
        public void onResume() {
            super.onResume();

            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setTitle(R.string.app_name);
        }
    }

    public static class PrivacyPreference extends BasePreferenceActivity {

        @Override
        public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
            super.onCreatePreferences(savedInstanceState, rootKey);
            setPreferencesFromResource(R.xml.privacy_fragment, rootKey);
        }

        @Override
        public void onResume() {
            super.onResume();
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.privacy);
        }
    }

    public static class PersoPreference extends BasePreferenceActivity {

        @Override
        public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
            super.onCreatePreferences(savedInstanceState, rootKey);
            setPreferencesFromResource(R.xml.perso_fragment, rootKey);
        }

        @Override
        public void onResume() {
            super.onResume();
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.perso);
        }
    }

    public static class PersoHomePreference extends BasePreferenceActivity {

        @Override
        public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
            super.onCreatePreferences(savedInstanceState, rootKey);
            setPreferencesFromResource(R.xml.perso_home_fragment, rootKey);
        }

        @Override
        public void onResume() {
            super.onResume();
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.home);
        }
    }

    public static class PersoConvPreference extends BasePreferenceActivity {

        @Override
        public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
            super.onCreatePreferences(savedInstanceState, rootKey);
            setPreferencesFromResource(R.xml.perso_conv_fragment, rootKey);
        }

        @Override
        public void onResume() {
            super.onResume();
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.conv);
        }
    }

    public static class MediaPreference extends BasePreferenceActivity {

        @Override
        public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
            super.onCreatePreferences(savedInstanceState, rootKey);
            setPreferencesFromResource(R.xml.media_fragment, rootKey);
        }

        @Override
        public void onResume() {
            super.onResume();
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.media_settings);
        }
    }

    public static class PersoGeneralPreference extends BasePreferenceActivity {

        @Override
        public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
            super.onCreatePreferences(savedInstanceState, rootKey);
            setPreferencesFromResource(R.xml.perso_general_fragment, rootKey);
        }

        @Override
        public void onResume() {
            super.onResume();
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.general);
        }

    }

    public static class CleanDatabasePreference extends BasePreferenceActivity {
        @Override
        public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
            super.onCreatePreferences(savedInstanceState, rootKey);
            setPreferencesFromResource(R.xml.clean_fragment, rootKey);

            findPreference("msgstore").setOnPreferenceClickListener(preference -> {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
                builder.setTitle(R.string.clean_msgstore);
                builder.setMessage(R.string.clean_database_msg);
                builder.setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                    ModuleSender.sendCleanDatabase("msgstore");
                });
                builder.setNegativeButton(android.R.string.cancel, ((dialogInterface, i) -> {
                    dialogInterface.dismiss();
                }));
                builder.create().show();
                return true;
            });

            findPreference("axolotl").setOnPreferenceClickListener(preference -> {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
                builder.setTitle(R.string.clean_axolotl);
                builder.setMessage(R.string.clean_database_msg);
                builder.setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                    ModuleSender.sendCleanDatabase("axolotl");
                });
                builder.setNegativeButton(android.R.string.cancel, ((dialogInterface, i) -> {
                    dialogInterface.dismiss();
                }));
                builder.create().show();
                return true;
            });

            findPreference("wa").setOnPreferenceClickListener(preference -> {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
                builder.setTitle(R.string.clean_wa);
                builder.setMessage(R.string.clean_database_msg);
                builder.setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                    ModuleSender.sendCleanDatabase("wa");
                });
                builder.setNegativeButton(android.R.string.cancel, ((dialogInterface, i) -> {
                    dialogInterface.dismiss();
                }));
                builder.create().show();
                return true;
            });

            findPreference("sticker").setOnPreferenceClickListener(preference -> {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
                builder.setTitle(R.string.clean_msgstore);
                builder.setMessage(R.string.clean_database_msg);
                builder.setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                    ModuleSender.sendCleanDatabase("sticker");
                });
                builder.setNegativeButton(android.R.string.cancel, ((dialogInterface, i) -> {
                    dialogInterface.dismiss();
                }));
                builder.create().show();
                return true;
            });
        }

        @Override
        public void onResume() {
            super.onResume();
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.clean_database);
        }
    }
}
