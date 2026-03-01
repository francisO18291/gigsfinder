package com.example.gigsfinder.local.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.gigsfinder.local.JobDetailActivity;
import com.example.gigsfinder.R;
import com.example.gigsfinder.local.models.Job;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder> {
    private List<Job> jobList;
    private Context context;

    public JobAdapter(List<Job> jobList, Context context) {
        this.jobList = jobList;
        this.context = context;
    }

    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_job, parent, false);
        return new JobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
        Job job = jobList.get(position);

        holder.tvTitle.setText(job.getTitle());
        holder.tvEmployer.setText(job.getEmployerName());
        holder.tvLocation.setText(job.getLocation());
        holder.tvPayRate.setText("$" + job.getPayRate() + " (" + job.getPayType() + ")");
        holder.tvJobType.setText(job.getJobType().replace("_", " ").toUpperCase());

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        holder.tvPostedDate.setText("Posted: " + sdf.format(new Date(job.getPostedAt())));

        holder.tvApplicants.setText(job.getApplicantCount() + " applicants");

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, JobDetailActivity.class);
                intent.putExtra("jobId", job.getJobId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return jobList.size();
    }

    public static class JobViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvTitle, tvEmployer, tvLocation, tvPayRate, tvJobType, tvPostedDate, tvApplicants;

        public JobViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvEmployer = itemView.findViewById(R.id.tvEmployer);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvPayRate = itemView.findViewById(R.id.tvPayRate);
            tvJobType = itemView.findViewById(R.id.tvJobType);
            tvPostedDate = itemView.findViewById(R.id.tvPostedDate);
            tvApplicants = itemView.findViewById(R.id.tvApplicants);
        }
    }
}