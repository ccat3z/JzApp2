package com.suda.jzapp.ui.fragment;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import com.melnykov.fab.FloatingActionButton;
import com.suda.jzapp.R;
import com.suda.jzapp.dao.local.account.AccountLocalDao;
import com.suda.jzapp.manager.RecordManager;
import com.suda.jzapp.manager.domain.RecordDetailDO;
import com.suda.jzapp.misc.Constant;
import com.suda.jzapp.ui.activity.MainActivity;
import com.suda.jzapp.ui.activity.record.CreateOrEditRecordActivity;
import com.suda.jzapp.ui.adapter.RecordAdapter;
import com.suda.jzapp.util.SPUtils;
import com.suda.jzapp.util.SnackBarUtil;
import com.suda.jzapp.util.ThemeUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.drakeet.materialdialog.MaterialDialog;

/**
 * Created by ghbha on 2016/2/15.
 */
public class RecordFrg extends Fragment implements MainActivity.ReloadCallBack {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        accountLocalDao = new AccountLocalDao();
        recordManager = new RecordManager(getActivity());

        View view = inflater.inflate(R.layout.record_frg_layout, container, false);
        backGround = view.findViewById(R.id.background);
        mAddRecordBt = (FloatingActionButton) view.findViewById(R.id.add_new_record);
        mAddRecordBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecordAdapter.resetOptStatus();
                if (SPUtils.gets(getActivity(), Constant.SP_FIRST_ADD, true)) {
                    SPUtils.put(getActivity(), Constant.SP_FIRST_ADD, false);
                    final MaterialDialog materialDialog = new MaterialDialog(getActivity());
                    materialDialog.setTitle("提示");
                    materialDialog.setMessage("长按可以进语音记账，更加快捷哦\n请说出如\"吃饭支出100元\"");
                    materialDialog.setNegativeButton(getResources().getString(R.string.ok), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            materialDialog.dismiss();
                        }
                    });
                    materialDialog.show();
                    return;
                }
                if (accountLocalDao.getAccountSize(getActivity()) == 0) {
                    SnackBarUtil.showSnackInfo(backGround, getActivity(), "请新建一个账户");
                } else {
                    Intent intent = new Intent(getActivity(), CreateOrEditRecordActivity.class);
                    getActivity().startActivityForResult(intent, MainActivity.REQUEST_RECORD);
                }
            }
        });

        mAddRecordBt.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return false;
            }
        });

        foot = View.inflate(getActivity(), R.layout.record_foot, null);

        footTv = ((TextView) foot.findViewById(R.id.foot_tip));
        nullTipTv = ((TextView) view.findViewById(R.id.null_tip));

        recordLv = (ListView) view.findViewById(R.id.record_lv);
        recordLv.addFooterView(foot);
        recordDetailDOs = new ArrayList<>();
        mRecordAdapter = new RecordAdapter(getActivity(), recordDetailDOs, this);
        recordLv.setAdapter(mRecordAdapter);
        recordLv.setOverScrollMode(View.OVER_SCROLL_NEVER);

        //resetFoot();

        reload(true);

        //recordLv.setEmptyView();

        recordLv.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (totalItemCount == 0)
                    return;
                if (firstVisibleItem + visibleItemCount == totalItemCount && !isRefresh) {
                    loadData();
                }
            }
        });


        ((MainActivity) getActivity()).setReloadRecordCallBack(this);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mainColor = getResources().getColor(ThemeUtil.getTheme(getActivity()).getMainColorID());
        mainDarkColor = getResources().getColor(ThemeUtil.getTheme(getActivity()).getMainDarkColorID());
        mAddRecordBt.setColorNormal(mainColor);
        mAddRecordBt.setColorPressed(mainDarkColor);
        backGround.setBackground(new ColorDrawable(mainColor));
        nullTipTv.setTextColor(mainColor);
        footTv.setTextColor(mainColor);
        //resetFoot();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void resetFoot() {
        nullTipTv.setTextColor(mainColor);
        footTv.setTextColor(mainColor);
        footTv.setOnClickListener(null);
        nullTipTv.setVisibility(recordDetailDOs.size() > 0 ? View.GONE : View.VISIBLE);
        DateFormat format1 = new SimpleDateFormat("yyyy年MM月dd日");
        footTv.setText(format1.format(new Date(System.currentTimeMillis())) + "\n您开启了记账旅程");
    }

    @Override
    public void reload(final boolean needUpdateData) {

        if (!needUpdateData) {
            mRecordAdapter.notifyDataSetChanged();
            //resetFoot();
            return;
        }

        isRefresh = true;
        curPage = 1;

        recordManager.getRecordByPageIndex(curPage, new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                curPage++;
                isRefresh = false;
                if (msg.what == Constant.MSG_SUCCESS) {
                    recordDetailDOs.clear();
                    recordDetailDOs.addAll((List<RecordDetailDO>) msg.obj);
                    mRecordAdapter.notifyDataSetChanged();
                    resetFoot();
                }
            }
        });
    }

    private void loadData() {
        isRefresh = true;
        recordManager.getRecordByPageIndex(curPage, new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == Constant.MSG_SUCCESS) {
                    if (((List<RecordDetailDO>) msg.obj).size() == 0) {
                        if (recordDetailDOs.size() > 0)
                            footTv.setVisibility(View.VISIBLE);
                    } else {
                        recordDetailDOs.addAll((List<RecordDetailDO>) msg.obj);
                        mRecordAdapter.notifyDataSetChanged();
                        isRefresh = false;
                    }
                    curPage++;
                }
            }
        });
    }

    private ListView recordLv;
    private TextView footTv;
    private View backGround;
    private int mainColor;
    private int mainDarkColor;
    private FloatingActionButton mAddRecordBt;
    private RecordManager recordManager;
    private RecordAdapter mRecordAdapter;
    private List<RecordDetailDO> recordDetailDOs;
    private int curPage = 1;
    private boolean isRefresh = true;
    private View foot;
    private TextView nullTipTv;

    private AccountLocalDao accountLocalDao;

}