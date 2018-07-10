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
    //Constants for fragments name.Showing this fragment,if app have been closed in this moment.
    public static final String SHOWING_FRAGMENT = "ShowingFragment";
    public static final int LAUNCHER_FRAGMENT = 0;
    public static final int SET_NAME_FRAGMENT = 1;
    public static final int SET_PAC_FRAGMENT = 2;
    public static final int TUTORIAL_FRAGMENT = 3;
    public static final int TUTORIAL_MOUNT_FRAGMENT = 4;
    public static final int DONE_FRAGMENT = 5;

    //Constants for testing pages
    public static final String SHOW_TUTORIAL_WALKTHROUGH = "ShowTutorialWalkthrough";
    public static final String SHOW_SETUP_NAME = "ShowSetupName";
    public static final String SHOW_SETUP_PAC = "ShowSetupPac";

    //Constants for shared preferences
    public static final String LINKA_ADDRESS_FOR_AUTO_UNLOCK = "linka_address_for_auto_unlock";

    //Constants for extras name
    public static final String IS_IT_OPEN_FROM_NOTIFICATION = "IsItOpenFromNotification";

    //Constants for notifications identifiers
    public static final int UPDATE_AVAILABLE_NOTIFICATION = 41;
    public static final String OPEN_SETTINGS = "OpenSettings";

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
