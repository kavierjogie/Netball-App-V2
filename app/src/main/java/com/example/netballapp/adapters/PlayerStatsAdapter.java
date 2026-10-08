package com.example.netballapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.netballapp.Model.PlayerStatsView;
import com.example.netballapp.R;

import java.util.List;
public class PlayerStatsAdapter extends RecyclerView.Adapter<PlayerStatsAdapter.StatsViewHolder> {

    private final List<PlayerStatsView> statsList;

    public PlayerStatsAdapter(List<PlayerStatsView> statsList) {
        this.statsList = statsList;
    }

    @NonNull
    @Override
    public StatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_player_action, parent, false);
        return new StatsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StatsViewHolder holder, int position) {
        PlayerStatsView stats = statsList.get(position);

        holder.tvPlayerName.setText(stats.getPlayer_name());

        holder.tvGoal.setText("Goal: " + stats.getGoal());
        holder.tvPenaltyGoal.setText("Penalty Goal: " + stats.getPenalty_goal());
        holder.tvGoalMissed.setText("Goal Missed: " + stats.getGoal_missed());
        int totalShots = stats.getGoal() + stats.getPenalty_goal() + stats.getGoal_missed();
        double successRate = totalShots > 0 ? ((double)(stats.getGoal() + stats.getPenalty_goal()) / totalShots) * 100 : 0;
        holder.tvSuccessRate.setText(String.format("Success Rate: %.1f%%", successRate));

        holder.tvFor.setText("For: " + stats.getFor_());
        holder.tvAgainst.setText("Against: " + stats.getAgainst());
        holder.tvOffensiveRebound.setText("Offensive Rebound: " + stats.getOffensive_rebound());
        holder.tvDefensiveRebound.setText("Defensive Rebound: " + stats.getDefensive_rebound());
        holder.tvCentrePassReceive.setText("Centre Pass Receive: " + stats.getCentre_pass_receive());
        holder.tvGoalAssist.setText("Goal Assist: " + stats.getGoal_assist());
        holder.tvDeflection.setText("Deflection: " + stats.getDeflection());
        holder.tvIntercept.setText("Intercept: " + stats.getIntercept());

        holder.tvDropBall.setText("Drop Ball: " + stats.getDrop_ball());
        holder.tvHeldBall.setText("Held Ball: " + stats.getHeld_ball());
        holder.tvStepping.setText("Stepping: " + stats.getStepping());
        holder.tvBreak.setText("Break: " + stats.getBreak_());
        holder.tvContact.setText("Contact: " + stats.getContact());
        holder.tvObstruction.setText("Obstruction: " + stats.getObstruction());
    }

    @Override
    public int getItemCount() {
        return statsList.size();
    }

    static class StatsViewHolder extends RecyclerView.ViewHolder {
        TextView tvPlayerName, tvGoal, tvPenaltyGoal, tvGoalMissed, tvFor, tvAgainst,
                tvDropBall, tvHeldBall, tvStepping, tvBreak, tvContact, tvObstruction,
                tvCentrePassReceive, tvGoalAssist, tvOffensiveRebound, tvDefensiveRebound,
                tvDeflection, tvIntercept, tvSuccessRate;

        public StatsViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPlayerName = itemView.findViewById(R.id.tvPlayerName);
            tvGoal = itemView.findViewById(R.id.tvGoal);
            tvPenaltyGoal = itemView.findViewById(R.id.tvPenaltyGoal);
            tvGoalMissed = itemView.findViewById(R.id.tvGoalMissed);
            tvFor = itemView.findViewById(R.id.tvFor);
            tvAgainst = itemView.findViewById(R.id.tvAgainst);
            tvDropBall = itemView.findViewById(R.id.tvDropBall);
            tvHeldBall = itemView.findViewById(R.id.tvHeldBall);
            tvStepping = itemView.findViewById(R.id.tvStepping);
            tvBreak = itemView.findViewById(R.id.tvBreak);
            tvContact = itemView.findViewById(R.id.tvContact);
            tvObstruction = itemView.findViewById(R.id.tvObstruction);
            tvCentrePassReceive = itemView.findViewById(R.id.tvCentrePassReceive);
            tvGoalAssist = itemView.findViewById(R.id.tvGoalAssist);
            tvOffensiveRebound = itemView.findViewById(R.id.tvOffensiveRebound);
            tvDefensiveRebound = itemView.findViewById(R.id.tvDefensiveRebound);
            tvDeflection = itemView.findViewById(R.id.tvDeflection);
            tvIntercept = itemView.findViewById(R.id.tvIntercept);
            tvSuccessRate = itemView.findViewById(R.id.tvSuccessRate);
        }
    }
}

