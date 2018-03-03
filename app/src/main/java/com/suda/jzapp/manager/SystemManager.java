package com.suda.jzapp.manager;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.suda.jzapp.dao.greendao.YiYan;
import com.suda.jzapp.dao.local.conf.ConfigLocalDao;
import com.suda.jzapp.misc.Constant;
import com.suda.jzapp.util.NetworkUtil;
import com.suda.jzapp.util.SPUtils;
import com.suda.jzapp.util.ThreadPoolUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by suda on 2016/8/5.
 */
public class SystemManager extends BaseManager {
    public SystemManager(Context context) {
        super(context);
    }

    private ConfigLocalDao configLocalDao = new ConfigLocalDao();

    public void getYiYan(final Handler handler) {


        ThreadPoolUtil.getThreadPoolService().execute(new Runnable() {
            @Override
            public void run() {

                String cuntomYiyan = (String) SPUtils.get(_context, true, "yi_yan_custom", "");
                if (!TextUtils.isEmpty(cuntomYiyan)) {
                    sendMessage(handler, cuntomYiyan);
                    return;
                }

                YiYan yiYan = configLocalDao.queryYiYan(_context);
                if (yiYan != null) {
                    sendMessage(handler, yiYan.getContent());
                }

                boolean syncWifi = (boolean) SPUtils.get(_context, true, "yi_yan_sync_wifi", true);

                if (syncWifi && !NetworkUtil.checkWifi(_context))
                    return;

                try {
                    String result1 = getYiYan();
                    if (!TextUtils.isEmpty(result1)) {
                        if (yiYan == null) {
                            sendMessage(handler, result1);
                            yiYan = new YiYan();
                        }
                        yiYan.setContent(result1);
                        configLocalDao.insertNewYiYan(yiYan, _context);
                    } else
                        sendEmptyMessage(handler, Constant.MSG_ERROR);
                } catch (Exception e) {
                    Log.e("@@@@@@@@", e.toString());
                }
            }
        });
    }

    private String getYiYan() {
        final String lwl12 = "https://api.lwl12.com/hitokoto/main/get?charset=utf-8";
        final String bilibibi = "http://hitoapi.cc/sp/";
        final String ad = "https://api.imjad.cn/hitokoto/?charset=utf-8&length=150&encode=json&fun=sync";
        final String _853 = "http://hitokoto.bronya.net/rand/";
        List<String> urls = new ArrayList<>();
        urls.add(lwl12);
        urls.add(bilibibi);
        urls.add(ad);
        urls.add(_853);
        Collections.shuffle(urls);
        String result1 = NetworkUtil.request(urls.get(0), "");
        switch (urls.get(0)) {
            case lwl12:
                break;
            case bilibibi:
                result1 = JSON.parseObject(result1).getString("text");
                break;
            case ad:
            case _853:
                result1 = JSON.parseObject(result1).getString("hitokoto");
                break;
        }
        return result1;
    }
}
