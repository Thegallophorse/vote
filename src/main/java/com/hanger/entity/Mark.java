package com.hanger.entity;

public class Mark {
    private Integer cId;
    private String cScore;


    public Mark() {
    }

    public Mark(Integer cId, String cScore) {
        this.cId = cId;
        this.cScore = cScore;
    }

    public Integer getcId() {
        return cId;
    }

    public void setcId(Integer cId) {
        this.cId = cId;
    }

    public String getcScore() {
        return cScore;
    }

    public void setcScore(String cScore) {
        this.cScore = cScore;
    }


    @Override
    public String toString() {
        return "m{" +
                "cId='" + cId + '\'' +
                ", cScore='" + cScore + '\'' +
                '}';
    }
}
