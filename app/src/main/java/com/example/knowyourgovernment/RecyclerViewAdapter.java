package com.example.knowyourgovernment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RepresentativeViewHolder> {

    private List<Representative> representatives;
    private MainActivity mainActivity;

    public RecyclerViewAdapter(List<Representative> representatives, MainActivity mainActivity) {
        this.representatives = representatives;
        this.mainActivity = mainActivity;
    }

    @NonNull
    @Override
    public RepresentativeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_view_row, parent, false);

        itemView.setOnClickListener(mainActivity);

        return new RepresentativeViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RepresentativeViewHolder holder, int position) {
        Representative representative = representatives.get(position);
        holder.office.setText(representative.getOffice());
        holder.name_party.setText(String.format("%s (%s)", representative.getName(), representative.getParty()));
    }

    @Override
    public int getItemCount() {
        return representatives.size();
    }
}
