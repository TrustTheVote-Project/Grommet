package com.rockthevote.grommet.ui;


import android.os.Bundle;
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

public class HelpActivity extends BaseActivity {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.text_content) TextView content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View v = getLayoutInflater().inflate(R.layout.info_activities, getContentView());
        ButterKnife.bind(this, v);
        setSupportActionBar(toolbar);

        Spanned result;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(getString(R.string.help_activity_text),
                    Html.FROM_HTML_MODE_LEGACY, null, new ListTagHandler());
        } else {
            result = Html.fromHtml(getString(R.string.help_activity_text),
                    null, new ListTagHandler());
        }

        content.setText(result);
        content.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public Toolbar getToolbar() {
        return toolbar;
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return R.id.nav_help;
    }
}
