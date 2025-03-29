package its.madruga.warevamp.app.ui.activitys;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.color.MaterialColors;

import its.madruga.warevamp.App;
import its.madruga.warevamp.BuildConfig;
import its.madruga.warevamp.R;
import its.madruga.warevamp.databinding.ActivityAboutBinding;

public class AboutActivity extends AppCompatActivity {
    ActivityAboutBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupInfoCard();

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    public void setupInfoCard() {
        String module_author = App.getInstance().getString(R.string.module_author);
        SpannableString authorName = new SpannableString(module_author + " I'ts Madruga");

        authorName.setSpan(new StyleSpan(Typeface.BOLD), 0, module_author.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        authorName.setSpan(new StyleSpan(Typeface.ITALIC), module_author.length() , authorName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        authorName.setSpan(new ForegroundColorSpan(MaterialColors.getColor(binding.getRoot(), com.google.android.material.R.attr.colorPrimary)), module_author.length(), authorName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        binding.infos.setVersion(authorName);

        binding.infos.getVersionView().setOnClickListener(v -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ItsMadruga")));
        });

        String module_telegram = App.getInstance().getString(R.string.module_telegram);

        SpannableString telegramChannel = new SpannableString(module_telegram + " @warevampmodule");

        telegramChannel.setSpan(new StyleSpan(Typeface.BOLD), 0, module_telegram.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        telegramChannel.setSpan(new StyleSpan(Typeface.ITALIC), module_telegram.length(), telegramChannel.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        telegramChannel.setSpan(new ForegroundColorSpan(MaterialColors.getColor(binding.getRoot(), com.google.android.material.R.attr.colorPrimary)), module_telegram.length(), telegramChannel.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        binding.infos.setWaVersion(telegramChannel);

        binding.infos.getWaVersionView().setOnClickListener(v -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/warevampmodule")));
        });

        String app_desc = App.getInstance().getString(R.string.app_desc);
        String module_version = App.getInstance().getString(R.string.module_version);

        String module_desc = app_desc + "\n" + module_version + " ";

        SpannableString moduleDesc = new SpannableString(module_desc + BuildConfig.VERSION_NAME);

        moduleDesc.setSpan(new StyleSpan(Typeface.BOLD), app_desc.length(), module_desc.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        moduleDesc.setSpan(new ForegroundColorSpan(MaterialColors.getColor(binding.getRoot(), com.google.android.material.R.attr.colorPrimary)), module_desc.length(), moduleDesc.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        binding.infos.setSubTitle(moduleDesc);
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return super.onSupportNavigateUp();
    }
}