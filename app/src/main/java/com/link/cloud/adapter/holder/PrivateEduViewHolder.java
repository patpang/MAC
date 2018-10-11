package com.link.cloud.adapter.holder;

import android.app.Activity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.link.cloud.R;
import com.link.cloud.api.bean.PrivateEduBean;
import com.link.cloud.base.BaseViewHolder;
import com.link.cloud.widget.TagLayout;
import com.zitech.framework.utils.ViewUtils;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * @author qianlu
 * @date 2018/9/26.
 * GitHub：qiandailu
 * email：zar.l@qq.com
 * description：
 */
public class PrivateEduViewHolder extends BaseViewHolder<PrivateEduBean> {
    private CircleImageView eduImage;
    private TextView calssName;
    private TextView coachName;
    private TagLayout sexualTags;
    private LinearLayout rootViewLayout;


    public PrivateEduViewHolder(View itemView) {
        super(itemView);
        eduImage = (CircleImageView) itemView.findViewById(R.id.eduImage);
        calssName = (TextView) itemView.findViewById(R.id.calssName);
        coachName = (TextView) itemView.findViewById(R.id.coachName);
        sexualTags = (TagLayout) itemView.findViewById(R.id.sexualTags);
        rootViewLayout = (LinearLayout) itemView.findViewById(R.id.rootViewLayout);
    }


    @Override
    public void setData(Activity activity, PrivateEduBean data, int position) {

    }

    public void setDates(Activity activity, final PrivateEduBean data, final int position, boolean isSeceter) {
        if (isSeceter) {
            rootViewLayout.setBackground(activity.getResources().getDrawable(R.drawable.border_lesson));
        } else {
            rootViewLayout.setBackground(activity.getResources().getDrawable(R.drawable.border_member));
        }
        ViewUtils.setOnClickListener(rootViewLayout, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(data, position);
                }
            }
        });

    }

}
