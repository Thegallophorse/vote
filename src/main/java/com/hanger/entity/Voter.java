package com.hanger.entity;

import com.alibaba.fastjson.annotation.JSONField;
import javax.persistence.Id;
import javax.persistence.Table;



@Table(name = "voter")
public class Voter {
    @Id
    @JSONField(ordinal = 1)
    private String vId;
    @JSONField(ordinal = 2)
    private String vPassword;
    @JSONField(ordinal = 3)
    private String vName;
    @JSONField(ordinal = 4)
    private String vMid;
    @JSONField(ordinal = 5)
    private Integer vStatus;//数据库如果有默认值，想加默认值则此处不必添加默认值


    public Voter() {
    }

    public Voter(Integer vStatus) {
        this.vStatus = vStatus;
    }

    public String getvId() {
        return vId;
    }

    public void setvId(String vId) {
        this.vId = vId;
    }

    public String getvPassword() {
        return vPassword;
    }

    public void setvPassword(String vPassword) {
        this.vPassword = vPassword;
    }

    public String getvName() {
        return vName;
    }

    public void setvName(String vName) {
        this.vName = vName;
    }

    public String getvMid() {
        return vMid;
    }

    public void setvMid(String vMid) {
        this.vMid = vMid;
    }

    public Integer getvStatus() {
        return vStatus;
    }

    public void setvStatus(Integer vStatus) {
        this.vStatus = vStatus;
    }


    @Override
    public String toString() {
        return "{\"vId\":\"" + vId + "\"," +
                "\"vPassword\":\"" + vPassword + "\"," +
                "\"vName\":\"" + vName + "\"," +
                "\"vMid\":\"" + vMid + "\"," +
                "\"vStatus\":\"" + vStatus + "\"}";
    }
}
