package com.linka.lockapp.aos.module.model;

import android.content.Context;
import android.os.Handler;
import android.provider.BaseColumns;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;
import com.linka.Lock.FirmwareAPI.Comms.LockEncV1;
import com.linka.lockapp.aos.AppDelegate;
import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceImpl;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceResponse;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceResponse.AccessKeysResponse;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceResponse.CheckKeyStatusForUserResponse;
import com.linka.lockapp.aos.module.api.LinkaAPIServiceResponse.GenMasterKeyResponse;
import com.linka.lockapp.aos.module.i18n._;
import com.linka.lockapp.aos.module.widget.LockControllerSetEncryptionKeyLogic;
import com.linka.lockapp.aos.module.widget.LockPairingController;
import com.linka.lockapp.aos.module.widget.LocksController;

import java.io.Serializable;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Vanson on 14/7/2016.
 */
@Table(name = "LinkaAccessKeys", id = BaseColumns._ID)
public class LinkaAccessKey extends Model implements Serializable {

    @Column(name = "linka_lock_address", index = true)
    public String linka_lock_address = "";

    @Column(name = "userProfile_id")
    public String userProfile_id = "";

    @Column(name = "key_id")
    public String key_id = "";

    @Column(name = "lock_id")
    public String lock_id = "";

    @Column(name = "last_login_date")
    public String last_login_date = "";

    @Column(name = "access_key_master")
    public String access_key_master = "";

    @Column(name = "access_key_master_2")
    public String access_key_master_2 = "";

    // NEVER Store these locally
    String access_key_origin = "";
    String access_key_origin_v2 = "";

    @Column(name = "access_key_admin")
    public String access_key_admin = "";

    @Column(name = "access_key_admin_2")
    public String access_key_admin_2 = "";

    @Column(name = "access_key_user")
    public String access_key_user = "";

    @Column(name = "access_key_user_2")
    public String access_key_user_2 = "";

    @Column(name = "v2_access_key_admin")
    public String v2_access_key_admin = "";

    @Column(name = "v2_access_key_admin_2")
    public String v2_access_key_admin_2 = "";

    @Column(name = "v2_access_key_user")
    public String v2_access_key_user = "";

    @Column(name = "v2_access_key_user_2")
    public String v2_access_key_user_2 = "";

    @Column(name = "is_valid")
    public boolean is_valid = false;

    @Column(name = "is_need_reset_master_key")
    public boolean is_need_reset_master_key = false;

    @Column(name = "is_reactivated_from_key_id")
    public String is_reactivated_from_key_id = "";

    @Column(name = "reactivation_key")
    public String reactivation_key = "";

    @Column(name = "api_user_id", index = true)
    public String api_user_id = "";



    public LinkaAccessKey() {
        super();
    }




    public static List<LinkaAccessKey> getKeys() {
        return new Select().from(LinkaAccessKey.class).execute();
    }

    public static LinkaAccessKey getKey(LinkaAccessKey key) {
        return new Select().from(LinkaAccessKey.class).where("_id = ?", key.getId()).executeSingle();
    }

    public static LinkaAccessKey getKeyFromLinka(Linka linka) {
        From from = new Select().from(LinkaAccessKey.class);
        if (AppDelegate.shouldLimitLinkaAccessToUserID) {
            String userID = LinkaAPIServiceImpl.getUserID();
            if (userID == null) {
                from = from.where("linka_lock_address = ? AND is_valid = 1 AND api_user_id = ?", linka.getMACAddress(), "UNDEFINED");
            } else {
                from = from.where("linka_lock_address = ? AND is_valid = 1 AND api_user_id = ?", linka.getMACAddress(), userID);
            }
        } else {
            from = from.where("linka_lock_address = ? AND is_valid = 1", linka.getMACAddress());
        }
        return from.executeSingle();
    }

    public static List<LinkaAccessKey> getAllKeysFromLinka(Linka linka)
    {
        From from = new Select().from(LinkaAccessKey.class);
        if (AppDelegate.shouldLimitLinkaAccessToUserID) {
            String userID = LinkaAPIServiceImpl.getUserID();
            if (userID == null) {
                from = from.where("linka_lock_address = ? AND is_valid = 1 AND api_user_id = ?", linka.getMACAddress(), "UNDEFINED");
            } else {
                from = from.where("linka_lock_address = ? AND is_valid = 1 AND api_user_id = ?", linka.getMACAddress(), userID);
            }
        } else {
            from = from.where("linka_lock_address = ? AND is_valid = 1", linka.getMACAddress());
        }
        return from.execute();
    }





