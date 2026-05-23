package com.example.hustlefix;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Job applications are stored under quotes and mirrored under applications/{jobId}.
 */
public final class ApplicationsHelper {

    private ApplicationsHelper() {}

    public static void saveApplicationForQuote(Quote quote) {
        if (quote == null || quote.getJobId() == null || quote.getId() == null) {
            return;
        }

        Map<String, Object> application = new HashMap<>();
        application.put("jobId", quote.getJobId());
        application.put("quoteId", quote.getId());
        application.put("workerId", quote.getWorkerId());
        application.put("workerName", quote.getWorkerName());
        application.put("clientId", quote.getClientId());
        application.put("clientName", quote.getClientName());
        application.put("amount", quote.getAmount());
        application.put("timeline", quote.getTimeline());
        application.put("message", quote.getMessage());
        application.put("status", quote.getStatus() != null ? quote.getStatus() : "pending");
        application.put("timestamp", quote.getTimestamp());

        DatabaseReference root = FirebaseDatabase.getInstance().getReference();
        root.child("applications").child(quote.getJobId()).child(quote.getId()).setValue(application);

        DatabaseReference countRef = root.child("jobs").child(quote.getJobId()).child("applicationsCount");
        countRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Integer count = currentData.getValue(Integer.class);
                if (count == null) {
                    count = 0;
                }
                currentData.setValue(count + 1);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {
                // Count update is best-effort
            }
        });
    }

    public static void showApplicationsForJob(AppCompatActivity activity, Job job) {
        if (job == null || job.getJobId() == null) {
            return;
        }

        FirebaseDatabase.getInstance().getReference("quotes")
                .orderByChild("jobId")
                .equalTo(job.getJobId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        StringBuilder message = new StringBuilder();
                        int count = 0;

                        for (DataSnapshot child : snapshot.getChildren()) {
                            Quote quote = child.getValue(Quote.class);
                            if (quote != null) {
                                count++;
                                message.append("• ")
                                        .append(quote.getWorkerName() != null ? quote.getWorkerName() : "Worker")
                                        .append(" — R")
                                        .append(String.format("%.2f", quote.getAmount()))
                                        .append(" (")
                                        .append(quote.getStatus() != null ? quote.getStatus() : "pending")
                                        .append(")\n");
                            }
                        }

                        if (count == 0) {
                            loadLegacyApplications(activity, job, message);
                            return;
                        }

                        showDialog(activity, job.getTitle(), message.toString(), count);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        showError(activity);
                    }
                });
    }

    private static void loadLegacyApplications(AppCompatActivity activity, Job job, StringBuilder message) {
        FirebaseDatabase.getInstance().getReference("applications")
                .child(job.getJobId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        int count = 0;
                        for (DataSnapshot child : snapshot.getChildren()) {
                            String workerName = child.child("workerName").getValue(String.class);
                            String status = child.child("status").getValue(String.class);
                            Double amount = child.child("amount").getValue(Double.class);
                            if (workerName != null) {
                                count++;
                                message.append("• ").append(workerName);
                                if (amount != null) {
                                    message.append(" — R").append(String.format("%.2f", amount));
                                }
                                message.append(" (").append(status != null ? status : "pending").append(")\n");
                            }
                        }

                        if (count == 0) {
                            message.append("No quotes or applications yet.\nWorkers can apply from Find Workers.");
                        }
                        showDialog(activity, job.getTitle(), message.toString(), count);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        showError(activity);
                    }
                });
    }

    private static void showDialog(Context context, String jobTitle, String body, int count) {
        new MaterialAlertDialogBuilder(context)
                .setTitle("Applications — " + jobTitle + " (" + count + ")")
                .setMessage(body.trim())
                .setPositiveButton(R.string.ok, null)
                .setNeutralButton("Quotes", (d, w) -> {
                    if (context instanceof AppCompatActivity) {
                        context.startActivity(new android.content.Intent(context, QuotesActivity.class));
                    }
                })
                .show();
    }

    private static void showError(Context context) {
        android.widget.Toast.makeText(context, "Failed to load applications", android.widget.Toast.LENGTH_SHORT).show();
    }
}
