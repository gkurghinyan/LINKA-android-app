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

##### Lock.BLE
-keep class com.linka.Lock.BLE.BluetoothLEDevice {
  public *;
}

-keep class com.linka.Lock.BLE.BluetoothLEDevice$AdRecord {
  *;
}

-keep class com.linka.Lock.BLE.BluetoothLeQueuedService {
  public *;
}

-keep class com.linka.Lock.BLE.BluetoothLeQueuedService$BluetoothGattQueuedActions {
  *;
}

-keep class com.linka.Lock.BLE.BluetoothLeService {
  public *;
}

-keep class com.linka.Lock.BLE.BluetoothLeQueuedService$GenericBluetoothGattCallback {
  *;
}

##### Lock.FirmwareAPI
-keep class com.linka.Lock.FirmwareAPI.LINKA_BLE_Service {
  *;
}

-keep class com.linka.Lock.FirmwareAPI.LINKA_BLE_Service$LocalBinder {
  *;
}

-keep class com.linka.Lock.FirmwareAPI.LINKA_BLE_Service$BluetoothGattCharacteristicBundle {
  *;
}

-keep class com.linka.Lock.FirmwareAPI.Comms.LockAdV1 {
  public *;
}

-keep class com.linka.Lock.FirmwareAPI.Comms.LockStatusPacket {
  public *;
}

-keep class com.linka.Lock.FirmwareAPI.Comms.LockInfoPacket {
  public *;
}

-keep class com.linka.Lock.FirmwareAPI.Comms.LockContextPacket {
  public *;
}

-keep class com.linka.Lock.FirmwareAPI.Comms.LockSettingPacket {
  public *;
}

-keep class com.linka.Lock.FirmwareAPI.Comms.LINKAGattAttributes {
  public *;
}

-keep class com.linka.Lock.FirmwareAPI.Comms.LockAckNakPacket {
  public *;
}

-keep class com.linka.Lock.FirmwareAPI.Comms.LockEncV1 {
  public *;
}

-keep class com.linka.Lock.FirmwareAPI.Comms.LockEncV1$KEY_PART {
  public *;
}

-keep class com.linka.Lock.FirmwareAPI.Comms.LockEncV1$KEY_SLOT {
  public *;
}

-keep class com.linka.Lock.FirmwareAPI.Comms.LockEncV1$PRIV_LEVEL {
  public *;
}

-keep class com.linka.Lock.FirmwareAPI.Comms.LockCommand {
  public *;
}

-keep class com.linka.Lock.FirmwareAPI.Comms.LockDataPacket {
  public *;
}

-keep class com.linka.Lock.FirmwareAPI.Debug.NrfUartService {
  public *;
}

-keep class com.linka.Lock.FirmwareAPI.Types.** {
  public *;
}

-keep class com.linka.Lock.Utility.DebugHelper {
  public *;
}

-keep class com.linka.Lock.Utility.LockLogDBHelper {
  public *;
}

