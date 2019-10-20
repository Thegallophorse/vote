package com.hanger.entity;

import com.alibaba.fastjson.annotation.JSONField;
import javax.persistence.Id;
import javax.persistence.Table;


@Table(name = "vote")
public class Vote {
    @Id
    @JSONField(ordinal = 1)
    private String vcId;
    @JSONField(ordinal = 2)
    private String vcMid;
    @JSONField(ordinal = 3)
    private String vcSname;
    @JSONField(ordinal = 4)
    private String vcTname;
    @JSONField(ordinal = 5)
    private String vcTheme;
    @JSONField(ordinal = 6)
    private String vcMode;
    @JSONField(ordinal = 7)
    private String vcWinnum;
    @JSONField(ordinal = 8)
    private String vcAllownum;

    @JSONField(ordinal = 9)
    //复杂数据类型
    private String vcVids;

    @JSONField(ordinal = 10)
    //复杂数据类型
    private String vcCids;

    @JSONField(ordinal = 11)
    //复杂数据类型
    private String vcWincids;

    public Vote() {
    }

    public Vote(String vcId, String vcWincids) {
        this.vcId = vcId;
        this.vcWincids = vcWincids;
    }

    public Vote(String vcId, String vcMid, String vcSname, String vcTname, String vcTheme, String vcMode, String vcWinnum, String vcAllownum) {
        this.vcId = vcId;
        this.vcMid = vcMid;
        this.vcSname = vcSname;
        this.vcTname = vcTname;
        this.vcTheme = vcTheme;
        this.vcMode = vcMode;
        this.vcWinnum = vcWinnum;
        this.vcAllownum = vcAllownum;
    }


    public Vote(String vcId, String vcMid, String vcSname, String vcTname, String vcTheme, String vcMode, String vcWinnum, String vcAllownum, String vcVids, String vcCids) {
        this.vcId = vcId;
        this.vcMid = vcMid;
        this.vcSname = vcSname;
        this.vcTname = vcTname;
        this.vcTheme = vcTheme;
        this.vcMode = vcMode;
        this.vcWinnum = vcWinnum;
        this.vcAllownum = vcAllownum;
        this.vcVids = vcVids;
        this.vcCids = vcCids;
    }

    public Vote(String vcId, String vcMid, String vcSname, String vcTname, String vcTheme, String vcMode, String vcWinnum, String vcAllownum, String vcVids, String vcCids, String vcWincids) {
        this.vcId = vcId;
        this.vcMid = vcMid;
        this.vcSname = vcSname;
        this.vcTname = vcTname;
        this.vcTheme = vcTheme;
        this.vcMode = vcMode;
        this.vcWinnum = vcWinnum;
        this.vcAllownum = vcAllownum;
        this.vcVids = vcVids;
        this.vcCids = vcCids;
        this.vcWincids = vcWincids;
    }

    public String getVcId() {
        return vcId;
    }

    public void setVcId(String vcId) {
        this.vcId = vcId;
    }

    public String getVcMid() {
        return vcMid;
    }

    public void setVcMid(String vcMid) {
        this.vcMid = vcMid;
    }

    public String getVcSname() {
        return vcSname;
    }

    public void setVcSname(String vcSname) {
        this.vcSname = vcSname;
    }

    public String getVcTname() {
        return vcTname;
    }

    public void setVcTname(String vcTname) {
        this.vcTname = vcTname;
    }

    public String getVcTheme() {
        return vcTheme;
    }

    public void setVcTheme(String vcTheme) {
        this.vcTheme = vcTheme;
    }

    public String getVcMode() {
        return vcMode;
    }

    public void setVcMode(String vcMode) {
        this.vcMode = vcMode;
    }

    public String getVcWinnum() {
        return vcWinnum;
    }

    public void setVcWinnum(String vcWinnum) {
        this.vcWinnum = vcWinnum;
    }

    public String getVcAllownum() {
        return vcAllownum;
    }

    public void setVcAllownum(String vcAllownum) {
        this.vcAllownum = vcAllownum;
    }

    public String getVcVids() {
        return vcVids;
    }

    public void setVcVids(String vcVids) {
        this.vcVids = vcVids;
    }

    public String getVcCids() {
        return vcCids;
    }

    public void setVcCids(String vcCids) {
        this.vcCids = vcCids;
    }

    public String getVcWincids() {
        return vcWincids;
    }

    public void setVcWincids(String vcWincids) {
        this.vcWincids = vcWincids;
    }




    @Override
    public String toString() {
        return "{\"vcId\":\"" + vcId + "\"," +
                "\"vcMid\":\"" + vcMid + "\"," +
                "\"vcSname\":\"" + vcSname + "\"," +
                "\"vcTname\":\"" + vcTname + "\"," +
                "\"vcTheme\":\"" + vcTheme + "\"," +
                "\"vcMode\":\"" + vcMode + "\"," +
                "\"vcWinnum\":\"" + vcWinnum + "\"," +
                "\"vcAllownum\":\"" + vcAllownum + "\"," +
                "\"vcVids\":" + vcVids + "," +
                "\"vcCids\":" + vcCids + "," +
                "\"vcWincids\":" + vcWincids + "}";
    }


    //重载toString,推荐json置null
    public String toString(Object object) {
        return "{\"vcId\":\"" + vcId + "\"," +
                "\"vcSname\":\"" + vcSname + "\"," +
                "\"vcTname\":\"" + vcTname + "\"," +
                "\"vcTheme\":\"" + vcTheme + "\"," +
                "\"vcMode\":\"" + vcMode + "\"," +
                "\"vcWinnum\":\"" + vcWinnum + "\"}";
    }


}
