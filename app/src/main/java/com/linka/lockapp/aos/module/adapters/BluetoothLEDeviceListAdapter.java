package com.linka.lockapp.aos.module.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.model.Linka;
import com.linka.lockapp.aos.module.widget.LocksController;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Vanson on 21/2/16.
 */
public class BluetoothLEDeviceListAdapter extends RecyclerView.Adapter<BluetoothLEDeviceListAdapter.ViewHolder> {

    public List<Linka> mItems = new ArrayList<>();

    public Context context;

    public BluetoothLEDeviceListAdapter(Context context) {
        super();
        this.context = context;
        mItems = new ArrayList<>();
    }

    public void setList(List<Linka> models)
    {
        // SET REFERENCE
        this.mItems.clear();
        for (Linka item : models) {
            this.mItems.add(item);
        }


        // SEARCH FILTER / SORT


        // DISPLAY
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(
                parent.getContext()).inflate(R.layout.list_item_ble_device, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        Linka device = mItems.get(position);

        holder.isClickDisabled = false;

        final String deviceName = device.getName();
        final String deviceMAC  = device.getMACAddress();
        if (deviceName != null && deviceName.length() > 0) {
            holder.deviceName.setText(deviceName + "\n" + deviceMAC);
        }
        else
        {
            holder.deviceName.setText(R.string.unknown_device);
//            holder.isClickDisabled = true;
        }


        List<Linka> linkas = LocksController.getInstance().getLinkas();
        for (Linka linka : linkas) {
            if (linka.lock_address.equals(device.lock_address)) {
                holder.isClickDisabled = true;
            }
        }

        if (holder.isClickDisabled) {
            holder.deviceName.setTextColor(context.getResources().getColor(R.color.linka_blue_tabbar));
        } else {
            holder.deviceName.setTextColor(context.getResources().getColor(R.color.linka_white));
        }


        holder.item = device;
        holder.position = position;
        holder.onClickDeviceItemListener = onClickDeviceItemListener;
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }



    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.device_name)
        TextView deviceName;
        @BindView(R.id.layout)
        View layout;

        Linka item;
        int position;
        View itemView;
        OnClickDeviceItemListener onClickDeviceItemListener;
        boolean isClickDisabled = false;

        public ViewHolder(View itemView)
        {
            super(itemView);
            this.itemView = itemView;
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.layout)
        void onClickItemCell() {
            if (onClickDeviceItemListener != null && !isClickDisabled) {
                onClickDeviceItemListener.onClickDeviceItem(item, position);
            }
        }
    }



    public interface OnClickDeviceItemListener {
        public void onClickDeviceItem(Linka item, int position);
    }
    public OnClickDeviceItemListener onClickDeviceItemListener;
    public void setOnClickDeviceItemListener(OnClickDeviceItemListener onClickDeviceItemListener) {
        this.onClickDeviceItemListener = onClickDeviceItemListener;
    }
}
