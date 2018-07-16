package com.linka.lockapp.aos.module.pages.help;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceImpl;
import com.linka.lockapp.aos.module.core.CoreFragment;
import com.linka.lockapp.aos.module.helpers.Helpers;
import com.linka.lockapp.aos.module.i18n._;
import com.linka.lockapp.aos.module.pages.others.WebPageFragment;
import com.zopim.android.sdk.api.ZopimChat;
import com.zopim.android.sdk.model.VisitorInfo;
import com.zopim.android.sdk.prechat.ZopimChatActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class HelpFragment extends CoreFragment {
    private Unbinder unbinder;

    public static HelpFragment newInstance() {

        Bundle args = new Bundle();
        HelpFragment fragment = new HelpFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_help, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);
        getAppMainActivity().setTitle("");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.tutorials)
    void onTutorialsClicked() {
        getAppMainActivity().pushFragment(TutorialsListFragment.newInstance());
    }

    @OnClick(R.id.faqs)
    void onFaqsClicked() {
        getAppMainActivity().pushFragmentWithoutAnimation(WebPageFragment.newInstance("", _.i(R.string.faq_url)));
    }

    @OnClick(R.id.email_us)
    void onEmailUsClicked() {
        Helpers.sendEmail(getActivity(), _.i(R.string.report_a_bug_url), _.i(R.string.report_a_bug));
    }

    @OnClick(R.id.live_chat)
    void onLiveChatClicked() {
        String chatEmail = LinkaAPIServiceImpl.getUserEmail();
        String[] names = chatEmail.split("@");
        String name = names[0];

        ZopimChat.init("4owkb9rQ1cOU3hAcZImdl3vKrPsCAjye");

        VisitorInfo visitorInfo = new VisitorInfo.Builder()
                .name(name)
                .email(chatEmail)
                .build();
        // set visitor info
        ZopimChat.setVisitorInfo(visitorInfo);

        startActivity(new Intent(getActivity().getApplicationContext(), ZopimChatActivity.class));
    }
}
