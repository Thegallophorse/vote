package com.hanger.entity;

import com.alibaba.fastjson.annotation.JSONField;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "candidate")
public class Candidate {
    @Id
    @JSONField(ordinal = 1)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cId;

    @JSONField(ordinal = 2)
    //复杂数据类型
    private String cInfo;

    @JSONField(ordinal = 3)
    private Long cSum;
    @JSONField(ordinal = 4)
    private Integer cAbstention;
    @JSONField(ordinal = 5)
    private Integer cDissenting;
    @JSONField(ordinal = 6)
    private String cMid;
    @JSONField(ordinal = 7)
    private String cVcid;



    public Candidate() {
    }

    public Candidate(String cInfo, String cMid, String cVcid) {
        this.cInfo = cInfo;
        this.cMid = cMid;
        this.cVcid = cVcid;
    }

    public Long getcId() {
        return cId;
    }

    public void setcId(Long cId) {
        this.cId = cId;
    }

    public String getcInfo() {
        return cInfo;
    }

    public void setcInfo(String cInfo) {
        this.cInfo = cInfo;
    }

    public Long getcSum() {
        return cSum;
    }

    public void setcSum(Long cSum) {
        this.cSum = cSum;
    }


    public Integer getcAbstention() {
        return cAbstention;
    }

    public void setcAbstention(Integer cAbstention) {
        this.cAbstention = cAbstention;
    }

    public Integer getcDissenting() {
        return cDissenting;
    }

    public void setcDissenting(Integer cDissenting) {
        this.cDissenting = cDissenting;
    }

    public String getcMid() {
        return cMid;
    }

    public void setcMid(String cMid) {
        this.cMid = cMid;
    }

    public String getcVcid() {
        return cVcid;
    }

    public void setcVcid(String cVcid) {
        this.cVcid = cVcid;
    }

    @Override
    public String toString() {
        return "{\"cId\":\"" + cId + "\"," +
                "\"cInfo\":" + cInfo + "," +
                "\"cSum\":\"" + cSum + "\"," +
                "\"cAbstention\":\"" + cAbstention + "\"," +
                "\"cDissenting\":\"" + cDissenting + "\"," +
                "\"cMid\":\"" + cMid + "\"," +
                "\"cVcid\":\"" + cVcid + "\"" + "}";
    }



}
