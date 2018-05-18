package com.linka.lockapp.aos.module.widget;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linka.lockapp.aos.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vanson on 3/12/15.
 */
public class GenericLoadMoreAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    protected static final int VIEWTYPE_ITEM = 1;
    protected static final int VIEWTYPE_LOADER = 2;

    public boolean showLoading = false;
    public List itemCountSource = new ArrayList();
    public GenericLoadMoreAdapter(Context context) {
    }

    public long getYourItemId(int position) {return position;}

    public RecyclerView.ViewHolder onCreateYourViewHolder(ViewGroup parent) {return null;}

    public void onBindYourViewHolder(T holder, int position) {}

    public void setItemCountSource(List itemCountSource) {
        this.itemCountSource = itemCountSource;
    }

    public void setLoadMore(boolean bool) {
        showLoading = bool;
        notifyDataSetChanged();
    }



    @Override
    public int getItemViewType(int position) {
        if (position > getItemRealCount() - 1) {
            return VIEWTYPE_LOADER;
        }
        return VIEWTYPE_ITEM;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEWTYPE_LOADER) {

            View view = LayoutInflater.from(
                    parent.getContext()).inflate(R.layout.recyclerview_more_progress, parent, false);
            return new LoaderViewHolder(view);

        } else if (viewType == VIEWTYPE_ITEM) {
            return onCreateYourViewHolder(parent);
        }

        throw new IllegalArgumentException("Invalid ViewType: " + viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        // Loader ViewHolder
        if (holder instanceof LoaderViewHolder) {
            return;
        }

        onBindYourViewHolder((T)holder, position);
    }

    @Override
    public int getItemCount() {
        return getItemRealCount() + (showLoading ? 1 : 0);
    }

    public int getItemRealCount() {

        return itemCountSource.size();
    }


    @Override
    public long getItemId(int position) {

        if (position != 0 && position == getItemCount() - 1) {
            return position;
        }
        return getYourItemId(position);
    }



    public static class LoaderViewHolder extends RecyclerView.ViewHolder {

        public LoaderViewHolder(View itemView) {
            super(itemView);
        }
    }
}
