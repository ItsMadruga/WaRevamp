package its.madruga.warevamp.app.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.SpannableString;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.android.material.card.MaterialCardView;

import its.madruga.warevamp.R;

public class InfoCard extends MaterialCardView {

    public InfoCard(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public InfoCard(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        inflate(context, R.layout.card_info, (ViewGroup) getRootView());

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.InfoCard,
                0, 0
        );

        try {
            var title = a.getText(R.styleable.InfoCard_android_title);
            var subtitle = a.getText(R.styleable.InfoCard_android_subtitle);
            var waVersion = a.getText(R.styleable.InfoCard_waversion);
            var version = a.getText(R.styleable.InfoCard_version);

            setTitle((String) title);
            if (subtitle != null) setSubTitle((String) subtitle);
            if (waVersion != null) setWaVersion((String) waVersion);
            if (version != null) setVersion((String) version);
        } finally {
            a.recycle();
        }
    }

    public void setVersion(String version) {
        if(version == null) return;
        TextView cardVersion = findViewById(R.id.card_info_version);
        cardVersion.setText(version);
        cardVersion.setVisibility(VISIBLE);
    }

    public void setVersion(SpannableString version) {
        if(version == null) return;
        TextView cardVersion = findViewById(R.id.card_info_version);
        cardVersion.setText(version);
        cardVersion.setVisibility(VISIBLE);
    }

    public void setWaVersion(String version) {
        if(version == null) return;
        TextView cardWaVersion = findViewById(R.id.card_info_waversion);
        cardWaVersion.setText(version);
        cardWaVersion.setVisibility(VISIBLE);
    }

    public void setWaVersion(SpannableString version) {
        if(version == null) return;
        TextView cardWaVersion = findViewById(R.id.card_info_waversion);
        cardWaVersion.setText(version);
        cardWaVersion.setVisibility(VISIBLE);
    }

    public void setTitle(String title) {
        if(title == null) return;
        TextView cardTitle = findViewById(R.id.card_info_title);
        cardTitle.setText(title);
        cardTitle.setVisibility(VISIBLE);
    }

    public void setTitle(SpannableString title) {
        if(title == null) return;
        TextView cardTitle = findViewById(R.id.card_info_title);
        cardTitle.setText(title);
        cardTitle.setVisibility(VISIBLE);
    }

    public void setSubTitle(String subTitle) {
        if(subTitle == null) return;
        TextView cardSubTitle = findViewById(R.id.card_info_subtitle);
        cardSubTitle.setText(subTitle);
        cardSubTitle.setVisibility(VISIBLE);
    }

    public void setSubTitle(SpannableString subTitle) {
        if(subTitle == null) return;
        TextView cardSubTitle = findViewById(R.id.card_info_subtitle);
        cardSubTitle.setText(subTitle);
        cardSubTitle.setVisibility(VISIBLE);
    }

    public void setCardBackground(int color) {
        MaterialCardView card = findViewById(R.id.card_info_container);
        card.setCardBackgroundColor(color);
    }

    public TextView getTitleView() {
        return findViewById(R.id.card_info_title);
    }

    public TextView getSubTitleView() {
        return findViewById(R.id.card_info_subtitle);
    }

    public TextView getVersionView() {
        return findViewById(R.id.card_info_version);
    }

    public TextView getWaVersionView() {
        return findViewById(R.id.card_info_waversion);
    }

}
