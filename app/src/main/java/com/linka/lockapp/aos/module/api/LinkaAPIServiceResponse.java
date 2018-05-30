package com.linka.lockapp.aos.module.api;

import com.linka.lockapp.aos.module.model.Linka;
import com.linka.lockapp.aos.module.model.LinkaAccessKey;
import com.linka.lockapp.aos.module.model.LinkaActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vanson on 10/6/2016.
 */
public class LinkaAPIServiceResponse {
    public String status;
    public String message;


    public static boolean isSuccess(LinkaAPIServiceResponse responseData) {
        if (responseData == null) {
            return false;
        }

        if (responseData.status != null) {
            if (responseData.status.equals("success")) {
                return true;
            }
        }

        return false;
    }

    public static boolean isError(LinkaAPIServiceResponse responseData) {
        if (responseData == null) {
            return false;
        }

        if (responseData.status != null) {
            if (!responseData.status.equals("success")) {
                return true;
            }
        }

        return false;
    }

    public static boolean isNetworkError(LinkaAPIServiceResponse responseData) {
        if (responseData == null) {
            return true;
        }

        return false;
    }



    public static class RegisterResponse extends LinkaAPIServiceResponse {
        public class Data {
            public String _id;
        }
        public Data data;
    }



    public static class LoginResponse extends LinkaAPIServiceResponse {
        public class Data {
            public String authToken;
            public String userId;
            public String userEmail;
            public String first_name;
            public String last_name;
            public String name;
            public boolean showWalkthrough;
        }
        public Data data;
    }



    public static class GetEmailResponse extends LinkaAPIServiceResponse {
        public class Data {
            public String userEmail;
        }
        public Data data;
    }



    public static class UploadImageResponse extends LinkaAPIServiceResponse {
        public class Data {
            public String _id;
        }
        public Data data;
    }



    public static class AccessKeysResponse extends LinkaAPIServiceResponse {
        public static class Data {
            public String _id;
            public String lock_id;
            public String owner;
            public String userProfile_id;
            public boolean is_valid;
            public boolean is_need_reset_master_key;
            public String createdAt;
            public String modifiedAt;
            public String access_key_master;
            public String access_key_admin;
            public String access_key_user;
            public String access_key_master_2;
            public String access_key_admin_2;
            public String access_key_user_2;
            // V2 Lock
            public String v2_access_key_admin;
            public String v2_access_key_admin_2;
            public String v2_access_key_user;
            public String v2_access_key_user_2;
            public String is_reactivated_from_key_id;
            public String reactivation_key;


            public LinkaAccessKey makeLinkaAccessKey(Linka linka)
            {
                LinkaAccessKey accessKey = new LinkaAccessKey();
                if (access_key_master != null) { accessKey.access_key_master = access_key_master; }
                if (access_key_admin != null) { accessKey.access_key_admin = access_key_admin; }
                if (access_key_user != null) { accessKey.access_key_user = access_key_user; }
                if (access_key_master_2 != null) { accessKey.access_key_master_2 = access_key_master_2; }
                if (access_key_admin_2 != null) { accessKey.access_key_admin_2 = access_key_admin_2; }
                if (access_key_user_2 != null) { accessKey.access_key_user_2 = access_key_user_2; }
                // V2 Lock
                if (v2_access_key_admin != null) { accessKey.v2_access_key_admin = v2_access_key_admin; }
                if (v2_access_key_admin_2 != null) { accessKey.v2_access_key_admin_2 = v2_access_key_admin_2; }
                if (v2_access_key_user != null) { accessKey.v2_access_key_user = v2_access_key_user; }
                if (v2_access_key_user_2 != null) { accessKey.v2_access_key_user_2 = v2_access_key_user_2; }
                accessKey.is_valid = is_valid;
                accessKey.is_need_reset_master_key = is_need_reset_master_key;
                if (is_reactivated_from_key_id != null) { accessKey.is_reactivated_from_key_id = is_reactivated_from_key_id; } else { accessKey.is_reactivated_from_key_id = ""; }
                if (lock_id != null) { accessKey.lock_id = lock_id; }
                if (userProfile_id != null) { accessKey.userProfile_id = userProfile_id; }

                accessKey.linka_lock_address = linka.getMACAddress();
                return accessKey;
            }
        }

        public Data data;
    }



    public static class GenMasterKeyResponse extends LinkaAPIServiceResponse {
        public static class Data {
            public String access_key_master;
            public String access_key_master_2;
            public String access_key_origin;
            public String access_key_origin_v2;
        }

        public Data data;
    }


    public static class CheckKeyStatusForUserResponse extends LinkaAPIServiceResponse {
        public static class Data {
            public boolean ownsKey = false;
            public boolean isOwner = false;
            public boolean hasAdmin = false;
            public AccessKeysResponse.Data key;
        }

        public Data data;
    }



    public static class AppVersionResponse extends LinkaAPIServiceResponse {
        public class Data {
            public String version;
            public int build = 0;
        }
        public Data data;
    }




    public static class LockSingleResponse extends LinkaAPIServiceResponse {
        public class Data {
            public String _id;
            public String name;
            public String userProfile_id;
            public String serial_no;
            public double latitude = 0;
            public double longitude = 0;
            public boolean is_locked;
            public String createdAt;
            public String modifiedAt;
        }
        public Data data;
    }


    public static class LocksResponse extends LinkaAPIServiceResponse {
        public class Data {
            public String _id;
            public String name;
            public String userProfile_id;
            public String serial_no;
            public double latitude = 0;
            public double longitude = 0;
            public boolean is_locked;
            public String createdAt;
            public String modifiedAt;
        }
        public List<Data> data = new ArrayList<>();
    }


    public static class AssociatedLocksResponse extends LinkaAPIServiceResponse {
        public class Data {
            public String _id;
            public String lock_serial_no;
            public String name;
            public boolean isOwner;
        }
        public List<Data> data = new ArrayList<>();
    }


    public static class LockPermissionsResponse extends LinkaAPIServiceResponse {
        public class Data {
            public String name;
            public String first_name;
            public String last_name;
            public String email;
            public boolean owner;
            public boolean isPendingApproval;
        }
        public List<Data> data = new ArrayList<>();
    }




    public static class ActivitiesResponse extends LinkaAPIServiceResponse {
        public class Data {
            public String userProfile_id;
            public String lock_id;
            public String record_date;
            public int linka_activity_status = 0;
            public int batteryPercent = 0;
            public String timestamp;
            public String timestamp_locked;
            public String msg_title;
            public String msg_desc;
            public double latitude = 0;
            public double longitude = 0;
            public String createdAt;
            public String modifiedAt;
            public String owner;
            public boolean removed = false;
            public String removedAt;

            public LinkaActivity makeLinkaActivity(Linka linka)
            {
                LinkaActivity activity = new LinkaActivity();
                if (latitude == 0 && longitude == 0)
                {
                    activity.latitude = "";
                    activity.longitude = "";
                }
                else
                {
                    activity.latitude = latitude + "";
                    activity.longitude = longitude + "";
                }
                activity.lock_name = linka.getName();
                activity.lock_address = linka.getUUIDAddress();
                activity.linka_id = linka.getId();
                activity.linka_activity_status = linka_activity_status;
                if (timestamp != null) { activity.timestamp = timestamp; };
                if (timestamp_locked != null) { activity.timestamp_locked = timestamp_locked; };
                activity.batteryPercent = batteryPercent;
                return activity;
            }
        }
        public List<Data> data = new ArrayList<>();
    }
}

