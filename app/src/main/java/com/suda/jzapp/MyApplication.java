package com.suda.jzapp;

import android.support.multidex.MultiDexApplication;

import com.suda.jzapp.dao.local.account.AccountLocalDao;
import com.suda.jzapp.dao.local.conf.ConfigLocalDao;
import com.suda.jzapp.util.LogUtils;
import com.suda.jzapp.util.SPUtils;

/**
 * Created by Suda on 2015/9/16.
 */
public class MyApplication extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        initData();
        LogUtils.isDebug = BuildConfig.DEBUG;
    }


    private void initData() {
        if ((boolean) SPUtils.get(this, "firstIn", true)) {
            SPUtils.put(this, "firstIn", false);
            ConfigLocalDao dao = new ConfigLocalDao();
            AccountLocalDao accountLocalDao = new AccountLocalDao();
            accountLocalDao.initBudget(this);
            dao.initAccountTypeDb(this);
            dao.initRecordType(this);
            dao.createDefaultAccount(this);
        }
    }
}
