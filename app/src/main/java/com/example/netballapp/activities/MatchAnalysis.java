package com.example.netballapp.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.netballapp.Model.Game;
import com.example.netballapp.Model.PlayerStatsView;
import com.example.netballapp.R;
import com.example.netballapp.adapters.PlayerStatsAdapter;
import com.example.netballapp.api.RetrofitClient;
import com.example.netballapp.api.SuperbaseAPI;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MatchAnalysis extends AppCompatActivity {

    private TextView tvMadibazScore, tvOppositionScore, tvGameName, tvGameDateVenue, tvTeamStats;
    private RecyclerView rvPlayerActions;
    private RadioGroup rbHalfToggle;
    private SuperbaseAPI api;
    private BarChart playerStatsChart, shootingAccuracyChart, errorsChart;
    private long gameID = -1;
    private int currentChartId = R.id.btnGoalsChart;
    private List<PlayerStatsView> currentStatsList;
    private MaterialCardView playerStatsChartCard, shootingAccuracyChartCard, errorsChartCard;
    private MaterialCardView positivePlayChartCard;
    private BarChart positivePlayChart;
    private Button btnPositivePlayChart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_analysis);
        tvGameName = findViewById(R.id.tvGameName);
        tvGameDateVenue = findViewById(R.id.tvGameDateVenue);
        tvMadibazScore = findViewById(R.id.tvMadibazScore);
        tvOppositionScore = findViewById(R.id.tvOppositionScore);
        rvPlayerActions = findViewById(R.id.rvPlayerActions);
        tvTeamStats = findViewById(R.id.tvTeamStats);
        rbHalfToggle = findViewById(R.id.rbHalfToggle);
        playerStatsChart = findViewById(R.id.playerStatsChart);
        shootingAccuracyChart = findViewById(R.id.shootingAccuracyChart);
        errorsChart = findViewById(R.id.errorsChart);
        playerStatsChartCard = findViewById(R.id.playerStatsChartCard);
        shootingAccuracyChartCard = findViewById(R.id.shootingAccuracyChartCard);
        errorsChartCard = findViewById(R.id.errorsChartCard);
        btnPositivePlayChart = findViewById(R.id.btnPositivePlayChart);
        positivePlayChartCard = findViewById(R.id.positivePlayChartCard);
        positivePlayChart = findViewById(R.id.positivePlayChart);

        rvPlayerActions.setLayoutManager(new LinearLayoutManager(this));

        Button btnGoalsChart = findViewById(R.id.btnGoalsChart);
        Button btnAccuracyChart = findViewById(R.id.btnAccuracyChart);
        Button btnErrorsChart = findViewById(R.id.btnErrorsChart);

        api = RetrofitClient.getClient().create(SuperbaseAPI.class);
        gameID = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getLong("game_ID", -1);

        View.OnClickListener chartToggleListener = v -> {
            currentChartId = v.getId();
            updateCharts(getSelectedHalf());
        };

        btnGoalsChart.setOnClickListener(chartToggleListener);
        btnAccuracyChart.setOnClickListener(chartToggleListener);
        btnErrorsChart.setOnClickListener(chartToggleListener);
        btnPositivePlayChart.setOnClickListener(chartToggleListener);
        rbHalfToggle.setOnCheckedChangeListener((group, checkedId) -> {
            updateCharts(getSelectedHalf());
            updateTeamStats(getSelectedHalf());
            updatePlayerList(getSelectedHalf());
        });

        if (gameID != -1) {
            loadGameData(gameID);
        } else {
            Toast.makeText(this, "Game ID not found", Toast.LENGTH_SHORT).show();
        }

    }

    private void loadGameData(long gameID) {
        api.getGameById("eq." + gameID).enqueue(new Callback<List<Game>>() {
            @Override
            public void onResponse(Call<List<Game>> call, Response<List<Game>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Game game = response.body().get(0);
                    tvGameName.setText(game.getGame_Name());
                    tvGameDateVenue.setText(game.getGame_Date() + " | " + game.getGame_Venue());
                    tvMadibazScore.setText("Madibaz: " + game.getGame_MadibazScore());
                    tvOppositionScore.setText(game.getGame_OppositionName()+": " + game.getGame_OppositionScore());

                    loadPlayerStats(null);
                } else {
                    Toast.makeText(MatchAnalysis.this, "Failed to fetch game info", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Game>> call, Throwable t) {
                Toast.makeText(MatchAnalysis.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadPlayerStats(@Nullable Integer half) {
        Call<List<PlayerStatsView>> call;
        if (half == null) {
            call = api.getPlayerStatsByGame("eq." + gameID);
        } else {
            call = api.getPlayerStatsByGameAndHalf("eq." + gameID, "eq.Half " + half);
        }

        call.enqueue(new Callback<List<PlayerStatsView>>() {
            @Override
            public void onResponse(Call<List<PlayerStatsView>> call, Response<List<PlayerStatsView>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentStatsList = response.body();
                    updatePlayerList(getSelectedHalf());
                    updateCharts(getSelectedHalf());
                    updateTeamStats(getSelectedHalf());
                }
            }
            @Override
            public void onFailure(Call<List<PlayerStatsView>> call, Throwable t) {
                Toast.makeText(MatchAnalysis.this, "Failed to load stats", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<PlayerStatsView> aggregateStatsByPlayer(List<PlayerStatsView> statsList) {
        Map<String, PlayerStatsView> mergedMap = new HashMap<>();

        for (PlayerStatsView stats : statsList) {
            String playerName = stats.getPlayer_name();
            if (!mergedMap.containsKey(playerName)) {
                PlayerStatsView copy = new PlayerStatsView();
                copy.setPlayer_name(playerName);
                mergedMap.put(playerName, copy);
            }

            PlayerStatsView total = mergedMap.get(playerName);

            total.setGoal(total.getGoal() + stats.getGoal());
            total.setPenalty_goal(total.getPenalty_goal() + stats.getPenalty_goal());
            total.setGoal_missed(total.getGoal_missed() + stats.getGoal_missed());
            total.setFor_(total.getFor_() + stats.getFor_());
            total.setAgainst(total.getAgainst() + stats.getAgainst());
            total.setOffensive_rebound(total.getOffensive_rebound() + stats.getOffensive_rebound());
            total.setDefensive_rebound(total.getDefensive_rebound() + stats.getDefensive_rebound());
            total.setCentre_pass_receive(total.getCentre_pass_receive() + stats.getCentre_pass_receive());
            total.setGoal_assist(total.getGoal_assist() + stats.getGoal_assist());
            total.setDeflection(total.getDeflection() + stats.getDeflection());
            total.setIntercept(total.getIntercept() + stats.getIntercept());
            total.setDrop_ball(total.getDrop_ball() + stats.getDrop_ball());
            total.setHeld_ball(total.getHeld_ball() + stats.getHeld_ball());
            total.setStepping(total.getStepping() + stats.getStepping());
            total.setBreak_(total.getBreak_() + stats.getBreak_());
            total.setContact(total.getContact() + stats.getContact());
            total.setObstruction(total.getObstruction() + stats.getObstruction());
        }
        return new ArrayList<>(mergedMap.values());
    }

    private void updatePlayerList(@Nullable Integer half) {
        if (currentStatsList == null) return;

        List<PlayerStatsView> filtered = filterStatsByHalf(half);

        if (half == null) {
            filtered = aggregateStatsByPlayer(filtered);
        }

        rvPlayerActions.setAdapter(new PlayerStatsAdapter(filtered));
    }

    private void updateCharts(@Nullable Integer half) {
        if (currentStatsList == null) return;

        List<PlayerStatsView> filtered = filterStatsByHalf(half);

        if (half == null) {
            filtered = aggregateStatsByPlayer(filtered);
        }

        playerStatsChartCard.setVisibility(View.GONE);
        shootingAccuracyChartCard.setVisibility(View.GONE);
        errorsChartCard.setVisibility(View.GONE);
        positivePlayChartCard.setVisibility(View.GONE);

        if (currentChartId == R.id.btnGoalsChart) {
            displayPlayerStatsChart(filtered);
            playerStatsChartCard.setVisibility(View.VISIBLE);
        } else if (currentChartId == R.id.btnAccuracyChart) {
            displayShootingAccuracyChart(filtered);
            shootingAccuracyChartCard.setVisibility(View.VISIBLE);
        } else if (currentChartId == R.id.btnErrorsChart) {
            displayErrorsChart(filtered);
            errorsChartCard.setVisibility(View.VISIBLE);
        } else if (currentChartId == R.id.btnPositivePlayChart) {
            displayPositivePlayChart(filtered);
            positivePlayChartCard.setVisibility(View.VISIBLE);
        }
    }

    private void updateTeamStats(@Nullable Integer half) {
        if (currentStatsList == null) return;
        List<PlayerStatsView> filtered = filterStatsByHalf(half);

        PlayerStatsView totals = new PlayerStatsView();
        for (PlayerStatsView stats : filtered) {
            totals.setGoal(totals.getGoal() + stats.getGoal());
            totals.setPenalty_goal(totals.getPenalty_goal() + stats.getPenalty_goal());
            totals.setGoal_missed(totals.getGoal_missed() + stats.getGoal_missed());
            totals.setFor_(totals.getFor_() + stats.getFor_());
            totals.setAgainst(totals.getAgainst() + stats.getAgainst());
            totals.setOffensive_rebound(totals.getOffensive_rebound() + stats.getOffensive_rebound());
            totals.setDefensive_rebound(totals.getDefensive_rebound() + stats.getDefensive_rebound());
            totals.setCentre_pass_receive(totals.getCentre_pass_receive() + stats.getCentre_pass_receive());
            totals.setGoal_assist(totals.getGoal_assist() + stats.getGoal_assist());
            totals.setDeflection(totals.getDeflection() + stats.getDeflection());
            totals.setIntercept(totals.getIntercept() + stats.getIntercept());
            totals.setDrop_ball(totals.getDrop_ball() + stats.getDrop_ball());
            totals.setHeld_ball(totals.getHeld_ball() + stats.getHeld_ball());
            totals.setStepping(totals.getStepping() + stats.getStepping());
            totals.setBreak_(totals.getBreak_() + stats.getBreak_());
            totals.setContact(totals.getContact() + stats.getContact());
            totals.setObstruction(totals.getObstruction() + stats.getObstruction());
        }

        int totalShots = totals.getGoal() + totals.getPenalty_goal() + totals.getGoal_missed();
        double successRate = totalShots > 0 ? ((double) (totals.getGoal() + totals.getPenalty_goal()) / totalShots) * 100 : 0;

        String statsText = "=== Team Totals ===\n" +
                "Goals: " + totals.getGoal() + "\n" +
                "Penalty Goals: " + totals.getPenalty_goal() + "\n" +
                "Goals Missed: " + totals.getGoal_missed() + "\n" +
                String.format("Success Rate: %.1f%%\n\n", successRate) +
                "Positive Play:\n" +
                "For: " + totals.getFor_() + "\n" +
                "Against: " + totals.getAgainst() + "\n" +
                "Offensive Rebound: " + totals.getOffensive_rebound() + "\n" +
                "Defensive Rebound: " + totals.getDefensive_rebound() + "\n" +
                "Centre Pass Receive: " + totals.getCentre_pass_receive() + "\n" +
                "Goal Assist: " + totals.getGoal_assist() + "\n" +
                "Deflection: " + totals.getDeflection() + "\n" +
                "Intercept: " + totals.getIntercept() + "\n\n" +
                "Penalties & Errors:\n" +
                "Drop Ball: " + totals.getDrop_ball() + "\n" +
                "Held Ball: " + totals.getHeld_ball() + "\n" +
                "Stepping: " + totals.getStepping() + "\n" +
                "Break: " + totals.getBreak_() + "\n" +
                "Contact: " + totals.getContact() + "\n" +
                "Obstruction: " + totals.getObstruction();

        tvTeamStats.setText(statsText);
    }

    private List<PlayerStatsView> filterStatsByHalf(@Nullable Integer half) {
        if (half == null) return currentStatsList;
        List<PlayerStatsView> filtered = new ArrayList<>();
        for (PlayerStatsView stats : currentStatsList) {
            if (stats.getHalf().equals("Half " + half)) filtered.add(stats);
        }
        return filtered;
    }

    private Integer getSelectedHalf() {
        int halfId = rbHalfToggle.getCheckedRadioButtonId();
        if (halfId == R.id.rbHalf1) return 1;
        else if (halfId == R.id.rbHalf2) return 2;
        else return null;
    }

    private void displayPlayerStatsChart(List<PlayerStatsView> statsList) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> names = new ArrayList<>();
        for (int i = 0; i < statsList.size(); i++) {
            entries.add(new BarEntry(i, statsList.get(i).getGoal()));
            names.add(statsList.get(i).getPlayer_name());
        }
        BarDataSet set = new BarDataSet(entries, "Goals");
        set.setColors(Color.BLUE);
        playerStatsChart.setData(new BarData(set));
        playerStatsChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(names));
        playerStatsChart.getXAxis().setGranularity(1f);
        playerStatsChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        playerStatsChart.getAxisRight().setEnabled(false);
        playerStatsChart.getDescription().setEnabled(false);
        playerStatsChart.invalidate();
    }

    private void displayShootingAccuracyChart(List<PlayerStatsView> statsList) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> names = new ArrayList<>();
        for (int i = 0; i < statsList.size(); i++) {
            int attempts = statsList.get(i).getGoal() + statsList.get(i).getPenalty_goal() + statsList.get(i).getGoal_missed();
            float accuracy = attempts > 0 ? ((float)(statsList.get(i).getGoal() + statsList.get(i).getPenalty_goal()) / attempts) * 100 : 0f;
            entries.add(new BarEntry(i, accuracy));
            names.add(statsList.get(i).getPlayer_name());
        }
        BarDataSet set = new BarDataSet(entries, "Shooting Accuracy (%)");
        set.setColors(Color.GREEN);
        shootingAccuracyChart.setData(new BarData(set));
        shootingAccuracyChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(names));
        shootingAccuracyChart.getXAxis().setGranularity(1f);
        shootingAccuracyChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        shootingAccuracyChart.getAxisRight().setEnabled(false);
        shootingAccuracyChart.getDescription().setEnabled(false);
        shootingAccuracyChart.invalidate();
    }

    private void displayErrorsChart(List<PlayerStatsView> statsList) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> names = new ArrayList<>();
        for (int i = 0; i < statsList.size(); i++) {
            entries.add(new BarEntry(i, new float[]{
                    statsList.get(i).getDrop_ball(),
                    statsList.get(i).getHeld_ball(),
                    statsList.get(i).getStepping(),
                    statsList.get(i).getContact(),
                    statsList.get(i).getObstruction()
            }));
            names.add(statsList.get(i).getPlayer_name());
        }
        BarDataSet set = new BarDataSet(entries, "Errors");
        set.setColors(new int[]{Color.RED, Color.MAGENTA, Color.YELLOW, Color.BLUE, Color.GRAY});
        set.setStackLabels(new String[]{"Drop Ball", "Held Ball", "Stepping", "Contact", "Obstruction"});
        errorsChart.setData(new BarData(set));
        errorsChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(names));
        errorsChart.getXAxis().setGranularity(1f);
        errorsChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        errorsChart.getAxisRight().setEnabled(false);
        errorsChart.getDescription().setEnabled(false);
        errorsChart.invalidate();
    }

    private void displayPositivePlayChart(List<PlayerStatsView> statsList) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> names = new ArrayList<>();
        for (int i = 0; i < statsList.size(); i++) {
            float[] values = new float[]{
                    statsList.get(i).getCentre_pass_receive(),
                    statsList.get(i).getGoal_assist(),
                    statsList.get(i).getOffensive_rebound(),
                    statsList.get(i).getDefensive_rebound(),
                    statsList.get(i).getDeflection(),
                    statsList.get(i).getIntercept()
            };
            entries.add(new BarEntry(i, values));
            names.add(statsList.get(i).getPlayer_name());
        }
        BarDataSet set = new BarDataSet(entries, "Positive Play");
        set.setColors(new int[]{Color.BLUE, Color.GREEN, Color.MAGENTA, Color.CYAN, Color.YELLOW, Color.LTGRAY});
        set.setStackLabels(new String[]{"Centre Pass Receive", "Goal Assist", "Offensive Rebound", "Defensive Rebound", "Deflection", "Intercept"});
        positivePlayChart.setData(new BarData(set));
        positivePlayChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(names));
        positivePlayChart.getXAxis().setGranularity(1f);
        positivePlayChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        positivePlayChart.getAxisRight().setEnabled(false);
        positivePlayChart.getDescription().setEnabled(false);
        positivePlayChart.invalidate();
    }

    public void onReturnClicked(View view) {
        Intent intent = new Intent(MatchAnalysis.this, DashboardActivity.class);
        startActivity(intent);
        finish();
    }
}
