package com.example.netballapp.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.netballapp.Model.Player;
import com.example.netballapp.R;
import com.example.netballapp.listeners.OnPlayerClickListener;
import androidx.cardview.widget.CardView;

import java.util.List;

public class PlayerAdapterCourt extends RecyclerView.Adapter<PlayerAdapterCourt.PlayerViewHolder>
{
    public List<Player> playerList;
    private final Context context;
    private OnPlayerClickListener listener;
    private int selectedPosition = RecyclerView.NO_POSITION;

    public PlayerAdapterCourt(List<Player> playerList, Context context, OnPlayerClickListener listener) {
        this.playerList = playerList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PlayerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.player_row2,parent,false);
        return new PlayerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayerViewHolder holder, int position) {
        Player player = playerList.get(position);

        String fullName = player.getPlayer_FirstName() + " " + player.getPlayer_Surname();
        holder.lblFullName.setText(fullName);
        holder.lblPosition.setText(player.getPlayer_position());

        if (selectedPosition == position) {
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.highlight_blue));
        } else {
            holder.cardView.setCardBackgroundColor(Color.WHITE);
        }

        holder.itemView.setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);
            listener.onPlayerClick(player);
        });
    }

    @Override
    public int getItemCount() {
        return playerList.size();
    }

    public static class PlayerViewHolder extends RecyclerView.ViewHolder {
        public CardView cardView;
        public TextView lblFullName;
        public TextView lblPosition;

        public PlayerViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            lblFullName = itemView.findViewById(R.id.txtFullName);
            lblPosition = itemView.findViewById(R.id.txtPosition);
        }
    }

    public void removePlayer(Player player) {
        int index = playerList.indexOf(player);
        if (index != -1) {
            playerList.remove(index);
            notifyItemRemoved(index);

            if (selectedPosition == index) {
                selectedPosition = RecyclerView.NO_POSITION;
            } else if (selectedPosition > index) {
                selectedPosition--;
            }
        }
    }
    public List<Player> getPlayers() {
        return playerList;
    }
}
