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
