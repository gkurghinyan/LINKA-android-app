package com.linka.lockapp.aos.module.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.linka.lockapp.aos.R;
import com.linka.lockapp.aos.module.model.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by kyle on 5/9/18.
 */

public class SharingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public List<User> mItems;
    private static final int FOOTER_TYPE = 1;
    private static final int NORMAL_TYPE = 2;

    public Context context;
    public static final int VISIBLE = 1;
    public static final int INVISIBLE = 0;
    private static int addButtonVisibility = INVISIBLE;
    private SimpleDateFormat allDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss", Locale.UK);
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd', 'h:mma", Locale.UK);


    public SharingAdapter(Context context) {
        super();
        this.context = context;
        mItems = new ArrayList<>();
    }

    public void setList(List<User> models) {
        // SET REFERENCE
        this.mItems.clear();
        for (User item : models) {
            this.mItems.add(item);
        }


        // SEARCH FILTER / SORT


        // DISPLAY
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == NORMAL_TYPE) {
            View v = LayoutInflater.from(
                    parent.getContext()).inflate(R.layout.list_item_user, parent, false);
            ViewHolder viewHolder = new ViewHolder(v);
            return viewHolder;
        } else {
            return new FooterViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.users_footer_card, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewHolder) {
            User user = mItems.get(position);


            String userName = user.name;
            String lastUsed = user.lastUsed;

            if (userName != null) {

//                String ownEmail = LinkaAPIServiceImpl.getUserEmail();
//                if (ownEmail != null && ownEmail.equals(user.email)) {
//                    userName = userName + " (You)";
//                }

                if (user.isPendingApproval) {
                    userName = userName + " (Requesting Access)";
                }

                ((ViewHolder) holder).userName.setText(userName);
                if (lastUsed != null) {
                    Date date = null;
                    try {
                        date = allDateFormat.parse(lastUsed);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if(date != null) {
                        ((ViewHolder) holder).lastUsed.setText("Last used: " + dateFormat.format(date));
                    }
                }
            }

            ((ViewHolder) holder).avatar.setColorFilter( context.getResources().getColor(R.color.linka_black_transparent), PorterDuff.Mode.MULTIPLY );
            ((ViewHolder) holder).item = user;
            ((ViewHolder) holder).position = position;
            ((ViewHolder) holder).onClickDeviceItemListener = onClickDeviceItemListener;
        } else {
            ((FooterViewHolder) holder).addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickDeviceItemListener.onAddButtonClicked();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size() + addButtonVisibility;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == mItems.size()) {
            return FOOTER_TYPE;
        }
        return NORMAL_TYPE;
    }

    public void removeUser(User user) {
        mItems.remove(user);
        notifyDataSetChanged();
    }

//    public void restoreUser(User user,int position){
//        mItems.add(position,user);
//        notifyItemInserted(position);
//    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.foreground_view)
        public ConstraintLayout foregroundView;
        @BindView(R.id.user_name)
        TextView userName;
        @BindView(R.id.last_used)
        TextView lastUsed;
        @BindView(R.id.person_avatar)
        ImageView avatar;

        User item;
        int position;
        View itemView;
        OnClickDeviceItemListener onClickDeviceItemListener;

        public ViewHolder(View itemView) {
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

    class FooterViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.plus_button)
        ImageView addButton;

        FooterViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }


    public void setAddButtonVisibility(int visibility) {
        addButtonVisibility = visibility;
        notifyDataSetChanged();
    }

    public interface OnClickDeviceItemListener {
        void onClickDeviceItem(User item, int position);

        void onAddButtonClicked();
    }

    public OnClickDeviceItemListener onClickDeviceItemListener;

    public void setOnClickDeviceItemListener(OnClickDeviceItemListener onClickDeviceItemListener) {
        this.onClickDeviceItemListener = onClickDeviceItemListener;
    }


}
