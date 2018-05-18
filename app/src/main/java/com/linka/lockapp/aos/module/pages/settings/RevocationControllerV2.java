package com.linka.lockapp.aos.module.pages.settings;

import android.content.Context;
import android.os.Handler;

import com.linka.lockapp.aos.AppMainActivity;
import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceImpl;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceResponse;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceResponse.CheckKeyStatusForUserResponse;
import com.linka.lockapp.aos.module.helpers.LogHelper;
import com.linka.lockapp.aos.module.i18n._;
import com.linka.lockapp.aos.module.model.Linka;
import com.linka.lockapp.aos.module.model.LinkaAccessKey;
import com.linka.lockapp.aos.module.widget.LockController;

import org.greenrobot.eventbus.Subscribe;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.linka.lockapp.aos.module.widget.LocksController.LOCKSCONTROLLER_NOTIFY_REFRESHED_SETTINGS;

/**
 * Created by Vanson on 6/8/2016.
 */
public class RevocationControllerV2 extends RevocationController {

    @Override
    public void implement(Context context, Linka linka, LockController lockController) {
        super.implement(context, linka, lockController);
    }

    public interface GenericCheckKeyStatusForUserCallback {
        void onCallback(LinkaAccessKey key, boolean ownsMasterKey);
    }


    boolean canStartFactoryReset = false;
    void startReadSettingsTimeout(){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                canStartFactoryReset = false;
            }
        }, 3000);
    }

    // generic check key function
    public void genericCheckKeyStatusForUser(final GenericCheckKeyStatusForUserCallback callback)
    {
        showLoading("", _.i(R.string.verifying_access));
        LinkaAPIServiceImpl.check_key_status_for_user(
                context,
                linka.lock_mac_address,
                new Callback<CheckKeyStatusForUserResponse>() {
                    @Override
                    public void onResponse(Call<CheckKeyStatusForUserResponse> call, Response<CheckKeyStatusForUserResponse> response) {

                        hideLoading();

                        if (LinkaAPIServiceImpl.check(response, false, context)) {
                            CheckKeyStatusForUserResponse body = response.body();
                            if (body.data.key != null && body.data.isOwner) {
                                LinkaAccessKey key = body.data.key.makeLinkaAccessKey(linka);
                                callback.onCallback(key, true);
                                return;
                            } else {
                                showAlert(
                                        _.i(R.string.access_denied),
                                        _.i(R.string.account_no_valid_admin_access),
                                        _.i(R.string.ok),
                                        null);
                                callback.onCallback(null, false);
                                return;
                            }
                        } else {
                            callback.onCallback(null, false);
                        }
                    }

                    @Override
                    public void onFailure(Call<CheckKeyStatusForUserResponse> call, Throwable t) {

                        hideLoading();
                        callback.onCallback(null, false);

                    }
                }
        );
    }


    boolean shouldReceiveRevokeAccessKeyNotification = false;
    boolean shouldReceiveResetFactorySettingsNotification = false;


    public void startResetMaster()
    {
        startResetMasterCallback();

        lockController.doDefaultSettings();
        showLoading("", _.i(R.string.resetting_factory));

    }

    //For the factory reset, we want to confirm that 1) The phone is connected to the internet, and 2) The phone is connected to the lock
    //So, we need to first call an API call to make sure that we are connected. The api call doubly serves the purpose to make sure the user has proper device permissions
    //Then, we read a setting to confirm that we are connected to the lock
    //Then, we can proceed to do the factory reset
    public void confirmConnected(){
        genericCheckKeyStatusForUser(new GenericCheckKeyStatusForUserCallback() {
            @Override
            public void onCallback(LinkaAccessKey key, boolean ownsMasterKey) {
                if (key != null && ownsMasterKey) {
                    showLoading("", _.i(R.string.revoking));

                    //Read a setting
                    lockController.doReadActuations();

                    canStartFactoryReset = true;

                    //Start timer to cancel revocation after 3 seconds if setting not read
                    startReadSettingsTimeout();

                }
            }
        });

    }


    void startResetMasterCallback() {

        LinkaAPIServiceImpl.reset_all_lock_information_and_keys(
                context,
                linka,
                new Callback<LinkaAPIServiceResponse>() {
                    @Override
                    public void onResponse(Call<LinkaAPIServiceResponse> call, Response<LinkaAPIServiceResponse> response) {

                        if (LinkaAPIServiceImpl.check(response, false, context)) {

                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    lockController.lockControllerPacketCallback = null;
                                    lockController.doSleep();


                                    hideLoading();

                                    showAlert(
                                            _.i(R.string.success),
                                            _.i(R.string.reset_factory_done),
                                            _.i(R.string.close),
                                            null);

                                    AppMainActivity.getInstance().removeLinka(linka);

                                }
                            }, 1500);
                        }
                    }

                    @Override
                    public void onFailure(Call<LinkaAPIServiceResponse> call, Throwable t) {

                        hideLoading();

                    }
                }
        );

    }


    @Subscribe
    public void onEvent(Object object) {

        //If we've successfully read settings, then we'll get a notification here, and then we can proceed to do the factory reset
        if (object instanceof String && ((String) object).equals(LOCKSCONTROLLER_NOTIFY_REFRESHED_SETTINGS)) {
            LogHelper.e("REVOCATION", "Read Setting");
            if(canStartFactoryReset){
                LogHelper.e("REVOCATION", "STARTING RESET MASTER");
                canStartFactoryReset = false;
                startResetMaster();
            }
        }
    }

}