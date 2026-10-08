package com.example.netballapp.Model;

public class PlayerCoach {
    private Long coach_ID;
    private Long player_ID;
    public PlayerCoach(Long coach_ID, Long player_ID) {
        this.coach_ID = coach_ID;
        this.player_ID = player_ID;
    }

    public Long getCoach_ID() { return coach_ID; }
    public void setCoach_ID(Long coach_ID) { this.coach_ID = coach_ID; }

    public Long getPlayer_ID() { return player_ID; }
    public void setPlayer_ID(Long player_ID) { this.player_ID = player_ID; }
}
