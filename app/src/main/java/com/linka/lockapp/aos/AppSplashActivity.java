package com.linka.lockapp.aos;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.linka.lockapp.aos.module.api.LinkaAPIServiceImpl;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceResponse;
import com.linka.lockapp.aos.module.helpers.Helpers;
import com.linka.lockapp.aos.module.helpers.LocksHelper;
import com.linka.lockapp.aos.module.helpers.LogHelper;
import com.linka.lockapp.aos.module.i18n._;
import com.pixplicity.easyprefs.library.Prefs;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Vanson on 17/
 * /16.
 */
public class AppSplashActivity extends Activity {

    @BindView(R.id.title_image)
    ImageView image;
    @BindView(R.id.title_text)
    TextView title;

    private Unbinder unbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        unbinder = ButterKnife.bind(this);
    }


    static boolean isAdded;

    @Override
    protected void onStop() {
        super.onStop();
        isAdded = false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        isAdded = true;
    }

    @Override
    protected void onPause() {
        super.onPause();


        if (handler != null) {
            handler.removeCallbacks(runnable);
            handler = null;
        }
        if (secondSplash != null){
            handler.removeCallbacks(startRunnable);
            secondSplash = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (handler != null) {
            handler.removeCallbacks(runnable);
            handler = null;
        }

        handler = new Handler();
        handler.post(runnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    Handler handler = null;
    Handler secondSplash = null;
    Runnable runnable = new Runnable() {
        @Override
        public void run() {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    askForPermission();
                }
            });

            handler = null;
        }
    };

    Runnable startRunnable = new Runnable() {
        @Override
        public void run() {
            startApp();
            secondSplash = null;
        }
    };


    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static String android_id = "";

    void askForPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check 
            if (this.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(_.i(R.string.location_access_required));
                builder.setMessage(_.i(R.string.location_access_required_desc));
                builder.setPositiveButton(_.i(R.string.ok), null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @TargetApi(Build.VERSION_CODES.M)
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(
                                new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                                PERMISSION_REQUEST_COARSE_LOCATION
                        );
                    }
                });
                builder.show();
            } else {
                setupApp();
            }
        } else {
            setupApp();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("AppSplashActivity", "coarse location permission granted");
                    setupApp();
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(_.i(R.string.functionality_limited));
                    builder.setMessage(_.i(R.string.not_able_to_discover_lock));
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            setupApp();
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }


    void startApp() {
        Intent intent = new Intent(AppSplashActivity.this, AppMainActivity.class);
        startActivity(intent);
        finish();
    }


    void setupApp() {

        //Test TEST TEST
        android_id = Settings.Secure.getString(AppDelegate.getInstance().getContentResolver(), Settings.Secure.ANDROID_ID);
        LogHelper.e("ANDROID ID", android_id);

        boolean gettingTokens = false;

        //For transitioning from pre-elise to elise
        //We need to call this api the first time we open the app after elise is downloaded
        if (LinkaAPIServiceImpl.isLoggedIn()) {
            if (!Prefs.getBoolean("tokens-transferred", false)) {
                transfer_tokens();
                gettingTokens = true;
            }
        } else {
            Prefs.putBoolean("tokens-transferred", true);
        }

        if (!gettingTokens) {
            getLocks();
        }
    }


    //Calls the api to get the list of associated locks
    void transfer_tokens() {

        LogHelper.e("Transfer tokens", "Transferring device token " + Helpers.device_token + " to new token " + android_id);

        LinkaAPIServiceImpl.transfer_device_tokens(AppSplashActivity.this, Helpers.device_token, android_id, new Callback<LinkaAPIServiceResponse>() {
            @Override
            public void onResponse(Call<LinkaAPIServiceResponse> call, Response<LinkaAPIServiceResponse> response) {

                if (LinkaAPIServiceImpl.check(response, false, null)) {
                    Prefs.putString("device_token", android_id);
                    Prefs.putBoolean("tokens-transferred", true);
                    Helpers.load_device_token();
                }
                getLocks();
            }

            @Override
            public void onFailure(Call<LinkaAPIServiceResponse> call, Throwable t) {
                getLocks();
            }
        });
    }


    void getLocks() {
        LocksHelper.get_locks(AppSplashActivity.this, new LocksHelper.LocksCallback() {
            @Override
            public void onNext() {
                image.setVisibility(View.INVISIBLE);
                title.setVisibility(View.VISIBLE);
                if(secondSplash != null){
                    secondSplash.removeCallbacks(startRunnable);
                    secondSplash = null;
                }
                secondSplash = new Handler();
                secondSplash.postDelayed(startRunnable,2000);
            }
        });

    }
}
