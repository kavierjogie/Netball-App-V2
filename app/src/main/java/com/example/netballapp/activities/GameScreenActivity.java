package com.example.netballapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.netballapp.Model.Court;
import com.example.netballapp.Model.Game;
import com.example.netballapp.Model.Player;
import com.example.netballapp.Model.PlayerAction;
import com.example.netballapp.R;
import com.example.netballapp.adapters.BenchAdapter;
import com.example.netballapp.api.RetrofitClient;
import com.example.netballapp.api.SuperbaseAPI;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GameScreenActivity extends AppCompatActivity {

    private TextView timerText, scoreMadibaz, scoreOpposition,scoreMadibazName,scoreOppositionName;
    private Button startButton, endHalfButton,addOppositionScoreButton;
    private CountDownTimer countUpTimer;
    private TextView centrePassText;
    private String currentCentrePassTeam;
    private EditText coachNotes;
    private boolean isTimerRunning = false;
    private static final long HALF_TIME_MILLIS = 30 * 60 * 1000;
    private long elapsedTime = 0;
    private int currentHalf = 1;
    private String oppositionName = "Opposition";
    private final java.util.Map<Long, java.util.Map<String, Integer>> playerStats = new java.util.HashMap<>();
    private final java.util.Map<Long, java.util.List<PlayerAction>> playerActionHistory = new java.util.HashMap<>();
    private final List<Player> onCourtPlayers = new java.util.ArrayList<>();
    private final List<Player> benchPlayers = new java.util.ArrayList<>();

    private SuperbaseAPI api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_screen);
        timerText = findViewById(R.id.timer);
        startButton = findViewById(R.id.startButton);
        endHalfButton = findViewById(R.id.endHalfButton);
        centrePassText = findViewById(R.id.centrePassText);
        scoreMadibaz = findViewById(R.id.scoreMadibazValue);
        scoreOpposition = findViewById(R.id.scoreOppositionValue);
        scoreMadibazName = findViewById(R.id.scoreMadibaz);
        scoreOppositionName = findViewById(R.id.scoreOpposition);
        addOppositionScoreButton = findViewById(R.id.addOppositionScoreButton);
        coachNotes = findViewById(R.id.coachNotes);

        scoreMadibazName.setText("Madibaz");
        oppositionName = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                .getString("oppositionName", "Opposition");
        scoreOppositionName.setText(oppositionName);

        addOppositionScoreButton.setEnabled(false);

        disableAllPlayerPositions();

        scoreMadibaz.setText("0");
        scoreOpposition.setText("0");

        updateTimerText();

        startButton.setOnClickListener(v -> {
            if (!isTimerRunning) {
                showCentrePassDialog();
            }
        });

        endHalfButton.setEnabled(false);
        endHalfButton.setAlpha(0.5f);

        endHalfButton.setOnClickListener(v -> {
            stopTimer();
            handleEndHalf();
        });

        api = RetrofitClient.getClient().create(SuperbaseAPI.class);

        Long gameId = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getLong("game_ID", -1);

        loadPlayers(gameId);

        addOppositionScoreButton.setOnClickListener(v -> {
            if (!isTimerRunning) {
                Toast.makeText(GameScreenActivity.this, "Start the game first!", Toast.LENGTH_SHORT).show();
                return;
            }
            int currentScore = Integer.parseInt(scoreOpposition.getText().toString());
            currentScore++;
            scoreOpposition.setText(String.valueOf(currentScore));

            if (oppositionName.equals(currentCentrePassTeam)) {
                currentCentrePassTeam = "Madibaz";
            } else {
                currentCentrePassTeam = oppositionName;
            }
            centrePassText.setText("Centre Pass: " + currentCentrePassTeam);

            Map<String, Object> updates = new java.util.HashMap<>();
            updates.put("game_opposition_score", currentScore);
            updates.put("game_current_centre_pass_team", currentCentrePassTeam);

            Call<List<Game>> call = api.updateGameScore("eq." + gameId, updates);
            call.enqueue(new Callback<List<Game>>() {
                @Override
                public void onResponse(Call<List<Game>> call, Response<List<Game>> response) {
                    if (!response.isSuccessful()) {
                        Toast.makeText(GameScreenActivity.this,
                                "Failed to update opposition score: " + response.code(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<List<Game>> call, Throwable t) {
                    Toast.makeText(GameScreenActivity.this,
                            "Network error: " + t.getLocalizedMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void showCentrePassDialog() {
        String[] options = {"Madibaz", oppositionName};

        new android.app.AlertDialog.Builder(this)
                .setTitle("Set First Centre Pass")
                .setItems(options, (dialog, which) -> {
                    currentCentrePassTeam = options[which];
                    centrePassText.setText("Centre Pass: " + currentCentrePassTeam);
                    startTimer();
                    addOppositionScoreButton.setEnabled(true);
                    enableAllPlayerPositions();
                    startButton.setEnabled(false);
                    startButton.setAlpha(0.5f);
                    endHalfButton.setEnabled(true);
                    endHalfButton.setAlpha(1.0f);
                })
                .setCancelable(false)
                .show();
    }

    private void updateScoreAndCentrePass() {
        int currentScore = Integer.parseInt(scoreMadibaz.getText().toString());
        currentScore++;
        scoreMadibaz.setText(String.valueOf(currentScore));

        if ("Madibaz".equals(currentCentrePassTeam)) {
            currentCentrePassTeam = oppositionName;
        } else {
            currentCentrePassTeam = "Madibaz";
        }
        centrePassText.setText("Centre Pass: " + currentCentrePassTeam);

        Long gameId = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getLong("game_ID", -1);
        Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("game_madibaz_score", currentScore);
        updates.put("game_current_centre_pass_team", currentCentrePassTeam);

        Call<List<Game>> call = api.updateGameScore("eq." + gameId, updates);
        call.enqueue(new Callback<List<Game>>() {
            @Override
            public void onResponse(Call<List<Game>> call, Response<List<Game>> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(GameScreenActivity.this,
                            "Failed to update score/centre pass: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Game>> call, Throwable t) {
                Toast.makeText(GameScreenActivity.this,
                        "Network error: " + t.getLocalizedMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startTimer() {
        countUpTimer = new CountDownTimer(HALF_TIME_MILLIS - elapsedTime, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                elapsedTime += 1000;
                updateTimerText();
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;
                handleEndHalf();
            }
        }.start();

        isTimerRunning = true;
    }

    private void stopTimer() {
        if (countUpTimer != null) {
            countUpTimer.cancel();
        }
        isTimerRunning = false;
    }

    private void handleEndHalf() {
        saveCoachNoteOnHalfEnd();

        if (currentHalf == 1) {
            currentHalf = 2;
            resetTimer();
            TextView halfLabel = findViewById(R.id.halfLabel);
            halfLabel.setText("2nd Half");

            startButton.setEnabled(true);
            startButton.setAlpha(1.0f);
        } else if (currentHalf == 2) {
            stopTimer();

            Intent intent = new Intent(GameScreenActivity.this, MatchAnalysis.class);
            startActivity(intent);
            finish();
        }
    }

    private void resetTimer() {
        elapsedTime = 0;
        updateTimerText();
    }

    private void updateTimerText() {
        int minutes = (int) (elapsedTime / 1000) / 60;
        int seconds = (int) (elapsedTime / 1000) % 60;

        String halfLabelText = currentHalf == 1 ? "1st Half" : "2nd Half";

        timerText.setText(String.format("%02d:%02d", minutes, seconds));
        TextView halfLabel = findViewById(R.id.halfLabel);
        halfLabel.setText(halfLabelText);
    }

    private void loadPlayers(Long gameId) {
        Call<List<Court>> call = api.getCourtAssignments("eq." + gameId);
        call.enqueue(new Callback<List<Court>>() {
            @Override
            public void onResponse(Call<List<Court>> call, Response<List<Court>> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(GameScreenActivity.this, "Server error: " + response.code(), Toast.LENGTH_SHORT).show();
                    return;
                }

                List<Court> courtAssignments = response.body();
                if (courtAssignments != null) {
                    onCourtPlayers.clear();
                    benchPlayers.clear();

                    List<Long> benchIds = new ArrayList<>();

                    for (Court court : courtAssignments) {
                        Long playerId = court.getPlayer_id();
                        String pos = court.getCourt_position_field();
                        Long courtId = court.getCourt_ID();

                        if (pos != null && isOnCourtPosition(pos)) {
                            loadPlayerDetails(playerId, pos, courtId);
                        } else {
                            benchIds.add(playerId);
                        }
                    }

                    if (!benchIds.isEmpty()) {
                        fetchBenchPlayersBatch(benchIds);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Court>> call, Throwable t) {
                Toast.makeText(GameScreenActivity.this, "Network error: " + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isOnCourtPosition(String pos) {
        return pos.equalsIgnoreCase("GS") ||
                pos.equalsIgnoreCase("GA") ||
                pos.equalsIgnoreCase("WA") ||
                pos.equalsIgnoreCase("C") ||
                pos.equalsIgnoreCase("WD") ||
                pos.equalsIgnoreCase("GD") ||
                pos.equalsIgnoreCase("GK");
    }

    private void fetchBenchPlayersBatch(List<Long> benchIds) {
        StringBuilder idQuery = new StringBuilder("in.(");
        for (int i = 0; i < benchIds.size(); i++) {
            idQuery.append(benchIds.get(i));
            if (i < benchIds.size() - 1) idQuery.append(",");
        }
        idQuery.append(")");

        Call<List<Player>> call = api.getPlayersByIds(idQuery.toString());
        call.enqueue(new Callback<List<Player>>() {
            @Override
            public void onResponse(Call<List<Player>> call, Response<List<Player>> response) {
                if (!response.isSuccessful() || response.body() == null) return;

                benchPlayers.addAll(response.body());

                highlightPositionsWithSubs();
            }

            @Override
            public void onFailure(Call<List<Player>> call, Throwable t) {
                Toast.makeText(GameScreenActivity.this, "Error loading bench players: " + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPlayerDetails(Long playerId, String pos, Long courtId) {
        Call<List<Player>> call = api.getPlayerById("eq." + playerId);
        call.enqueue(new Callback<List<Player>>() {
            @Override
            public void onResponse(Call<List<Player>> call, Response<List<Player>> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(GameScreenActivity.this, "Error loading player: " + response.code(), Toast.LENGTH_SHORT).show();
                    return;
                }

                List<Player> players = response.body();
                if (players != null && !players.isEmpty()) {
                    Player player = players.get(0);
                    onCourtPlayers.add(player);

                    String playerDisplay = getInitials(player) + "\n(" + pos + ")";

                    int resId = getResources().getIdentifier("pos" + pos, "id", getPackageName());
                    TextView posText = findViewById(resId);
                    if (posText != null) {
                        posText.setText(playerDisplay);

                        posText.setOnClickListener(v -> {
                            if (!isTimerRunning) {
                                Toast.makeText(GameScreenActivity.this, "Start the game first!", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            showActionDialog(player, pos);
                        });

                        posText.setOnLongClickListener(v -> {
                            showBenchSwapDialog(player, pos, courtId, posText);
                            return true;
                        });
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Player>> call, Throwable t) {
                Toast.makeText(GameScreenActivity.this, "Network error: " + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private boolean hasSubForPosition(String pos) {
        for (Player p : benchPlayers) {
            if (p.getPlayer_position() != null && p.getPlayer_position().equalsIgnoreCase(pos)) {
                return true;
            }
        }
        return false;
    }

    private void highlightPositionsWithSubs() {
        String[] positions = {"GS", "GA", "WA", "C", "WD", "GD", "GK"};
        for (String pos : positions) {
            int resId = getResources().getIdentifier("pos" + pos, "id", getPackageName());
            TextView posText = findViewById(resId);
            if (posText != null) {
                if (hasSubForPosition(pos)) {
                    posText.setText(posText.getText() + " *");
                    posText.setTextColor(getResources().getColor(R.color.sub_available));
                } else {
                    posText.setTextColor(getResources().getColor(android.R.color.black));
                }
            }
        }
    }

    private void showBenchSwapDialog(Player currentPlayer, String pos, Long courtId, TextView posText) {
        List<Player> eligibleBench = new ArrayList<>();
        for (Player p : benchPlayers) {
            if (p.getPlayer_position() != null && p.getPlayer_position().equalsIgnoreCase(pos)) {
                eligibleBench.add(p);
            }
        }

        if (eligibleBench.isEmpty()) {
            Toast.makeText(this, "No bench players for position " + pos, Toast.LENGTH_SHORT).show();
            return;
        }

        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_bench_swap);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setGravity(Gravity.CENTER);

        RecyclerView recyclerView = dialog.findViewById(R.id.recyclerBench);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        BenchAdapter adapter = new BenchAdapter(eligibleBench, newPlayer -> {
            swapPlayer(currentPlayer, newPlayer, pos, courtId, posText);
            dialog.dismiss();
        });
        recyclerView.setAdapter(adapter);

        dialog.show();
    }

    private void swapPlayer(Player currentPlayer, Player newPlayer, String pos, Long courtId, TextView posText) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("player_id", newPlayer.getPlayer_ID());

        Call<List<Court>> call = api.updateCourt("eq." + courtId, updates);
        call.enqueue(new Callback<List<Court>>() {
            @Override
            public void onResponse(Call<List<Court>> call, Response<List<Court>> response) {
                if (response.isSuccessful()) {

                    posText.setText(getInitials(newPlayer) + "\n(" + pos + ")");
                    posText.setOnClickListener(v -> showActionDialog(newPlayer, pos));
                    posText.setOnLongClickListener(v -> {
                        showBenchSwapDialog(newPlayer, pos, courtId, posText);
                        return true;
                    });

                    onCourtPlayers.remove(currentPlayer);
                    onCourtPlayers.add(newPlayer);
                    benchPlayers.remove(newPlayer);
                    benchPlayers.add(currentPlayer);
                } else {
                    Toast.makeText(GameScreenActivity.this, "Failed to swap: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Court>> call, Throwable t) {
                Toast.makeText(GameScreenActivity.this, "Network error: " + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveCoachNoteOnHalfEnd() {
        String note = coachNotes.getText().toString().trim();
        if (note.isEmpty()) return;

        Long gameId = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getLong("game_ID", -1);
        if (gameId == -1) return;

        Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("game_coach_note", note);

        Call<List<Game>> call = api.updateGameScore("eq." + gameId, updates);
        call.enqueue(new Callback<List<Game>>() {
            @Override
            public void onResponse(Call<List<Game>> call, Response<List<Game>> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(GameScreenActivity.this, "Failed to save coach note: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<List<Game>> call, Throwable t) {
                Toast.makeText(GameScreenActivity.this, "Network error: " + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getInitials(Player player) {
        String firstInitial = player.getPlayer_FirstName().substring(0, 1).toUpperCase();
        String lastInitial = player.getPlayer_Surname().substring(0, 1).toUpperCase();
        return firstInitial + "." + lastInitial + ".";
    }

    private void showActionDialog(Player player, String pos) {
        String[] actions = {"Goal", "Penalty Goal", "Goal Missed", "For", "Against", "Drop Ball", "Held Ball", "Stepping", "Break", "Contact", "Obstruction", "Centre Pass Receive", "Goal Assist", "Offensive Rebound", "Defensive Rebound", "Deflection", "Intercept"};

        Map<String, Integer> stats = playerStats.get(player.getPlayer_ID());

        String[] actionsWithTotals = new String[actions.length];
        for (int i = 0; i < actions.length; i++) {
            int count = 0;
            if (stats != null) {
                count = stats.getOrDefault(actions[i], 0);
            }
            actionsWithTotals[i] = actions[i] + " (" + count + ")";
        }

        new android.app.AlertDialog.Builder(this)
                .setTitle("Record Action for " + player.getPlayer_FirstName() + " " + player.getPlayer_Surname())
                .setItems(actionsWithTotals, (dialog, which) -> {
                    String actionType = actions[which];

                    String timestamp = timerText.getText().toString().split(" ")[0];
                    String period = "Half " + currentHalf;
                    Long gameId = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getLong("game_ID", -1);

                    PlayerAction action = new PlayerAction(
                            actionType,
                            timestamp,
                            period,
                            player.getPlayer_ID(),
                            gameId
                    );

                    updatePlayerStats(action);

                    playerActionHistory.putIfAbsent(player.getPlayer_ID(), new java.util.ArrayList<>());
                    playerActionHistory.get(player.getPlayer_ID()).add(action);

                    savePlayerAction(action);

                    if (actionType.equals("Goal") || actionType.equals("Penalty Goal")) {
                        updateScoreAndCentrePass();
                    }

                    showActionDialog(player, pos);
                })
                .setNegativeButton("Close", null)
                .show();
    }

    private void updatePlayerStats(PlayerAction action) {
        playerStats.putIfAbsent(action.getPlayer_ID(), new java.util.HashMap<>());
        Map<String, Integer> stats = playerStats.get(action.getPlayer_ID());

        int currentCount = stats.getOrDefault(action.getAction_Type(), 0);
        stats.put(action.getAction_Type(), currentCount + 1);
    }

    private void savePlayerAction(PlayerAction action) {
        Call<Void> call = api.recordPlayerAction(action);
        call.enqueue(new Callback<Void>()
        {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                } else {
                    Toast.makeText(GameScreenActivity.this, "Error saving action: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(GameScreenActivity.this, "Network error: " + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void disableAllPlayerPositions() {
        String[] positions = {"GS", "GA", "WA", "C", "WD", "GD", "GK"};
        for (String pos : positions) {
            int resId = getResources().getIdentifier("pos" + pos, "id", getPackageName());
            TextView posText = findViewById(resId);
            if (posText != null) {
                posText.setClickable(false);
                posText.setAlpha(0.5f);
            }
        }
    }
    private void enableAllPlayerPositions() {
        String[] positions = {"GS", "GA", "WA", "C", "WD", "GD", "GK"};
        for (String pos : positions) {
            int resId = getResources().getIdentifier("pos" + pos, "id", getPackageName());
            TextView posText = findViewById(resId);
            if (posText != null) {
                posText.setClickable(true);
                posText.setAlpha(1.0f);
            }
        }
    }
}