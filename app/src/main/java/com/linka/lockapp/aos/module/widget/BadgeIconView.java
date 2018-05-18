package com.linka.lockapp.aos.module.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.linka.lockapp.aos.R;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by van on 13/7/15.
 */
public class BadgeIconView extends RelativeLayout {

    @BindView(R.id.icon)
    public ImageView icon;
    @BindView(R.id.badge)
    public TextView badge;

    public boolean observe_notification_counter = false;

    public BadgeIconView(Context context) {
        super(context);
        init();
    }

    public BadgeIconView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        getAttrs(attrs);
    }

    public BadgeIconView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        getAttrs(attrs);
    }

    public void init() {
        LayoutInflater mInflater = LayoutInflater.from(getContext());
        View view = mInflater.inflate(R.layout.activity_main_navbar_icon, this, true);
        ButterKnife.bind(this,view);
        ButterKnife.setDebug(true);
    }

    public void getAttrs(AttributeSet attrs)
    {
        TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.BadgeIconView);
        int iconID = a.getResourceId(R.styleable.BadgeIconView_iconSrc, 0);
        icon.setAdjustViewBounds(true);
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        icon.setImageResource(iconID);

        String badgeText = a.getString(R.styleable.BadgeIconView_badgeText);
        setBadgeText(badgeText);

        a.recycle();
    }

    public void setBadgeText(String text)
    {
        if (text == null) text = "";
        if (text != null)
        {
            badge.setText(text);
        }

        if (text.equals("")
                || text.equals("0"))
        {
            badge.setVisibility(View.GONE);
        }
        else
        {
            badge.setVisibility(View.VISIBLE);
        }
    }


    public void onWakenUp() {
        super.onAttachedToWindow();
        if (observe_notification_counter) {
            //
            EventBus.getDefault().register(this);
        }
    }

    public void onNotifyAsleep() {
        EventBus.getDefault().unregister(this);
        super.onDetachedFromWindow();
    }

    public void onEvent(Object object)
    {
        if (object instanceof String)
        {
            if (object.equals("[NOTIFICATION_COUNT_UPDATE_COUNTER]"))
            {
    //
            }
        }
    }
}
