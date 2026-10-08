package com.example.netballapp.Model;

import com.google.gson.annotations.SerializedName;

public class Game {
    @SerializedName("game_ID")
    private Long game_ID;

    @SerializedName("game_name")
    private String game_Name;

    @SerializedName("game_opposition_name")
    private String game_OppositionName;

    @SerializedName("game_venue")
    private String game_Venue;

    @SerializedName("game_date")
    private String game_Date;

    @SerializedName("game_type")
    private String game_Type;

    @SerializedName("game_madibaz_score")
    private Integer game_MadibazScore;

    @SerializedName("game_opposition_score")
    private Integer game_OppositionScore;

    @SerializedName("game_timer")
    private Integer game_Timer;

    @SerializedName("game_current_centre_pass_team")
    private String game_CurrentCentrePassTeam;

    @SerializedName("game_coach_note")
    private String game_CoachNote;

    public String getGame_Venue() {
        return game_Venue;
    }

    public Integer getGame_MadibazScore() {
        return game_MadibazScore;
    }

    public Integer getGame_OppositionScore() {
        return game_OppositionScore;
    }

    public Game(String game_Name, String game_OppositionName, String game_Venue, String game_Date, String game_Type, Integer game_MadibazScore, Integer game_OppositionScore, Integer game_Timer, String game_CurrentCentrePassTeam, String game_CoachNote) {
        this.game_Name = game_Name;
        this.game_OppositionName = game_OppositionName;
        this.game_Venue = game_Venue;
        this.game_Date = game_Date;
        this.game_Type = game_Type;
        this.game_MadibazScore = game_MadibazScore;
        this.game_OppositionScore = game_OppositionScore;
        this.game_Timer = game_Timer;
        this.game_CurrentCentrePassTeam = game_CurrentCentrePassTeam;
        this.game_CoachNote = game_CoachNote;
    }
    public Game(String game_Name, String game_OppositionName, String game_Venue, String game_Date, String game_Type) {
        this.game_Name = game_Name;
        this.game_OppositionName = game_OppositionName;
        this.game_Venue = game_Venue;
        this.game_Date = game_Date;
        this.game_Type = game_Type;
    }

    public Game() {
    }

    public String getGame_OppositionName() {
        return game_OppositionName;
    }

    public String getGame_Date() {
        return game_Date;
    }

    public String getGame_Name() {
        return game_Name;
    }

    public Long getGame_ID() {
        return game_ID;
    }

    public String getGame_Type() {
        return game_Type;
    }
}
