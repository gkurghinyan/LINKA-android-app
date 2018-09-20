package com.linka.lockapp.aos.module.pages.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

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

    public static String CAN_START_FACTORY_RESET = "can_start_factory_reset";

    @Override
    public void implement(Context context, Linka linka, LockController lockController) {
        super.implement(context, linka, lockController);
    }

    public interface GenericCheckKeyStatusForUserCallback {
        void onCallback(LinkaAccessKey key, boolean ownsMasterKey);
    }

    Handler revocationTimeoutHandler = new Handler();

    boolean canStartFactoryReset = false;
    boolean canClearKeysFromServer = false;
    void startReadSettingsTimeout(){
        revocationTimeoutHandler.postDelayed(resetTimeout, 3000);
    }

    Runnable resetTimeout = new Runnable() {
        @Override
        public void run() {

            LogHelper.e("REVOCATION", "Timeout !!");

            //If they have cleared keys from lock, but for some reason disconnected, then they are in a bad state
            //Need them to contact LINKA support to help clear keys from server manually
            if(canClearKeysFromServer){
                new AlertDialog.Builder(context)
                        .setTitle("Error")
                        .setMessage("Unable to connect. Please contact LINKA support. Error code 989")
                        .setCancelable(false)
                        .show();
            }else{
                Toast.makeText(context, "Unable to connect to device", Toast.LENGTH_LONG).show();
            }

            canStartFactoryReset = false;
            canClearKeysFromServer = false;
            lockController.clearSettingsQueue();
            hideLoading();

        }
    };

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

    public void startResetMaster()
    {
        lockController.doDefaultSettings();
        canClearKeysFromServer = true;

        //Read a setting
        lockController.doReadActuations();

        //Start timer to cancel revocation after 3 seconds if setting not read
        startReadSettingsTimeout();

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
                    showLoading("", "Confirming connection...");

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

                            lockController.doSleep();

                            hideLoading();

                            Toast.makeText(context, "Successfully Reset Device", Toast.LENGTH_LONG).show();

                            AppMainActivity.getInstance().removeLinka(linka);
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
        if (object instanceof String && ((String) object).equals(CAN_START_FACTORY_RESET)) {
            LogHelper.e("REVOCATION", "Read Setting");
            if(canStartFactoryReset){
                revocationTimeoutHandler.removeCallbacks(resetTimeout);

                LogHelper.e("REVOCATION", "STARTING RESET MASTER");
                canStartFactoryReset = false;
                startResetMaster();
            }else if(canClearKeysFromServer){

                revocationTimeoutHandler.removeCallbacks(resetTimeout);

                LogHelper.e("REVOCATION", "Clearing Keys from server");
                canClearKeysFromServer = false;
                startResetMasterCallback();
            }
        }
    }



    /* ==================================================================================================================================*/

    /*  Occasionally the factory reset will go wrong, and the lock will be factory reset, but the keys will still be on the server
        If this happens, we will show a popup and inform the user that they need to do a factory reset.
     */

    public void doForceFactoryResetServer(){
        showLoading("", _.i(R.string.resetting_factory));
        checkIfCRCAlreadyFactoryReset();
    }


    public interface CheckIfFactoryReset {
        void onResult(boolean isFactoryReset);
    }

    boolean shouldCheckCRCAlreadyFactoryReset = false;

    //Checks if the lock is already factory reset by looking at the CRC of the keys
    private void checkIfCRCAlreadyFactoryReset(){
        startCheckCRCTimeout();
        shouldCheckCRCAlreadyFactoryReset = true;
        lockController.checkIfFactoryReset(new CheckIfFactoryReset() {
            @Override
            public void onResult(boolean isFactoryReset) {

                revocationTimeoutHandler.removeCallbacks(checkCRCTimeout);

                if(isFactoryReset && shouldCheckCRCAlreadyFactoryReset){
                    shouldCheckCRCAlreadyFactoryReset = false;
                    startResetMasterCallback(); //Do a factory reset immediately!!
                }
            }
        });
    }

    void startCheckCRCTimeout(){
        revocationTimeoutHandler.postDelayed(checkCRCTimeout, 3000);
    }

    Runnable checkCRCTimeout = new Runnable() {
        @Override
        public void run() {

            hideLoading();

            showAlert(
                    _.i("Not Connected"),
                    _.i("Please connect to LINKA and Try Again"),
                    _.i(R.string.ok),
                    null);


            shouldCheckCRCAlreadyFactoryReset = false;
            canStartFactoryReset = false;
            canClearKeysFromServer = false;
            lockController.clearSettingsQueue();
        }
    };

}