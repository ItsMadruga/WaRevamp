package its.madruga.warevamp.app.ui.activitys;

import static its.madruga.warevamp.app.core.Utils.WHATSAPP_PACKAGE;
import static its.madruga.warevamp.app.core.Utils.isInstalled;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.color.MaterialColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

import its.madruga.warevamp.App;
import its.madruga.warevamp.BuildConfig;
import its.madruga.warevamp.R;
import its.madruga.warevamp.app.core.XposedChecker;
import its.madruga.warevamp.broadcast.Events;
import its.madruga.warevamp.broadcast.receivers.ModuleReceiver;
import its.madruga.warevamp.broadcast.senders.ModuleSender;
import its.madruga.warevamp.databinding.ActivityMainBinding;
import its.madruga.warevamp.app.ui.views.InfoCard;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupInfoCard();
        setupUpdateCard();

        setSupportActionBar(binding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.app_name);
    }

    public void setupInfoCard() {
        InfoCard infoCard = binding.infos;
        InfoCard wppReboot = binding.wppReboot;

        infoCard.setTitle(App.getInstance().getString(R.string.app_name));
        infoCard.setSubTitle(App.getInstance().getString(R.string.app_desc));

        SpannableString versionString = new SpannableString(App.getInstance().getString(R.string.module_status) + " " + (XposedChecker.isActive() ? App.getInstance().getString(R.string.module_enable) : App.getInstance().getString(R.string.module_disable)));

        versionString.setSpan(new StyleSpan(Typeface.BOLD), 0, 15, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        versionString.setSpan(new ForegroundColorSpan(XposedChecker.isActive() ? MaterialColors.getColor(binding.getRoot(), com.google.android.material.R.attr.colorPrimary) : Color.RED), 15, versionString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        infoCard.setVersion(versionString);

        if (!isInstalled(WHATSAPP_PACKAGE)) {
            setDisableWpp();
        }

        if (!XposedChecker.isActive()) {
            binding.infos.getWaVersionView().setVisibility(View.GONE);
            return;
        }

        ModuleSender.sendCheckWpp();

        new ModuleReceiver().registerReceiver(Events.ACTION_WA_REVAMP_ENABLED, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                var appVersion = intent.getStringExtra("version");

                SpannableString versionString1 = new SpannableString(App.getInstance().getString(R.string.whatsapp_version) + " " + appVersion);
                versionString1.setSpan(new StyleSpan(Typeface.BOLD), 0, 18, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                versionString1.setSpan(new ForegroundColorSpan(MaterialColors.getColor(binding.getRoot(), com.google.android.material.R.attr.colorPrimary)), 18, versionString1.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                infoCard.setWaVersion(versionString1);

                SpannableString rebootString = new SpannableString(App.getInstance().getString(R.string.reboot_wpp));
                rebootString.setSpan(new StyleSpan(Typeface.BOLD), 0, rebootString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                rebootString.setSpan(new ForegroundColorSpan(MaterialColors.getColor(binding.getRoot(), com.google.android.material.R.attr.colorPrimary)), 0, rebootString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                wppReboot.setSubTitle(rebootString);

                binding.wppReboot.setVisibility(View.VISIBLE);
                wppReboot.setEnabled(true);

            }
        });
        wppReboot.setOnClickListener(v -> {
            ModuleSender.sendRebootWpp();
        });
    }

    public void setDisableWpp() {
        SpannableString versionString = new SpannableString(App.getInstance().getString(R.string.whatsapp_version) + " " + App.getInstance().getString(R.string.module_disable));

        versionString.setSpan(new StyleSpan(Typeface.BOLD), 0, 17, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        versionString.setSpan(new ForegroundColorSpan(Color.RED), 18, versionString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        binding.infos.setWaVersion(versionString);

        binding.wppReboot.setVisibility(View.GONE);
    }

    public void setupUpdateCard() {
        new Thread(() -> {
            try {
                URL url = new URL("https://raw.githubusercontent.com/ItsMadruga/WaRevamp/refs/heads/master/update.json");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    String version = jsonResponse.getString("version");
                    String date = jsonResponse.getString("date");
                    String link = jsonResponse.getString("link");
                    String changelog = jsonResponse.getString("changelog");

                    if (!version.equals(BuildConfig.VERSION_NAME) && !link.isEmpty()) {
                        MainActivity.this.runOnUiThread(() -> {
                            InfoCard update = binding.update;

                            SpannableString updateTitle = new SpannableString(App.getInstance().getString(R.string.module_update_avaliable) + " - v" + version);

                            updateTitle.setSpan(new StyleSpan(Typeface.BOLD), 0, updateTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                            updateTitle.setSpan(new ForegroundColorSpan(MaterialColors.getColor(binding.getRoot(), com.google.android.material.R.attr.colorSurface)), 0, updateTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                            update.setTitle(updateTitle);

                            SpannableString updateSummary = new SpannableString(App.getInstance().getString(R.string.module_update_summary));

                            updateSummary.setSpan(new ForegroundColorSpan(MaterialColors.getColor(binding.getRoot(), com.google.android.material.R.attr.colorSurface)), 0, updateSummary.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                            update.setSubTitle(updateSummary);

                            update.setCardBackground(MaterialColors.getColor(binding.getRoot(), com.google.android.material.R.attr.colorPrimary));

                            update.setVisibility(View.VISIBLE);

                            update.setOnClickListener(view -> {
                                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(MainActivity.this);
                                builder.setTitle(App.getInstance().getString(R.string.module_update_avaliable));
                                builder.setMessage(App.getInstance().getString(R.string.module_version) + " " + version + "\n\n" + changelog);
                                builder.setNegativeButton(android.R.string.cancel, (dialogInterface, i) -> {
                                    dialogInterface.dismiss();
                                });
                                builder.setPositiveButton(R.string.module_update, (dialogInterface, i) -> {
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setData(android.net.Uri.parse(link));
                                    startActivity(intent);
                                });
                                builder.create().show();
                            });
                        });
                    }
                } else {
                    Log.e("WaRevamp", "Error: " + responseCode);
                }

            } catch (Exception e) {
                Log.e("WaRevamp", "Error: " + e.getMessage());
            }
        }).start();
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupInfoCard();
    }
}