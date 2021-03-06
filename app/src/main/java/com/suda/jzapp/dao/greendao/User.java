package com.suda.jzapp.dao.greendao;

import org.greenrobot.greendao.annotation.*;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table "USER".
 */
@Entity
public class User {

    @Id(autoincrement = true)
    private Long id;
    private String userId;
    private String userName;
    private String headImage;
    private Long userCode;

    @Generated(hash = 586692638)
    public User() {
    }

    public User(Long id) {
        this.id = id;
    }

    @Generated(hash = 1699084683)
    public User(Long id, String userId, String userName, String headImage, Long userCode) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.headImage = headImage;
        this.userCode = userCode;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getHeadImage() {
        return headImage;
    }

    public void setHeadImage(String headImage) {
        this.headImage = headImage;
    }

    public Long getUserCode() {
        return userCode;
    }

    public void setUserCode(Long userCode) {
        this.userCode = userCode;
    }

}
