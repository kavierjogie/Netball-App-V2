package com.example.netballapp.Model;

public class Coach {
    private Long coach_ID;
    private String coach_firstname;
    private String coach_surname;
    private String coach_role;
    private String coach_username;
    private String coach_password;

    public Coach(String coach_firstname, String coach_surname, String coach_role, String coach_username, String coach_password) {
        this.coach_firstname = coach_firstname;
        this.coach_surname = coach_surname;
        this.coach_role = coach_role;
        this.coach_username = coach_username;
        this.coach_password = coach_password;
    }

    public Long getCoach_ID() {
        return coach_ID;
    }

    public void setCoach_ID(Long coach_ID) {
        this.coach_ID = coach_ID;
    }

    public String getCoach_firstname() {
        return coach_firstname;
    }


    public String getCoach_surname() {
        return coach_surname;
    }

    public void setCoach_surname(String coach_surname) {
        this.coach_surname = coach_surname;
    }

    public String getCoach_role() {
        return coach_role;
    }

    public void setCoach_role(String coach_role) {
        this.coach_role = coach_role;
    }

    public String getCoach_username() {
        return coach_username;
    }

    public void setCoach_username(String coach_username) {
        this.coach_username = coach_username;
    }

    public String getCoach_password() {
        return coach_password;
    }

    public void setCoach_password(String coach_password) {
        this.coach_password = coach_password;
    }
}
