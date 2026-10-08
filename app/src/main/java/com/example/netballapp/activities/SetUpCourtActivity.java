package com.example.netballapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.netballapp.Model.Court;
import com.example.netballapp.Model.Player;
import com.example.netballapp.Model.PlayerCoach;
import com.example.netballapp.R;
import com.example.netballapp.adapters.PlayerAdapter;
import com.example.netballapp.adapters.PlayerAdapterCourt;
import com.example.netballapp.api.RetrofitClient;
import com.example.netballapp.api.SuperbaseAPI;
import com.example.netballapp.listeners.OnPlayerClickListener;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SetUpCourtActivity extends AppCompatActivity
{
    private PlayerAdapterCourt adapter;
    private RecyclerView lstPlayers;
    private SuperbaseAPI api;
    private Player selectedPlayer;
    private long currentGameId;
    TextView posGA, posGS, posC, posWA, posWD, posGD, posGK;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up_court);

        currentGameId = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                .getLong("game_ID", -1);

        if (currentGameId == -1) {
            Toast.makeText(this, "Game ID not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        lstPlayers = findViewById(R.id.lstPlayers);
        posGA = findViewById(R.id.posGA);
        posGS = findViewById(R.id.posGS);
        posC = findViewById(R.id.posC);
        posWA = findViewById(R.id.posWA);
        posGD = findViewById(R.id.posGD);
        posGK = findViewById(R.id.posGK);
        posWD=findViewById(R.id.posWD);

        lstPlayers = findViewById(R.id.lstPlayers);
        lstPlayers.setLayoutManager(new LinearLayoutManager(this));

        api = RetrofitClient.getClient().create(SuperbaseAPI.class);

        posGA.setOnClickListener(v -> assignPlayerToPosition("GA"));
        posGS.setOnClickListener(v -> assignPlayerToPosition("GS"));
        posC.setOnClickListener(v -> assignPlayerToPosition("C"));
        posWA.setOnClickListener(v -> assignPlayerToPosition("WA"));
        posWD.setOnClickListener(v -> assignPlayerToPosition("WD"));
        posGD.setOnClickListener(v -> assignPlayerToPosition("GD"));
        posGK.setOnClickListener(v -> assignPlayerToPosition("GK"));

        loadPlayersFromSupabase();
        loadPlayers(currentGameId);
    }

    private void assignPlayerToPosition(String position) {
        if (selectedPlayer == null) {
            Toast.makeText(this, "Please select a player first.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!position.equals(selectedPlayer.getPlayer_position())) {
            Toast.makeText(this, selectedPlayer.getPlayer_FirstName() + " can only play " + selectedPlayer.getPlayer_position(), Toast.LENGTH_SHORT).show();
            return;
        }

        Court assignment = new Court(position, currentGameId, selectedPlayer.getPlayer_ID());

        api.assignPlayerToCourt(assignment).enqueue(new Callback<List<Court>>() {
            @Override
            public void onResponse(Call<List<Court>> call, Response<List<Court>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    String playerDisplay = getInitials(selectedPlayer) + "\n(" + position + ")";

                    switch (position) {
                        case "GA": posGA.setText(playerDisplay); break;
                        case "GS": posGS.setText(playerDisplay); break;
                        case "C":  posC.setText(playerDisplay); break;
                        case "WA": posWA.setText(playerDisplay); break;
                        case "WD": posWD.setText(playerDisplay); break;
                        case "GD": posGD.setText(playerDisplay); break;
                        case "GK": posGK.setText(playerDisplay); break;
                    }

                    adapter.removePlayer(selectedPlayer);
                    selectedPlayer = null;

                } else {
                   // Toast.makeText(SetUpCourtActivity.this, "Assignment failed: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Court>> call, Throwable t) {
                Toast.makeText(SetUpCourtActivity.this, "API error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private String getInitials(Player player) {
        String firstInitial = player.getPlayer_FirstName().substring(0, 1).toUpperCase();
        String lastInitial = player.getPlayer_Surname().substring(0, 1).toUpperCase();
        return firstInitial + "." + lastInitial + ".";
    }

    private void loadPlayersFromSupabase() {
        long coachId = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                .getLong("coach_ID", -1);
        if (coachId == -1) {
            Toast.makeText(this, "Coach ID not found.", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<List<Player>> call = api.getPlayersForCoach("*,player_coach!inner(*)", "eq." + coachId);

        call.enqueue(new Callback<List<Player>>() {
            @Override
            public void onResponse(Call<List<Player>> call, Response<List<Player>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Player> playerList = response.body();
                    adapter = new PlayerAdapterCourt(playerList, SetUpCourtActivity.this, player -> {selectedPlayer = player;
                    });
                    lstPlayers.setAdapter(adapter);
                } else {
                    Toast.makeText(SetUpCourtActivity.this, "Failed to load players", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<List<Player>> call, Throwable t) {
                Toast.makeText(SetUpCourtActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    private boolean isTeamComplete() {
        return !posGA.getText().toString().equals("GA") &&
                !posGS.getText().toString().equals("GS") &&
                !posC.getText().toString().equals("C") &&
                !posWA.getText().toString().equals("WA") &&
                !posWD.getText().toString().equals("WD") &&
                !posGD.getText().toString().equals("GD") &&
                !posGK.getText().toString().equals("GK");
    }

    private void loadPlayers(Long gameId) {
        Call<List<Court>> call = api.getCourtAssignments("eq." + gameId);

        call.enqueue(new Callback<List<Court>>() {
            @Override
            public void onResponse(Call<List<Court>> call, Response<List<Court>> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(SetUpCourtActivity.this, "Server error: " + response.code(), Toast.LENGTH_SHORT).show();
                    return;
                }

                List<Court> courtAssignments = response.body();
                if (courtAssignments != null) {
                    for (Court court : courtAssignments) {
                        Long playerId = court.getPlayer_id();
                        String pos = court.getCourt_position_field();

                        loadPlayerDetails(playerId, pos);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Court>> call, Throwable t) {
                Toast.makeText(SetUpCourtActivity.this, "Network error: " + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPlayerDetails(Long playerId, String pos) {
        Call<List<Player>> call = api.getPlayerById("eq." + playerId);
        call.enqueue(new Callback<List<Player>>() {
            @Override
            public void onResponse(Call<List<Player>> call, Response<List<Player>> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(SetUpCourtActivity.this, "Error loading player: " + response.code(), Toast.LENGTH_SHORT).show();
                    return;
                }

                List<Player> players = response.body();
                if (players != null && !players.isEmpty()) {
                    Player player = players.get(0);
                    String playerName = player.getPlayer_FirstName() + " " + player.getPlayer_Surname();

                    int resId = getResources().getIdentifier("pos" + pos, "id", getPackageName());
                    TextView posText = findViewById(resId);
                    if (posText != null) {
                        posText.setText(playerName);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Player>> call, Throwable t) {
                Toast.makeText(SetUpCourtActivity.this, "Network error: " + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void onBackClicked(View view) {
        Intent intent = new Intent(SetUpCourtActivity.this, SetUpNewGameActivity.class);
        startActivity(intent);
        finish();
    }

    public void onNextClicked(View view) {
        if (!isTeamComplete()) {
            Toast.makeText(this, "Please assign all 7 positions before continuing.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(SetUpCourtActivity.this, SetBenchPlayersActivity.class);
        intent.putExtra("players_list", new ArrayList<>(adapter.getPlayers()));
        startActivity(intent);
        finish();
    }
}