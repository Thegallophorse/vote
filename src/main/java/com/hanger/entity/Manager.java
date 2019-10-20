package com.hanger.entity;

import com.alibaba.fastjson.annotation.JSONField;
import javax.persistence.Id;
import javax.persistence.Table;



@Table(name = "manager")
public class Manager {
    @Id
    @JSONField(ordinal = 1)
    private String mId;
    @JSONField(ordinal = 2)
    private String mPassword;
    @JSONField(ordinal = 3)
    //复杂数据类型
    private String mFields;
    @JSONField(ordinal = 4)
    //复杂数据类型
    private String mFiles;

    public Manager() {
    }

    public Manager(String mId, String mPassword) {
        this.mId = mId;
        this.mPassword = mPassword;
    }

    public String getmId() {
        return mId;
    }

    public String getmPassword() {
        return mPassword;
    }

    public void setmPassword(String mPassword) {
        this.mPassword = mPassword;
    }

    public String getmFields() {
        return mFields;
    }

    public void setmFields(String mFields) {
        this.mFields = mFields;
    }

    public String getmFiles() {
        return mFiles;
    }

    public void setmFiles(String mFiles) {
        this.mFiles = mFiles;
    }

    @Override
    public String toString() {
        return "{\"mId\":\"" + mId + "\"," +
                "\"mPassword\":\"" + mPassword +
                "\"mFields\":" + mFields +
                "\"mFiles\":" + mFiles +"}";
    }

}

