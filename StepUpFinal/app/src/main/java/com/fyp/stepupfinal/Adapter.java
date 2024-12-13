package com.fyp.stepupfinal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class Adapter extends RecyclerView.Adapter<Adapter.ChallengeViewHolder> {
    Context context;
    private ArrayList<ChallengeModel> challengeList = new ArrayList<>();

    public Adapter(Context context, ArrayList<ChallengeModel> challengeList) {
        this.context = context;
        this.challengeList = challengeList;
    }

    @NonNull
    @Override
    public ChallengeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_each_challenge, parent, false);
            return new ChallengeViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Adapter.ChallengeViewHolder holder, int position) {
        ChallengeModel challenge = challengeList.get(position);
        holder.challengeTitle.setText(challenge.getTitle());
        holder.challengeXP.setText(challenge.getPoints() + " points");
        holder.challengeDaysRemaining.setText(String.valueOf(challenge.getTimer()));


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, Challenge.class);
                intent.putExtra("Title", challenge.getTitle());
                intent.putExtra("Description", challenge.getDescription());
                intent.putExtra("Points", challenge.getPoints());
                intent.putExtra("Duration", challenge.getDuration());
                intent.putExtra("Timer", challenge.getTimer());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return challengeList.size();
    }

    public class ChallengeViewHolder extends RecyclerView.ViewHolder{

        TextView challengeTitle, challengeXP, challengeDaysRemaining;
        public ChallengeViewHolder(@NonNull View itemView) {
            super(itemView);
            challengeTitle = itemView.findViewById(R.id.tvchallengetitle);
            challengeXP = itemView.findViewById(R.id.tvchallengepoints);
            challengeDaysRemaining = itemView.findViewById(R.id.tvdaysremaining);

        }
    }
}
