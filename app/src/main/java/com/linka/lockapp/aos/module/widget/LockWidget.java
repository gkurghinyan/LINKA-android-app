package com.linka.lockapp.aos.module.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.support.annotation.LayoutRes;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.lzyzsd.circleprogress.Utils;
import com.linka.lockapp.aos.AppDelegate;
import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.i18n._;
import com.linka.lockapp.aos.module.model.Linka;

import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Vanson on 18/2/16.
 */
public class LockWidget extends RelativeLayout {
    private static final int LOCK_MECHANISM_ROTATION_STATE_LOCKED = -70;
    //    private static final int LOCK_MECHANISM_ROTATION_STATE_LOCKED_WITH_ARROWS = 73;
//    private static final int LOCK_MECHANISM_ROTATION_STATE_UNLOCKED = 0;
    private static final int LOCK_MECHANISM_ROTATION_STATE_LOCKED_WITH_ARROWS = 0;
    private static final int LOCK_MECHANISM_ROTATION_STATE_UNLOCKED = 73;

    private static final String TAG = "LOCKWIDGET";
    private final Context mContext;
    @BindView(R.id.lock_low_battery_icon)
    ImageView lowBatteryIcon; //ImageView for connection icon
    @BindView(R.id.lock_battery_percent_remaining)
    TextView batteryPercentRemaining;//TextView for percent battery
    @BindView(R.id.lock_battery_days_remaining)
    TextView batteryDaysRemaining; //textview for battery days remaining
    @BindView(R.id.arc_progress)
    ArcProgress arcProgress; //ArcProgress for batterylife
    @BindView(R.id.lock_ring)
    ImageView lockRing;  //ImageView for lock ring
    @BindView(R.id.lock_ring_mechanism)
    ImageView lockMechanism;  //ImageView for lock ring
    @BindView(R.id.locking_status_text)
    LinkaTextView lockingStatusText;
    @BindView(R.id.locking_status_layout)
    LinearLayout lockingStatusLayout;
    @BindView(R.id.center_content)
    LinearLayout centerContent;

    private boolean isLocked;
    private boolean isLocking;
    private boolean isUnlocking;
    private int blueColor = R.color.linka_blue;
    private int redColor = R.color.linka_red;
    private int orangeColor = R.color.linka_orange;

    public LockWidget(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;

        TypedArray a = mContext.obtainStyledAttributes(attrs, R.styleable.LockWidget, 0, 0);
        Integer batteryPercent = a.getInteger(R.styleable.LockWidget_lock_battery_percent, 0);

        a.recycle();

        loadLayout(R.layout.widget_linka_lock);

        setWillNotDraw(false); // forces onDraw to be called
    }

    public LockInteractListener lockInteractListener;

    public interface LockInteractListener {
        public void onLockRingClick();
    }

