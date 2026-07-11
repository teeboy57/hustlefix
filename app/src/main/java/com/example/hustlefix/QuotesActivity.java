package com.example.hustlefix;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
public class QuotesActivity extends AppCompatActivity {
    private ImageView ivBack, ivFilter;
    private TabLayout tabLayout;
    private TextView tvPendingCount, tvAcceptedCount, tvTotalAmount;
    private EditText etSearch;
    private ProgressBar progressBar;
    private RecyclerView recyclerQuotes;
    private LinearLayout emptyState;
    private TextView tvEmptyMessage;
    private DatabaseReference quotesRef;
    private FirebaseUser currentUser;
    private List<Quote> quoteList;
    private List<Quote> filteredList;
    private QuoteAdapter adapter;
    private int currentTab = 0;
    private String searchQuery = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageManager.applyLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quotes);
        initViews();
        setupFirebase();
        setupTabs();
        setupSearch();
        loadQuotes();
        setupClickListeners();
    }
    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        ivFilter = findViewById(R.id.ivFilter);
        tabLayout = findViewById(R.id.tabLayout);
        tvPendingCount = findViewById(R.id.tvPendingCount);
        tvAcceptedCount = findViewById(R.id.tvAcceptedCount);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        etSearch = findViewById(R.id.etSearch);
        progressBar = findViewById(R.id.progressBar);
        recyclerQuotes = findViewById(R.id.recyclerQuotes);
        emptyState = findViewById(R.id.emptyState);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
        recyclerQuotes.setLayoutManager(new LinearLayoutManager(this));
        quoteList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new QuoteAdapter(filteredList);
        recyclerQuotes.setAdapter(adapter);
    }
    private void setupFirebase() {
        quotesRef = FirebaseDatabase.getInstance().getReference("quotes");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }
    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition();
                filterQuotes();
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }
    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().toLowerCase();
                filterQuotes();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    private void setupClickListeners() {
        ivBack.setOnClickListener(v -> finish());
        ivBack.setOnLongClickListener(v -> {
            NavigationHelper.showNavigationDialog(QuotesActivity.this);
            return true;
        });
        ivFilter.setOnClickListener(v -> showFilterDialog());
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_app_navigation, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return NavigationHelper.onOptionsItemSelected(this, item) || super.onOptionsItemSelected(item);
    }
    private void loadQuotes() {
        progressBar.setVisibility(View.VISIBLE);
        quotesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                quoteList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Quote quote = data.getValue(Quote.class);
                    if (quote != null) {
                        quote.setId(data.getKey());
                        // Show quotes where user is either client or worker
                        if ((currentUser != null && quote.getClientId() != null && quote.getClientId().equals(currentUser.getUid())) ||
                                (currentUser != null && quote.getWorkerId() != null && quote.getWorkerId().equals(currentUser.getUid()))) {
                            quoteList.add(quote);
                        }
                    }
                }
                updateStats();
                filterQuotes();
                progressBar.setVisibility(View.GONE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(QuotesActivity.this, "Failed to load quotes", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void updateStats() {
        int pendingCount = 0;
        int acceptedCount = 0;
        double totalAmount = 0;
        for (Quote quote : quoteList) {
            if ("pending".equalsIgnoreCase(quote.getStatus())) {
                pendingCount++;
            } else if ("accepted".equalsIgnoreCase(quote.getStatus())) {
                acceptedCount++;
                totalAmount += quote.getAmount();
            }
        }
        tvPendingCount.setText(String.valueOf(pendingCount));
        tvAcceptedCount.setText(String.valueOf(acceptedCount));
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "ZA"));
        tvTotalAmount.setText(currencyFormat.format(totalAmount));
    }
    private void filterQuotes() {
        filteredList.clear();
        for (Quote quote : quoteList) {
            boolean statusMatch = true;
            switch (currentTab) {
                case 0: // Pending
                    statusMatch = "pending".equalsIgnoreCase(quote.getStatus());
                    break;
                case 1: // All
                    statusMatch = true;
                    break;
                case 2: // Accepted
                    statusMatch = "accepted".equalsIgnoreCase(quote.getStatus());
                    break;
            }
            boolean searchMatch = true;
            if (!searchQuery.isEmpty()) {
                searchMatch = (quote.getWorkerName() != null && quote.getWorkerName().toLowerCase().contains(searchQuery)) ||
                        (quote.getJobTitle() != null && quote.getJobTitle().toLowerCase().contains(searchQuery));
            }
            if (statusMatch && searchMatch) {
                filteredList.add(quote);
            }
        }
        adapter.notifyDataSetChanged();
        if (filteredList.isEmpty()) {
            String message;
            if (searchQuery.isEmpty()) {
                switch (currentTab) {
                    case 0: message = "No pending quotes"; break;
                    case 1: message = "No quotes available"; break;
                    case 2: message = "No accepted quotes"; break;
                    default: message = "No quotes available";
                }
            } else {
                message = "No quotes matching \"" + searchQuery + "\"";
            }
            showEmptyState(message);
        } else {
            hideEmptyState();
        }
    }
    private void showFilterDialog() {
        String[] filterOptions = {"Sort by Date", "Sort by Amount", "Sort by Worker"};
        new AlertDialog.Builder(this)
                .setTitle("Filter Quotes")
                .setItems(filterOptions, (dialog, which) -> {
                    switch (which) {
                        case 0: sortQuotes("date"); break;
                        case 1: sortQuotes("amount"); break;
                        case 2: sortQuotes("worker"); break;
                    }
                })
                .show();
    }
    private void sortQuotes(String sortBy) {
        switch (sortBy) {
            case "date":
                filteredList.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
                break;
            case "amount":
                filteredList.sort((a, b) -> Double.compare(b.getAmount(), a.getAmount()));
                break;
            case "worker":
                filteredList.sort((a, b) -> {
                    if (a.getWorkerName() == null) return 1;
                    if (b.getWorkerName() == null) return -1;
                    return a.getWorkerName().compareTo(b.getWorkerName());
                });
                break;
        }
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "Sorted by " + sortBy, Toast.LENGTH_SHORT).show();
    }
    private void acceptQuote(Quote quote) {
        new AlertDialog.Builder(this)
                .setTitle("Accept Quote")
                .setMessage("Accept quote from " + quote.getWorkerName() + " for " + quote.getFormattedAmount() + "?")
                .setPositiveButton("Accept", (dialog, which) -> {
                    progressBar.setVisibility(View.VISIBLE);
                    quotesRef.child(quote.getId()).child("status").setValue("accepted")
                            .addOnSuccessListener(aVoid -> {
                                progressBar.setVisibility(View.GONE);
                                quote.setStatus("accepted");
                                updateStats();
                                filterQuotes();
                                Toast.makeText(QuotesActivity.this, "Quote accepted!", Toast.LENGTH_SHORT).show();
                                // Update job status
                                DatabaseReference jobRef = FirebaseDatabase.getInstance().getReference("jobs").child(quote.getJobId());
                                jobRef.child("status").setValue("in_progress");
                                jobRef.child("assignedTo").setValue(quote.getWorkerId());
                                jobRef.child("assignedToName").setValue(quote.getWorkerName());
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(QuotesActivity.this, "Failed to accept quote", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void declineQuote(Quote quote) {
        new AlertDialog.Builder(this)
                .setTitle("Decline Quote")
                .setMessage("Decline quote from " + quote.getWorkerName() + "?")
                .setPositiveButton("Decline", (dialog, which) -> {
                    progressBar.setVisibility(View.VISIBLE);
                    quotesRef.child(quote.getId()).child("status").setValue("declined")
                            .addOnSuccessListener(aVoid -> {
                                progressBar.setVisibility(View.GONE);
                                quote.setStatus("declined");
                                updateStats();
                                filterQuotes();
                                Toast.makeText(QuotesActivity.this, "Quote declined", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(QuotesActivity.this, "Failed to decline quote", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void openChat(Quote quote) {
        if (currentUser == null) return;
        String myId = currentUser.getUid();
        String otherId;
        String otherName;
        if (quote.getClientId() != null && quote.getClientId().equals(myId)) {
            otherId = quote.getWorkerId();
            otherName = quote.getWorkerName();
        } else {
            otherId = quote.getClientId();
            otherName = quote.getClientName();
        }
        ChatLauncher.openChat(this, otherId, otherName);
    }
    private void showEmptyState(String message) {
        emptyState.setVisibility(View.VISIBLE);
        recyclerQuotes.setVisibility(View.GONE);
        tvEmptyMessage.setText(message);
    }
    private void hideEmptyState() {
        emptyState.setVisibility(View.GONE);
        recyclerQuotes.setVisibility(View.VISIBLE);
    }
    // QuoteAdapter
    private class QuoteAdapter extends RecyclerView.Adapter<QuoteAdapter.ViewHolder> {
        private List<Quote> quotes;
        QuoteAdapter(List<Quote> quotes) {
            this.quotes = quotes;
        }
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_quote, parent, false);
            return new ViewHolder(view);
        }
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Quote quote = quotes.get(position);
            holder.tvWorkerName.setText(quote.getWorkerName() != null ? quote.getWorkerName() : "Unknown Worker");
            holder.tvJobTitle.setText(quote.getJobTitle() != null ? quote.getJobTitle() : "Job");
            holder.tvMessage.setText(quote.getMessage() != null && !quote.getMessage().isEmpty() ? quote.getMessage() : "No description provided");
            holder.tvAmount.setText(quote.getFormattedAmount());
            holder.tvTimeline.setText(quote.getTimeline() != null ? quote.getTimeline() : "TBD");
            if (quote.getTimestamp() > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                holder.tvReceivedDate.setText("Received: " + sdf.format(new Date(quote.getTimestamp())));
            } else {
                holder.tvReceivedDate.setText("Received recently");
            }
            // Set status badge
            if ("pending".equalsIgnoreCase(quote.getStatus())) {
                holder.tvStatus.setText("Pending");
                holder.tvStatus.setBackgroundResource(R.drawable.badge_pending);
                holder.actionButtons.setVisibility(View.VISIBLE);
                holder.acceptedMessage.setVisibility(View.GONE);
                // Show accept/decline buttons only for client
                if (currentUser != null && quote.getClientId() != null && quote.getClientId().equals(currentUser.getUid())) {
                    holder.btnAccept.setVisibility(View.VISIBLE);
                    holder.btnDecline.setVisibility(View.VISIBLE);
                } else {
                    holder.btnAccept.setVisibility(View.GONE);
                    holder.btnDecline.setVisibility(View.GONE);
                }
            } else if ("accepted".equalsIgnoreCase(quote.getStatus())) {
                holder.tvStatus.setText("Accepted");
                holder.tvStatus.setBackgroundResource(R.drawable.badge_accepted);
                holder.actionButtons.setVisibility(View.GONE);
                holder.acceptedMessage.setVisibility(View.VISIBLE);
            } else {
                holder.tvStatus.setText(quote.getStatus());
                holder.actionButtons.setVisibility(View.GONE);
                holder.acceptedMessage.setVisibility(View.GONE);
            }
            holder.btnAccept.setOnClickListener(v -> acceptQuote(quote));
            holder.btnDecline.setOnClickListener(v -> declineQuote(quote));
            holder.btnChat.setOnClickListener(v -> openChat(quote));
        }
        @Override
        public int getItemCount() {
            return quotes.size();
        }
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvWorkerName, tvJobTitle, tvStatus, tvMessage, tvAmount, tvTimeline, tvReceivedDate;
            Button btnAccept, btnDecline, btnChat;
            LinearLayout actionButtons, acceptedMessage;
            ViewHolder(View itemView) {
                super(itemView);
                tvWorkerName = itemView.findViewById(R.id.tvWorkerName);
                tvJobTitle = itemView.findViewById(R.id.tvJobTitle);
                tvStatus = itemView.findViewById(R.id.tvStatus);
                tvMessage = itemView.findViewById(R.id.tvMessage);
                tvAmount = itemView.findViewById(R.id.tvAmount);
                tvTimeline = itemView.findViewById(R.id.tvTimeline);
                tvReceivedDate = itemView.findViewById(R.id.tvReceivedDate);
                btnAccept = itemView.findViewById(R.id.btnAccept);
                btnDecline = itemView.findViewById(R.id.btnDecline);
                btnChat = itemView.findViewById(R.id.btnChat);
                actionButtons = itemView.findViewById(R.id.actionButtons);
                acceptedMessage = itemView.findViewById(R.id.acceptedMessage);
            }
        }
    }
}