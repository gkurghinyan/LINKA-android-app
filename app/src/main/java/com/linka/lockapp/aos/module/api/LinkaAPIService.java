package com.linka.lockapp.aos.module.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Vanson on 10/6/2016.
 */
public interface LinkaAPIService {


    /* AUTH */

    @POST("/api/logout")
    Call<LinkaAPIServiceResponse> logout();

    @POST("/api/register_v3")
    Call<LinkaAPIServiceResponse.RegisterResponse> register(
            @Body LinkaAPIServiceJSON.Register body
    );

    @FormUrlEncoded
    @POST("/api/login_v3")
    Call<LinkaAPIServiceResponse.LoginResponse> login(
            @Field("email") String email,
            @Field("password") String password,
            @Field("device_token") String device_token,
            @Field("platform") String platform,
            @Field("device_name") String device_name,
            @Field("os_version") String os_version
    );

    @FormUrlEncoded
    @POST("/api/login_facebook_v2")
    Call<LinkaAPIServiceResponse.LoginResponse> login_facebook(
            @Field("password") String password,
            @Field("device_token") String device_token,
            @Field("platform") String platform,
            @Field("device_name") String device_name,
            @Field("os_version") String os_version
    );


    /* GETEMAIL */
    @GET("/api/get_email")
    Call<LinkaAPIServiceResponse.GetEmailResponse> get_email();



    /* LOCKS */

    @GET("/api/locks")
    Call<LinkaAPIServiceResponse.LocksResponse> get_locks();


    @GET("/api/locks/")
    Call<LinkaAPIServiceResponse.LocksResponse> get_lock_single(
            @Query("query") String query
    );


    @FormUrlEncoded
    @POST("/api/locks")
    Call<LinkaAPIServiceResponse> upsert_lock(
            @Field("name") String name,
            @Field("serial_no") String serial_no,
            @Field("latitude") String latitude,
            @Field("longitude") String longitude,
            @Field("is_locked") boolean is_locked
    );


    @FormUrlEncoded
    @POST("/api/locks")
    Call<LinkaAPIServiceResponse> upsert_lock(
            @Field("name") String name,
            @Field("serial_no") String serial_no,
            @Field("is_locked") boolean is_locked
    );




    /* ACTIVITIES */

    @GET("/api/activitys")
    Call<LinkaAPIServiceResponse> get_activities();


    @FormUrlEncoded
    @POST("/api/activitys")
    Call<LinkaAPIServiceResponse> add_activity(
            @Field("lock_serial_no") String lock_serial_no,
            @Field("msg_title") String msg_title,
            @Field("msg_desc") String msg_desc,
            @Field("latitude") String latitude,
            @Field("longitude") String longitude,
            @Field("linka_activity_status") int linka_activity_status,
            @Field("batteryPercent") int batteryPercent,
            @Field("timestamp") String timestamp,
            @Field("timestamp_locked") String timestamp_locked,
            @Field("uuid") String uuid,
            @Field("platform") String platform,
            @Field("os_version") String os_version,
            @Field("fw_version") String fw_version,
            @Field("api_version") String api_version,
            @Field("pac") int pac,
            @Field("actuations") long actuations,
            @Field("temperature") double temperature,
            @Field("sleep_lock_sec") int sleep_lock_sec,
            @Field("sleep_unlock_sec") int sleep_unlock_sec
    );



    @FormUrlEncoded
    @PUT("/api/activitys/fetch")
    Call<LinkaAPIServiceResponse.ActivitiesResponse> fetch_activities(
            @Field("lock_serial_no") String lock_serial_no
    );




    /* KEYS */

    @FormUrlEncoded
    @PUT("/api/keys/gen_master_key")
    Call<LinkaAPIServiceResponse.GenMasterKeyResponse> gen_master_key(
            @Field("lock_serial_no") String lock_serial_no
    );


    @FormUrlEncoded
    @PUT("/api/keys/check_key_status_for_user_v2")
    Call<LinkaAPIServiceResponse.CheckKeyStatusForUserResponse> check_key_status_for_user(
            @Field("lock_serial_no") String lock_serial_no,
            @Field("device_token") String device_token
    );


