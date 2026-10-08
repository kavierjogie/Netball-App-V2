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

import com.example.netballapp.Model.Coach;
import com.example.netballapp.Model.Player;
import com.example.netballapp.Model.PlayerCoach;
import com.example.netballapp.R;
import com.example.netballapp.Model.UIUtils;
import com.example.netballapp.api.RetrofitClient;
import com.example.netballapp.api.SuperbaseAPI;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterCoachActivity extends AppCompatActivity {
    private EditText edtFirstName, edtSurname, edtUsername, edtPassword, edtConfirmPassword;
    private AutoCompleteTextView actvRole;
    private SuperbaseAPI api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_coach);

        edtFirstName = findViewById(R.id.edtFirstName);
        edtSurname = findViewById(R.id.edtSurname);
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        actvRole = findViewById(R.id.actvRole);

        String[] roles = {"Head Coach", "Assistant Coach"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.dropdown_item, roles);
        actvRole.setAdapter(adapter);


        api = RetrofitClient.getClient().create(SuperbaseAPI.class);
    }

    public void onBackClicked(View view) {
            Intent intent = new Intent(RegisterCoachActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
    }


    public void onRegisterClicked(View view) {
        String firstName = edtFirstName.getText().toString().trim();
        String surname = edtSurname.getText().toString().trim();
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();
        String role = actvRole.getText().toString().trim();

        if (firstName.isEmpty()) { UIUtils.fieldError(edtFirstName, "Required"); return; }
        if (username.isEmpty()) { UIUtils.fieldError(edtUsername, "Required"); return; }
        if (password.isEmpty()) { UIUtils.fieldError(edtPassword, "Required"); return; }
        if (!password.equals(confirmPassword)) {
            UIUtils.fieldError(edtConfirmPassword, "Passwords don't match");
            return;
        }

        Coach coach = new Coach(firstName,surname,role,username,password);

        Call<List<Coach>> call = api.registerCoach(coach);
        call.enqueue(new Callback<List<Coach>>() {
            @Override
            public void onResponse(Call<List<Coach>> call, Response<List<Coach>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Coach savedCoach = response.body().get(0);

                    long coachId = savedCoach.getCoach_ID();

                    getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                            .edit()
                            .putLong("coach_ID", coachId)
                            .apply();

                    insertDefaultPlayers(coachId);

                    Toast.makeText(RegisterCoachActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterCoachActivity.this, LoginActivity.class));
                    finish();
                } else {
                    UIUtils.fieldError(edtUsername, "That username may already be taken");
                }
            }

            @Override
            public void onFailure(Call<List<Coach>> call, Throwable t) {
                UIUtils.networkError(RegisterCoachActivity.this, null);
            }
        });
    }

    private void insertDefaultPlayers(long coachId) {
        Player[] defaultPlayers = new Player[]{
                new Player("James", "Smith", 1, "GK", "1995-11-23", 190),
                new Player("Emily", "Johnson", 5, "WA", "1998-05-19", 175),
                new Player("Liam", "Brown", 12, "C", "2000-12-14", 180),
                new Player("Chloe", "Williams", 9, "GS", "1997-05-20", 178),
                new Player("Ethan", "Jones", 3, "WD", "1996-11-13", 182),
                new Player("Sophia", "Garcia", 7, "GK", "1999-05-18", 177),
                new Player("Noah", "Martinez", 11, "GA", "2001-08-19", 185),
                new Player("Olivia", "Davis", 6, "C", "1998-09-05", 170),
                new Player("Lucas", "Rodriguez", 2, "WA", "2000-04-24", 183),
                new Player("Mia", "Wilson", 8, "GD", "1997-06-23", 176)
        };

        for (Player p : defaultPlayers) {
            Call<List<Player>> call = api.registerPlayer(p);
            call.enqueue(new Callback<List<Player>>() {
                @Override
                public void onResponse(Call<List<Player>> call, Response<List<Player>> response) {
                    if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                        Player registeredPlayer = response.body().get(0);

                        PlayerCoach pc = new PlayerCoach(coachId, registeredPlayer.getPlayer_ID());
                        api.assignPlayerToCoach(pc).enqueue(new Callback<PlayerCoach>() {
                            @Override
                            public void onResponse(Call<PlayerCoach> call, Response<PlayerCoach> response) {
                                if (!response.isSuccessful()) {
                                    System.err.println("Failed to link player " + registeredPlayer.getPlayer_FirstName());
                                }
                            }
                            @Override
                            public void onFailure(Call<PlayerCoach> call, Throwable t) {
                                System.err.println("Error linking player " + registeredPlayer.getPlayer_FirstName() + ": " + t.getMessage());
                            }
                        });

                    } else {
                        UIUtils.showMessage(RegisterCoachActivity.this, "Failed to insert player: " + p.getPlayer_FirstName());
                    }
                }

                @Override
                public void onFailure(Call<List<Player>> call, Throwable t) {
                    UIUtils.networkError(RegisterCoachActivity.this, null);
                }
            });
        }
    }
}