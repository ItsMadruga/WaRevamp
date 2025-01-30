package its.madruga.warevamp.app.ui.activitys;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.color.MaterialColors;

import its.madruga.warevamp.App;
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
        SpannableString authorName = new SpannableString(App.getInstance().getString(R.string.module_author));
        authorName.setSpan(new StyleSpan(Typeface.BOLD), 0, 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        authorName.setSpan(new StyleSpan(Typeface.ITALIC), 7 , authorName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        authorName.setSpan(new ForegroundColorSpan(MaterialColors.getColor(binding.getRoot(), com.google.android.material.R.attr.colorPrimary)), 8, authorName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        binding.infos.setVersion(authorName);

        binding.infos.getVersionView().setOnClickListener(v -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ItsMadruga")));
        });

        SpannableString telegramChannel = new SpannableString(App.getInstance().getString(R.string.module_telegram));
        telegramChannel.setSpan(new StyleSpan(Typeface.BOLD), 0, 18, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        telegramChannel.setSpan(new StyleSpan(Typeface.ITALIC), 18, telegramChannel.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        telegramChannel.setSpan(new ForegroundColorSpan(MaterialColors.getColor(binding.getRoot(), com.google.android.material.R.attr.colorPrimary)), 18, telegramChannel.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        binding.infos.setWaVersion(telegramChannel);

        binding.infos.getWaVersionView().setOnClickListener(v -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/warevampmodule")));
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return super.onSupportNavigateUp();
    }
}