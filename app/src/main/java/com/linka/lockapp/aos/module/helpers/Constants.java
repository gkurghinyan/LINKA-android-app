package com.linka.lockapp.aos.module.helpers;

/**
 * Created by Vanson on 18/2/16.
 */

/*
NOTE

THIS FUNCTION IS NOT USED



THIS FUNCTION IS NOT USED



NOTE
 */

public class Constants {
    public static final String SHOWING_FRAGMENT = "ShowingFragment";
    public static final int LAUNCHER_FRAGMENT = 0;
    public static final int SET_NAME_FRAGMENT = 1;
    public static final int SET_PAC_FRAGMENT = 2;
    public static final int TUTORIAL_FRAGMENT = 3;
    public static final int TUTORIAL_MOUNT_FRAGMENT = 4;
    public static final int DONE_FRAGMENT = 5;

    public enum NotificationType {

        //TODO: try to get this as capitalized. Currently doesn't work with all caps
        alert("alert"),
        warning("warning"),
        notification("notification");

        private String stringValue;

        NotificationType(String stringVal) {
            stringValue = stringVal;
        }

        @Override
        public String toString() {
            return stringValue;
        }

    }

}
