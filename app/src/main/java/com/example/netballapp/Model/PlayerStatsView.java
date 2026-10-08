package com.example.netballapp.Model;

public class PlayerStatsView {
    private long game_ID;
    private long player_ID;
    private String player_name;
    private int goal;
    private int penalty_goal;
    private int goal_missed;
    private int for_;
    private int against;
    private int drop_ball;
    private int held_ball;
    private int stepping;
    private int break_;
    private int contact;
    private int obstruction;
    private int centre_pass_receive;
    private int goal_assist;

    private int offensive_rebound;
    private int defensive_rebound;
    private int deflection;
    private int intercept;
    private String half;

    public String getHalf() {
        return half;
    }

    public void setHalf(String half) {
        this.half = half;
    }

    public long getGame_ID() {
        return game_ID;
    }

    public long getPlayer_ID() {
        return player_ID;
    }

    public String getPlayer_name() {
        return player_name;
    }

    public int getGoal() {
        return goal;
    }

    public int getPenalty_goal() {
        return penalty_goal;
    }

    public int getGoal_missed() {
        return goal_missed;
    }

    public int getFor_() {
        return for_;
    }

    public int getAgainst() {
        return against;
    }

    public int getDrop_ball() {
        return drop_ball;
    }

    public int getHeld_ball() {
        return held_ball;
    }

    public int getStepping() {
        return stepping;
    }

    public int getBreak_() {
        return break_;
    }

    public int getContact() {
        return contact;
    }

    public int getObstruction() {
        return obstruction;
    }

    public int getCentre_pass_receive() {
        return centre_pass_receive;
    }

    public int getGoal_assist() {
        return goal_assist;
    }

    public int getOffensive_rebound() {
        return offensive_rebound;
    }

    public int getDefensive_rebound() {
        return defensive_rebound;
    }

    public int getDeflection() {
        return deflection;
    }

    public int getIntercept() {
        return intercept;
    }

    public void setGame_ID(long game_ID) {
        this.game_ID = game_ID;
    }

    public void setPlayer_ID(long player_ID) {
        this.player_ID = player_ID;
    }

    public void setPlayer_name(String player_name) {
        this.player_name = player_name;
    }

    public void setGoal(int goal) {
        this.goal = goal;
    }

    public void setPenalty_goal(int penalty_goal) {
        this.penalty_goal = penalty_goal;
    }

    public void setGoal_missed(int goal_missed) {
        this.goal_missed = goal_missed;
    }

    public void setFor_(int for_) {
        this.for_ = for_;
    }

    public void setAgainst(int against) {
        this.against = against;
    }

    public void setDrop_ball(int drop_ball) {
        this.drop_ball = drop_ball;
    }

    public void setHeld_ball(int held_ball) {
        this.held_ball = held_ball;
    }

    public void setStepping(int stepping) {
        this.stepping = stepping;
    }

    public void setBreak_(int break_) {
        this.break_ = break_;
    }

    public void setContact(int contact) {
        this.contact = contact;
    }

    public void setObstruction(int obstruction) {
        this.obstruction = obstruction;
    }

    public void setCentre_pass_receive(int centre_pass_receive) {
        this.centre_pass_receive = centre_pass_receive;
    }

    public void setGoal_assist(int goal_assist) {
        this.goal_assist = goal_assist;
    }

    public void setOffensive_rebound(int offensive_rebound) {
        this.offensive_rebound = offensive_rebound;
    }

    public void setDefensive_rebound(int defensive_rebound) {
        this.defensive_rebound = defensive_rebound;
    }

    public void setDeflection(int deflection) {
        this.deflection = deflection;
    }

    public void setIntercept(int intercept) {
        this.intercept = intercept;
    }
}
