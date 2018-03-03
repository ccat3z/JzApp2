package com.suda.jzapp.manager;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;

import com.suda.jzapp.dao.greendao.Record;
import com.suda.jzapp.dao.greendao.RecordType;
import com.suda.jzapp.dao.greendao.RemarkTip;
import com.suda.jzapp.dao.local.account.AccountLocalDao;
import com.suda.jzapp.dao.local.conf.ConfigLocalDao;
import com.suda.jzapp.dao.local.record.RecordLocalDAO;
import com.suda.jzapp.dao.local.record.RecordTypeLocalDao;
import com.suda.jzapp.manager.domain.AccountDetailDO;
import com.suda.jzapp.manager.domain.ChartRecordDo;
import com.suda.jzapp.manager.domain.ExcelRecord;
import com.suda.jzapp.manager.domain.LineChartDo;
import com.suda.jzapp.manager.domain.MonthReport;
import com.suda.jzapp.manager.domain.MyDate;
import com.suda.jzapp.manager.domain.RecordDetailDO;
import com.suda.jzapp.manager.domain.VoiceDo;
import com.suda.jzapp.misc.Constant;
import com.suda.jzapp.util.MoneyUtil;
import com.suda.jzapp.util.ThreadPoolUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

/**
 * Created by ghbha on 2016/2/28.
 */
public class RecordManager extends BaseManager {
    public RecordManager(Context context) {
        super(context);
    }

    /**
     * 创建新记录
     *
     * @param record
     */
    public void createNewRecord(final Record record, final Handler handler) {
        //1网络创建不成功 SyncStatus 置0
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(record.getRecordDate());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        record.setRecordDate(calendar.getTime());
        record.setIsDel(false);
        record.setSyncStatus(false);
        recordLocalDAO.createNewRecord(_context, record);
        sendEmptyMessage(handler, Constant.MSG_SUCCESS);

        //记录备注
        if (!TextUtils.isEmpty(record.getRemark()) && (!(record.getRecordTypeID() == Constant.CHANGE_TYPE || record.getRecordTypeID() == Constant.TRANSFER_TYPE))) {
            RemarkTip remarkTip = recordLocalDAO.selectRemarkTipByRemark(_context, record.getRemark());
            if (remarkTip == null) {
                recordLocalDAO.insertNewRemarkTip(_context, record.getRemark(), false);
            } else {
                remarkTip.setUseTimes(remarkTip.getUseTimes() + 1);
                recordLocalDAO.updateRemarkTip(_context, remarkTip, false);
            }
        }

    }

    /**
     * 修改记录
     *
     * @param record
     * @param handler
     */
    public void updateOldRecord(final Record record, final Handler handler) {
        record.setSyncStatus(false);
        recordLocalDAO.updateOldRecord(_context, record);
        sendEmptyMessage(handler, Constant.MSG_SUCCESS);
    }

    /**
     * 创建新新记录类型
     */
    public void createNewRecordType(final RecordType recordType, final Handler handler) {

        if (recordTypeDao.haveCreate(_context, recordType.getRecordDesc(), recordType.getRecordType())) {
            handler.sendEmptyMessage(Constant.MSG_ERROR);
            return;
        }

        //设置索引
        recordType.setIndex(recordTypeDao.getMaxIndexByRecordType(_context, recordType.getRecordType()));
        recordType.setSexProp(Constant.Sex.ALL.getId());
        recordType.setOccupation(Constant.Occupation.ALL.getId());
        recordType.setSysType(false);

        recordType.setSyncStatus(false);
        recordTypeDao.createNewRecordType(_context, recordType);
        handler.sendEmptyMessage(Constant.MSG_SUCCESS);
    }

    @Deprecated
    public synchronized void updateRecordTypeIndex(final Handler handler) {
        updateRecordTypeIndex(handler, false);
    }

    /**
     * 更新记录类型排序
     *
     * @param handler
     * @param serviceSync
     */
    public synchronized void updateRecordTypeIndex(final Handler handler, boolean serviceSync) {

    }

    /**
     * 更新记录类型
     *
     * @param recordType
     * @param handler
     */
    public void updateRecordType(final RecordType recordType, final Handler handler) {
        if (!recordType.getSysType())
            recordType.setSyncStatus(false);
        recordTypeDao.updateRecordType(_context, recordType);
        if (recordType.getIsDel()) {
            updateRecordTypeIndex(null);
        }
        sendEmptyMessage(handler, Constant.MSG_SUCCESS);
    }

