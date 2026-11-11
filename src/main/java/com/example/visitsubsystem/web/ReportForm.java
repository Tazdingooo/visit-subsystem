package com.example.visitsubsystem.web;

public class ReportForm {

    private String talkingPoints;
    private String feedback;
    private boolean materialsDistributed;

    public String getTalkingPoints() {
        return talkingPoints;
    }

    public void setTalkingPoints(String talkingPoints) {
        this.talkingPoints = talkingPoints;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public boolean isMaterialsDistributed() {
        return materialsDistributed;
    }

    public void setMaterialsDistributed(boolean materialsDistributed) {
        this.materialsDistributed = materialsDistributed;
    }
}
