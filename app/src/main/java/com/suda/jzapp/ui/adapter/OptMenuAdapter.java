package com.suda.jzapp.ui.adapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.suda.jzapp.R;
import com.suda.jzapp.manager.SystemManager;
import com.suda.jzapp.manager.domain.OptDO;
import com.suda.jzapp.misc.Constant;
import com.suda.jzapp.ui.activity.MainActivity;
import com.suda.jzapp.ui.activity.system.AboutActivity;
import com.suda.jzapp.ui.activity.system.EditThemeActivity;
import com.suda.jzapp.util.SPUtils;
import com.suda.jzapp.util.ThemeUtil;

import java.util.List;

/**
 * Created by ghbha on 2016/2/14.
 */
public class OptMenuAdapter extends BaseAdapter {
    private Activity context;
    private List<OptDO> optDOs;
    private SystemManager mSystemManager;

    public OptMenuAdapter(List<OptDO> optDOs, Activity context) {
        this.optDOs = optDOs;
        this.context = context;
        mSystemManager = new SystemManager(context);
    }

    @Override
    public int getCount() {
        return optDOs.size();
    }

    @Override
    public Object getItem(int position) {
        return optDOs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;


        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(context, R.layout.opt_item, null);
            holder.icon = (ImageView) convertView.findViewById(R.id.item_icon);
            holder.title = (TextView) convertView.findViewById(R.id.item_title);
            holder.tip = convertView.findViewById(R.id.tip);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            convertView.setBackgroundResource(R.drawable.ripple);
        }

        final OptDO optDO = optDOs.get(position);


        boolean showEditBudgetTip = (boolean) SPUtils.get(context, Constant.SP_TIP_DONATE, true);
        if (optDO.getAct() == AboutActivity.class && showEditBudgetTip) {
            holder.tip.setVisibility(View.VISIBLE);
            if (showEditBudgetTip) {
                AnimatorSet mAnimatorSet = new AnimatorSet();
                ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(holder.tip, "alpha", 1, 0);
                objectAnimator.setRepeatMode(Animation.RESTART);
                objectAnimator.setRepeatCount(Integer.MAX_VALUE);
                objectAnimator.setDuration(1000);
                mAnimatorSet.playTogether(objectAnimator);
                mAnimatorSet.start();
            }
        } else {
            holder.tip.setVisibility(View.GONE);
        }

        holder.icon.setImageResource(optDO.getIcon());
        holder.icon.setColorFilter(context.getResources().getColor(ThemeUtil.getTheme(context).getMainColorID()));
        holder.title.setText(optDO.getTltle());
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (optDO.getId() == 7) {
                    context.finish();
                } else {
                    Intent intent = new Intent(context, optDO.getAct());
                    if (optDO.getAct() == EditThemeActivity.class)
                        context.startActivityForResult(intent, MainActivity.REQUEST_EDIT_THEME);
                    else if (optDO.getAct() == AboutActivity.class)
                        context.startActivityForResult(intent, MainActivity.REQUEST_ABOUT);
                    else
                        context.startActivity(intent);
                }
            }
        });
        return convertView;
    }

    public class ViewHolder {
        public TextView title;
        public ImageView icon;
        public View tip;
    }
}
