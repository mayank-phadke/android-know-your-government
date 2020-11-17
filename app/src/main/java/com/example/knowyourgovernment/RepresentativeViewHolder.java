package com.example.knowyourgovernment;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RepresentativeViewHolder extends RecyclerView.ViewHolder {

    TextView office;
    TextView name_party;

    public RepresentativeViewHolder(@NonNull View itemView) {
        super(itemView);
        office = itemView.findViewById(R.id.office_text_view);
        name_party = itemView.findViewById(R.id.name_party_text_view);
    }
}
