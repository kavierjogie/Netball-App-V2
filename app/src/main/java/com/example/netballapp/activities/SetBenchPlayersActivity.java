package com.example.netballapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.netballapp.Model.Court;
import com.example.netballapp.Model.Player;
import com.example.netballapp.R;
import com.example.netballapp.Model.UIUtils;
import com.example.netballapp.adapters.PlayerAdapterCourt;
import com.example.netballapp.api.RetrofitClient;
import com.example.netballapp.api.SuperbaseAPI;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SetBenchPlayersActivity extends AppCompatActivity {

    private RecyclerView lstPlayers;
    private PlayerAdapterCourt adapter;
    private SuperbaseAPI api;
    private Player selectedPlayer;
    private LinearLayout attackContainer, defenceContainer, centreContainer;
    private int attackBenchCount = 0;
    private int defenceBenchCount = 0;
    private int centreBenchCount = 0;
    private int totalBenchCount = 0;
    private static final int MAX_TOTAL_BENCH = 4;
    private long currentGameId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_bench_players);

        lstPlayers = findViewById(R.id.lstPlayers);
        attackContainer = findViewById(R.id.attackContainer);
        defenceContainer = findViewById(R.id.defenceContainer);
        centreContainer = findViewById(R.id.centreContainer);

        lstPlayers.setLayoutManager(new LinearLayoutManager(this));

        api = RetrofitClient.getClient().create(SuperbaseAPI.class);

        currentGameId = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                .getLong("game_ID", -1);
        if (currentGameId == -1) {
            Toast.makeText(this, "Game ID not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        List<Player> playerList = (List<Player>) getIntent().getSerializableExtra("players_list");
        adapter = new PlayerAdapterCourt(playerList, this, player -> {
            selectedPlayer = player;
        });
        lstPlayers.setAdapter(adapter);

        attackContainer.setOnClickListener(v -> assignPlayerToBench(attackContainer, "ATTACK"));
        defenceContainer.setOnClickListener(v -> assignPlayerToBench(defenceContainer, "DEFENCE"));
        centreContainer.setOnClickListener(v -> assignPlayerToBench(centreContainer, "CENTRE"));
    }

    private void assignPlayerToBench(LinearLayout container, String section) {
        if (selectedPlayer == null) {
            UIUtils.showMessage(this, "Please select a player first.");
            return;
        }

        String position = selectedPlayer.getPlayer_position();
        boolean valid = false;

        switch (section) {
            case "ATTACK":
                valid = position.equals("GS") || position.equals("GA") || position.equals("WA");
                break;
            case "CENTRE":
                valid = position.equals("C");
                break;
            case "DEFENCE":
                valid = position.equals("WD") || position.equals("GD") || position.equals("GK");
                break;
        }

        if (!valid) {
            UIUtils.showMessage(this, "This player cannot be assigned to " + section + " bench!");
            return;
        }

        if (totalBenchCount >= MAX_TOTAL_BENCH) {
            UIUtils.showMessage(this, "All bench positions are full!");
            return;
        }

        switch (section) {
            case "ATTACK": attackBenchCount++; break;
            case "DEFENCE": defenceBenchCount++; break;
            case "CENTRE": centreBenchCount++; break;
        }
        totalBenchCount++;

        String benchPosition = section + "_" + (section.equals("ATTACK") ? attackBenchCount :
                section.equals("DEFENCE") ? defenceBenchCount : centreBenchCount);

        Court assignment = new Court(benchPosition, currentGameId, selectedPlayer.getPlayer_ID());

        Call<List<Court>> call = api.assignPlayerToCourt(assignment);
        call.enqueue(new Callback<List<Court>>() {
            @Override
            public void onResponse(Call<List<Court>> call, Response<List<Court>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TextView playerView = new TextView(SetBenchPlayersActivity.this);
                    playerView.setText(selectedPlayer.getPlayer_FirstName() + " " + selectedPlayer.getPlayer_Surname());
                    playerView.setTextSize(16);
                    playerView.setPadding(8, 8, 8, 8);
                    playerView.setBackgroundColor(Color.parseColor("#cccccc"));
                    playerView.setTextColor(Color.BLACK);
                    container.addView(playerView);

                    adapter.removePlayer(selectedPlayer);
                    selectedPlayer = null;
                } else {
                    UIUtils.showMessage(SetBenchPlayersActivity.this, "Assignment failed: " + response.code());
                    switch (section) {
                        case "ATTACK": attackBenchCount--; break;
                        case "DEFENCE": defenceBenchCount--; break;
                        case "CENTRE": centreBenchCount--; break;
                    }
                    totalBenchCount--;
                }
            }

            @Override
            public void onFailure(Call<List<Court>> call, Throwable t) {
                UIUtils.networkError(SetBenchPlayersActivity.this, null);
                switch (section) {
                    case "ATTACK": attackBenchCount--; break;
                    case "DEFENCE": defenceBenchCount--; break;
                    case "CENTRE": centreBenchCount--; break;
                }
                totalBenchCount--;
            }
        });
    }

    public void onBackClicked(android.view.View view) {
        Intent intent = new Intent(SetBenchPlayersActivity.this, SetUpCourtActivity.class);
        startActivity(intent);
        finish();
    }

    public void onStartGameClicked(View view) {
        Intent intent = new Intent(SetBenchPlayersActivity.this, GameScreenActivity.class);
        startActivity(intent);
        finish();
    }
}
