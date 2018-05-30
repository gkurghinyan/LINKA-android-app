package com.linka.lockapp.aos.module.api;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceResponse.AccessKeysResponse;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceResponse.ActivitiesResponse;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceResponse.AppVersionResponse;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceResponse.CheckKeyStatusForUserResponse;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceResponse.GenMasterKeyResponse;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceResponse.GetEmailResponse;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceResponse.LocksResponse;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceResponse.LoginResponse;
import com.linka.lockapp.aos.module.eventbus.WrongCredentialsBusEventMessage;
import com.linka.lockapp.aos.module.helpers.Helpers;
import com.linka.lockapp.aos.module.helpers.LogHelper;
import com.linka.lockapp.aos.module.model.Linka;
import com.linka.lockapp.aos.module.model.LinkaAccessKey;
import com.linka.lockapp.aos.module.model.LinkaActivity;
import com.linka.lockapp.aos.module.model.LinkaAddress;
import com.linka.lockapp.aos.module.model.LinkaName;
import com.pixplicity.easyprefs.library.Prefs;

import org.greenrobot.eventbus.EventBus;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Vanson on 13/7/2016.
 */
public class LinkaAPIServiceImpl {

    static final String URLImageBase = "https://app.linkalock.com/cfs/files/images/";

    public static String URLImage(String picture) {
        if (picture == null) {
            return null;
        }
        return LinkaAPIServiceImpl.URLImageBase + picture + "/?store=size640";
    }


    public static void saveAuth(String authToken, String userId, String userEmail, String userFirstName, String userLastName, String userName, boolean showWalkthrough) {
        SharedPreferences.Editor edit = Prefs.edit();
        edit.putString("x-auth-token", authToken);
        edit.putString("x-user-id", userId);
        edit.putString("user-email", userEmail);
        edit.putString("user-first-name", userFirstName);
        edit.putString("user-last-name", userLastName);
        edit.putString("user-name", userName);
        edit.putBoolean("show-walkthrough", showWalkthrough);
        edit.commit();
    }

    public static void clearAuth() {
        SharedPreferences.Editor edit = Prefs.edit();
        edit.remove("x-auth-token");
        edit.remove("x-user-id");
        edit.remove("user-email");
        edit.remove("user-first-name");
        edit.remove("user-last-name");
        edit.remove("user-name");
        edit.remove("show-walkthrough");
        edit.remove("fcm-token");
        edit.commit();
    }

    public static void saveEmail(String userEmail) {
        SharedPreferences.Editor edit = Prefs.edit();
        edit.putString("user-email", userEmail);
        edit.commit();
    }

    public static String getUserEmail() {
        SharedPreferences.Editor edit = Prefs.edit();
        if (Prefs.getString("user-email", null) == null) {
            return null;
        }

        return Prefs.getString("user-email", null);
    }

    public static boolean isLoggedIn() {
        SharedPreferences.Editor edit = Prefs.edit();
        return Prefs.getString("x-auth-token", null) != null;
    }

    public static String getUserID() {
        SharedPreferences.Editor edit = Prefs.edit();
        if (Prefs.getString("x-user-id", null) == null) {
            return null;
        }

        return Prefs.getString("x-user-id", null);
    }

    public static String getFcmToken() {
        return Prefs.getString("fcm-token", null);
    }

    public static void saveFcmToken(String token) {
        Prefs.putString("fcm-token", token);
    }


    public static boolean doErrors(LinkaAPIServiceResponse responseData, Context context) {
        if (context == null) {
            return false;
        }
        try {
            if (responseData == null) {
                LogHelper.e("Error:", "Network Error Popup");
                new AlertDialog.Builder(context)
                        .setTitle(R.string.network_error)
                        .setMessage(R.string.please_check_network)
                        .setNegativeButton(R.string.ok, null)
                        .show();
            }

            if (responseData.message != null) {
                if (responseData.message.equals("Wrong username or password.")) {
                    EventBus.getDefault().post(new WrongCredentialsBusEventMessage(WrongCredentialsBusEventMessage.SHOW));
                } else {
                    new AlertDialog.Builder(context)
                            .setTitle(R.string.error)
                            .setMessage(responseData.message)
                            .setNegativeButton(R.string.ok, null)
                            .show();
                }
            } else {
                new AlertDialog.Builder(context)
                        .setTitle(R.string.error)
                        .setMessage(R.string.error_invalid)
                        .setNegativeButton(R.string.ok, null)
                        .show();
            }
        } catch (Exception ex) {
            // Bug https://bugzilla.linkalock.com/bugzilla/show_bug.cgi?id=158
            // Very rarely this AlertDialog will crash the app, because of the following error:
            // Unable to add window -- token android.os.BinderProxy@36e850c is not valid; is your activity running?
            // This network error message is important, but not enough to wreck the app
            // So for now we catch the exception and do nothing
        }
        return true;
    }


