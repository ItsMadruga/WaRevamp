package its.madruga.warevamp.ui.activitys;

import static its.madruga.warevamp.core.Receivers.sendCheckWpp;
import static its.madruga.warevamp.core.Receivers.sendRebootWpp;
import static its.madruga.warevamp.core.Utils.WHATSAPP_PACKAGE;
import static its.madruga.warevamp.core.Utils.isInstalled;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.color.MaterialColors;

import its.madruga.warevamp.App;
import its.madruga.warevamp.BuildConfig;
import its.madruga.warevamp.R;
import its.madruga.warevamp.core.XposedChecker;
import its.madruga.warevamp.databinding.ActivityMainBinding;
import its.madruga.warevamp.ui.views.InfoCard;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupInfoCard();

        setSupportActionBar(binding.toolbar);
        binding.toolbar.setTitle(R.string.app_name);
    }

    public void setupInfoCard() {
        InfoCard infoCard = binding.infos;
        InfoCard wppReboot = binding.wppReboot;

        infoCard.setTitle(App.getInstance().getString(R.string.app_name));
        infoCard.setSubTitle(App.getInstance().getString(R.string.app_desc));
        SpannableString versionString = new SpannableString(App.getInstance().getString(R.string.module_version) + " " + BuildConfig.VERSION_NAME);
        versionString.setSpan(new StyleSpan(Typeface.BOLD), 0, 16, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        versionString.setSpan(new ForegroundColorSpan(MaterialColors.getColor(binding.getRoot(), com.google.android.material.R.attr.colorPrimary)), 15, versionString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        infoCard.setVersion(versionString);

        if (!isInstalled(WHATSAPP_PACKAGE) || !XposedChecker.isActive()){
            setDisableWpp();
        }

        sendCheckWpp();

        BroadcastReceiver wppReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SpannableString versionString = new SpannableString(App.getInstance().getString(R.string.whatsapp_version) + " " + intent.getStringExtra("wa_version"));
                versionString.setSpan(new StyleSpan(Typeface.BOLD), 0, 18, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                versionString.setSpan(new ForegroundColorSpan(MaterialColors.getColor(binding.getRoot(), com.google.android.material.R.attr.colorPrimary)), 18, versionString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                infoCard.setWaVersion(versionString);

                SpannableString rebootString = new SpannableString(App.getInstance().getString(R.string.reboot_wpp));
                rebootString.setSpan(new StyleSpan(Typeface.BOLD), 0, rebootString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                rebootString.setSpan(new ForegroundColorSpan(MaterialColors.getColor(binding.getRoot(), com.google.android.material.R.attr.colorPrimary)), 0, rebootString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                wppReboot.setSubTitle(rebootString);
                wppReboot.setEnabled(true);
            }
        };

        ContextCompat.registerReceiver(App.getInstance(), wppReceiver, new IntentFilter(BuildConfig.APPLICATION_ID + ".WaRevampEnable"), ContextCompat.RECEIVER_EXPORTED);

        wppReboot.setOnClickListener(v -> {
            sendRebootWpp();
        });
    }

    public void setDisableWpp() {
        SpannableString versionString = new SpannableString(App.getInstance().getString(R.string.module_version) + " " + App.getInstance().getString(R.string.module_disable));
        versionString.setSpan(new StyleSpan(Typeface.BOLD), 0, 19, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        versionString.setSpan(new ForegroundColorSpan(Color.RED), 16, versionString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.infos.setWaVersion(versionString);

        SpannableString rebootString = new SpannableString(App.getInstance().getString(R.string.reboot_wpp));
        rebootString.setSpan(new ForegroundColorSpan(Color.RED), 0, rebootString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.wppReboot.setSubTitle(rebootString);
        binding.wppReboot.setEnabled(false);
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return super.onSupportNavigateUp();
    }
}