    public static LinkaAccessKey getKeyFromLinkaLockAddress(String linka_lock_address) {
        From from = new Select().from(LinkaAccessKey.class);
        if (AppDelegate.shouldLimitLinkaAccessToUserID) {
            String userID = LinkaAPIServiceImpl.getUserID();
            if (userID == null) {
                from = from.where("linka_lock_address = ? AND is_valid = 1 AND api_user_id = ?", linka_lock_address, "UNDEFINED");
            } else {
                from = from.where("linka_lock_address = ? AND is_valid = 1 AND api_user_id = ?", linka_lock_address, userID);
            }
        } else {
            from = from.where("linka_lock_address = ? AND is_valid = 1", linka_lock_address);
        }
        return from.executeSingle();
    }

    public static LinkaAccessKey getKeyFromKeyId(String keyId) {
        From from = new Select().from(LinkaAccessKey.class);
        if (AppDelegate.shouldLimitLinkaAccessToUserID) {
            String userID = LinkaAPIServiceImpl.getUserID();
            if (userID == null) {
                from = from.where("key_id = ? AND is_valid = 1 AND api_user_id = ?", keyId, "UNDEFINED");
            } else {
                from = from.where("key_id= ? AND is_valid = 1 AND api_user_id = ?", keyId, userID);
            }
        } else {
            from = from.where("key_id = ? AND is_valid = 1", keyId);
        }
        return from.executeSingle();
    }



    public static void deleteAllKeysFromLinka(Linka linka) {
        From from = new Delete().from(LinkaAccessKey.class);
        if (AppDelegate.shouldLimitLinkaAccessToUserID) {
            String userID = LinkaAPIServiceImpl.getUserID();
            if (userID == null) {
                from = from.where("linka_lock_address = ? AND is_valid = 1 AND api_user_id = ?", linka.getMACAddress(), "UNDEFINED");
            } else {
                from = from.where("linka_lock_address = ? AND is_valid = 1 AND api_user_id = ?", linka.getMACAddress(), userID);
            }
        } else {
            from = from.where("linka_lock_address = ? AND is_valid = 1", linka.getMACAddress());
        }
        from.execute();
    }





    public boolean isAdmin()
    {
        if (!access_key_admin.equals("") && !access_key_admin_2.equals(""))
        {
            return true;
        }
        return false;
    }

    public boolean isUser()
    {
        if (!access_key_user.equals("") && !access_key_user_2.equals(""))
        {
            return true;
        }
        return false;
    }