    @FormUrlEncoded
    @PUT("/api/keys/register_lock_with_master_keys")
    Call<LinkaAPIServiceResponse.AccessKeysResponse> register_lock_with_master_keys(
            @Field("lock_serial_no") String lock_serial_no,
            @Field("access_key_master") String access_key_master,
            @Field("access_key_master_2") String access_key_master_2,
            @Field("device_token") String device_token,
            @Field("device_app_name") String device_app_name
    );




    @FormUrlEncoded
    @PUT("/api/keys/request_for_user_permission_v2")
    Call<LinkaAPIServiceResponse> send_request_for_user_permission(
            @Field("lock_serial_no") String lock_serial_no
    );



    @FormUrlEncoded
    @PUT("/api/keys/reset_all_lock_information_and_keys")
    Call<LinkaAPIServiceResponse> reset_all_lock_information_and_keys(
            @Field("lock_serial_no") String lock_serial_no,
            @Field("device_token") String device_token,
            @Field("api_version") int api_version
    );


    @FormUrlEncoded
    @PUT("/api/keys/lock_permissions")
    Call<LinkaAPIServiceResponse.LockPermissionsResponse> lock_permissions(
            @Field("lock_serial_no") String lock_serial_no
    );


    @FormUrlEncoded
    @PUT("/api/keys/transfer_ownership")
    Call<LinkaAPIServiceResponse> transfer_ownership(
            @Field("userId") String userId,
            @Field("lock_serial_no") String lock_serial_no
    );



    @FormUrlEncoded
    @PUT("/api/keys/revoke_access")
    Call<LinkaAPIServiceResponse> revoke_access(
            @Field("userId") String userId,
            @Field("lock_serial_no") String lock_serial_no
    );




    @FormUrlEncoded
    @PUT("/api/keys/send_invite")
    Call<LinkaAPIServiceResponse> send_invite(
            @Field("userId") String userId,
            @Field("lock_serial_no") String lock_serial_no
    );

    @FormUrlEncoded
    @PUT("/api/keys/send_invite")
    Call<LinkaAPIServiceResponse> send_invite_with_email(
            @Field("email") String email,
            @Field("lock_serial_no") String lock_serial_no
    );


    @FormUrlEncoded
    @POST("/api/transfer_device_tokens")
    Call<LinkaAPIServiceResponse> transfer_device_tokens(
            @Field("oldToken") String oldToken,
            @Field("newToken") String newToken
    );



    @FormUrlEncoded
    @PUT("/api/keys/associated_locks")
    Call<LinkaAPIServiceResponse.AssociatedLocksResponse> associated_locks(
            @Field("device_token") String device_token
    );


    @FormUrlEncoded
    @POST("/api/verify_device")
    Call<LinkaAPIServiceResponse> verify_device(
            @Field("device_token") String device_token,
            @Field("platform") String platform,
            @Field("device_name") String device_name,
            @Field("os_version") String os_version
    );



    // MARK: - PUSH

    @FormUrlEncoded
    @POST("/api/update_push_token")
    Call<LinkaAPIServiceResponse> update_push_token(
            @Field("device_token") String device_token,
            @Field("push_token") String push_token
    );





    /* GET APP VERSION + UPDATE */

    @GET("/api/app_version/{platform}")
    Call<LinkaAPIServiceResponse.AppVersionResponse> get_app_version(
            @Path("platform") String platform
    );






    /* SUBMIT ERRORS */

    @FormUrlEncoded
    @POST("/api/systemerrors/")
    Call<LinkaAPIServiceResponse> post_error(
            @Field("lock_serial_no") String lock_serial_no,
            @Field("code") String code,
            @Field("desc") String desc,
            @Field("token") String token,
            @Field("appName") String appName,
            @Field("accessKey") String accessKey
    );




    /* REQUEST PWD CHANGE CODE */

    @FormUrlEncoded
    @POST("/api/resetpassword/reset")
    Call<LinkaAPIServiceResponse> request_reset_password_code(
            @Field("action") String action,
            @Field("email") String email
    );




    /* REQUEST PWD CHANGE */

    @FormUrlEncoded
    @POST("/api/resetpassword/reset")
    Call<LinkaAPIServiceResponse> reset_password(
            @Field("action") String action,
            @Field("code") String code,
            @Field("new_password") String new_password,
            @Field("new_confirm_password") String new_confirm_password
    );

}
