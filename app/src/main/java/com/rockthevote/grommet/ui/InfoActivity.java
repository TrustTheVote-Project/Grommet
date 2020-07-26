package com.rockthevote.grommet.ui;


import android.os.Bundle;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.rockthevote.grommet.R;
import com.rockthevote.grommet.util.ListTagHandler;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InfoActivity extends BaseActivity {

    public static final String STRING_RES_PARAM = "string_res_param";
    @StringRes int stringRes;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.text_content) TextView content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View v = getLayoutInflater().inflate(R.layout.info_activities, getContentView());
        stringRes = getIntent().getIntExtra(STRING_RES_PARAM, R.string.about_activity_text);

        ButterKnife.bind(this, v);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(getActivityTitle());

        Spanned result;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(getString(stringRes),
                    Html.FROM_HTML_MODE_LEGACY, null, new ListTagHandler());
        } else {
            result = Html.fromHtml(getString(stringRes),
                    null, new ListTagHandler());
        }

        content.setText(result);
        content.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private @StringRes int getActivityTitle() {
        switch (stringRes) {
            case R.string.about_activity_text:
                return R.string.nav_title_about;
            case R.string.data_usage_activity_text:
                return R.string.nav_title_data_usage;
            case R.string.help_activity_text:
                return R.string.nav_title_help;
            case R.string.privacy_activity_text:
                return R.string.nav_title_privacy;
            default:
                return R.string.nav_title_help;
        }
    }

    @Override
    public Toolbar getToolbar() {
        return toolbar;
    }

    @Override
    protected int getSelfNavDrawerItem() {
        switch (stringRes) {
            case R.string.about_activity_text:
                return R.id.nav_about;
            case R.string.data_usage_activity_text:
                return R.id.nav_data_usage;
            case R.string.help_activity_text:
                return R.id.nav_help;
            case R.string.privacy_activity_text:
                return R.id.nav_privacy;
            default:
                return R.id.nav_help;
        }
    }

}