    public static LinkaAccessKey createNewOrReplaceKey(Linka linka, AccessKeysResponse.Data key, String linka_lock_address) {
        if (key == null) {
            return new LinkaAccessKey();
        }

        LinkaAccessKey existingAccessKey = null;
        if (linka != null) {
            existingAccessKey = LinkaAccessKey.getKeyFromLinka(linka);
        } else if (linka_lock_address != null) {
            existingAccessKey = LinkaAccessKey.getKeyFromLinkaLockAddress(linka_lock_address);
        }

        if (existingAccessKey != null) {
            // Bug 141 - Remove master keys from phone, they should only ever be stored on server
            // Admin keys are sufficient for all access
            //if (key.access_key_master != null) { existingAccessKey.access_key_master = key.access_key_master; }
            //if (key.access_key_master_2 != null) { existingAccessKey.access_key_master_2 = key.access_key_master_2; }
            existingAccessKey.access_key_master = ""; // For existing users
            existingAccessKey.access_key_master_2 = ""; // For existing users
            if (key._id != null) { existingAccessKey.key_id = key._id; }
            if (key.access_key_user != null) { existingAccessKey.access_key_user = key.access_key_user; }
            if (key.access_key_admin_2 != null) { existingAccessKey.access_key_admin_2 = key.access_key_admin_2; }
            if (key.access_key_user_2 != null) { existingAccessKey.access_key_user_2 = key.access_key_user_2; }
            // Version 2
            if (key.v2_access_key_admin != null) { existingAccessKey.v2_access_key_admin = key.v2_access_key_admin; }
            if (key.v2_access_key_admin_2 != null) { existingAccessKey.v2_access_key_admin_2 = key.v2_access_key_admin_2; }
            if (key.v2_access_key_user != null) { existingAccessKey.v2_access_key_user = key.v2_access_key_user; }
            if (key.v2_access_key_user_2 != null) { existingAccessKey.v2_access_key_user_2 = key.v2_access_key_user_2; }

            existingAccessKey.is_valid = key.is_valid;
            existingAccessKey.is_need_reset_master_key = key.is_need_reset_master_key;
            if (key.is_reactivated_from_key_id != null) { existingAccessKey.is_reactivated_from_key_id = key.is_reactivated_from_key_id; } else { existingAccessKey.is_reactivated_from_key_id = ""; }
            if (key.lock_id != null) { existingAccessKey.lock_id = key.lock_id; }
            if (key.userProfile_id != null) { existingAccessKey.userProfile_id = key.userProfile_id; }
            if (key.owner != null) { existingAccessKey.api_user_id = key.owner; }
            existingAccessKey.save();
            return existingAccessKey;
        } else {
            LinkaAccessKey accessKey = new LinkaAccessKey();
            if (key._id != null) { accessKey.key_id = key._id; }
            if (key.access_key_master != null) { accessKey.access_key_master = key.access_key_master; }
            if (key.access_key_master_2 != null) { accessKey.access_key_master_2 = key.access_key_master_2; }
            if (key.access_key_admin != null) { accessKey.access_key_admin = key.access_key_admin; }
            if (key.access_key_user != null) { accessKey.access_key_user = key.access_key_user; }
            if (key.access_key_admin_2 != null) { accessKey.access_key_admin_2 = key.access_key_admin_2; }
            if (key.access_key_user_2 != null) { accessKey.access_key_user_2 = key.access_key_user_2; }
            if (key.access_key_admin != null) { accessKey.access_key_admin = key.access_key_admin; }
            if (key.access_key_user != null) { accessKey.access_key_user = key.access_key_user; }
            accessKey.is_valid = key.is_valid;
            accessKey.is_need_reset_master_key = key.is_need_reset_master_key;
            if (key.is_reactivated_from_key_id != null) { accessKey.is_reactivated_from_key_id = key.is_reactivated_from_key_id; } else { accessKey.is_reactivated_from_key_id = ""; }
            if (key.lock_id != null) { accessKey.lock_id = key.lock_id; }
            if (key.userProfile_id != null) { accessKey.userProfile_id = key.userProfile_id; }
            if (key.owner != null) { accessKey.api_user_id = key.owner; }

            // If the admin or user keys exist then wipe the master keys so they aren't saved locally
            // This is for security reasons. Bug 141
            // Only need to check for existence of user, since both cases contain user keys
            if (key.access_key_user != null) {
                accessKey.access_key_master = "";
                accessKey.access_key_master_2 = "";
            }

            if (linka != null) {
                accessKey.linka_lock_address = linka.getMACAddress();
            } else if (linka_lock_address != null) {
                accessKey.linka_lock_address = linka_lock_address;
            }

            accessKey.save();
            return accessKey;
        }

//        return null;
    }







    // RUN THIS CALL WHEN EVERY BACKGROUND - FOREGROUND
//    class func validateExistingAccessKey(linka : Linka) {
//        let existingAccessKey = LinkaAccessKey.getKeyFromLinka(linka)
//
//        if (existingAccessKey != nil) {
//
//            LinkaAPIService.obtain_access_token_from_access_token(
//                    linka,
//                    access_key_public: existingAccessKey.access_key_public,
//                    completion: { (responseObject, error) in
//
//                let _existingAccessKey = LinkaAccessKey.getKeyFromLinka(linka)
//
//                if error != nil {
//                    _existingAccessKey.is_valid = false
//                } else if responseObject != nil {
//                    _existingAccessKey.is_valid = true
//                }
//
//                let realm = try! Realm()
//                try! realm.write {
//                    realm.add(_existingAccessKey, update: true)
//                }
//            })
//
//        } else {
//            return
//        }
//    }


    public interface AccessKeyCallback {
        public void onDone(LinkaAccessKey accessKey, boolean isValid);
    }

















    public interface LinkaAccessKeyCallback
    {
        void onObtain(LinkaAccessKey accessKey, boolean isValid, boolean showError, int code);
    }

    public interface LinkaAccessKeyDetailedErrorCallback
    {
        void onObtain(LinkaAccessKey accessKey, boolean isValid, boolean showError, int code, String error);
    }




