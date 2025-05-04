package com.dating.flairbit.dto;

public class MatchTriggerEvent {
    private String groupId;

    public MatchTriggerEvent() {
    }

    public MatchTriggerEvent(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    @Override
    public String toString() {
        return "MatchTriggerEvent{groupId='" + groupId + "'}";
    }
}
