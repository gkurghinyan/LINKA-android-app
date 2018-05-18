package com.linka.lockapp.aos.module.gcm;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceImpl;
import com.linka.lockapp.aos.module.helpers.LogHelper;

/**
 * Created by kyle on 4/29/18.
 */

public class MyFirebaseInstanceIdService extends com.google.firebase.iid.FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        LogHelper.e("Firebase", "Refreshed token: " + refreshedToken);

        getFcmToken();
    }


    public static String getFcmToken(){
        String token = FirebaseInstanceId.getInstance().getToken();

        LogHelper.e("Firebase", "Got token: " + token);

        //If the tokens are different, then we need to send it to the server
        String oldToken = LinkaAPIServiceImpl.getFcmToken();

        //Send the token to the server
        if(oldToken == null || !oldToken.equals(token)){
            LinkaAPIServiceImpl.update_push_token(token);
        }

        return token;
    }
}