    // RUN THIS CALL WHEN PAIRING UP TO CREATE ACCESS KEY
    public static void createAccessKey(final Context context, final Linka linka, final LinkaAccessKeyCallback callback)
    {
        // check for lock key status
        // if != valid user
        //    if has admin
        //        register new access token without parameters
        //        callback with response
        //    else
        //        call to fetch master keys
        //        try to pair + setup master keys with the Lock
        //        upon completion of setting up master keys, save master key into LinkaAccessKey database
        //        register new access token with master keys
        //        callback with response

        final LinkaAccessKey existingAccessKey = LinkaAccessKey.getKeyFromLinka(linka);
        LinkaAPIServiceImpl.check_key_status_for_user(
                context,
                linka.lock_mac_address,
                new Callback<CheckKeyStatusForUserResponse>() {
                    @Override
                    public void onResponse(Call<CheckKeyStatusForUserResponse> call, Response<CheckKeyStatusForUserResponse> response) {
                        if (!LinkaAPIServiceImpl.check(response, false, context))
                        {
                            if (existingAccessKey != null)
                            {
                                callback.onObtain(
                                        existingAccessKey,
                                        existingAccessKey.is_valid,
                                        true,
                                        10200
                                );
                                return;
                            }
                            else
                            {
                                callback.onObtain(
                                        null,
                                        false,
                                        true,
                                        10400
                                );
                                return;
                            }
                        }
                        else
                        {
                            CheckKeyStatusForUserResponse body = response.body();
                            if (body.data.ownsKey)
                            {
                                if (body.data.isOwner)
                                {

                                        // if not the same master key / not having the key at all =>

                                        LinkaAccessKey key = LinkaAccessKey.createNewOrReplaceKey(
                                                linka,
                                                body.data.key,
                                                null
                                        );

                                        callback.onObtain(
                                                key,
                                                key.is_valid,
                                                true,
                                                10202
                                        );

                                        return;

                                }
                                else
                                {

                                    // if not the same master key / not having the key at all =>

                                    LinkaAccessKey key = LinkaAccessKey.createNewOrReplaceKey(
                                            linka,
                                            body.data.key,
                                            null
                                    );

                                    callback.onObtain(
                                            key,
                                            key.is_valid,
                                            true,
                                            10204
                                    );

                                    return;
                                }
                            }

                            else
                            {
                                if (body.data.hasAdmin)
                                {
                                    LinkaAccessKey.callRegisterNewAccessToken(
                                            context,
                                            linka,
                                            null,
                                            callback);

                                    return;
                                }
                                else
                                {
                                    LinkaAccessKey.startLinkaResettingMasterKeys(
                                            context,
                                            linka,
                                            callback);

                                    return;
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<CheckKeyStatusForUserResponse> call, Throwable t) {
                        if (existingAccessKey != null)
                        {
                            callback.onObtain(
                                    existingAccessKey,
                                    existingAccessKey.is_valid,
                                    true,
                                    10402
                            );
                            return;
                        }
                        else
                        {
                            callback.onObtain(
                                    null,
                                    false,
                                    true,
                                    10403
                            );
                            return;
                        }
                    }
                });
    }





    public static void callRegisterNewAccessToken(final Context context, final Linka linka, LinkaAccessKey linkaAccessKey, final LinkaAccessKeyCallback callback)
    {
        LinkaAPIServiceImpl.register_lock_with_master_keys(
                context,
                linka,
                linkaAccessKey,
                new Callback<AccessKeysResponse>() {
                    @Override
                    public void onResponse(Call<AccessKeysResponse> call, Response<AccessKeysResponse> response) {
                        if (LinkaAPIServiceImpl.check(response, false, context))
                        {
                            AccessKeysResponse body = response.body();

                            if (body.data != null)
                            {
                                LinkaAccessKey accessKey = LinkaAccessKey.createNewOrReplaceKey(
                                        linka,
                                        body.data,
                                        null
                                );

                                if (accessKey != null)
                                {
                                    callback.onObtain(
                                            accessKey,
                                            accessKey.is_valid,
                                            true,
                                            10404
                                    );
                                    return;
                                }
                            }

                            callback.onObtain(
                                    null,
                                    false,
                                    true,
                                    10405
                            );
                            return;
                        }

                        callback.onObtain(
                                null,
                                false,
                                true,
                                10406
                        );
                        return;
                    }

                    @Override
                    public void onFailure(Call<AccessKeysResponse> call, Throwable t) {
                        callback.onObtain(
                                null,
                                false,
                                true,
                                10407
                        );
                        return;
                    }
                }
        );
    }


    public static void startLinkaResettingMasterKeys(final Context context, final Linka linka, final LinkaAccessKeyCallback callback)
    {
        LinkaAPIServiceImpl.gen_master_key(
                context,
                linka,
                new Callback<GenMasterKeyResponse>() {
                    @Override
                    public void onResponse(Call<GenMasterKeyResponse> call, Response<GenMasterKeyResponse> response) {
                        LinkaAccessKey key = null;
                        if (LinkaAPIServiceImpl.check(response, false, context)) {
                            key = new LinkaAccessKey();
                            GenMasterKeyResponse body = response.body();
                            key.access_key_master = body.data.access_key_master;
                            key.access_key_master_2 = body.data.access_key_master_2;
                            key.access_key_origin = body.data.access_key_origin;
                            key.access_key_origin_v2 = body.data.access_key_origin_v2;

                            if (key != null) {

                                final LinkaAccessKey finalKey = key;
                                LockPairingController lockPairingController = new LockPairingController(
                                        context,
                                        linka,
                                        LocksController.getInstance().lockBLEServiceProxy,
                                        new LockPairingController.LockPairingControllerCallback() {
                                            @Override
                                            public void onConnect(final LockPairingController _lockPairingController) {

                                                Handler handler = new Handler();
                                                handler.postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        LinkaAccessKey.startLinkaResettingMasterKeys_OnConnected(
                                                                context,
                                                                _lockPairingController,
                                                                linka,
                                                                callback,
                                                                finalKey
                                                        );
                                                    }
                                                }, 1500);
                                            }
                                        }
                                );
                                lockPairingController.initialize(true);
                            }
                        } else {
                            callback.onObtain(null, false, true, 10408);
                            return;
                        }
                    }

                    @Override
                    public void onFailure(Call<GenMasterKeyResponse> call, Throwable t) {
                        callback.onObtain(null, false, true, 10409);
                        return;
                    }
                }
        );
    }