    public static boolean check(Response response, boolean shouldShowError, Context context) {

        LinkaAPIServiceResponse responseData = null;
        Object data = response.body();
        if (data instanceof LinkaAPIServiceResponse) {
            responseData = (LinkaAPIServiceResponse) data;
        }
        LinkaAPIServiceResponse errorData = LinkaAPIServiceManager.extractErrorFromResponse(response);


        if (LinkaAPIServiceResponse.isSuccess(responseData)) {

            return true;
        }

        if (LinkaAPIServiceResponse.isError(errorData)) {
            if (shouldShowError) {
                doErrors(errorData, context);
            }
            return false;
        }

        if (LinkaAPIServiceResponse.isNetworkError(responseData)
                && LinkaAPIServiceResponse.isNetworkError(errorData)) {
            if (shouldShowError) {
                doErrors(responseData, context);
            }
            return false;
        }

        return true;
    }





    /* AUTH */

    public static Call<LinkaAPIServiceResponse> logout(
            final Context context,
            final Callback<LinkaAPIServiceResponse> callback
    ) {
        LinkaAPIServiceConfig.log("logout");
        Call<LinkaAPIServiceResponse> call = LinkaAPIServiceManager.getInstance().logout();
        call.enqueue(new Callback<LinkaAPIServiceResponse>() {
            @Override
            public void onResponse(Call<LinkaAPIServiceResponse> call, Response<LinkaAPIServiceResponse> response) {
                if (check(response, true, context)) {
                    clearAuth();
                    callback.onResponse(call, response);
                } else {
                    clearAuth();
                    callback.onResponse(call, response);
                }
            }

            @Override
            public void onFailure(Call<LinkaAPIServiceResponse> call, Throwable t) {
                clearAuth();
                callback.onResponse(call, null);
            }
        });
        return call;
    }


    public static Call<LinkaAPIServiceResponse.RegisterResponse> register(
            final Context context,
            LinkaAPIServiceJSON.Register body,
            final Callback<LinkaAPIServiceResponse.RegisterResponse> callback
    ) {
        LinkaAPIServiceConfig.log("register: " + body.email + "  " + body.password);
        Call<LinkaAPIServiceResponse.RegisterResponse> call = LinkaAPIServiceManager.getInstance().register(body);
        call.enqueue(new Callback<LinkaAPIServiceResponse.RegisterResponse>() {
            @Override
            public void onResponse(Call<LinkaAPIServiceResponse.RegisterResponse> call, Response<LinkaAPIServiceResponse.RegisterResponse> response) {
                if (check(response, true, context)) {
                    callback.onResponse(call, response);
                } else {
                    callback.onResponse(call, response);
                }
            }

            @Override
            public void onFailure(Call<LinkaAPIServiceResponse.RegisterResponse> call, Throwable t) {
                doErrors(null, context);
                callback.onFailure(call, t);
            }
        });
        return call;
    }


