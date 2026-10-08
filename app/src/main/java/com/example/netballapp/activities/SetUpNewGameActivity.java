package com.example.netballapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.netballapp.Model.CoachGame;
import com.example.netballapp.Model.Game;
import com.example.netballapp.Model.UIUtils;
import com.example.netballapp.R;
import com.example.netballapp.api.RetrofitClient;
import com.example.netballapp.api.SuperbaseAPI;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SetUpNewGameActivity extends AppCompatActivity {
    private EditText edtGameName, edtOppositionName, edtGameVenue, edtGameDate;
    private AutoCompleteTextView actvGameType;
    private SuperbaseAPI api;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up_new_game);

        edtGameName = findViewById(R.id.edtGameName);
        edtOppositionName = findViewById(R.id.edtOpposition);
        edtGameVenue = findViewById(R.id.edtVenue);
        edtGameDate = findViewById(R.id.edtGameDate);
        actvGameType=findViewById(R.id.actvGameType);

        String[] gameTypes = {"Friendly", "League", "Tournament"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.dropdown_item, gameTypes);
        actvGameType.setAdapter(adapter);

        api = RetrofitClient.getClient().create(SuperbaseAPI.class);
    }

    public void onBackClicked(View view) {
        Intent intent = new Intent(SetUpNewGameActivity.this, DashboardActivity.class);
        startActivity(intent);
        finish();
    }

    public void onAddGameToDBClicked(View view) {
        String gameName = edtGameName.getText().toString().trim();
        String oppositionName= edtOppositionName.getText().toString().trim();
        String gameDate=edtGameDate.getText().toString().trim();
        String gameVenue=edtGameVenue.getText().toString().trim();
        String gameType = actvGameType.getText().toString().trim();


        if (gameName.isEmpty()) { UIUtils.fieldError(edtGameName, "Required"); return; }
        if (oppositionName.isEmpty()) { UIUtils.fieldError(edtOppositionName, "Required"); return; }
        if (gameDate.isEmpty()) { UIUtils.fieldError(edtGameDate, "Required"); return; }
        if (gameVenue.isEmpty()) { UIUtils.fieldError(edtGameVenue, "Required"); return; }

        Game game = new Game(gameName,oppositionName,gameVenue,gameDate,gameType,0,0,0,"","");

        Call<List<Game>> call = api.setUpNewGame(game);
        call.enqueue(new Callback<List<Game>>() {
            @Override
            public void onResponse(Call<List<Game>> call, Response<List<Game>> response) {
                if (response.isSuccessful()) {
                    Game savedGame = response.body().get(0);
                    Toast.makeText(SetUpNewGameActivity.this, "Set Up New Game successful!", Toast.LENGTH_SHORT).show();

                    getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                            .edit()
                            .putLong("game_ID", savedGame.getGame_ID())
                            .putString("oppositionName", edtOppositionName.getText().toString().trim())
                            .apply();

                    long coachId = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                            .getLong("coach_ID", -1);

                    CoachGame coachGame = new CoachGame(coachId, savedGame.getGame_ID());

                    api.assignCoachToGame(coachGame).enqueue(new Callback<CoachGame>() {
                        @Override
                        public void onResponse(Call<CoachGame> call, Response<CoachGame> response) {
                            if (response.isSuccessful()) {
                                Log.d("COACH_GAME", "Coach linked to game successfully!");
                            } else {
                                Log.e("COACH_GAME", "Failed to link coach: " + response.toString());
                            }
                        }

                        @Override
                        public void onFailure(Call<CoachGame> call, Throwable t) {
                            Log.e("COACH_GAME", "Error: " + t.getMessage());
                        }
                    });

                    Intent intent = new Intent(SetUpNewGameActivity.this, SetUpCourtActivity.class);
                    startActivity(intent);
                    finish();
                }

                else
                {
                    UIUtils.showMessage(SetUpNewGameActivity.this, "Couldn't create the game (error " + response.code() + ")");
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e("API_ERROR", "Error Body: " + errorBody);
                        } else {
                            Log.e("API_ERROR", "No error body returned");
                        }
                    } catch (IOException e) {
                        Log.e("API_ERROR", "IOException while reading error body", e);
                    }

                }
            }
            @Override
            public void onFailure(Call<List<Game>> call, Throwable t) {
                UIUtils.networkError(SetUpNewGameActivity.this, null);
            }
        });
    }

    public void onGameDateClicked(View view) {
        UIUtils.showFutureDatePicker(this, edtGameDate);
    }
}