    /**
     * 获取收入、支持记录
     */
    public List<RecordType> getRecordTypeByType(final int type) {

        List<RecordType> recordTypes = new ArrayList<RecordType>();
        if (type == Constant.RecordType.SHOURU.getId()) {
            recordTypes.addAll(recordTypeDao.getAllShouRuRecordType(_context));
        } else {
            recordTypes.addAll(recordTypeDao.getAllZhiChuRecordType(_context));
        }
        if (recordTypes == null || recordTypes.size() == 0) {
            recordTypeDao.clearAllRecordType(_context);
            configLocalDao.initRecordType(_context);
            return getRecordTypeByType(type);
        }
        return recordTypes;

    }

    /**
     * 根据id获取记录类型
     *
     * @param id
     * @return
     */
    public RecordType getRecordTypeByID(long id) {
        return recordTypeDao.getRecordTypeByRecordTypeId(_context, id);
    }

    /**
     * 更新排序
     *
     * @param list
     */
    public void updateRecordTypesOrder(List<RecordType> list) {
        recordTypeDao.updateRecordOrder(_context, list);
        //同步部分
        updateRecordTypeIndex(null);
    }

    /**
     * 月份作为分页条件查询记录
     *
     * @param pageIndex
     * @param handler
     */
    public void getRecordByPageIndex(final int pageIndex, final Handler handler) {
        ThreadPoolUtil.getThreadPoolService().execute(new Runnable() {
            @Override
            public void run() {
                int pageIndexTmp = pageIndex * 2 - 1;
                List<RecordDetailDO> recordDetailDos = new ArrayList<>();
                appendRecords(recordDetailDos, getRecordByPageIndex(pageIndexTmp));
                appendRecords(recordDetailDos, getRecordByPageIndex(pageIndexTmp + 1));
                sendMessage(handler, recordDetailDos, true);
            }
        });
    }

    private void appendRecords(List<RecordDetailDO> recordDetailDos, List<RecordDetailDO> append) {
        if (append != null && append.size() > 0)
            recordDetailDos.addAll(append);
    }

    private List<RecordDetailDO> getRecordByPageIndex(int pageIndex) {
        List<MyDate> dates = recordLocalDAO.getRecordDate(_context, pageIndex);
        List<RecordDetailDO> recordDetailDos = new ArrayList<>();
        if (dates.size() == 0) {
            return null;
        }
        RecordDetailDO recordDetailDOMonthFirst = new RecordDetailDO();
        recordDetailDOMonthFirst.setIsFirstDay(true);
        MyDate myDate = dates.get(0);
        recordDetailDOMonthFirst.setRecordDate(myDate.getDate());
        recordDetailDos.add(recordDetailDOMonthFirst);
        double todayAllInMoney, todayAllOutMoney = 0;
        Map<Long, RecordType> recordTypeMap = new ArrayMap<>();
        for (MyDate date : dates) {
            todayAllInMoney = 0;
            todayAllOutMoney = 0;
            RecordDetailDO recordDetailDayFirst = new RecordDetailDO();
            recordDetailDayFirst.setIsDayFirstDay(true);
            recordDetailDayFirst.setRecordDate(date.getDate());
            recordDetailDos.add(recordDetailDayFirst);
            List<Record> records = recordLocalDAO.getRecordByMyDate(_context, date);
            for (Record record : records) {
                RecordType recordType = recordTypeMap.get(record.getRecordTypeID());
                if (recordType == null) {
                    recordType = recordTypeDao.getRecordTypeByRecordTypeId(_context, record.getRecordTypeID());
                    recordTypeMap.put(record.getRecordTypeID(), recordType);
                }
                RecordDetailDO recordDetailDO = new RecordDetailDO();
                recordDetailDO.setRecordDate(date.getDate());
                recordDetailDO.setRecordID(record.getRecordId());
                recordDetailDO.setRecordMoney(record.getRecordMoney());
                recordDetailDO.setRemark(record.getRemark());
                recordDetailDO.setIconId(recordType == null ? 0 : recordType.getRecordIcon());
                recordDetailDO.setRecordDesc(recordType == null ? "" : recordType.getRecordDesc());
                if (recordDetailDO.getRecordMoney() > 0)
                    todayAllInMoney += recordDetailDO.getRecordMoney();
                else
                    todayAllOutMoney += recordDetailDO.getRecordMoney();

                recordDetailDos.add(recordDetailDO);
            }
            recordDetailDayFirst.setTodayAllInMoney(todayAllInMoney);
            recordDetailDayFirst.setTodayAllOutMoney(todayAllOutMoney);
        }
        return recordDetailDos;
    }

