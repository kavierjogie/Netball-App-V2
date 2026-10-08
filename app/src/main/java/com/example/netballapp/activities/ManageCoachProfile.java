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
import com.example.netballapp.Model.SessionManager;
import com.example.netballapp.R;
import com.example.netballapp.Model.UIUtils;
import com.example.netballapp.api.RetrofitClient;
import com.example.netballapp.api.SuperbaseAPI;

import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ManageCoachProfile extends AppCompatActivity {
    private EditText edtFirstName, edtSurname, edtUsername, edtPassword, edtConfirmPassword;
    private AutoCompleteTextView actvRole;
    private long currentCoachId;
    private SuperbaseAPI api;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_coach_profile);
        currentCoachId = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                .getLong("coach_ID", -1);
        if (currentCoachId == -1) {
            Toast.makeText(this, "Coach ID not found. Please log in again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

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

        loadCoachProfile(adapter);
    }
    private void loadCoachProfile(ArrayAdapter<String> adapter) {
        UIUtils.setLoading(this, true);
        Call<List<Coach>> call = api.getCoachById("eq." + currentCoachId);
        call.enqueue(new Callback<List<Coach>>() {
            @Override
            public void onResponse(Call<List<Coach>> call, Response<List<Coach>> response) {
                UIUtils.setLoading(ManageCoachProfile.this, false);
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Coach coach = response.body().get(0);
                    edtFirstName.setText(coach.getCoach_firstname());
                    edtSurname.setText(coach.getCoach_surname());
                    edtUsername.setText(coach.getCoach_username());
                    edtPassword.setText(coach.getCoach_password());
                    edtConfirmPassword.setText(coach.getCoach_password());
                    actvRole.setText(coach.getCoach_role(), false);
                } else {
                    Toast.makeText(ManageCoachProfile.this, "Coach profile not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Coach>> call, Throwable t) {
                UIUtils.setLoading(ManageCoachProfile.this, false);
                UIUtils.networkError(ManageCoachProfile.this, () -> loadCoachProfile(adapter));
            }
        });
    }
    public void onUpdateProfileClicked(View view) {
        String firstname = edtFirstName.getText().toString().trim();
        String surname = edtSurname.getText().toString().trim();
        String role = actvRole.getText().toString().trim();
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();

        if (!password.equals(confirmPassword)) {
            UIUtils.fieldError(edtConfirmPassword, "Passwords don't match");
            return;
        }

        Coach updatedCoach = new Coach(firstname,surname,role,username,password);

        Call<List<Coach>> call = api.updateCoachProfile("eq." + currentCoachId, updatedCoach);
        call.enqueue(new Callback<List<Coach>>() {
            @Override
            public void onResponse(Call<List<Coach>> call, Response<List<Coach>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Toast.makeText(ManageCoachProfile.this, "Profile updated!", Toast.LENGTH_SHORT).show();
                } else {
                    UIUtils.showMessage(ManageCoachProfile.this, "Update failed.");
                }
            }

            @Override
            public void onFailure(Call<List<Coach>> call, Throwable t) {
                UIUtils.networkError(ManageCoachProfile.this, null);
            }
        });
    }

    public void onBackClicked(View view) {
        Intent intent = new Intent(ManageCoachProfile.this, DashboardActivity.class);
        startActivity(intent);
        finish();
    }

    public void onLogoutClicked(View view) {
        SessionManager.logout(this);
    }
}