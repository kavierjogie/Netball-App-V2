package com.example.netballapp.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.netballapp.Model.Game;
import com.example.netballapp.Model.SessionManager;
import com.example.netballapp.R;
import com.example.netballapp.Model.UIUtils;
import com.example.netballapp.adapters.GameAdapter;
import com.example.netballapp.api.RetrofitClient;
import com.example.netballapp.api.SuperbaseAPI;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageGamesActivity extends AppCompatActivity {
    private SuperbaseAPI api;
    private GameAdapter adapter;
    private List<Game> games;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_games);

        ListView listView = findViewById(R.id.gamesListView);
        TextView emptyView = findViewById(R.id.emptyView);
        listView.setEmptyView(emptyView);

        games = new ArrayList<>();

        adapter = new GameAdapter(this, games, new GameAdapter.GameActionListener() {
            @Override
            public void onView(Game game, int position) {
                getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                        .edit()
                        .putLong("game_ID", game.getGame_ID())
                        .apply();
                Intent intent = new Intent(ManageGamesActivity.this, MatchAnalysis.class);
                startActivity(intent);
            }

            @Override
            public void onUpdate(Game game, int position) {
                Intent intent = new Intent(ManageGamesActivity.this, UpdateGameDetails.class);
                intent.putExtra("game_id", game.getGame_ID());
                startActivity(intent);
            }


            @Override
            public void onDelete(Game game, int position) {
                new AlertDialog.Builder(ManageGamesActivity.this)
                        .setTitle("Delete " + game.getGame_Name() + "?")
                        .setMessage("The game and its records are removed permanently.")
                        .setPositiveButton("Delete game", (dialog, which) -> deleteGameFromAPI(game.getGame_ID(), position))
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        listView.setAdapter(adapter);

        api = RetrofitClient.getClient().create(SuperbaseAPI.class);

        loadGamesFromAPI();
    }

    private void loadGamesFromAPI() {
        UIUtils.setLoading(this, true);
        long coachId = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                .getLong("coach_ID", -1);

        Call<List<Game>> call = api.getGamesForCoach("*,coach_game!inner(*)", "eq." + coachId);

        call.enqueue(new Callback<List<Game>>() {
            @Override
            public void onResponse(Call<List<Game>> call, Response<List<Game>> response) {
                UIUtils.setLoading(ManageGamesActivity.this, false);
                if (response.isSuccessful() && response.body() != null) {
                    games.clear();
                    games.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    UIUtils.showMessage(ManageGamesActivity.this, "Couldn't load games (error " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<List<Game>> call, Throwable t) {
                UIUtils.setLoading(ManageGamesActivity.this, false);
                UIUtils.networkError(ManageGamesActivity.this, () -> loadGamesFromAPI());
            }
        });
    }

    private void deleteGameFromAPI(long gameId, int position) {
        Call<Void> call = api.deleteGame("eq." + gameId);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    games.remove(position);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(ManageGamesActivity.this, "Game deleted", Toast.LENGTH_SHORT).show();
                } else {
                    UIUtils.showMessage(ManageGamesActivity.this, "Failed to delete game");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                UIUtils.networkError(ManageGamesActivity.this, null);
            }
        });
    }

    public void onBackClicked(View view) {
        Intent intent = new Intent(ManageGamesActivity.this, DashboardActivity.class);
        startActivity(intent);
        finish();
    }

    public void onLogoutClicked(View view) {
        SessionManager.logout(this);
    }
}
