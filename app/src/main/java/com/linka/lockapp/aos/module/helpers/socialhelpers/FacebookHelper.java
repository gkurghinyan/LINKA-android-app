package com.linka.lockapp.aos.module.helpers.socialhelpers;

import android.os.Bundle;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.linka.lockapp.aos.AppMainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Vanson on 17/8/15.
 */
public class FacebookHelper {

    public static List<String> permissionNeeds = Arrays.asList("email");

    public static void authenticate(final FacebookHelperCallback callback)
    {
        AppMainActivity.getInstance().callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().logInWithReadPermissions(
                AppMainActivity.instance,
                permissionNeeds);
        LoginManager.getInstance().registerCallback(AppMainActivity.getInstance().callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(final LoginResult loginResults) {

                        GraphRequest request = GraphRequest.newMeRequest(
                                loginResults.getAccessToken(),
                                new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(
                                            JSONObject object,
                                            GraphResponse response) {
                                        // Application code
                                        Log.e("LoginActivity", response.toString());
                                        if (callback != null) try {
                                            callback.OnSuccess(
                                                    object.getString("id"),
                                                    object.getString("email"),
                                                    object.getString("name"),
                                                    loginResults.getAccessToken()
                                            );
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id,name,email,gender, birthday");
                        request.setParameters(parameters);
                        request.executeAsync();

                    }
                    @Override
                    public void onCancel() {

                        Log.e("dd","facebook login canceled");

                    }


                    @Override
                    public void onError(FacebookException e) {

                        Log.e("dd", "facebook login failed error " + e.getLocalizedMessage());

                        LoginManager.getInstance().logOut();

                    }
                });
    }


    public interface FacebookHelperCallback
    {
        void OnSuccess(String facebook_account, String facebook_email, String facebook_display_name, AccessToken access_token);
    }

}
