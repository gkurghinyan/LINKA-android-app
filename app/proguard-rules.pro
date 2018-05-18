# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/Vanson/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# ActiveAndroid
-keep class com.activeandroid.** { *; }
-keep class com.activeandroid.**.** { *; }
-keep class * extends com.activeandroid.Model
-keep class * extends com.activeandroid.serializer.TypeSerializer

# Retrofit 2.X
## https://square.github.io/retrofit/ ##

-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

-dontwarn com.squareup.okhttp.**
-keep class com.squareup.okhttp.** { *;}
-dontwarn okio.

-dontwarn com.pixplicity.easyprefs.**
-keep class com.pixplicity.easyprefs.** { *;}

-keep class java.** { *; }
-keep class javax.** { *; }
-keep class android.** { *; }
-keep class org.apache.** { *; }

-dontwarn java.**
-dontwarn javax.**
-dontwarn android.**
-dontwarn org.apache.**


-dontwarn com.opencsv.**
-keep class com.opencsv.** { *;}

-dontwarn butterknife.**
-keep class butterknife.** { *;}

-dontwarn com.malinskiy.superrecyclerview.SwipeDismissRecyclerViewTouchListener*
-keep class com.malinskiy.superrecyclerview.SwipeDismissRecyclerViewTouchListener* { *;}

-dontwarn com.marshalchen.**
-keep class com.marshalchen.** { *;}

-dontwarn org.codehaus.mojo.**
-keep class org.codehaus.mojo.** { *;}

-dontwarn org.springframework.**
-keep class org.springframework.** { *;}

-dontwarn sun.misc.**
-keep class sun.misc.** { *;}

## GreenRobot EventBus specific rules ##
# https://github.com/greenrobot/EventBus/blob/master/HOWTO.md#proguard-configuration

-keepclassmembers class ** {
    public void onEvent*(***);
}

# Only required if you use AsyncExecutor
-keepclassmembers class * extends de.greenrobot.event.util.ThrowableFailureEvent {
    public <init>(java.lang.Throwable);
}

# Don't warn for missing support classes
-dontwarn de.greenrobot.event.util.*$Support
-dontwarn de.greenrobot.event.util.*$SupportManagerFragment



-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclassmembers class * extends java.lang.Enum {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

-keepattributes InnerClasses


-keep class com.linka.lockapp.aos.* {
  *;
}

##### adapters
-keep class com.linka.lockapp.aos.module.adapters.** {
  *;
}

##### api
-keep class com.linka.lockapp.aos.module.api.** {
  *;
}

##### core
-keep class com.linka.lockapp.aos.module.core.** {
  public *;
}

##### gcm
-keep class com.linka.lockapp.aos.module.gcm.** {
  public *;
}

##### i18n
-keep class com.linka.lockapp.aos.module.i18n.** {
  public *;
}

##### model
-keep class com.linka.lockapp.aos.module.model.** {
  public *;
}

##### pages
-keep class com.linka.lockapp.aos.module.pages.** {
  public *;
}

##### helpers
-keep class com.linka.lockapp.aos.module.helpers.BLEHelpers {
  public *;
}

##### model
-keep class com.linka.lockapp.aos.module.model.** {
  public *;
}

##### widget
-keep class com.linka.lockapp.aos.module.widget.** {
  public *;
}

-keep class com.linka.lockapp.aos.module.widget.LockBLEGenericListener {
  public *;
}

-keep class com.linka.lockapp.aos.module.widget.LockBLEServiceListener {
  public *;
}

-keep class com.linka.lockapp.aos.module.widget.LockGattUpdateReceiver {
  public *;
}

-keep class com.linka.lockapp.aos.module.widget.LockPairingController {
  public *;
}

-keep class com.linka.lockapp.aos.module.widget.LocksController {
  public *;
}

-keep class com.linka.lockapp.aos.module.widget.LockController {
  public *;
}

-keep class com.linka.lockapp.aos.module.widget.LockController$LinkaBLECommunicationManager {
  public *;
}

-keep class com.linka.lockapp.aos.module.widget.LockController$LinkaBLEConnectionManager {
  public *;
}

-keep class com.linka.lockapp.aos.module.widget.LockController$OnRefreshListener {
  public *;
}

-keep class com.linka.lockapp.aos.module.widget.LockController$LockControllerPacketCallback {
  public *;
}

### model

### LogHelper

-keep class com.linka.lockapp.aos.module.helpers.LogHelper {
  public *;
}

-keep class com.linka.lockapp.aos.module.helpers.LogHelper$LogLevel {
  public *;
}
