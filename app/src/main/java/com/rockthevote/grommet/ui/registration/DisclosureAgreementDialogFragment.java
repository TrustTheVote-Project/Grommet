package com.rockthevote.grommet.ui.registration;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rockthevote.grommet.R;
import com.rockthevote.grommet.util.ListTagHandler;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DisclosureAgreementDialogFragment extends DialogFragment {

    @BindView(R.id.text_content) TextView content;

    private DisclosureListener listener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setCancelable(false);
        View v = inflater.inflate(R.layout.dialog_disclosure_agreement, container);
        ButterKnife.bind(this, v);

        Spanned result;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(getString(R.string.disclosure_agreement),
                    Html.FROM_HTML_MODE_LEGACY, null, new ListTagHandler());
        } else {
            result = Html.fromHtml(getString(R.string.disclosure_agreement),
                    null, new ListTagHandler());
        }

        content.setText(result);
        content.setMovementMethod(LinkMovementMethod.getInstance());
        getDialog().setTitle(getString(R.string.disclosure_agreement_title));
        getDialog().setCancelable(false);

        return v;
    }

    public void setListener(DisclosureListener listener) {
        this.listener = listener;
    }

    @OnClick(R.id.disclosure_decline_button)
    public void onDeclineClick(View v) {
        if (null != listener) {
            listener.onDeclineClick();
        }
    }

    @OnClick(R.id.disclosure_accept_button)
    public void onAcceptClick(View v) {
        if (null != listener) {
            listener.onAcceptClick();
        }
    }

    public interface DisclosureListener {

        void onDeclineClick();

        void onAcceptClick();
    }
}
