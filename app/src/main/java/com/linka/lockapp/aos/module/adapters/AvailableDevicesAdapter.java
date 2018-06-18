package com.linka.lockapp.aos.module.adapters;

import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.model.Linka;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AvailableDevicesAdapter extends RecyclerView.Adapter<AvailableDevicesAdapter.DeviceHolder> {

    private List<Pair<String,Linka>> devices;

    public AvailableDevicesAdapter() {
        devices = new ArrayList<>();
    }

    @Override
    public DeviceHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DeviceHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_device, parent, false));
    }

    @Override
    public void onBindViewHolder(DeviceHolder holder, int position) {
        holder.deviceName.setText(devices.get(position).second.getName());
        holder.inviteEmail.setText(devices.get(position).first);
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public void insertDevice(Pair<String,Linka> pair) {
        devices.add(pair);
    }

    public void removeDevice(int position){
        devices.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreDevice(Pair<String,Linka> pair, int position){
        devices.add(position,pair);
        notifyItemInserted(position);
    }

    public class DeviceHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.foreground_view)
        public ConstraintLayout foregroundView;
        @BindView(R.id.background_view)
        public RelativeLayout backgroundView;
        @BindView(R.id.device_name)
        TextView deviceName;
        @BindView(R.id.invite_email)
        TextView inviteEmail;

        DeviceHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
