package com.example.netballapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.netballapp.Model.Player;
import com.example.netballapp.Model.SessionManager;
import com.example.netballapp.Model.UIUtils;
import com.example.netballapp.R;
import com.example.netballapp.api.RetrofitClient;
import com.example.netballapp.api.SuperbaseAPI;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdatePlayerProfile extends AppCompatActivity
{
    private EditText edtFirstName, edtSurname, edtPlayerNumber, edtDOB, edtHeight;
    private AutoCompleteTextView actvPosition;
    private long currentPlayerId;
    private SuperbaseAPI api;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_player_profile);

        currentPlayerId = getIntent().getLongExtra("player_id",-1);

        if (currentPlayerId == -1) {
            Toast.makeText(this, "Player ID not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        edtFirstName = findViewById(R.id.edtFirstName);
        edtSurname = findViewById(R.id.edtSurname);
        edtPlayerNumber = findViewById(R.id.edtPlayerNumber);
        edtDOB = findViewById(R.id.edtDOB);
        edtHeight = findViewById(R.id.edtHeight);
        actvPosition = findViewById(R.id.actvPosition);

        String[] positions = {"GS", "GA", "WA", "C", "WD", "GD", "GK"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.dropdown_item, positions);
        actvPosition.setAdapter(adapter);

        api = RetrofitClient.getClient().create(SuperbaseAPI.class);

        loadPlayerProfile(adapter);
    }

    private void loadPlayerProfile(ArrayAdapter<String> adapter)
    {
        Call<List<Player>> call = api.getPlayerById("eq." + currentPlayerId);
        call.enqueue(new Callback<List<Player>>() {
            @Override
            public void onResponse(Call<List<Player>> call, Response<List<Player>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Player player = response.body().get(0);
                    edtFirstName.setText(player.getPlayer_FirstName());
                    edtSurname.setText(player.getPlayer_Surname());
                    edtPlayerNumber.setText(String.valueOf(player.getPlayer_Number()));
                    edtDOB.setText(player.getPlayer_DOB());
                    edtHeight.setText(String.valueOf(player.getPlayer_Height()));

                    String playerPosition = player.getPlayer_position();
                    if (playerPosition != null && !playerPosition.isEmpty()) {
                        actvPosition.setText(playerPosition, false);
                    }
                } else {
                    Toast.makeText(UpdatePlayerProfile.this, "Player profile not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Player>> call, Throwable t) {
                Toast.makeText(UpdatePlayerProfile.this, "Failed to load profile: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onUpdatePlayerToDBClicked(View view) {
        String firstname = edtFirstName.getText().toString().trim();
        String surname = edtSurname.getText().toString().trim();
        String strPlayerNumber = edtPlayerNumber.getText().toString().trim();
        String position = actvPosition.getText().toString().trim();
        String dob = edtDOB.getText().toString().trim();
        String strHeight = edtHeight.getText().toString().trim();

        if (firstname.isEmpty()) {
            Toast.makeText(this, "First Name is required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (surname.isEmpty()) {
            Toast.makeText(this, "Surname is required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (strPlayerNumber.isEmpty()) {
            Toast.makeText(this, "Player Number is required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (position.isEmpty()) {
            Toast.makeText(this, "Please select a Position", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dob.isEmpty()) {
            Toast.makeText(this, "Date of Birth is required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (strHeight.isEmpty()) {
            Toast.makeText(this, "Height is required", Toast.LENGTH_SHORT).show();
            return;
        }

        Integer playerNumber = null;
        Integer height = null;
        try {
            playerNumber = Integer.parseInt(strPlayerNumber);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid Player Number", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            height = Integer.parseInt(strHeight);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid Height", Toast.LENGTH_SHORT).show();
            return;
        }

        Player updatedPlayer = new Player(firstname, surname, playerNumber, position, dob, height);

        Call<List<Player>> call = api.updatePlayerProfile("eq." + currentPlayerId, updatedPlayer);
        call.enqueue(new Callback<List<Player>>() {
            @Override
            public void onResponse(Call<List<Player>> call, Response<List<Player>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Toast.makeText(UpdatePlayerProfile.this, "Profile updated!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(UpdatePlayerProfile.this, Player_Profiles.class));
                    finish();
                } else {
                    Toast.makeText(UpdatePlayerProfile.this, "Update failed.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Player>> call, Throwable t) {
                Toast.makeText(UpdatePlayerProfile.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onBackClicked(View view) {
        Intent intent = new Intent(UpdatePlayerProfile.this, Player_Profiles.class);
        startActivity(intent);
        finish();
    }

    public void onLogoutClicked(View view) {
        SessionManager.logout(this);
    }

    public void onDOBClicked(View view) {
        UIUtils.showDatePicker(this, edtDOB);
    }
}