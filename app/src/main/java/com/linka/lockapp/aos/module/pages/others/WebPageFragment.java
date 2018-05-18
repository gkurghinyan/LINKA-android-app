package com.linka.lockapp.aos.module.pages.others;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.core.CoreFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Vanson on 20/4/2016.
 */
public class WebPageFragment extends CoreFragment {

    @BindView(R.id.webView)
    WebView webView;

    Unbinder unbinder;

    public static WebPageFragment newInstance(String title, String url) {
        Bundle bundle = new Bundle();
        WebPageFragment fragment = new WebPageFragment();
        bundle.putString("title", title);
        bundle.putString("url", url);
        fragment.setArguments(bundle);
        return fragment;
    }


    public WebPageFragment() {
    }

    String title;
    String url;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_web_page, container, false);
        ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            Bundle bundle = getArguments();
            title = bundle.getString("title");
            url = bundle.getString("url");
            init(savedInstanceState);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }


    void init(Bundle savedInstanceState) {

        getAppMainActivity().setTitle(title);

        if (url != null) {
            webView.setWebViewClient(new WebViewClient());
            webView.loadUrl(url);
        }

    }


}