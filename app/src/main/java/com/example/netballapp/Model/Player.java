package com.example.netballapp.Model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Player implements Serializable {
    @SerializedName("player_ID")
    private Long player_ID;

    @SerializedName("player_firstname")
    private String player_FirstName;

    @SerializedName("player_surname")
    private String player_Surname;

    @SerializedName("player_number")
    private Integer player_Number;
    @SerializedName("player_position")
    private String player_position;

    @SerializedName("player_dob")
    private String player_DOB;

    @SerializedName("player_height")
    private Integer player_Height;

    public Player(String player_FirstName, String player_Surname, Integer player_Number, String player_position, String player_DOB, Integer player_Height) {
        this.player_FirstName = player_FirstName;
        this.player_Surname = player_Surname;
        this.player_Number = player_Number;
        this.player_position = player_position;
        this.player_DOB = player_DOB;
        this.player_Height = player_Height;
    }
    public Player(String player_FirstName, String player_Surname) {
        this.player_FirstName = player_FirstName;
        this.player_Surname = player_Surname;
    }

    public Player(long player_ID, String player_FirstName, String player_Surname, String player_position) {
        this.player_ID = player_ID;
        this.player_FirstName = player_FirstName;
        this.player_Surname = player_Surname;
        this.player_position = player_position;
    }
    public Player(Long player_ID) {
        this.player_ID = player_ID;
    }

    public String getPlayer_FirstName() {
        return player_FirstName;
    }

    public String getPlayer_Surname() {
        return player_Surname;
    }

    public long getPlayer_ID() {
        return player_ID;
    }

    public Integer getPlayer_Number() {
        return player_Number;
    }

    public String getPlayer_position() {
        return player_position;
    }

    public String getPlayer_DOB() {
        return player_DOB;
    }


    public Integer getPlayer_Height() {
        return player_Height;
    }
}
