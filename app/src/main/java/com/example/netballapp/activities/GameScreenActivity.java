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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.netballapp.Model.Court;
import com.example.netballapp.Model.Game;
import com.example.netballapp.Model.Player;
import com.example.netballapp.Model.PlayerAction;
import com.example.netballapp.Model.UIUtils;
import com.example.netballapp.R;
import com.example.netballapp.adapters.BenchAdapter;
import com.example.netballapp.api.RetrofitClient;
import com.example.netballapp.api.SuperbaseAPI;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;

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

        endHalfButton.setOnClickListener(v -> new android.app.AlertDialog.Builder(this)
                .setTitle(currentHalf == 1 ? "End 1st half?" : "End the game?")
                .setMessage(currentHalf == 1
                        ? "The clock stops and resets for the 2nd half."
                        : "The clock stops and you'll go to the match analysis.")
                .setPositiveButton(currentHalf == 1 ? "End half" : "End game", (d, w) -> {
                    stopTimer();
                    handleEndHalf();
                })
                .setNegativeButton("Keep playing", null)
                .show());

        api = RetrofitClient.getClient().create(SuperbaseAPI.class);

        Long gameId = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getLong("game_ID", -1);

        loadPlayers(gameId);

        addOppositionScoreButton.setOnClickListener(v -> {
            if (!isTimerRunning) {
                UIUtils.showMessage(GameScreenActivity.this, "Start the game first");
                return;
            }
            String previousCentrePass = currentCentrePassTeam;
            changeScore(scoreOpposition, "game_opposition_score", 1, otherTeam(currentCentrePassTeam));
            UIUtils.confirmHaptic(v);
            Snackbar.make(v, oppositionName + " goal", Snackbar.LENGTH_LONG)
                    .setAction("Undo", u -> changeScore(scoreOpposition, "game_opposition_score", -1, previousCentrePass))
                    .show();
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

    private String otherTeam(String team) {
        return "Madibaz".equals(team) ? oppositionName : "Madibaz";
    }

    /** Updates a score on screen immediately, then saves it. delta is -1 when undoing. */
    private void changeScore(TextView scoreView, String column, int delta, String centrePassTeam) {
        int score = Integer.parseInt(scoreView.getText().toString()) + delta;
        scoreView.setText(String.valueOf(score));
        currentCentrePassTeam = centrePassTeam;
        centrePassText.setText("Centre Pass: " + currentCentrePassTeam);

        Long gameId = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getLong("game_ID", -1);
        Map<String, Object> updates = new HashMap<>();
        updates.put(column, score);
        updates.put("game_current_centre_pass_team", currentCentrePassTeam);

        api.updateGameScore("eq." + gameId, updates).enqueue(new Callback<List<Game>>() {
            @Override
            public void onResponse(Call<List<Game>> call, Response<List<Game>> response) {
                if (!response.isSuccessful()) {
                    UIUtils.showMessage(GameScreenActivity.this, "Score wasn't saved (error " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<List<Game>> call, Throwable t) {
                UIUtils.networkError(GameScreenActivity.this, null);
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
        UIUtils.setLoading(this, true);
        Call<List<Court>> call = api.getCourtAssignments("eq." + gameId);
        call.enqueue(new Callback<List<Court>>() {
            @Override
            public void onResponse(Call<List<Court>> call, Response<List<Court>> response) {
                UIUtils.setLoading(GameScreenActivity.this, false);
                if (!response.isSuccessful()) {
                    UIUtils.showMessage(GameScreenActivity.this, "Couldn't load the court (error " + response.code() + ")");
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
                UIUtils.setLoading(GameScreenActivity.this, false);
                UIUtils.networkError(GameScreenActivity.this, () -> loadPlayers(gameId));
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
                UIUtils.networkError(GameScreenActivity.this, () -> fetchBenchPlayersBatch(benchIds));
            }
        });
    }

    private void loadPlayerDetails(Long playerId, String pos, Long courtId) {
        Call<List<Player>> call = api.getPlayerById("eq." + playerId);
        call.enqueue(new Callback<List<Player>>() {
            @Override
            public void onResponse(Call<List<Player>> call, Response<List<Player>> response) {
                if (!response.isSuccessful()) {
                    UIUtils.showMessage(GameScreenActivity.this, "Couldn't load a player (error " + response.code() + ")");
                    return;
                }

                List<Player> players = response.body();
                if (players != null && !players.isEmpty()) {
                    Player player = players.get(0);
                    onCourtPlayers.add(player);

                    int resId = getResources().getIdentifier("pos" + pos, "id", getPackageName());
                    TextView posText = findViewById(resId);
                    if (posText != null) {
                        bindPosition(posText, player, pos, courtId);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Player>> call, Throwable t) {
                UIUtils.networkError(GameScreenActivity.this, () -> loadPlayerDetails(playerId, pos, courtId));
            }
        });
    }
    private void bindPosition(TextView posText, Player player, String pos, Long courtId) {
        posText.setText(getInitials(player) + "\n(" + pos + ")");
        posText.setOnClickListener(v -> {
            if (!isTimerRunning) {
                UIUtils.showMessage(GameScreenActivity.this, "Start the game first");
                return;
            }
            showActionDialog(player, pos);
        });
        posText.setOnLongClickListener(v -> {
            showBenchSwapDialog(player, pos, courtId, posText);
            return true;
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
            UIUtils.showMessage(this, "No bench players for " + pos);
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
        bindPosition(posText, newPlayer, pos, courtId);
        onCourtPlayers.remove(currentPlayer);
        onCourtPlayers.add(newPlayer);
        benchPlayers.remove(newPlayer);
        benchPlayers.add(currentPlayer);
        UIUtils.confirmHaptic(posText);

        Runnable revert = () -> {
            bindPosition(posText, currentPlayer, pos, courtId);
            onCourtPlayers.remove(newPlayer);
            onCourtPlayers.add(currentPlayer);
            benchPlayers.remove(currentPlayer);
            benchPlayers.add(newPlayer);
        };

        Map<String, Object> updates = new HashMap<>();
        updates.put("player_id", newPlayer.getPlayer_ID());

        api.updateCourt("eq." + courtId, updates).enqueue(new Callback<List<Court>>() {
            @Override
            public void onResponse(Call<List<Court>> call, Response<List<Court>> response) {
                if (!response.isSuccessful()) {
                    revert.run();
                    UIUtils.showMessage(GameScreenActivity.this, "Substitution wasn't saved (error " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<List<Court>> call, Throwable t) {
                revert.run();
                UIUtils.networkError(GameScreenActivity.this,
                        () -> swapPlayer(currentPlayer, newPlayer, pos, courtId, posText));
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
                    UIUtils.showMessage(GameScreenActivity.this, "Coach note wasn't saved (error " + response.code() + ")");
                }
            }
            @Override
            public void onFailure(Call<List<Game>> call, Throwable t) {
                UIUtils.networkError(GameScreenActivity.this, () -> saveCoachNoteOnHalfEnd());
            }
        });
    }

    private String getInitials(Player player) {
        String firstInitial = player.getPlayer_FirstName().substring(0, 1).toUpperCase();
        String lastInitial = player.getPlayer_Surname().substring(0, 1).toUpperCase();
        return firstInitial + "." + lastInitial + ".";
    }

    private static final String[] ACTIONS = {"Goal", "Penalty Goal", "Goal Missed", "For", "Against", "Drop Ball", "Held Ball", "Stepping", "Break", "Contact", "Obstruction", "Centre Pass Receive", "Goal Assist", "Offensive Rebound", "Defensive Rebound", "Deflection", "Intercept"};

    private void showActionDialog(Player player, String pos) {
        BottomSheetDialog sheet = new BottomSheetDialog(this);
        View content = getLayoutInflater().inflate(R.layout.sheet_player_actions, null);
        ((TextView) content.findViewById(R.id.sheetTitle))
                .setText("Record action for " + player.getPlayer_FirstName() + " " + player.getPlayer_Surname());

        ListView list = content.findViewById(R.id.actionList);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        Runnable refresh = () -> {
            adapter.clear();
            Map<String, Integer> stats = playerStats.get(player.getPlayer_ID());
            for (String a : ACTIONS) {
                adapter.add(a + " (" + (stats == null ? 0 : stats.getOrDefault(a, 0)) + ")");
            }
        };
        refresh.run();
        list.setAdapter(adapter);
        list.setOnItemClickListener((parent, view, which, id) -> recordAction(player, ACTIONS[which], list, refresh));

        sheet.setContentView(content);
        sheet.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
        sheet.show();
    }

    private void recordAction(Player player, String actionType, View anchor, Runnable refresh) {
        String timestamp = timerText.getText().toString().split(" ")[0];
        Long gameId = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).getLong("game_ID", -1);
        PlayerAction action = new PlayerAction(actionType, timestamp, "Half " + currentHalf, player.getPlayer_ID(), gameId);

        boolean isGoal = actionType.equals("Goal") || actionType.equals("Penalty Goal");
        String previousCentrePass = currentCentrePassTeam;

        updatePlayerStats(action, 1);
        playerActionHistory.putIfAbsent(player.getPlayer_ID(), new ArrayList<>());
        playerActionHistory.get(player.getPlayer_ID()).add(action);
        if (isGoal) {
            changeScore(scoreMadibaz, "game_madibaz_score", 1, otherTeam(currentCentrePassTeam));
            UIUtils.confirmHaptic(anchor);
        }
        refresh.run();

        // Undo can be tapped before the insert returns its ID; the callback finishes the delete then.
        boolean[] undone = {false};
        api.recordPlayerAction(action).enqueue(new Callback<List<PlayerAction>>() {
            @Override
            public void onResponse(Call<List<PlayerAction>> call, Response<List<PlayerAction>> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().isEmpty()) {
                    UIUtils.showMessage(anchor, actionType + " wasn't saved (error " + response.code() + ")");
                    return;
                }
                action.setAction_ID(response.body().get(0).getAction_ID());
                if (undone[0]) deletePlayerAction(action);
            }

            @Override
            public void onFailure(Call<List<PlayerAction>> call, Throwable t) {
                UIUtils.networkError(anchor, null);
            }
        });

        Snackbar.make(anchor, actionType + " \u00b7 " + getInitials(player), Snackbar.LENGTH_LONG)
                .setAction("Undo", v -> {
                    undone[0] = true;
                    updatePlayerStats(action, -1);
                    playerActionHistory.get(player.getPlayer_ID()).remove(action);
                    // ponytail: restores the centre pass from before this goal; stale if another goal landed in between
                    if (isGoal) changeScore(scoreMadibaz, "game_madibaz_score", -1, previousCentrePass);
                    refresh.run();
                    if (action.getAction_ID() != null) deletePlayerAction(action);
                })
                .show();
    }

    private void updatePlayerStats(PlayerAction action, int delta) {
        playerStats.putIfAbsent(action.getPlayer_ID(), new HashMap<>());
        Map<String, Integer> stats = playerStats.get(action.getPlayer_ID());
        stats.put(action.getAction_Type(), stats.getOrDefault(action.getAction_Type(), 0) + delta);
    }

    private void deletePlayerAction(PlayerAction action) {
        api.deletePlayerAction("eq." + action.getAction_ID()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
                    UIUtils.showMessage(GameScreenActivity.this, "Undo wasn't saved (error " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                UIUtils.networkError(GameScreenActivity.this, () -> deletePlayerAction(action));
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