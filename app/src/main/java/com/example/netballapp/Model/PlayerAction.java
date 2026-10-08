package com.example.netballapp.Model;

public class PlayerAction {
    private Long action_ID;
    private String action_Type;
    private String action_TimeStamp;
    private String action_Period;
    private Long player_ID;
    private Long game_ID;

    public PlayerAction(String action_Type, String action_TimeStamp, String action_Period, Long player_ID, Long game_ID) {
        this.action_Type = action_Type;
        this.action_TimeStamp = action_TimeStamp;
        this.action_Period = action_Period;
        this.player_ID = player_ID;
        this.game_ID = game_ID;
    }

    public String getAction_Period() {
        return action_Period;
    }

    public Long getAction_ID() {
        return action_ID;
    }

    public void setAction_ID(Long action_ID) {
        this.action_ID = action_ID;
    }

    public Long getPlayer_ID() {
        return player_ID;
    }

    public String getAction_Type() {
        return action_Type;
    }
    public String getAction_TimeStamp() {
        return action_TimeStamp;
    }
}
