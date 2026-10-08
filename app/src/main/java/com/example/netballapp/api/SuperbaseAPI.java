package com.example.netballapp.api;

import com.example.netballapp.Model.Coach;
import com.example.netballapp.Model.CoachGame;
import com.example.netballapp.Model.Court;
import com.example.netballapp.Model.Game;
import com.example.netballapp.Model.Player;
import com.example.netballapp.Model.PlayerAction;
import com.example.netballapp.Model.PlayerCoach;
import com.example.netballapp.Model.PlayerStatsView;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.Query;
import retrofit2.http.POST;

public interface SuperbaseAPI {

    @PATCH("rest/v1/coach")
    @Headers({"Prefer: return=representation"})
    Call<List<Coach>> updateCoachProfile(@Query("coach_ID") String idFilter, @Body Coach updatedCoach);

    @GET("rest/v1/coach")
    Call<List<Coach>> getCoachById(@Query("coach_ID") String idFilter);

    @GET("rest/v1/coach")
    Call<List<Coach>> loginCoach(@Query("coach_username") String username, @Query("coach_password") String password);

    @POST("rest/v1/coach")
    @Headers({"Prefer: return=representation"})
    Call<List<Coach>> registerCoach(@Body Coach coach);

    @GET("rest/v1/player") Call<List<Player>> getPlayerById(@Query("player_ID") String idFilter);

    @POST("rest/v1/player")
    @Headers({"Prefer: return=representation"})
    Call<List<Player>> registerPlayer(@Body Player player);

    @POST("rest/v1/game")
    @Headers({"Prefer: return=representation"})
    Call<List<Game>> setUpNewGame(@Body Game game);

    @POST("rest/v1/court")
    @Headers({"Prefer: resolution=merge-duplicates", "Prefer: return=representation"})
    Call<List<Court>> assignPlayerToCourt(@Body Court assignment);

    @GET("rest/v1/court")
    Call<List<Court>> getCourtAssignments(@Query("game_id") String gameId);

    @POST("rest/v1/player_action")
    Call<Void> recordPlayerAction(@Body PlayerAction action);

    @GET("rest/v1/game")
    Call<List<Game>> getGameById(@Query("game_ID") String gameIdFilter);

    @POST("rest/v1/player_coach")
    Call<PlayerCoach> assignPlayerToCoach(@Body PlayerCoach playerCoach);

    @POST("rest/v1/coach_game")
    Call<CoachGame> assignCoachToGame(@Body CoachGame coachGame);

    @GET("rest/v1/player_stats")
    Call<List<PlayerStatsView>> getPlayerStatsByGame(@Query("game_ID") String gameIdFilter);

    @GET("rest/v1/game")
    Call<List<Game>> getGamesForCoach(@Query("select") String select, @Query("coach_game.coach_ID") String coachIdEq);

    @GET("rest/v1/player")
    Call<List<Player>> getPlayersForCoach(@Query("select") String select, @Query("player_coach.coach_ID") String coachIdEq);

    @DELETE("rest/v1/game")
    @Headers("Prefer: return=minimal")
    Call<Void> deleteGame(@Query("game_ID") String idFilter);

    @DELETE("rest/v1/player")
    @Headers("Prefer: return=minimal")
    Call<Void> deletePlayer(@Query("player_ID") String idFilter);

    @PATCH("rest/v1/game")
    @Headers({"Prefer: return=representation"})
    Call<List<Game>> updateGameScore(@Query("game_ID") String gameIdFilter, @Body Map<String, Object> updates);

    @GET("rest/v1/player_stats")
    Call<List<PlayerStatsView>> getPlayerStatsByGameAndHalf(@Query("game_ID") String gameId, @Query("half") String half);

    @PATCH("rest/v1/court")
    Call<List<Court>> updateCourt(@Query("court_ID") String courtIdFilter, @Body Map<String, Object> updates);

    @GET("rest/v1/player")
    Call<List<Player>> getPlayersByIds(@Query("player_ID") String ids);

    @PATCH("rest/v1/game")
    @Headers({"Prefer: return=representation"})
    Call<List<Game>> updateGame(@Query("game_ID") String id, @Body Game game);

    @PATCH("rest/v1/player")
    @Headers({"Prefer: return=representation"})
    Call<List<Player>> updatePlayerProfile(@Query("player_ID") String idFilter, @Body Player updatedPlayer);
}