    @OnClick(R.id.lock_ring)
    void onLockRingClick() {
        if (lockInteractListener != null) {
            lockInteractListener.onLockRingClick();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Resources res = getResources();

        // Ratio of size between the lock ring image and the arcprogress
        Float widthRatio = 0.70f;

        float width = lockRing.getWidth();
//        float measuredWidth = lockRing.getMeasuredWidth();
//        BitmapDrawable bd=(BitmapDrawable) lockRing.getDrawable();
//        int imageWidth=bd.getBitmap().getWidth();
//        imageWidth = imageWidth / (int)getResources().getDisplayMetrics().density;
//        Log.d("LockWidget", "width= " + width + " measuredWidth=" + measuredWidth + " imageWidth= " + imageWidth);

        float strokeAdjustment = Utils.dp2px(getResources(), arcProgress.getStrokeWidth());
        int arcWidth = Math.round(width * widthRatio);
        int adjustedWidth = arcWidth - Math.round(strokeAdjustment);

        //Log.d("LockWidget", "arcWidth= " + arcWidth + " adjustedWidth=" + adjustedWidth);

        ViewGroup.LayoutParams layoutParams = arcProgress.getLayoutParams();
        layoutParams.width = adjustedWidth;
        layoutParams.height = adjustedWidth;

        arcProgress.setLayoutParams(layoutParams);

        super.onDraw(canvas);

    }

    public void setLinka(Linka linka) {
        if (linka == null || !linka.isRecorded) {
            lowBatteryIcon.setVisibility(INVISIBLE);
            batteryDaysRemaining.setVisibility(INVISIBLE);
            batteryPercentRemaining.setVisibility(INVISIBLE);
            lockingStatusLayout.setVisibility(INVISIBLE);
            arcProgress.setVisibility(INVISIBLE);
        } else {
            lowBatteryIcon.setVisibility(VISIBLE);
            batteryDaysRemaining.setVisibility(VISIBLE);
            batteryPercentRemaining.setVisibility(VISIBLE);
            arcProgress.setVisibility(INVISIBLE);

            setBatteryPercent(linka.batteryPercent);
            setBatteryDaysRemaining(linka.getBatteryRemainingRepresentation(linka.settings_unlocked_sleep, linka.settings_locked_sleep), linka.isCharging, linka.batteryPercent);
            setIsLocked(linka.isLocked);
            setLockingStatus(linka.isLocking, linka.isUnlocking);
        }
    }

    int random(int min, int max){
        Random rn = new Random();
        return  rn.nextInt(max - min + 1) + min;
    }


    public void setIsLocked(boolean isLocked) {
        this.isLocked = isLocked;
        int rotation = LOCK_MECHANISM_ROTATION_STATE_UNLOCKED;
        if (isLocked) {
            rotation = LOCK_MECHANISM_ROTATION_STATE_LOCKED_WITH_ARROWS;
        }

//        Log.d(TAG, "setIsLocked: " + isLocked + " rotation:" + rotation);

        lockMechanism.setRotation(rotation);

//        lockMechanism.setRotation((isLocked) ? LOCK_MECHANISM_ROTATION_STATE_UNLOCKED :LOCK_MECHANISM_ROTATION_STATE_LOCKED_WITH_ARROWS );
    }

    public void setLockingStatus(boolean isLocking, boolean isUnlocking) {
        if (isLocking || isUnlocking) {
            lockingStatusLayout.setVisibility(VISIBLE);
        } else {
            lockingStatusLayout.setVisibility(INVISIBLE);
        }

        if (isLocking) {
            lockingStatusText.setText(R.string.locking);
        } else if (isUnlocking) {
            lockingStatusText.setText(R.string.unlocking);
        }
    }

    public void setBatteryPercent(Integer batteryPercent) {
        batteryPercentRemaining.setTextColor(getResources().getColor(blueColor));
        if (batteryPercent < AppDelegate.battery_mid && batteryPercent >= AppDelegate.battery_low_below){
            lowBatteryIcon.setImageResource(R.drawable.icon_activity_battery_mid);
        } else if (batteryPercent < AppDelegate.battery_critically_low_below) {
            lowBatteryIcon.setImageResource(R.drawable.icon_activity_battery_low_critical);
            batteryPercentRemaining.setTextColor(getResources().getColor(redColor));
        } else if (batteryPercent < AppDelegate.battery_low_below && batteryPercent >= AppDelegate.battery_critically_low_below) {
            lowBatteryIcon.setImageResource(R.drawable.icon_activity_battery_low);
            batteryPercentRemaining.setTextColor(getResources().getColor(orangeColor));
        } else {
            lowBatteryIcon.setImageResource(R.drawable.icon_activity_battery_high);
        }

        batteryPercentRemaining.setText(getResources().getString(R.string.battery_percent, batteryPercent));
        arcProgress.setProgress(batteryPercent);
    }

    public void setBatteryDaysRemaining(String representation, boolean isCharging, int batteryPercent) {
        if(isCharging) {
            if(batteryPercent == 100) {
                batteryDaysRemaining.setText(_.i(R.string.charged));
            }
            else{
                batteryDaysRemaining.setText(_.i(R.string.charging));
            }
        }
         else{   batteryDaysRemaining.setText(representation + " " + _.i(R.string.remaining));
        }
    }

    protected void loadLayout(@LayoutRes int resLayoutId) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(resLayoutId, this, true);
        ButterKnife.bind(this);
    }

    public void setLowBattery(boolean lowBattery) {
    }

}
