package com.example.netballapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.netballapp.Model.Coach;
import com.example.netballapp.R;
import com.example.netballapp.Model.UIUtils;
import com.example.netballapp.api.RetrofitClient;
import com.example.netballapp.api.SuperbaseAPI;

import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private EditText edtUsername, edtPassword;
    private SuperbaseAPI api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtUsername = findViewById(R.id.username);
        edtPassword = findViewById(R.id.password);

        api = RetrofitClient.getClient().create(SuperbaseAPI.class);
    }

    public void onLoginClicked(View view) {
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (username.isEmpty()) { UIUtils.fieldError(edtUsername, "Enter your username"); return; }
        if (password.isEmpty()) { UIUtils.fieldError(edtPassword, "Enter your password"); return; }

        if (!isNetworkAvailable()) {
            UIUtils.networkError(this, () -> onLoginClicked(view));
            return;
        }

        // Busy state: block double-submits and show that something is happening.
        Button loginButton = (Button) view;
        CharSequence label = loginButton.getText();
        loginButton.setEnabled(false);
        loginButton.setText("Signing in…");
        Runnable reset = () -> { loginButton.setEnabled(true); loginButton.setText(label); };

        Call<List<Coach>> call = api.loginCoach("eq." + username, "eq." + password);

        call.enqueue(new Callback<List<Coach>>() {
            @Override
            public void onResponse(Call<List<Coach>> call, Response<List<Coach>> response) {
                reset.run();
                if (!response.isSuccessful()) {
                    UIUtils.showMessage(LoginActivity.this, "Couldn't sign in (error " + response.code() + ")");
                    return;
                }

                List<Coach> coaches = response.body();
                if (coaches != null && !coaches.isEmpty()) {
                    Coach coach = coaches.get(0);

                    getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                            .edit()
                            .putLong("coach_ID", coach.getCoach_ID())
                            .apply();

                    startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                    finish();
                } else {
                    UIUtils.fieldError(edtPassword, "Incorrect username or password");
                }
            }

            @Override
            public void onFailure(Call<List<Coach>> call, Throwable t) {
                reset.run();
                UIUtils.networkError(LoginActivity.this, () -> onLoginClicked(view));
            }
        });
    }

    public void onRegisterCoachClicked(View view) {
        Intent intent = new Intent(LoginActivity.this, RegisterCoachActivity.class);
        startActivity(intent);
        finish();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }
}