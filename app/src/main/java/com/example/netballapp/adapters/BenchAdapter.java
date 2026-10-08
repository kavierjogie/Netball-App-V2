package com.example.netballapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.netballapp.Model.Player;
import com.example.netballapp.R;

import java.util.List;

public class BenchAdapter extends RecyclerView.Adapter<BenchAdapter.ViewHolder> {

    private final List<Player> benchPlayers;
    private final OnPlayerClickListener listener;

    public interface OnPlayerClickListener {
        void onPlayerClick(Player player);
    }

    public BenchAdapter(List<Player> benchPlayers, OnPlayerClickListener listener) {
        this.benchPlayers = benchPlayers;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bench_player, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Player player = benchPlayers.get(position);
        holder.nameText.setText(player.getPlayer_FirstName() + " " + player.getPlayer_Surname() +
                " (" + player.getPlayer_position() + ")");
        holder.itemView.setOnClickListener(v -> listener.onPlayerClick(player));
    }

    @Override
    public int getItemCount() {
        return benchPlayers.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;

        public ViewHolder(View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.benchPlayerName);
        }
    }
}
