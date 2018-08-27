package com.linka.lockapp.aos.module.adapters;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.model.LinkaActivity;
import com.linka.lockapp.aos.module.model.LinkaActivity.LinkaActivityType;
import com.linka.lockapp.aos.module.model.Notification;
import com.linka.lockapp.aos.module.widget.GenericLoadMoreAdapter;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Vanson on 18/2/16.
 */
public class NotificationListAdapter extends GenericLoadMoreAdapter<NotificationListAdapter.ViewHolder> {
    public static final String UPDATE_NOTIFICATIONS_COUNT = "UpdateNotificationsCount";

    private List<Notification> mItems;
    private View.OnClickListener mOnclickListener;
    public Context context;


    public NotificationListAdapter(Context context) {
        super(context);
        this.context = context;
        mItems = new ArrayList<>();
        setItemCountSource(mItems);
    }

    public void setList(List<Notification> models) {
        // SET REFERENCE
        this.mItems.clear();
        for (Notification item : models) {
            this.mItems.add(item);
        }


        // SEARCH FILTER / SORT


        // DISPLAY
    }

    public void setOnclickListener(View.OnClickListener onclickListener) {
        this.mOnclickListener = onclickListener;
    }

    @Override
    public ViewHolder onCreateYourViewHolder(ViewGroup parent) {
        View v = LayoutInflater.from(
                parent.getContext()).inflate(R.layout.list_item_notification, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindYourViewHolder(ViewHolder holder, int position) {

        Notification item = mItems.get(position);

        holder.notification.setText(item.body);
        holder.time.setText(item.time);
        int icon = 0;

        if (item.type == LinkaActivityType.isLocked) {
            holder.notificationIcon.setColorFilter(context.getResources().getColor(R.color.linka_blue), android.graphics.PorterDuff.Mode.MULTIPLY);
            icon = R.drawable.close_white_linka;
        } else if (item.type == LinkaActivityType.isUnlocked || item.type == LinkaActivityType.isAutoUnlocked) {
            holder.notificationIcon.setColorFilter(context.getResources().getColor(R.color.linka_blue), android.graphics.PorterDuff.Mode.MULTIPLY);
            icon = R.drawable.open_white_linka;
        } else if (item.type == LinkaActivityType.isTamperAlert) {
            holder.notificationIcon.setColorFilter(context.getResources().getColor(R.color.linka_blue), android.graphics.PorterDuff.Mode.MULTIPLY);
            icon = R.drawable.panic_icon;
        } else if (item.type == LinkaActivityType.isBatteryLow) {
            icon = R.drawable.danger_icon;
        } else if (item.type == LinkaActivityType.isBatteryCriticallyLow) {
            icon = R.drawable.danger_icon;
        } else if (item.type == LinkaActivityType.isAutoUnlockEnabled) {
            icon = R.drawable.danger_icon;
        }
        holder.container.setTag(item);
        holder.container.setOnClickListener(mOnclickListener);
        holder.notificationIcon.setImageResource(icon);
        if (mItems.get(position).isRead) {
            holder.container.setBackgroundColor(context.getResources().getColor(R.color.linka_white));
        } else {
            holder.container.setBackgroundColor(context.getResources().getColor(R.color.unread_notif_back_color));
        }
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    public void updateReadState() {
        EventBus.getDefault().post(UPDATE_NOTIFICATIONS_COUNT);
        List<Long> list = new ArrayList<>();
        for (Notification notification : mItems) {
            if (!notification.isRead) {
                list.add(notification.id);
            }
        }
        LinkaActivity.updateReadState(list);
    }

    public boolean isUnreadItemExist() {
        for (Notification notification : mItems) {
            if (!notification.isRead) {
                return true;
            }
        }
        return false;
    }


    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.root)
        ConstraintLayout container;
        @BindView(R.id.notification_icon)
        ImageView notificationIcon;
        @BindView(R.id.notification)
        TextView notification;
        @BindView(R.id.time)
        TextView time;

        Notification item;
        int position;
        View itemView;

        public ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            ButterKnife.bind(this, itemView);
        }
    }

}
