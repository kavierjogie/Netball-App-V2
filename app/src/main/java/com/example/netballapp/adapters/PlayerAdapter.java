package com.example.netballapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.netballapp.Model.Player;
import com.example.netballapp.R;
import com.example.netballapp.activities.UpdatePlayerProfile;
import com.example.netballapp.listeners.OnPlayerActionListener;

import java.util.List;

public class PlayerAdapter extends RecyclerView.Adapter<PlayerAdapter.PlayerViewHolder> {

    private final Context context;
    private final List<Player> playerList;
    private final OnPlayerActionListener listener;

    public PlayerAdapter(Context context, List<Player> playerList, OnPlayerActionListener listener) {
        this.context = context;
        this.playerList = playerList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PlayerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.player_row, parent, false);
        return new PlayerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayerViewHolder holder, int position) {
        Player player = playerList.get(position);

        holder.txtFullName.setText(player.getPlayer_FirstName() + " " + player.getPlayer_Surname());
        holder.txtPlayerNumber.setText("Number: " + (player.getPlayer_Number() != null ? player.getPlayer_Number() : "-"));
        holder.txtPlayerPosition.setText("Position: " + (player.getPlayer_position() != null ? player.getPlayer_position() : "-"));
        holder.txtPlayerDOB.setText("DOB: " + (player.getPlayer_DOB() != null ? player.getPlayer_DOB() : "-"));
        holder.txtPlayerHeight.setText("Height: " + (player.getPlayer_Height() != null ? player.getPlayer_Height() + " cm" : "-"));

        holder.btnUpdate.setOnClickListener(v -> {
            Intent intent = new Intent(context, UpdatePlayerProfile.class);
            intent.putExtra("player_id", player.getPlayer_ID());
            context.startActivity(intent);
        });

        holder.btnDelete.setOnClickListener(v -> listener.onDeletePlayer(player, position));
    }


    @Override
    public int getItemCount() {
        return playerList.size();
    }

    public static class PlayerViewHolder extends RecyclerView.ViewHolder {
        TextView txtFullName, txtPlayerNumber, txtPlayerPosition, txtPlayerDOB, txtPlayerHeight;
        Button btnUpdate, btnDelete;

        public PlayerViewHolder(@NonNull View itemView) {
            super(itemView);
            txtFullName = itemView.findViewById(R.id.txtFullName);
            txtPlayerNumber = itemView.findViewById(R.id.txtPlayerNumber);
            txtPlayerPosition = itemView.findViewById(R.id.txtPlayerPosition);
            txtPlayerDOB = itemView.findViewById(R.id.txtPlayerDOB);
            txtPlayerHeight = itemView.findViewById(R.id.txtPlayerHeight);
            btnUpdate = itemView.findViewById(R.id.btnUpdate);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    public void removePlayer(int position) {
        if (position >= 0 && position < playerList.size()) {
            playerList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public List<Player> getPlayers() {
        return playerList;
    }
}
