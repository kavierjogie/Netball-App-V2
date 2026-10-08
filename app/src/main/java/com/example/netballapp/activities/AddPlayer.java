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

import com.example.netballapp.Model.Player;
import com.example.netballapp.Model.PlayerCoach;
import com.example.netballapp.Model.SessionManager;
import com.example.netballapp.Model.UIUtils;
import com.example.netballapp.R;
import com.example.netballapp.api.RetrofitClient;
import com.example.netballapp.api.SuperbaseAPI;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddPlayer extends AppCompatActivity
{
    private EditText edtFirstName, edtSurname, edtPlayerNumber, edtDOB, edtHeight;
    private AutoCompleteTextView actvPosition;
    private SuperbaseAPI api;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_player);

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
    }

    public void onBackClicked(View view)
    {
        Intent intent = new Intent(AddPlayer.this, Player_Profiles.class);
        startActivity(intent);
        finish();
    }

    public void onAddPlayerToDBClicked(View view) {
        String firstName = edtFirstName.getText().toString().trim();
        String surname = edtSurname.getText().toString().trim();
        String strPlayerNumber = edtPlayerNumber.getText().toString().trim();
        String dateOfBirth = edtDOB.getText().toString().trim();
        String strHeight = edtHeight.getText().toString().trim();
        String position = actvPosition.getText().toString().trim();

        if (firstName.isEmpty() || !firstName.matches("[a-zA-Z]+")) {
            Toast.makeText(this, "Please enter a valid first name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (surname.isEmpty() || !surname.matches("[a-zA-Z]+")) {
            Toast.makeText(this, "Please enter a valid surname", Toast.LENGTH_SHORT).show();
            return;
        }

        Integer playerNumber = null;
        if (!strPlayerNumber.isEmpty()) {
            try {
                playerNumber = Integer.parseInt(strPlayerNumber);
                if (playerNumber <= 0) {
                    Toast.makeText(this, "Player number must be greater than 0", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter a valid player number", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            Toast.makeText(this, "Player number field is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        Integer height = null;
        if (!strHeight.isEmpty()) {
            try {
                height = Integer.parseInt(strHeight);
                if (height < 50 || height > 250) {
                    Toast.makeText(this, "Please enter a valid height in cm", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter a valid height", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            Toast.makeText(this, "Height field is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] validPositions = {"GS", "GA", "WA", "C", "WD", "GD", "GK"};
        boolean isValidPosition = false;
        for (String p : validPositions) {
            if (p.equals(position)) {
                isValidPosition = true;
                break;
            }
        }
        if (!isValidPosition) {
            Toast.makeText(this, "Please select a valid position", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dateOfBirth.isEmpty()) {
            Toast.makeText(this, "Date of Birth cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        Player player = new Player(firstName, surname, playerNumber, position, dateOfBirth, height);

        Call<List<Player>> call = api.registerPlayer(player);
        call.enqueue(new Callback<List<Player>>() {
            @Override
            public void onResponse(Call<List<Player>> call, Response<List<Player>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Player registeredPlayer = response.body().get(0);

                    long coachId = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                            .getLong("coach_ID", -1);

                    if (coachId != -1) {
                        PlayerCoach pc = new PlayerCoach(coachId, registeredPlayer.getPlayer_ID());
                        api.assignPlayerToCoach(pc).enqueue(new Callback<PlayerCoach>() {
                            @Override
                            public void onResponse(Call<PlayerCoach> call, Response<PlayerCoach> response) {
                                if (!response.isSuccessful()) {
                                    Log.e("AddPlayer", "Player-Coach assignment failed: " + response.code());
                                }
                            }

                            @Override
                            public void onFailure(Call<PlayerCoach> call, Throwable t) {
                                Log.e("AddPlayer", "Player-Coach assignment error: " + t.getMessage());
                            }
                        });
                    }

                    Toast.makeText(AddPlayer.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(AddPlayer.this, Player_Profiles.class));
                    finish();
                } else {
                    Toast.makeText(AddPlayer.this, "Registration failed. Username might already exist.", Toast.LENGTH_SHORT).show();
                }
            }


            @Override
            public void onFailure(Call<List<Player>> call, Throwable t) {
                Toast.makeText(AddPlayer.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void onDOBClicked(View view) {
        UIUtils.showDatePicker(this, edtDOB);
    }

    public void onLogoutClicked(View view) {
            SessionManager.logout(this);
    }
}