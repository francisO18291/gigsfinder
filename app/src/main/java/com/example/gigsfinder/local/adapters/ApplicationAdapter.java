package com.example.gigsfinder.local.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.gigsfinder.R;
import com.example.gigsfinder.local.models.Application;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ApplicationAdapter extends RecyclerView.Adapter<ApplicationAdapter.ApplicationViewHolder> {
    private List<Application> applicationList;
    private Context context;
    private FirebaseFirestore db;

    public ApplicationAdapter(List<Application> applicationList, Context context) {
        this.applicationList = applicationList;
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ApplicationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_application, parent, false);
        return new ApplicationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ApplicationViewHolder holder, int position) {
        Application application = applicationList.get(position);

        holder.tvJobTitle.setText(application.getJobTitle());
        holder.tvSeekerName.setText("Applicant: " + application.getSeekerName());
        holder.tvStatus.setText("Status: " + application.getStatus().toUpperCase());

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        holder.tvAppliedDate.setText("Applied: " + sdf.format(new Date(application.getAppliedAt())));

        if (application.getCoverLetter() != null && !application.getCoverLetter().isEmpty()) {
            holder.tvCoverLetter.setText(application.getCoverLetter());
            holder.tvCoverLetter.setVisibility(View.VISIBLE);
        } else {
            holder.tvCoverLetter.setVisibility(View.GONE);
        }

        if ("pending".equals(application.getStatus())) {
            holder.btnAccept.setVisibility(View.VISIBLE);
            holder.btnReject.setVisibility(View.VISIBLE);

            holder.btnAccept.setOnClickListener(v -> {
                updateApplicationStatus(application, "accepted", position);
            });

            holder.btnReject.setOnClickListener(v -> {
                updateApplicationStatus(application, "rejected", position);
            });
        } else {
            holder.btnAccept.setVisibility(View.GONE);
            holder.btnReject.setVisibility(View.GONE);
        }


        switch (application.getStatus()) {
            case "accepted":
                holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
                break;
            case "rejected":
                holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
                break;
            default:
                holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
                break;
        }
    }

    private void updateApplicationStatus(Application application, String status, int position) {
        application.setStatus(status);
        application.setRespondedAt(System.currentTimeMillis());

        db.collection("applications").document(application.getApplicationId())
                .update("status", status, "respondedAt", application.getRespondedAt())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Application " + status, Toast.LENGTH_SHORT).show();
                    notifyItemChanged(position);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error updating application", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getItemCount() {
        return applicationList.size();
    }

    public static class ApplicationViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvJobTitle, tvSeekerName, tvStatus, tvAppliedDate, tvCoverLetter;
        Button btnAccept, btnReject;

        public ApplicationViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            tvJobTitle = itemView.findViewById(R.id.tvJobTitle);
            tvSeekerName = itemView.findViewById(R.id.tvSeekerName);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvAppliedDate = itemView.findViewById(R.id.tvAppliedDate);
            tvCoverLetter = itemView.findViewById(R.id.tvCoverLetter);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}