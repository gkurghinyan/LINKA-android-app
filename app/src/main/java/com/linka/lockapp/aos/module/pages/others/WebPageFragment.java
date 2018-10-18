package com.linka.lockapp.aos.module.pages.others;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.core.CoreFragment;
import com.linka.lockapp.aos.module.widget.ThreeDotsView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Vanson on 20/4/2016.
 */
public class WebPageFragment extends CoreFragment {

    private final int WEB_VIEW_INFLATE_DELAY = 50;

    @BindView(R.id.webview_stub)
    ViewStub webViewStub;

    @BindView(R.id.root)
    FrameLayout root;

    @BindView(R.id.three_dots)
    ThreeDotsView threeDotsView;

    Unbinder unbinder;
    WebView webView;

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
        getAppMainActivity().setBackIconVisible(true);
        unbinder = ButterKnife.bind(this, rootView);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (webViewStub != null) {
                    webView = (WebView) webViewStub.inflate();
                    init();
                }
            }
        }, WEB_VIEW_INFLATE_DELAY);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            Bundle bundle = getArguments();
            title = bundle.getString("title");
            url = bundle.getString("url");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }


    @SuppressLint("SetJavaScriptEnabled")
    void init() {

        getAppMainActivity().setTitle(title);

        if (url != null) {
            webView.setWebViewClient(new WebViewClient(){
                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                    if(threeDotsView != null) {
                        threeDotsView.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    if(threeDotsView != null) {
                        threeDotsView.setVisibility(View.GONE);
                    }
                }


            });
            webView.getSettings().setJavaScriptEnabled(true);
            webView.loadUrl(url);
        }

    }


}