    /**
     * 获取账户当月流水账
     *
     * @param accountID
     * @param startDate
     * @param endDate
     * @param handler
     */
    public void getRecordsByMonthAndAccount(final long accountID, final long startDate, final long endDate, final Handler handler) {
        ThreadPoolUtil.getThreadPoolService().execute(new Runnable() {
            @Override
            public void run() {

                Map<String, Object> map = new ArrayMap<String, Object>();
                List<RecordDetailDO> recordDetailDos = new ArrayList<>();
                Map<Long, RecordType> recordTypeMap = new ArrayMap<>();
                double outCount = 0;
                double inCount = 0;
                List<Record> records = recordLocalDAO.getRecordByMonthAndAccount(_context, accountID, startDate, endDate);
                for (Record record : records) {
                    RecordType recordType = recordTypeMap.get(record.getRecordTypeID());
                    if (recordType == null) {
                        recordType = recordTypeDao.getRecordTypeByRecordTypeId(_context, record.getRecordTypeID());
                        recordTypeMap.put(record.getRecordTypeID(), recordType);
                    }
                    RecordDetailDO recordDetailDO = new RecordDetailDO();
                    recordDetailDO.setRecordDate(record.getRecordDate());
                    recordDetailDO.setRecordID(record.getRecordId());
                    recordDetailDO.setRecordMoney(record.getRecordMoney());
                    recordDetailDO.setRemark(record.getRemark());
                    recordDetailDO.setIconId(recordType.getRecordIcon());
                    if (recordType.getRecordType() == Constant.RecordType.CHANGE.getId()) {
                        recordDetailDO.setIconId(Constant.RecordTypeConstant.ICON_TYPE_YU_E_BIAN_GENG);
                    }

                    recordDetailDO.setRecordDesc(recordType.getRecordDesc());
                    recordDetailDos.add(recordDetailDO);
                    if (record.getRecordMoney() > 0) {
                        inCount += record.getRecordMoney();
                    } else {
                        outCount += record.getRecordMoney();
                    }

                }
                map.put("recordDetailDos", recordDetailDos);
                map.put("inCount", inCount);
                map.put("outCount", Math.abs(outCount));

                sendMessage(handler, map, true);
            }
        });
    }

