package com.example.netballapp.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.netballapp.Model.Game;
import com.example.netballapp.Model.Player;
import com.example.netballapp.Model.SessionManager;
import com.example.netballapp.adapters.PlayerAdapter;
import com.example.netballapp.R;
import com.example.netballapp.api.RetrofitClient;
import com.example.netballapp.api.SuperbaseAPI;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Player_Profiles extends AppCompatActivity {
    private SuperbaseAPI api;
    private PlayerAdapter adapter;
    private List<Player> players;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_profiles);

        RecyclerView recyclerView = findViewById(R.id.playerRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        players = new ArrayList<>();

        adapter = new PlayerAdapter(this, players, (player, position) ->
                new AlertDialog.Builder(Player_Profiles.this)
                        .setTitle("Confirm Deletion")
                        .setMessage("Delete " + player.getPlayer_FirstName() + "?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            deletePlayerFromAPI(player.getPlayer_ID(), position);
                        })
                        .setNegativeButton("Cancel", null)
                        .show()
        );

        recyclerView.setAdapter(adapter);

        api = RetrofitClient.getClient().create(SuperbaseAPI.class);
        loadPlayersFromAPI();
    }

    private void loadPlayersFromAPI() {
        long coachId = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                .getLong("coach_ID", -1);

        Call<List<Player>> call = api.getPlayersForCoach("*,player_coach!inner(*)", "eq." + coachId);

        call.enqueue(new Callback<List<Player>>() {
            @Override
            public void onResponse(Call<List<Player>> call, Response<List<Player>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    players.clear();
                    players.addAll(response.body());
                    adapter.notifyDataSetChanged();

                    toggleEmptyView();
                } else {
                    Log.e("API", "Unsuccessful: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Player>> call, Throwable t) {
                Log.e("API", "Failed: " + t.getMessage());
            }
        });
    }

    private void deletePlayerFromAPI(long playerId, int position) {
        Call<Void> call = api.deletePlayer("eq." + playerId);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    adapter.removePlayer(position);
                    toggleEmptyView();
                    Toast.makeText(Player_Profiles.this, "Player deleted", Toast.LENGTH_SHORT).show();
                }
                else {
                    String errorMsg = "Error code: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += "\n" + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(Player_Profiles.this, errorMsg, Toast.LENGTH_LONG).show();
                    Log.e("API_DELETE_PLAYER", errorMsg);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(Player_Profiles.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("API_DELETE_PLAYER", "Failure", t);
            }
        });
    }

    private void toggleEmptyView() {
        View emptyView = findViewById(R.id.emptyView);
        RecyclerView recyclerView = findViewById(R.id.playerRecyclerView);

        if (players.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
    public void onBackClicked(View view) {
        startActivity(new Intent(this, DashboardActivity.class));
        finish();
    }

    public void onAddPlayerClicked(View view) {
        startActivity(new Intent(this, AddPlayer.class));
        finish();
    }

    public void onLogoutClicked(View view) {
        SessionManager.logout(this);
    }
}
