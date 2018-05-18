package com.linka.lockapp.aos.module.api;

/**
 * Created by Vanson on 14/7/2016.
 */
public class LinkaAPIServiceJSON {
    public static class Register {
        public String email;
        public String password;
        public Profile profile = new Profile();

        public class Profile {
            public String first_name;
            public String last_name;
            public String name;
        }

        public String device_token;
        public String platform;
        public String device_name;
        public String os_version;
    }
}