    public static Call<LoginResponse> login(
            final Context context,
            String email,
            String password,
            final Callback<LoginResponse> callback
    ) {
        LinkaAPIServiceConfig.log("login: " + email + "  " + password);
        Call<LoginResponse> call = LinkaAPIServiceManager.getInstance().login(
                email,
                password,
                Helpers.device_token,
                Helpers.platform,
                Helpers.device_name,
                Helpers.os_version
        );
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (check(response, true, context)) {
                    saveAuth(response.body().data.authToken,
                            response.body().data.userId,
                            response.body().data.userEmail,
                            response.body().data.first_name,
                            response.body().data.last_name,
                            response.body().data.name,
                            response.body().data.showWalkthrough);
                    callback.onResponse(call, response);
                } else {
                    callback.onResponse(call, response);
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                doErrors(null, context);
                callback.onFailure(call, t);
            }
        });
        return call;
    }

    public static Call<LoginResponse> login_facebook(
            final Context context,
            String password,
            final Callback<LoginResponse> callback
    ) {
        LinkaAPIServiceConfig.log("login facebook: " + password);
        Call<LoginResponse> call = LinkaAPIServiceManager.getInstance().login_facebook(
                password,
                Helpers.device_token,
                Helpers.platform,
                Helpers.device_name,
                Helpers.os_version
        );
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (check(response, true, context)) {
                    saveAuth(response.body().data.authToken,
                            response.body().data.userId,
                            response.body().data.userEmail,
                            response.body().data.first_name,
                            response.body().data.last_name,
                            response.body().data.name,
                            response.body().data.showWalkthrough);
                    callback.onResponse(call, response);
                } else {
                    callback.onResponse(call, response);
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                doErrors(null, context);
                callback.onFailure(call, t);
            }
        });
        return call;
    }






    /* GETEMAIL */

    public static Call<GetEmailResponse> get_email(
            final Context context,
            final Callback<GetEmailResponse> callback
    ) {
        LinkaAPIServiceConfig.log("Get email");
        Call<GetEmailResponse> call = LinkaAPIServiceManager.getInstance().get_email();
        call.enqueue(new Callback<GetEmailResponse>() {
            @Override
            public void onResponse(Call<GetEmailResponse> call, Response<GetEmailResponse> response) {
                if (check(response, true, context)) {
                    if (response.body() != null) {
                        if (response.body().data != null) {
                            LogHelper.e("Got email response:", response.body().data.userEmail);
                            saveEmail(response.body().data.userEmail);
                        }
                    }
                    callback.onResponse(call, response);

                } else {
                    callback.onResponse(call, response);
                }

            }

            @Override
            public void onFailure(Call<GetEmailResponse> call, Throwable t) {
                LogHelper.e("Couldn't get Email response:", "Probably no network connection");
                doErrors(null, context);
                callback.onFailure(call, t);
            }
        });
        return call;
    }







    /* LOCKS */


    public static Call<LocksResponse> get_lock_single(
            final Context context,
            final Linka linka,
            final Callback<LocksResponse> callback
    ) {

        Call<LocksResponse> call = null;

        call = LinkaAPIServiceManager.getInstance().get_lock_single("{\"serial_no\":\"" + linka.getMACAddress() + "\"}");

        LinkaAPIServiceConfig.log("get_lock_single: " + linka.getMACAddress());
        call.enqueue(new Callback<LocksResponse>() {
            @Override
            public void onResponse(Call<LocksResponse> call, Response<LocksResponse> response) {
                if (check(response, true, context)) {
                    LocksResponse locksResponse = response.body();
                    if (locksResponse != null) {
                        if (locksResponse.data.size() > 0) {
                            LocksResponse.Data data = locksResponse.data.get(0);

                            if (linka != null && data.name != null) {
                                LinkaName.saveLinkaNameForMacAddress(
                                        linka.getMACAddress(),
                                        data.name
                                );
                            }

                            if (linka != null && data.latitude != 0 && data.longitude != 0) {
                                linka.saveLatLng(data.latitude, data.longitude);
                            }
                        }
                    }
                    callback.onResponse(call, response);
                } else {
                    callback.onResponse(call, response);
                }
            }

            @Override
            public void onFailure(Call<LocksResponse> call, Throwable t) {
//                doErrors(null, context);
                callback.onFailure(call, t);
            }
        });
        return call;
    }


    public static Call<LinkaAPIServiceResponse> upsert_lock(
            final Context context,
            Linka linka,
            final Callback<LinkaAPIServiceResponse> callback
    ) {

        Call<LinkaAPIServiceResponse> call = null;

        if (linka.latitude != null && linka.longitude != null && !linka.latitude.equals("") && !linka.longitude.equals("")) {
            call = LinkaAPIServiceManager.getInstance().upsert_lock(
                    linka.getName(),
                    linka.getMACAddress(),
                    linka.latitude,
                    linka.longitude,
                    linka.isLocked
            );
        } else {
            call = LinkaAPIServiceManager.getInstance().upsert_lock(
                    linka.getName(),
                    linka.getMACAddress(),
                    linka.isLocked
            );
        }

        LinkaAPIServiceConfig.log("upsert_lock: " + linka.getMACAddress() + ", isLocked: " + (linka.isLocked ? "true" : "false"));

        // Lock is done manually, let app know state changed
        call.enqueue(new Callback<LinkaAPIServiceResponse>() {
            @Override
            public void onResponse(Call<LinkaAPIServiceResponse> call, Response<LinkaAPIServiceResponse> response) {
                if (check(response, true, context)) {
                    callback.onResponse(call, response);
                } else {
                    callback.onResponse(call, response);
                }
            }

            @Override
            public void onFailure(Call<LinkaAPIServiceResponse> call, Throwable t) {
//                doErrors(null, context);
                callback.onFailure(call, t);
            }
        });
        return call;
    }


    public static Call<LinkaAPIServiceResponse> add_activity(
            final Context context,
            Linka linka,
            LinkaActivity activity,
            final Callback<LinkaAPIServiceResponse> callback
    ) {
        int type = activity.linka_activity_status;
        boolean should_update = false;
        String body = "";
        String latitude = activity.latitude;
        String longitude = activity.longitude;
        String record_date = activity.timestamp;

        if (type == LinkaActivity.LinkaActivityType.isLocked.getValue()) {
            should_update = true;
            if (latitude.equals("0") && longitude.equals("0")) {
                body = "Locked";
            } else {
                LinkaAddress address = LinkaAddress.getAddressForLatLng(latitude, longitude);
                if (address != null) {
                    body = "Locked at " + address.address;
                } else {
                    body = "Locked";
                }
            }
        } else if (type == LinkaActivity.LinkaActivityType.isUnlocked.getValue()) {
            should_update = true;
            body = "Unlocked";
        } else if (type == LinkaActivity.LinkaActivityType.isBatteryLow.getValue()) {
            body = "Battery low at " + activity.batteryPercent + "%";
        } else if (type == LinkaActivity.LinkaActivityType.isBatteryCriticallyLow.getValue()) {
            body = "Battery low at " + activity.batteryPercent + "%";
        } else if (type == LinkaActivity.LinkaActivityType.isTamperAlert.getValue()) {
            should_update = true;
            body = "Tamper Alert";
        } else if (type == LinkaActivity.LinkaActivityType.isRenamed.getValue()) {
            body = "Lock renamed from [" + activity.old_lock_name + "] to [" + activity.new_lock_name + "]";
        } else if (type == LinkaActivity.LinkaActivityType.isBackInRange.getValue()) {
            body = "Back in Range";
        } else if (type == LinkaActivity.LinkaActivityType.isOutOfRange.getValue()) {
            body = "Out of Range";
        }

        if (!should_update) {
            return null;
        }

        LinkaAPIServiceConfig.log("add_activity: " + linka.getMACAddress() + " " + body + " " + latitude + " " + longitude);

        Call<LinkaAPIServiceResponse> call = LinkaAPIServiceManager.getInstance().add_activity(
                linka.getMACAddress(),
                "",
                body,
                latitude,
                longitude,
                activity.linka_activity_status,
                activity.batteryPercent,
                activity.timestamp,
                activity.timestamp_locked,
                activity.linka_uuid,
                activity.platform,
                activity.os_version,
                activity.fw_version,
                activity.api_version,
                activity.pac,
                activity.actuations,
                activity.temperature,
                activity.sleep_lock_sec,
                activity.sleep_unlock_sec
        );
        call.enqueue(new Callback<LinkaAPIServiceResponse>() {
            @Override
            public void onResponse(Call<LinkaAPIServiceResponse> call, Response<LinkaAPIServiceResponse> response) {
                if (check(response, false, context)) {
                    callback.onResponse(call, response);
                } else {
                    callback.onResponse(call, response);
                }
            }

            @Override
            public void onFailure(Call<LinkaAPIServiceResponse> call, Throwable t) {
//                doErrors(null, context);
                callback.onFailure(call, t);
            }
        });
        return call;
    }


    public static Call<ActivitiesResponse> fetch_activities(
            final Context context,
            Linka linka,
            final Callback<ActivitiesResponse> callback
    ) {
        LinkaAPIServiceConfig.log("fetch_activities");

        Call<ActivitiesResponse> call = LinkaAPIServiceManager.getInstance().fetch_activities(linka.getMACAddress());
        call.enqueue(new Callback<ActivitiesResponse>() {
            @Override
            public void onResponse(Call<ActivitiesResponse> call, Response<ActivitiesResponse> response) {
                if (check(response, true, context)) {
                    callback.onResponse(call, response);
                } else {
                    callback.onResponse(call, response);
                }
            }

            @Override
            public void onFailure(Call<ActivitiesResponse> call, Throwable t) {
                //Every time on startup, if no internet, this code runs and causes "Network Error"
                doErrors(null, context);
                callback.onFailure(call, t);
            }
        });
        return call;
    }


    // MARK: - KEYS V2

    public static Call<GenMasterKeyResponse> gen_master_key(
            final Context context,
            Linka linka,
            final Callback<GenMasterKeyResponse> callback
    ) {
        LinkaAPIServiceConfig.log("gen_master_key");

        Call<GenMasterKeyResponse> call = LinkaAPIServiceManager.getInstance().gen_master_key(linka.getMACAddress());
        call.enqueue(new Callback<GenMasterKeyResponse>() {
            @Override
            public void onResponse(Call<GenMasterKeyResponse> call, Response<GenMasterKeyResponse> response) {
                if (check(response, true, context)) {
                    callback.onResponse(call, response);
                } else {
                    callback.onResponse(call, response);
                }
            }

            @Override
            public void onFailure(Call<GenMasterKeyResponse> call, Throwable t) {
                doErrors(null, context);
                callback.onFailure(call, t);
            }
        });
        return call;
    }


    public static Call<CheckKeyStatusForUserResponse> check_key_status_for_user(
            final Context context,
            String lock_serial_no,
            final Callback<CheckKeyStatusForUserResponse> callback
    ) {
        LinkaAPIServiceConfig.log("check_key_status_for_user");

        String token = Helpers.device_token;
        if (token == null) {
            token = "";
        }


        Call<CheckKeyStatusForUserResponse> call = LinkaAPIServiceManager.getInstance().check_key_status_for_user(
                lock_serial_no,
                token
        );
        call.enqueue(new Callback<CheckKeyStatusForUserResponse>() {
            @Override
            public void onResponse(Call<CheckKeyStatusForUserResponse> call, Response<CheckKeyStatusForUserResponse> response) {
                callback.onResponse(call, response);
            }

            @Override
            public void onFailure(Call<CheckKeyStatusForUserResponse> call, Throwable t) {
                callback.onFailure(call, t);
            }
        });
        return call;
    }


    public static Call<AccessKeysResponse> register_lock_with_master_keys(
            final Context context,
            Linka linka,
            LinkaAccessKey accessKey,
            final Callback<AccessKeysResponse> callback
    ) {
        LinkaAPIServiceConfig.log("register_lock_with_master_keys");

        String token = Helpers.device_token;
        if (token == null) {
            token = "";
        }
        String appName = "Android";

        String master = "";
        String master_2 = "";
        if (accessKey != null) {
            master = accessKey.access_key_master;
            master_2 = accessKey.access_key_master_2;
        }

        Call<AccessKeysResponse> call = LinkaAPIServiceManager.getInstance().register_lock_with_master_keys(
                linka.getMACAddress(),
                master,
                master_2,
                token,
                appName);
        call.enqueue(new Callback<AccessKeysResponse>() {
            @Override
            public void onResponse(Call<AccessKeysResponse> call, Response<AccessKeysResponse> response) {
                if (check(response, true, context)) {
                    callback.onResponse(call, response);
                } else {
                    callback.onResponse(call, response);
                }
            }

            @Override
            public void onFailure(Call<AccessKeysResponse> call, Throwable t) {
                doErrors(null, context);
                callback.onFailure(call, t);
            }
        });
        return call;
    }


    public static Call<LinkaAPIServiceResponse> send_request_for_user_permission(
            final Context context,
            Linka linka,
            final Callback<LinkaAPIServiceResponse> callback
    ) {
        LinkaAPIServiceConfig.log("send_request_for_user_permission");

        String token = Helpers.device_token;
        if (token == null) {
            token = "";
        }
        String appName = "Android";

        Call<LinkaAPIServiceResponse> call = LinkaAPIServiceManager.getInstance().send_request_for_user_permission(
                linka.getMACAddress()
        );
        call.enqueue(new Callback<LinkaAPIServiceResponse>() {
            @Override
            public void onResponse(Call<LinkaAPIServiceResponse> call, Response<LinkaAPIServiceResponse> response) {
                if (check(response, true, context)) {
                    callback.onResponse(call, response);
                } else {
                    callback.onResponse(call, response);
                }
            }

            @Override
            public void onFailure(Call<LinkaAPIServiceResponse> call, Throwable t) {
                doErrors(null, context);
                callback.onFailure(call, t);
            }
        });
        return call;
    }


    public static Call<LinkaAPIServiceResponse> reset_all_lock_information_and_keys(
            final Context context,
            Linka linka,
            final Callback<LinkaAPIServiceResponse> callback
    ) {
        LinkaAPIServiceConfig.log("reset_all_lock_information_and_keys");

        String token = Helpers.device_token;
        if (token == null) {
            token = "";
        }
        String appName = "Android";

        Call<LinkaAPIServiceResponse> call = LinkaAPIServiceManager.getInstance().reset_all_lock_information_and_keys(
                linka.getMACAddress(),
                token,
                1);
        call.enqueue(new Callback<LinkaAPIServiceResponse>() {
            @Override
            public void onResponse(Call<LinkaAPIServiceResponse> call, Response<LinkaAPIServiceResponse> response) {
                if (check(response, true, context)) {
                    callback.onResponse(call, response);
                } else {
                    callback.onResponse(call, response);
                }
            }

            @Override
            public void onFailure(Call<LinkaAPIServiceResponse> call, Throwable t) {
                doErrors(null, context);
                callback.onFailure(call, t);
            }
        });
        return call;
    }


    //Sends the push token to the server.
    //If response is successful, saves the new token
    public static Call<LinkaAPIServiceResponse> update_push_token(
            final String push_token
    ) {

        LogHelper.e("Impl", "Updating push token");
        String device_token = Helpers.device_token;

        Call<LinkaAPIServiceResponse> call = LinkaAPIServiceManager.getInstance().update_push_token(device_token, push_token);
        call.enqueue(new Callback<LinkaAPIServiceResponse>() {
            @Override
            public void onResponse(Call<LinkaAPIServiceResponse> call, Response<LinkaAPIServiceResponse> response) {
                if (check(response, false, null)) {
                    saveFcmToken(push_token);
                }
            }

            @Override
            public void onFailure(Call<LinkaAPIServiceResponse> call, Throwable t) {

            }
        });
        return call;
    }


    // MARK: - GET APP VERSION + UPDATE

    public static Call<AppVersionResponse> get_app_version(
            final Context context,
            final boolean alwaysShowMessage,
            final Callback<AppVersionResponse> callback
    ) {
        String appName = "Android";
        LinkaAPIServiceConfig.log("get_app_version: " + appName);
        Call<AppVersionResponse> call = LinkaAPIServiceManager.getInstance().get_app_version(appName);
        call.enqueue(new Callback<AppVersionResponse>() {
            @Override
            public void onResponse(Call<AppVersionResponse> call, Response<AppVersionResponse> response) {
                if (check(response, false, context)) {

                    try {
                        PackageInfo pInfo = null;
                        pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                        String version = pInfo.versionName;
                        int verCode = pInfo.versionCode;
                        if (verCode < response.body().data.build) {
                            new AlertDialog.Builder(context)
                                    .setTitle("")
                                    .setMessage(R.string.app_new_version)
                                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.linka.lockapp.aos"));
                                            context.startActivity(intent);
                                        }
                                    })
                                    .setNegativeButton(R.string.no, null)
                                    .show();
                        } else {
                            if (alwaysShowMessage) {
                                new AlertDialog.Builder(context)
                                        .setTitle("")
                                        .setMessage(R.string.no_app_new_version)
                                        .setNegativeButton(R.string.ok, null)
                                        .show();
                            }
                        }
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<AppVersionResponse> call, Throwable t) {

            }
        });
        return call;
    }


    public static Call<LinkaAPIServiceResponse.AssociatedLocksResponse> associated_locks(
            final Context context,
            final Callback<LinkaAPIServiceResponse.AssociatedLocksResponse> callback
    ) {
        LinkaAPIServiceConfig.log("send_request_for_user_permission");

        String token = Helpers.device_token;
        if (token == null) {
            token = "";
        }
        String appName = "Android";

        Call<LinkaAPIServiceResponse.AssociatedLocksResponse> call = LinkaAPIServiceManager.getInstance().associated_locks(
                token
        );
        call.enqueue(new Callback<LinkaAPIServiceResponse.AssociatedLocksResponse>() {
            @Override
            public void onResponse(Call<LinkaAPIServiceResponse.AssociatedLocksResponse> call, Response<LinkaAPIServiceResponse.AssociatedLocksResponse> response) {
                callback.onResponse(call, response);
            }

            @Override
            public void onFailure(Call<LinkaAPIServiceResponse.AssociatedLocksResponse> call, Throwable t) {
                doErrors(null, context);
                callback.onFailure(call, t);
            }
        });
        return call;
    }


    public static Call<LinkaAPIServiceResponse> verify_device() {
        LinkaAPIServiceConfig.log("verify_device");

        Call<LinkaAPIServiceResponse> call = LinkaAPIServiceManager.getInstance().verify_device(
                Helpers.device_token,
                Helpers.platform,
                Helpers.device_name,
                Helpers.os_version
        );
        call.enqueue(new Callback<LinkaAPIServiceResponse>() {
            @Override
            public void onResponse(Call<LinkaAPIServiceResponse> call, Response<LinkaAPIServiceResponse> response) {
            }

            @Override
            public void onFailure(Call<LinkaAPIServiceResponse> call, Throwable t) {
            }
        });
        return call;
    }


    public static Call<LinkaAPIServiceResponse.LockPermissionsResponse> lock_permissions(
            final Context context,
            Linka linka,
            final Callback<LinkaAPIServiceResponse.LockPermissionsResponse> callback
    ) {
        LinkaAPIServiceConfig.log("send_request_for_user_permission");

        String token = Helpers.device_token;
        if (token == null) {
            token = "";
        }
        String appName = "Android";

        Call<LinkaAPIServiceResponse.LockPermissionsResponse> call = LinkaAPIServiceManager.getInstance().lock_permissions(
                linka.lock_mac_address
        );
        call.enqueue(new Callback<LinkaAPIServiceResponse.LockPermissionsResponse>() {
            @Override
            public void onResponse(Call<LinkaAPIServiceResponse.LockPermissionsResponse> call, Response<LinkaAPIServiceResponse.LockPermissionsResponse> response) {
                if (check(response, true, context)) {
                    callback.onResponse(call, response);
                } else {
                    callback.onResponse(call, response);
                }
            }

            @Override
            public void onFailure(Call<LinkaAPIServiceResponse.LockPermissionsResponse> call, Throwable t) {
                doErrors(null, context);
                callback.onFailure(call, t);
            }
        });
        return call;
    }


    public static Call<LinkaAPIServiceResponse> transfer_ownership(
            final Context context,
            Linka linka,
            String email,
            final Callback<LinkaAPIServiceResponse> callback
    ) {
        LinkaAPIServiceConfig.log("send_request_for_user_permission");

        String token = Helpers.device_token;
        if (token == null) {
            token = "";
        }
        String appName = "Android";

        Call<LinkaAPIServiceResponse> call = LinkaAPIServiceManager.getInstance().transfer_ownership(
                email,
                linka.lock_mac_address
        );
        call.enqueue(new Callback<LinkaAPIServiceResponse>() {
            @Override
            public void onResponse(Call<LinkaAPIServiceResponse> call, Response<LinkaAPIServiceResponse> response) {
                if (check(response, true, context)) {
                    callback.onResponse(call, response);
                } else {
                    callback.onResponse(call, response);
                }
            }

            @Override
            public void onFailure(Call<LinkaAPIServiceResponse> call, Throwable t) {
                doErrors(null, context);
                callback.onFailure(call, t);
            }
        });
        return call;
    }


    public static Call<LinkaAPIServiceResponse> revoke_access(
            final Context context,
            Linka linka,
            String email,
            final Callback<LinkaAPIServiceResponse> callback
    ) {
        LinkaAPIServiceConfig.log("send_request_for_user_permission");

        String token = Helpers.device_token;
        if (token == null) {
            token = "";
        }
        String appName = "Android";

        Call<LinkaAPIServiceResponse> call = LinkaAPIServiceManager.getInstance().revoke_access(
                email,
                linka.lock_mac_address
        );
        call.enqueue(new Callback<LinkaAPIServiceResponse>() {
            @Override
            public void onResponse(Call<LinkaAPIServiceResponse> call, Response<LinkaAPIServiceResponse> response) {
                if (check(response, true, context)) {
                    callback.onResponse(call, response);
                } else {
                    callback.onResponse(call, response);
                }
            }

            @Override
            public void onFailure(Call<LinkaAPIServiceResponse> call, Throwable t) {
                doErrors(null, context);
                callback.onFailure(call, t);
            }
        });
        return call;
    }


    public static Call<LinkaAPIServiceResponse> send_invite(
            final Context context,
            Linka linka,
            String email,
            final Callback<LinkaAPIServiceResponse> callback
    ) {
        LinkaAPIServiceConfig.log("send_request_for_user_permission");

        String token = Helpers.device_token;
        if (token == null) {
            token = "";
        }
        String appName = "Android";

        Call<LinkaAPIServiceResponse> call = LinkaAPIServiceManager.getInstance().send_invite(
                email,
                linka.lock_mac_address
        );
        call.enqueue(new Callback<LinkaAPIServiceResponse>() {
            @Override
            public void onResponse(Call<LinkaAPIServiceResponse> call, Response<LinkaAPIServiceResponse> response) {
                if (check(response, true, context)) {
                    callback.onResponse(call, response);
                } else {
                    callback.onResponse(call, response);
                }
            }

            @Override
            public void onFailure(Call<LinkaAPIServiceResponse> call, Throwable t) {
                doErrors(null, context);
                callback.onFailure(call, t);
            }
        });
        return call;
    }


    public static Call<LinkaAPIServiceResponse> transfer_device_tokens(
            final Context context,
            String oldToken,
            String newToken,
            final Callback<LinkaAPIServiceResponse> callback
    ) {
        LinkaAPIServiceConfig.log("send_request_for_user_permission");

        String token = Helpers.device_token;
        String appName = "Android";

        Call<LinkaAPIServiceResponse> call = LinkaAPIServiceManager.getInstance().transfer_device_tokens(
                oldToken,
                newToken
        );
        call.enqueue(new Callback<LinkaAPIServiceResponse>() {
            @Override
            public void onResponse(Call<LinkaAPIServiceResponse> call, Response<LinkaAPIServiceResponse> response) {
                callback.onResponse(call, response);
            }

            @Override
            public void onFailure(Call<LinkaAPIServiceResponse> call, Throwable t) {
                callback.onFailure(call, t);
            }
        });
        return call;
    }


    // MARK: - POST ERRORS

    public static Call<LinkaAPIServiceResponse> post_error(
            final Context context,
            Linka linka,
            String code,
            String desc,
            String accessKey
    ) {
        String token = Helpers.device_token;
        if (token == null) {
            token = "";
        }
        String appName = "Android";

        Call<LinkaAPIServiceResponse> call = LinkaAPIServiceManager.getInstance().post_error(
                linka.getMACAddress(),
                code,
                desc,
                token,
                appName,
                accessKey
        );
        call.enqueue(new Callback<LinkaAPIServiceResponse>() {
            @Override
            public void onResponse(Call<LinkaAPIServiceResponse> call, Response<LinkaAPIServiceResponse> response) {
                if (check(response, false, context)) {

                }
            }

            @Override
            public void onFailure(Call<LinkaAPIServiceResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });
        return call;
    }


    // MARK: - REQUEST EMAIL PASSWORD CODE

    public static Call<LinkaAPIServiceResponse> request_reset_password_code(
            final Context context,
            String email,
            final Callback<LinkaAPIServiceResponse> callback
    ) {
        Call<LinkaAPIServiceResponse> call = LinkaAPIServiceManager.getInstance().request_reset_password_code(
                "request_reset_password_code",
                email
        );
        call.enqueue(new Callback<LinkaAPIServiceResponse>() {
            @Override
            public void onResponse(Call<LinkaAPIServiceResponse> call, Response<LinkaAPIServiceResponse> response) {
                if (check(response, true, context)) {
                    callback.onResponse(call, response);
                } else {
                    callback.onResponse(call, response);
                }
            }

            @Override
            public void onFailure(Call<LinkaAPIServiceResponse> call, Throwable t) {
                doErrors(null, context);
                callback.onFailure(call, t);
            }
        });
        return call;
    }


    // MARK: - REQUEST PASSWORD CHANGE

    public static Call<LinkaAPIServiceResponse> reset_password(
            final Context context,
            String code,
            String newPassword,
            String confirmNewPassword,
            final Callback<LinkaAPIServiceResponse> callback
    ) {

        Call<LinkaAPIServiceResponse> call = LinkaAPIServiceManager.getInstance().reset_password(
                "reset_password",
                code,
                newPassword,
                confirmNewPassword
        );
        call.enqueue(new Callback<LinkaAPIServiceResponse>() {
            @Override
            public void onResponse(Call<LinkaAPIServiceResponse> call, Response<LinkaAPIServiceResponse> response) {
                if (check(response, true, context)) {
                    callback.onResponse(call, response);
                } else {
                    callback.onResponse(call, response);
                }
            }

            @Override
            public void onFailure(Call<LinkaAPIServiceResponse> call, Throwable t) {
                doErrors(null, context);
                callback.onFailure(call, t);
            }
        });
        return call;
    }
}