    static boolean isWaitingToResetMasterKeys = false;
    static boolean isResetMasterKeysTerminated = false;



    public static void startLinkaResettingMasterKeys_OnConnected(
            final Context context,
            final LockPairingController lockController,
            final Linka linka,
            final LinkaAccessKeyCallback callback,
            final LinkaAccessKey key
    )
    {
        String _first_step = key.access_key_master_2;
        String _second_step = key.access_key_master;

        if (lockController != null)
        {
            LinkaAccessKey existingAccesskey = LinkaAccessKey.getKeyFromLinka(linka);
            if (existingAccesskey != null)
            {
                // If we already have a key, use the subkey to make the change
                // Since as of V2 we are no longer storing the master key locally
                // This code should be called during the 'User Revocation' flow
                String subKey;
                if (lockController.lockControllerBundle.isV2Lock) {
                    subKey = existingAccesskey.v2_access_key_admin;
                } else {
                    subKey = existingAccesskey.access_key_admin;
                }

                lockController.lockControllerBundle.setSubkey(LockEncV1.dataWithHexString(subKey), 0 , LockEncV1.PRIV_LEVEL.PRIV_ADMIN);
            }
            else
            {
                // Use the default key from the server
                // Based on which version of the lock we have
                String masterKey = "";

                if (lockController.lockControllerBundle.isV2Lock) {
                    masterKey = key.access_key_origin_v2;
                } else {
                    masterKey = key.access_key_origin;
                }

                lockController.lockControllerBundle.setKey(LockEncV1.unobscureKey(masterKey), 0 , LockEncV1.PRIV_LEVEL.PRIV_ADMIN);
            }

            if (lockController.lockControllerBundle.mLockEnc.getNeighbourKeyIndex() == 0)
            {
                _first_step = key.access_key_master;
                _second_step = key.access_key_master_2;
            }
        }

        final String first_step = _first_step;
        final String second_step = _second_step;


        isWaitingToResetMasterKeys = true;
        isResetMasterKeysTerminated = false;


        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isWaitingToResetMasterKeys) {
                    isResetMasterKeysTerminated = true;

                    lockController.deinitialize(null);
                    callback.onObtain(null, false, true, 10410);
                    return;
                }
            }
        }, 24000);


        Handler handler2 = new Handler();
        handler2.postDelayed(new Runnable() {
            @Override
            public void run() {

                boolean _ret = lockController.doAction_SetEncryptionKey(
                        LockEncV1.dataWithHexString(first_step),
                        new LockControllerSetEncryptionKeyLogic.LockControllerSetEncryptionKeyCallback() {
                            @Override
                            public void onComplete(boolean ret) {

                                if (isResetMasterKeysTerminated) {
                                    return;
                                }

                                isWaitingToResetMasterKeys = false;


                                if (ret) {
                                    LinkaAccessKey.callRegisterNewAccessToken(
                                            context,
                                            linka,
                                            key,
                                            new LinkaAccessKeyCallback() {
                                                @Override
                                                public void onObtain(final LinkaAccessKey accessKey, final boolean isValid, boolean showError, int code) {

                                                    if (accessKey == null) {

                                                        lockController.deinitialize(null);
                                                        callback.onObtain(null, false, true, code);
                                                        return;
                                                    }

                                                    final int keyIndex = lockController.lockControllerBundle.mLockEnc.getKeyIndex();
                                                    int neighbourKeyIndex = lockController.lockControllerBundle.mLockEnc.getNeighbourKeyIndex();

                                                    lockController.lockControllerBundle.setKey(
                                                            LockEncV1.dataWithHexString(first_step),
                                                            neighbourKeyIndex,
                                                            LockEncV1.PRIV_LEVEL.PRIV_ADMIN);

                                                    boolean _ret = lockController.doAction_SetEncryptionKey(
                                                            LockEncV1.dataWithHexString(second_step),
                                                            new LockControllerSetEncryptionKeyLogic.LockControllerSetEncryptionKeyCallback() {
                                                                @Override
                                                                public void onComplete(boolean ret) {

                                                                    if (ret) {

                                                                        lockController.lockControllerBundle.setKey(
                                                                                LockEncV1.dataWithHexString(second_step),
                                                                                keyIndex,
                                                                                LockEncV1.PRIV_LEVEL.PRIV_ADMIN);


                                                                        lockController.deinitialize(new LockPairingController.OnDisconnectCallback() {
                                                                            @Override
                                                                            public void onComplete() {
                                                                                callback.onObtain(accessKey, isValid, true, 10300);
                                                                            }
                                                                        });
                                                                        return;

                                                                    } else {


                                                                        lockController.deinitialize(null);
                                                                        callback.onObtain(null, false, true, 10412);
                                                                        return;

                                                                    }
                                                                }
                                                            });

                                                    if (!_ret) {

                                                        lockController.deinitialize(null);
                                                        callback.onObtain(null, false, true, 10413);
                                                        return;

                                                    }
                                                }
                                            });
                                } else {

                                    lockController.deinitialize(null);
                                    callback.onObtain(null, false, true, 10414);
                                    return;

                                }

                            }
                        });
            }
        }, 2500);
    }





    // RUN TO PREPARE PAIR UP DIALOGS AND CALLBACKS
    public static void tryRegisterLock(final Context context, final Linka linka, final LinkaAccessKeyDetailedErrorCallback callback)
    {
        LinkaAPIServiceImpl.upsert_lock(
                context,
                linka,
                new Callback<LinkaAPIServiceResponse>() {
                    @Override
                    public void onResponse(Call<LinkaAPIServiceResponse> call, Response<LinkaAPIServiceResponse> response) {
                        if (!LinkaAPIServiceImpl.check(response, false, context))
                        {
                            callback.onObtain(null, false, true, 10501, _.i(R.string.failed_to_pair_up_internet));
                            return;
                        }

                        LinkaAccessKey.createAccessKey(context, linka, new LinkaAccessKeyCallback() {
                            @Override
                            public void onObtain(LinkaAccessKey accessKey, boolean isValid, boolean showError, int code) {
                                if (accessKey != null)
                                {
                                    if (isValid)
                                    {
                                        callback.onObtain(accessKey, true, showError, 302, "");
                                        return;
                                    }
                                    else
                                    {
                                        callback.onObtain(accessKey, false, showError, code, "Invalid Error " + " (Error Code: " + code + ")");
                                        return;
                                    }
                                }
                                else
                                {
                                    callback.onObtain(null, false, showError, code, _.i(R.string.failed_to_pair_up_connection) + " (Error Code: " + code + ")");
                                    return;
                                }
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<LinkaAPIServiceResponse> call, Throwable t) {
                        callback.onObtain(null, false, true, 44404, _.i(R.string.failed_to_pair_up_internet));
                        return;
                    }
                }
        );
    }
}
