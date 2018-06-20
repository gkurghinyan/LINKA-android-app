package com.linka.lockapp.aos.module.pages.others;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.core.CoreFragment;
import com.linka.lockapp.aos.module.other.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Vanson on 20/4/2016.
 */
public class WebPageFragment extends CoreFragment {

    @BindView(R.id.webView)
    WebView webView;
    @BindView(R.id.root)
    LinearLayout root;

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
        unbinder = ButterKnife.bind(this, rootView);

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
            webView.setWebViewClient(new WebViewClient(){
                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                    Utils.showLoading(WebPageFragment.this.getContext(),webView);
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    Utils.cancelLoading();
                }


            });
            webView.loadUrl(url);
        }

    }


}