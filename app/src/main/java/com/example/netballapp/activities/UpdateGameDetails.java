package com.example.netballapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.example.netballapp.Model.Game;
import com.example.netballapp.Model.SessionManager;
import com.example.netballapp.Model.UIUtils;
import com.example.netballapp.R;
import com.example.netballapp.api.RetrofitClient;
import com.example.netballapp.api.SuperbaseAPI;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdateGameDetails extends AppCompatActivity {

    private EditText edtGameName, edtOpposition, edtGameDate, edtVenue;
    private AutoCompleteTextView actvGameType;
    private long currentGameId;
    private SuperbaseAPI api;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_game_details);

        currentGameId = getIntent().getLongExtra("game_id", -1);

        if (currentGameId == -1) {
            Toast.makeText(this, "Game ID not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        edtGameName = findViewById(R.id.edtGameName);
        edtOpposition = findViewById(R.id.edtOpposition);
        edtGameDate = findViewById(R.id.edtGameDate);
        edtVenue = findViewById(R.id.edtVenue);
        actvGameType = findViewById(R.id.actvGameType);

        String[] gameTypes = {"Friendly", "League", "Tournament"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.dropdown_item, gameTypes);
        actvGameType.setAdapter(adapter);

        api = RetrofitClient.getClient().create(SuperbaseAPI.class);

        loadGameDetails(adapter);
    }

    private void loadGameDetails(ArrayAdapter<String> adapter) {
        UIUtils.setLoading(this, true);
        Call<List<Game>> call = api.getGameById("eq." + currentGameId);
        call.enqueue(new Callback<List<Game>>() {
            @Override
            public void onResponse(Call<List<Game>> call, Response<List<Game>> response) {
                UIUtils.setLoading(UpdateGameDetails.this, false);
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Game game = response.body().get(0);
                    edtGameName.setText(game.getGame_Name());
                    edtOpposition.setText(game.getGame_OppositionName());
                    edtGameDate.setText(game.getGame_Date());
                    edtVenue.setText(game.getGame_Venue());

                    String type = game.getGame_Type();
                    if (type != null && !type.isEmpty()) {
                        actvGameType.setText(type, false);
                    }
                } else {
                    Toast.makeText(UpdateGameDetails.this, "Game details not found", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<List<Game>> call, Throwable t) {
                UIUtils.setLoading(UpdateGameDetails.this, false);
                UIUtils.networkError(UpdateGameDetails.this, () -> loadGameDetails(adapter));
            }
        });
    }

    public void onUpdateGameToDBClicked(View view) {
        String gameName = edtGameName.getText().toString().trim();
        String opposition = edtOpposition.getText().toString().trim();
        String gameDate = edtGameDate.getText().toString().trim();
        String venue = edtVenue.getText().toString().trim();
        String type = actvGameType.getText().toString().trim();

        if (gameName.isEmpty()) {
            UIUtils.fieldError(edtGameName, "Required");
            return;
        }

        if (opposition.isEmpty()) {
            UIUtils.fieldError(edtOpposition, "Required");
            return;
        }

        if (gameDate.isEmpty()) {
            UIUtils.fieldError(edtGameDate, "Required");
            return;
        }

        if (venue.isEmpty()) {
            UIUtils.fieldError(edtVenue, "Required");
            return;
        }

        if (type.isEmpty()) {
            UIUtils.fieldError(actvGameType, "Choose a game type");
            return;
        }

        Game updatedGame = new Game(gameName, opposition,  venue,gameDate, type);

        Call<List<Game>> call = api.updateGame("eq." + currentGameId, updatedGame);
        call.enqueue(new Callback<List<Game>>() {
            @Override
            public void onResponse(Call<List<Game>> call, Response<List<Game>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Toast.makeText(UpdateGameDetails.this, "Game updated!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(UpdateGameDetails.this, ManageGamesActivity.class));
                    finish();
                } else {
                    String errorMsg = "Update failed. Code: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += " - " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        errorMsg += " (failed to parse error)";
                    }
                    UIUtils.showMessage(UpdateGameDetails.this, errorMsg);
                }
            }
            @Override
            public void onFailure(Call<List<Game>> call, Throwable t) {
                UIUtils.networkError(UpdateGameDetails.this, null);
            }
        });

    }
    public void onBackClicked(View view) {
        Intent intent = new Intent(UpdateGameDetails.this, ManageGamesActivity.class);
        startActivity(intent);
        finish();
    }

    public void onGameDateClicked(View view) {
        UIUtils.showDatePicker(this, edtGameDate);
    }

    public void onLogoutClicked(View view) {
        SessionManager.logout(this);
    }
}
