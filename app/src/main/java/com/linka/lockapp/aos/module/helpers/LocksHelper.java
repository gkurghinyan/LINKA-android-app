package com.linka.lockapp.aos.module.helpers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.linka.lockapp.aos.AppSplashActivity;
import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceImpl;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceManager;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceResponse;
import com.linka.lockapp.aos.module.model.Linka;
import com.linka.lockapp.aos.module.model.LinkaAccessKey;
import com.linka.lockapp.aos.module.widget.LocksController;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by kyle on 5/10/18.
 */

public class LocksHelper {

    static List<String> new_Locks = new ArrayList<>();

    static Context context;
    static LocksCallback callback;

    public interface LocksCallback {
        void onNext();
    }

    //Calls the api to get the list of associated locks
    public static void get_locks(final Context m_context, final LocksCallback m_callback){
        context = m_context;
        callback = m_callback;

        LogHelper.e("Associated Locks","Getting associated locks with device token " + Helpers.device_token);

        LinkaAPIServiceImpl.associated_locks(context, new Callback<LinkaAPIServiceResponse.AssociatedLocksResponse>() {
            @Override
            public void onResponse(Call<LinkaAPIServiceResponse.AssociatedLocksResponse> call, Response<LinkaAPIServiceResponse.AssociatedLocksResponse> response) {
                LinkaAPIServiceResponse errorData = LinkaAPIServiceManager.extractErrorFromResponse(response);
                if (LinkaAPIServiceImpl.check(response, false, null)) {


                    //Check that we have all of the LINKA access key objects
                    List<LinkaAccessKey> accessKeyList = LinkaAccessKey.getKeys();
                    for(LinkaAccessKey accessKey : accessKeyList){
                        LogHelper.e("Access Key", "Got Access Key : " + accessKey.linka_lock_address);
                    }

                    List<Linka> linkaList = Linka.getLinkas();
                    for(Linka linka : linkaList){
                        LogHelper.e("LINKA", "Got LINKA " + linka.lock_mac_address);

                        boolean exists = false;
                        //Check if this exists, and if not, then delete it from the list
                        for(LinkaAPIServiceResponse.AssociatedLocksResponse.Data lock_data : response.body().data) {
                            if(lock_data.lock_serial_no.equals(linka.lock_mac_address)){
                                exists = true;
                            }
                        }

                        if(!exists){
                            LinkaAccessKey.deleteAllKeysFromLinka(linka);
                            linka.delete();
                        }

                    }

                    for(LinkaAPIServiceResponse.AssociatedLocksResponse.Data lock_data : response.body().data){

                        LinkaAccessKey accessKey = LinkaAccessKey.getKeyFromKeyId(lock_data._id);
                        if(accessKey == null){
                            LogHelper.e("Linka Access key " , "Changed " + lock_data.lock_serial_no);
                            get_keys(lock_data.lock_serial_no, lock_data.name);

                            //Save off all the LINKA's that we are adding, so we know when all the api's have returned
                            new_Locks.add(lock_data.lock_serial_no);
                        }

                    }

                    callback.onNext();
                }else{
                    if(errorData != null && errorData.message != null){

                        //If everything is setup properly, shouldn't return device not verified.
                        // But if it does, then we need to ask the user to go to their email to verify
                        if(errorData.message.equals("Device not verified")){

                            if(context == null){return;}

                            //Tell user to check their email
                            new AlertDialog.Builder(context)
                                    .setTitle("Verify your email")
                                    .setMessage("Please check your email to verify this device")
                                    .setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            callback.onNext();
                                        }
                                    })
                                    .show();

                            //Send email to verify device
                            LinkaAPIServiceImpl.verify_device();
                            return;
                        }
                    }

                    callback.onNext();
                }
            }

            @Override
            public void onFailure(Call<LinkaAPIServiceResponse.AssociatedLocksResponse> call, Throwable t) {
                callback.onNext();
            }
        });
    }


    static void get_keys(final String lock_mac_address, final String lock_name){
        LogHelper.e("Keys","Getting Keys for lock " + lock_mac_address);

        LinkaAPIServiceImpl.check_key_status_for_user(context, lock_mac_address, new Callback<LinkaAPIServiceResponse.CheckKeyStatusForUserResponse>() {
            @Override
            public void onResponse(Call<LinkaAPIServiceResponse.CheckKeyStatusForUserResponse> call, Response<LinkaAPIServiceResponse.CheckKeyStatusForUserResponse> response) {

                if(LinkaAPIServiceImpl.check(response, false, null)) {

                    //Check if the LINKA already exists, and if so, we modify the object instead of replacing it

                    Linka existingLinka = Linka.getLinkaByAddress(lock_mac_address);
                    if(existingLinka == null) {
                        Linka linka = Linka.makeLinka(lock_mac_address, lock_mac_address, lock_mac_address);
                        LinkaAccessKey accessKey = LinkaAccessKey.createNewOrReplaceKey(linka, response.body().data.key, linka.lock_mac_address);
                        linka.saveSettings();
                    }else{
                        LinkaAccessKey.createNewOrReplaceKey(existingLinka, response.body().data.key, existingLinka.lock_mac_address);
                    }
                }

                //Lets move on to the next activity only once we have added all the LINKA's
                new_Locks.remove(lock_mac_address);
                if (new_Locks.size() == 0) {
                    callback.onNext();
                }

            }

            @Override
            public void onFailure(Call<LinkaAPIServiceResponse.CheckKeyStatusForUserResponse> call, Throwable t) {

                new_Locks.remove(lock_mac_address);
                if(new_Locks.size() == 0){
                    callback.onNext();
                }
            }
        });
    }

}
