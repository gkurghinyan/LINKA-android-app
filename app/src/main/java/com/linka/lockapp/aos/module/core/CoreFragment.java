package com.linka.lockapp.aos.module.core;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.linka.lockapp.aos.AppMainActivity;
import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.other.Utils;
import com.linka.lockapp.aos.module.pages.walkthrough.WalkthroughActivity;


/**
 * Created by van on 13/7/15.
 */
public class CoreFragment extends Fragment {
    private boolean isFragmentStop = false;

    protected CoreActivity getCoreActivity()
    {
        return (CoreActivity) getActivity();
    }

    protected AppMainActivity getAppMainActivity()
    {
        return (AppMainActivity) getActivity();
    }

    protected WalkthroughActivity getWalkthroughActivity(){
        return (WalkthroughActivity) getActivity();
    }

    protected void toastMake(String... message)
    {
        String msg = "";
        for (int i = 0; i < message.length; i++)
        {
            msg += message[i] + " ";
        }
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }



    /* ON CHANGE FRAGMENT CALLBACK */

    public void onChangeFragment()
    {

    }

    @Override
    public void onResume() {
        super.onResume();
        isFragmentStop = false;
    }

    @Override
    public void onStop() {
        super.onStop();
        isFragmentStop = true;
    }

    /* ASYNC ON START BOOTSTRAP - HANDLER + RUNNABLE */

    public void onStartAsync() {}
    public void onViewCreatedAsync() {}

    Runnable asyncRunnableOnStart = new Runnable() {
        @Override
        public void run() {
            if (!isAdded()) return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onStartAsync();
                }
            });
        }
    };
    Runnable asyncRunnableOnViewCreated = new Runnable() {
        @Override
        public void run() {
            if (!isAdded()) return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onViewCreatedAsync();
                }
            });
        }
    };

    Handler asyncHandlerOnStart = new Handler();
    Handler asyncHandlerOnViewCreated = new Handler();

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        asyncHandlerOnViewCreated.postDelayed(asyncRunnableOnViewCreated, 10);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        asyncHandlerOnViewCreated.removeCallbacks(asyncRunnableOnViewCreated);
//        cancelLoading();
    }




    public void showLoading(ViewGroup viewGroup) {
        Utils.showLoading(getContext(),viewGroup);
    }

    public void cancelLoading() {
        Utils.cancelLoading();
    }



    public void showAlert(String title, String message) {
        if(!isFragmentStop) {
            new AlertDialog.Builder(getActivity())
                    .setTitle(title)
                    .setMessage(message)
                    .setNegativeButton(R.string.ok, null)
                    .show();
        }
    }


}
