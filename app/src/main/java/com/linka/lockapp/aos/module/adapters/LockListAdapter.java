package com.linka.lockapp.aos.module.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.model.Linka;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by kyle on 5/9/18.
 */

public class LockListAdapter extends RecyclerView.Adapter<LockListAdapter.ViewHolder> {

    public List<Linka> mItems = new ArrayList<>();

    public Context context;

    public LockListAdapter(Context context) {
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
                parent.getContext()).inflate(R.layout.sidebar_lock_list_adapter, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        Linka linka = mItems.get(position);

        //holder.itemText.setText(linka.lock_mac_address); //DEBUG - Use The MAC ID instead of the name
        holder.itemText.setText(linka.getName());

        holder.item = linka;
        holder.position = position;
        holder.onClickDeviceItemListener = onClickDeviceItemListener;
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }



    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item_text)
        TextView itemText;

        Linka item;
        int position;
        View itemView;
        OnClickDeviceItemListener onClickDeviceItemListener;

        public ViewHolder(View itemView)
        {
            super(itemView);
            this.itemView = itemView;
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.layout)
        void onClickItemCell() {
            if (onClickDeviceItemListener != null) {
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
