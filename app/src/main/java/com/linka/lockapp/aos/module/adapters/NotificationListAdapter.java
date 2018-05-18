package com.linka.lockapp.aos.module.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.model.LinkaActivity.LinkaActivityType;
import com.linka.lockapp.aos.module.model.Notification;
import com.linka.lockapp.aos.module.widget.GenericLoadMoreAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Vanson on 18/2/16.
 */
public class NotificationListAdapter extends GenericLoadMoreAdapter<NotificationListAdapter.ViewHolder> {


    public List<Notification> mItems = new ArrayList<>();
    public View.OnClickListener mOnclickListener;

    public Context context;


    public NotificationListAdapter(Context context) {
        super(context);
        this.context = context;
        mItems = new ArrayList<>();
        setItemCountSource(mItems);
    }

    public void setList(List<Notification> models)
    {
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
            icon = R.drawable.icon_activity_locked;
        } else if (item.type == LinkaActivityType.isUnlocked) {
            icon = R.drawable.icon_activity_unlocked;
        } else if (item.type == LinkaActivityType.isTamperAlert) {
            icon = R.drawable.icon_activity_tamper;
        } else if (item.type == LinkaActivityType.isBatteryLow) {
            icon = R.drawable.icon_activity_battery_low;
        } else if (item.type == LinkaActivityType.isBatteryCriticallyLow) {
            icon = R.drawable.icon_activity_battery_low_critical;
        }
        holder.container.setTag(item);
        holder.container.setOnClickListener(mOnclickListener);
        holder.notificationIcon.setImageResource(icon);
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }



    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.container)
        View container;
        @BindView(R.id.notification_icon)
        ImageView notificationIcon;
        @BindView(R.id.notification)
        TextView notification;
        @BindView(R.id.time)
        TextView time;
        @BindView(R.id.message_item_content)
        LinearLayout messageItemContent;

        Notification item;
        int position;
        View itemView;

        public ViewHolder(View itemView)
        {
            super(itemView);
            this.itemView = itemView;
            ButterKnife.bind(this, itemView);
        }
    }

}