    /**
     * 获取本月收支
     *
     * @param handler
     * @param out
     */
    public void getOutOrInRecordThisMonth(final Handler handler, final boolean out) {
        Calendar calendar = Calendar.getInstance();
        getOutOrInRecordByMonth(handler, out, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));
    }

    /**
     * 获取每个月收支
     *
     * @param handler
     * @param out
     * @param year
     * @param month
     */
    public void getOutOrInRecordByMonth(final Handler handler, final boolean out, final int year, final int month) {
        ThreadPoolUtil.getThreadPoolService().execute(new Runnable() {
            @Override
            public void run() {
                List<ChartRecordDo> list = new ArrayList<ChartRecordDo>();
                list.addAll(recordLocalDAO.getOutOrInRecordByMonth(_context, out, year, month));
                double moneyCount = 0;
                for (ChartRecordDo chartRecordDo : list) {
                    moneyCount += chartRecordDo.getRecordMoney();
                }
                for (ChartRecordDo chartRecordDo : list) {
                    chartRecordDo.setPer(chartRecordDo.getRecordMoney() / moneyCount * 100);
                }
                sendMessage(handler, list, true);
            }
        });
    }

    /**
     * 获取某年收支趋势
     *
     * @param year
     * @param handler
     */
    public void getYearRecordDetail(final int year, final Handler handler) {
        ThreadPoolUtil.getThreadPoolService().execute(new Runnable() {
            @Override
            public void run() {
                Map<Integer, Double> out = recordLocalDAO.getYearRecordDetail(_context, year, true);
                Map<Integer, Double> in = recordLocalDAO.getYearRecordDetail(_context, year, false);
                List<LineChartDo> list = new ArrayList<LineChartDo>();
                Calendar calendar = Calendar.getInstance();
                int maxMon = calendar.get(Calendar.YEAR) != year ? 12 : calendar.get(Calendar.MONTH) + 1;

                for (int i = 0; i < maxMon; i++) {
                    double monthIn = 0;
                    double monthOut = 0;
                    if (out.get(i) != null)
                        monthOut = out.get(i);
                    if (in.get(i) != null)
                        monthIn = in.get(i);
                    LineChartDo lineChartDo = new LineChartDo();
                    lineChartDo.setMonth(i + 1);
                    lineChartDo.setAllIn(monthIn);
                    lineChartDo.setAllOut(monthOut);
                    lineChartDo.setAllLeft(monthIn + monthOut);
                    list.add(lineChartDo);
                }
                sendMessage(handler, list, true);
            }
        });
    }

    /**
     * 获取备注提示
     *
     * @param handler
     */
    public void getRemarkTips(final Handler handler) {
        ThreadPoolUtil.getThreadPoolService().execute(new Runnable() {
            @Override
            public void run() {
                List<RemarkTip> remarkTips = recordLocalDAO.selectRemarkTips(_context);
                sendMessage(handler, remarkTips, true);
            }
        });
    }

    /**
     * 删除备注
     * @param id
     */
    public void deleteRemarkTip(long id){
        recordLocalDAO.deleteRemarkTip(_context,id);
    }

    /**
     * 转账
     *
     * @param outAccountId
     * @param inAccountId
     * @param money
     * @param handler
     */
    public void moneyTransFer(final long outAccountId, final long inAccountId, final double money, final Handler handler) {
        final AccountDetailDO outAccountDetailDO = accountManager.getAccountByID(outAccountId);
        final AccountDetailDO inAccountDetailDO = accountManager.getAccountByID(inAccountId);

        accountManager.updateAccountMoney(outAccountId, -money, new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                accountManager.updateAccountMoney(inAccountId, money, new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        Record record = new Record();
                        record.setIsDel(false);
                        record.setRemark("转出到" + inAccountDetailDO.getAccountName());
                        record.setRecordId(System.currentTimeMillis());
                        record.setAccountID(outAccountId);
                        record.setRecordType(Constant.RecordType.CHANGE.getId());
                        record.setRecordTypeID(Constant.TRANSFER_TYPE);
                        record.setRecordMoney(MoneyUtil.getFormatNum(-money));
                        record.setRecordDate(new Date(System.currentTimeMillis()));
                        createNewRecord(record, new Handler() {
                            @Override
                            public void handleMessage(Message msg) {
                                super.handleMessage(msg);
                                Record record = new Record();
                                record.setIsDel(false);
                                record.setRemark("从" + outAccountDetailDO.getAccountName() + "转入");
                                record.setRecordId(System.currentTimeMillis());
                                record.setAccountID(inAccountId);
                                record.setRecordType(Constant.RecordType.CHANGE.getId());
                                record.setRecordTypeID(Constant.TRANSFER_TYPE);
                                record.setRecordMoney(MoneyUtil.getFormatNum(money));
                                record.setRecordDate(new Date(System.currentTimeMillis()));
                                createNewRecord(record, new Handler() {
                                    @Override
                                    public void handleMessage(Message msg) {
                                        super.handleMessage(msg);
                                        if (handler != null)
                                            handler.sendEmptyMessage(Constant.MSG_SUCCESS);
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });

    }

    /**
     * 获取月报
     *
     * @param handler
     */
    public void getThisMonthReport(final Handler handler) {
        ThreadPoolUtil.getThreadPoolService().execute(new TimerTask() {
            @Override
            public void run() {
                Calendar calendar = Calendar.getInstance();
                Map<Integer, Double> in = recordLocalDAO.getYearMonthRecordDetail(_context, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), false);
                Map<Integer, Double> out = recordLocalDAO.getYearMonthRecordDetail(_context, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), true);

                List<ChartRecordDo> chartRecordDos = recordLocalDAO.getOutOrInRecordByMonth(_context, true, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));

                MonthReport monthReport = new MonthReport();

                monthReport.setAllMoney(accountLocalDao.getAllMoney(_context));

                monthReport.setBudgetMoney(accountLocalDao.getBudget(_context));
                monthReport.setInMoney(0.00);
                monthReport.setOutMoney(0.00);
                if (in.get(calendar.get(Calendar.MONTH)) != null) {
                    monthReport.setInMoney(in.get(calendar.get(Calendar.MONTH)));
                }
                if (out.get(calendar.get(Calendar.MONTH)) != null) {
                    monthReport.setOutMoney(out.get(calendar.get(Calendar.MONTH)));
                }
                if (chartRecordDos != null && chartRecordDos.size() > 0) {
                    monthReport.setOutMaxType(chartRecordDos.get(0).getRecordDesc());
                    monthReport.setOutMaxMoney(Math.abs(chartRecordDos.get(0).getRecordMoney()));
                }

                sendMessage(handler, monthReport, true);
            }
        });

    }

    /**
     * 根据类型月份查记录
     *
     * @param recordTypeID
     * @param recordYear
     * @param recordMonth
     * @return
     */
    public List<RecordDetailDO> getRecordsByRecordTypeIDAndMonth(long recordTypeID, int recordYear, int recordMonth) {
        List<Record> records = recordLocalDAO.getRecordsByRecordTypeIDAndMonth(_context, recordTypeID, recordYear, recordMonth);
        List<RecordDetailDO> recordDetailDOs = new ArrayList<>();
        Map<Long, RecordType> recordTypeMap = new ArrayMap<>();
        for (Record record : records) {
            RecordType recordType = recordTypeMap.get(record.getRecordTypeID());
            if (recordType == null) {
                recordType = recordTypeDao.getRecordTypeByRecordTypeId(_context, record.getRecordTypeID());
                recordTypeMap.put(record.getRecordTypeID(), recordType);
            }
            RecordDetailDO recordDetailDO = new RecordDetailDO();
            recordDetailDO.setRecordDate(record.getRecordDate());
            recordDetailDO.setRecordID(record.getRecordId());
            recordDetailDO.setRecordMoney(record.getRecordMoney());
            recordDetailDO.setRemark(record.getRemark());
            recordDetailDO.setIconId(recordType.getRecordIcon());
            if (recordType.getRecordType() == Constant.RecordType.CHANGE.getId()) {
                recordDetailDO.setIconId(Constant.RecordTypeConstant.ICON_TYPE_YU_E_BIAN_GENG);
            }

            recordDetailDO.setRecordDesc(recordType.getRecordDesc());
            recordDetailDOs.add(recordDetailDO);
        }
        return recordDetailDOs;
    }

    /**
     * 导出csv
     *
     * @param start
     * @param end
     * @param handler
     */
    public void exportToExcel(long start, long end, Handler handler) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Map<Long, RecordType> recordTypeMap = new ArrayMap<>();
        Map<Long, AccountDetailDO> accountHashMap = new ArrayMap<>();
        List<Record> records = recordLocalDAO.getRecordByMonth(_context, start, end);
        List<ExcelRecord> excelRecords = new ArrayList<>();
        for (Record record : records) {
            ExcelRecord excelRecord = new ExcelRecord();
            RecordType recordType = recordTypeMap.get(record.getRecordTypeID());
            if (recordType == null) {
                recordType = recordTypeDao.getRecordTypeByRecordTypeId(_context, record.getRecordTypeID());
                recordTypeMap.put(record.getRecordTypeID(), recordType);
            }

            AccountDetailDO account = accountHashMap.get(record.getAccountID());
            if (account == null) {
                account = accountManager.getAccountByID(record.getAccountID());
                accountHashMap.put(record.getAccountID(), account);
            }
            if (account == null)
                excelRecord.setRecordAccount("--");
            else
                excelRecord.setRecordAccount(account.getAccountName());
            excelRecord.setRecordMoney(record.getRecordMoney());
            excelRecord.setRecordDate(record.getRecordDate());
            excelRecord.setRecordName(recordType.getRecordDesc());
            excelRecord.setRemark(record.getRemark());
            excelRecords.add(excelRecord);
        }

        FileWriter fw;
        BufferedWriter bfw;
        String fileName = simpleDateFormat.format(new Date(start)) + "_" + simpleDateFormat.format(new Date(end)) + "消费流水.csv";
        File file = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + fileName);
        try {
            fw = new FileWriter(file);
            bfw = new BufferedWriter(fw);
            bfw.write(0xFEFF);
            bfw.write("日期" + ',');
            bfw.write("账户" + ',');
            bfw.write("记录名" + ',');
            bfw.write("备注" + ',');
            bfw.write("金额");
            bfw.newLine();
            for (int i = 0; i < excelRecords.size(); i++) {
                ExcelRecord excelRecord = excelRecords.get(i);
                bfw.write(simpleDateFormat.format(excelRecord
                        .getRecordDate()) + ',');
                bfw.write(excelRecord.getRecordAccount() + ',');
                bfw.write(excelRecord.getRecordName() + ',');
                bfw.write((TextUtils.isEmpty(excelRecord.getRemark()) ? "\t" : excelRecord.getRemark()) + ',');
                bfw.write(String.valueOf(excelRecord.getRecordMoney()));
                bfw.newLine();
            }
            bfw.flush();
            bfw.close();
            sendMessage(handler, fileName);
        } catch (Exception r) {
            sendEmptyMessage(handler, Constant.MSG_ERROR);
        }
    }

    public RecordType getRecordTypeByNameAndType(String name, int reocordType) {
        return recordTypeDao.getRecordTypeByNameAndType(_context, name, reocordType);
    }

    /**
     * 解析语音
     *
     * @param content
     * @param handler
     */
    public void parseVoice(String content, Handler handler) {
        VoiceDo voiceDo = new VoiceDo();
        String[] contents = null;
        int recordType = Constant.RecordType.SHOURU.getId();
        String recordDetail = "";
        double money = 0.00f;
        try {

            String splitZhichuWord = getZhiChuWord(content);
            String splitShouruWord = getShouRuWord(content);

            if (!TextUtils.isEmpty(splitZhichuWord)) {
                recordType = Constant.RecordType.ZUICHU.getId();
                contents = content.split(splitZhichuWord);
            } else if (!TextUtils.isEmpty(splitShouruWord)) {
                recordType = Constant.RecordType.SHOURU.getId();
                contents = content.split(splitShouruWord);
            } else {
                voiceDo.setResultCode(Constant.VOICE_PARSE_FAIL);
                sendMessage(handler, voiceDo);
                return;
            }

            recordDetail = contents[0];
            if (contents[1].contains("十")) {
                money = 10.00;
            } else {
                money = Double.parseDouble(contents[1].replace("元", "").replace("块", "").replace("钱", ""));
            }

            RecordType recordTypeVoice = getRecordTypeByNameAndType(recordDetail, recordType);
            if (recordTypeVoice == null) {
                voiceDo.setResultCode(Constant.VOICE_PARSE_NOT_FOUND_RECORD_TYPE);
            } else {
                voiceDo.setResultCode(Constant.VOICE_PARSE_SUCCESS);
                voiceDo.setRecordTypeDo(recordTypeVoice);
                voiceDo.setMoney(money);
            }
            voiceDo.setSplitStr(recordDetail);
            sendMessage(handler, voiceDo);

        } catch (Exception e) {
            voiceDo.setResultCode(Constant.VOICE_PARSE_FAIL);
            sendMessage(handler, voiceDo);
        }
    }

    /**
     * 匹配支出分词
     *
     * @param content
     * @return
     */
    private String getZhiChuWord(String content) {
        for (String word : Constant.ZHI_CHU_WORD) {
            if (content.contains(word))
                return word;
        }
        return "";
    }

    /**
     * 匹配收入分词
     *
     * @param content
     * @return
     */
    private String getShouRuWord(String content) {
        for (String word : Constant.SHOU_RU_WORD) {
            if (content.contains(word))
                return word;
        }
        return "";
    }

    public String getWidgetRecordDayCount() {
        int count = recordLocalDAO.getRecordDayCount(_context);
        return count == 0 ? "" : "您已坚持记账" + count + "天";
    }

    RecordLocalDAO recordLocalDAO = new RecordLocalDAO();
    RecordTypeLocalDao recordTypeDao = new RecordTypeLocalDao();
    ConfigLocalDao configLocalDao = new ConfigLocalDao();
    AccountLocalDao accountLocalDao = new AccountLocalDao();
    AccountManager accountManager = new AccountManager(_context);

    private final static String RECORD_INDEX_UPDATE = "RECORD_INDEX_UPDATE";

}
