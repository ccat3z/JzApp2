package com.suda.jzapp.dao.greendao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

import com.suda.jzapp.dao.greendao.Account;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "ACCOUNT".
*/
public class AccountDao extends AbstractDao<Account, Long> {

    public static final String TABLENAME = "ACCOUNT";

    /**
     * Properties of entity Account.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property AccountID = new Property(1, Long.class, "AccountID", false, "ACCOUNT_ID");
        public final static Property AccountTypeID = new Property(2, Integer.class, "AccountTypeID", false, "ACCOUNT_TYPE_ID");
        public final static Property AccountName = new Property(3, String.class, "AccountName", false, "ACCOUNT_NAME");
        public final static Property AccountMoney = new Property(4, Double.class, "AccountMoney", false, "ACCOUNT_MONEY");
        public final static Property AccountRemark = new Property(5, String.class, "AccountRemark", false, "ACCOUNT_REMARK");
        public final static Property AccountColor = new Property(6, String.class, "AccountColor", false, "ACCOUNT_COLOR");
        public final static Property SyncStatus = new Property(7, Boolean.class, "SyncStatus", false, "SYNC_STATUS");
        public final static Property IsDel = new Property(8, Boolean.class, "isDel", false, "IS_DEL");
        public final static Property ObjectID = new Property(9, String.class, "ObjectID", false, "OBJECT_ID");
    };


    public AccountDao(DaoConfig config) {
        super(config);
    }
    
    public AccountDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"ACCOUNT\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"ACCOUNT_ID\" INTEGER," + // 1: AccountID
                "\"ACCOUNT_TYPE_ID\" INTEGER," + // 2: AccountTypeID
                "\"ACCOUNT_NAME\" TEXT," + // 3: AccountName
                "\"ACCOUNT_MONEY\" REAL," + // 4: AccountMoney
                "\"ACCOUNT_REMARK\" TEXT," + // 5: AccountRemark
                "\"ACCOUNT_COLOR\" TEXT," + // 6: AccountColor
                "\"SYNC_STATUS\" INTEGER," + // 7: SyncStatus
                "\"IS_DEL\" INTEGER," + // 8: isDel
                "\"OBJECT_ID\" TEXT);"); // 9: ObjectID
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"ACCOUNT\"";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, Account entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        Long AccountID = entity.getAccountID();
        if (AccountID != null) {
            stmt.bindLong(2, AccountID);
        }
 
        Integer AccountTypeID = entity.getAccountTypeID();
        if (AccountTypeID != null) {
            stmt.bindLong(3, AccountTypeID);
        }
 
        String AccountName = entity.getAccountName();
        if (AccountName != null) {
            stmt.bindString(4, AccountName);
        }
 
        Double AccountMoney = entity.getAccountMoney();
        if (AccountMoney != null) {
            stmt.bindDouble(5, AccountMoney);
        }
 
        String AccountRemark = entity.getAccountRemark();
        if (AccountRemark != null) {
            stmt.bindString(6, AccountRemark);
        }
 
        String AccountColor = entity.getAccountColor();
        if (AccountColor != null) {
            stmt.bindString(7, AccountColor);
        }
 
        Boolean SyncStatus = entity.getSyncStatus();
        if (SyncStatus != null) {
            stmt.bindLong(8, SyncStatus ? 1L: 0L);
        }
 
        Boolean isDel = entity.getIsDel();
        if (isDel != null) {
            stmt.bindLong(9, isDel ? 1L: 0L);
        }
 
        String ObjectID = entity.getObjectID();
        if (ObjectID != null) {
            stmt.bindString(10, ObjectID);
        }
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public Account readEntity(Cursor cursor, int offset) {
        Account entity = new Account( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getLong(offset + 1), // AccountID
            cursor.isNull(offset + 2) ? null : cursor.getInt(offset + 2), // AccountTypeID
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // AccountName
            cursor.isNull(offset + 4) ? null : cursor.getDouble(offset + 4), // AccountMoney
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // AccountRemark
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // AccountColor
            cursor.isNull(offset + 7) ? null : cursor.getShort(offset + 7) != 0, // SyncStatus
            cursor.isNull(offset + 8) ? null : cursor.getShort(offset + 8) != 0, // isDel
            cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9) // ObjectID
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, Account entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setAccountID(cursor.isNull(offset + 1) ? null : cursor.getLong(offset + 1));
        entity.setAccountTypeID(cursor.isNull(offset + 2) ? null : cursor.getInt(offset + 2));
        entity.setAccountName(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setAccountMoney(cursor.isNull(offset + 4) ? null : cursor.getDouble(offset + 4));
        entity.setAccountRemark(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setAccountColor(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setSyncStatus(cursor.isNull(offset + 7) ? null : cursor.getShort(offset + 7) != 0);
        entity.setIsDel(cursor.isNull(offset + 8) ? null : cursor.getShort(offset + 8) != 0);
        entity.setObjectID(cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(Account entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(Account entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
}
