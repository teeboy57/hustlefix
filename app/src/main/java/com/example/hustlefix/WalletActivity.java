package com.example.hustlefix;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class WalletActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvWalletBalance, tvEmptyTransactions;
    private MaterialButton btnTopUp, btnWithdraw;
    private ProgressBar progressBar;
    private RecyclerView rvTransactions;
    private TransactionAdapter adapter;
    private List<Transaction> transactionList = new ArrayList<>();

    private DatabaseReference userRef;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageManager.applyLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId == null) {
            finish();
            return;
        }
        userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);

        initViews();
        setupToolbar();
        loadWalletBalance();
        loadTransactions();
        setupClickListeners();
    }

    private void loadTransactions() {
        DatabaseReference transRef = FirebaseDatabase.getInstance().getReference("transactions").child(currentUserId);
        transRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                transactionList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Transaction t = ds.getValue(Transaction.class);
                    if (t != null) transactionList.add(t);
                }
                Collections.reverse(transactionList);
                adapter.notifyDataSetChanged();

                if (transactionList.isEmpty()) {
                    tvEmptyTransactions.setVisibility(View.VISIBLE);
                    rvTransactions.setVisibility(View.GONE);
                } else {
                    tvEmptyTransactions.setVisibility(View.GONE);
                    rvTransactions.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvWalletBalance = findViewById(R.id.tvWalletBalance);
        tvEmptyTransactions = findViewById(R.id.tvEmptyTransactions);
        btnTopUp = findViewById(R.id.btnTopUp);
        btnWithdraw = findViewById(R.id.btnWithdraw);
        progressBar = findViewById(R.id.progressBar);
        rvTransactions = findViewById(R.id.rvTransactions);

        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TransactionAdapter(transactionList);
        rvTransactions.setAdapter(adapter);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Wallet");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadWalletBalance() {
        setLoading(true);
        userRef.child("walletBalance").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                setLoading(false);
                double balance = 0;
                if (snapshot.exists()) {
                    Double val = snapshot.getValue(Double.class);
                    if (val != null) balance = val;
                }
                tvWalletBalance.setText(String.format(Locale.getDefault(), "R%.2f", balance));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                setLoading(false);
            }
        });
    }

    private void setupClickListeners() {
        btnTopUp.setOnClickListener(v -> showTopUpDialog());
        btnWithdraw.setOnClickListener(v -> showWithdrawDialog());
    }

    private void showWithdrawDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_withdraw, null);
        EditText etAmount = dialogView.findViewById(R.id.etWithdrawAmount);
        EditText etAccount = dialogView.findViewById(R.id.etAccountNumber);
        RadioGroup rgBank = dialogView.findViewById(R.id.rgBank);

        new AlertDialog.Builder(this)
                .setTitle("Withdraw Funds")
                .setView(dialogView)
                .setPositiveButton("Withdraw", (dialog, which) -> {
                    String amountStr = etAmount.getText().toString().trim();
                    String account = etAccount.getText().toString().trim();
                    int selectedBankId = rgBank.getCheckedRadioButtonId();

                    if (amountStr.isEmpty() || account.isEmpty() || selectedBankId == -1) {
                        Toast.makeText(this, "Please fill in all details", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double amount = Double.parseDouble(amountStr);
                    processWithdrawal(amount, account);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void processWithdrawal(double amount, String account) {
        setLoading(true);
        userRef.child("walletBalance").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double currentBalance = 0;
                if (snapshot.exists()) {
                    Double val = snapshot.getValue(Double.class);
                    if (val != null) currentBalance = val;
                }

                if (currentBalance >= amount) {
                    double newBalance = currentBalance - amount;
                    userRef.child("walletBalance").setValue(newBalance)
                            .addOnSuccessListener(aVoid -> {
                                setLoading(false);
                                Toast.makeText(WalletActivity.this, "Withdrawal of R" + amount + " processed to account " + account, Toast.LENGTH_LONG).show();
                                recordTransaction("Withdrawal", -amount);
                            });
                } else {
                    setLoading(false);
                    Toast.makeText(WalletActivity.this, "Insufficient balance!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                setLoading(false);
            }
        });
    }

    private void recordTransaction(String type, double amount) {
        DatabaseReference transRef = FirebaseDatabase.getInstance().getReference("transactions").child(currentUserId);
        String id = transRef.push().getKey();
        if (id == null) return;

        Map<String, Object> trans = new HashMap<>();
        trans.put("id", id);
        trans.put("type", type);
        trans.put("amount", amount);
        trans.put("timestamp", System.currentTimeMillis());

        transRef.child(id).setValue(trans);
    }

    private void showTopUpDialog() {
        EditText etAmount = new EditText(this);
        etAmount.setHint("Enter amount (e.g. 500)");
        etAmount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        
        new AlertDialog.Builder(this)
                .setTitle("Top Up Wallet")
                .setMessage("Enter the amount of virtual money you want to add.")
                .setView(etAmount)
                .setPositiveButton("Add Funds", (dialog, which) -> {
                    String val = etAmount.getText().toString().trim();
                    if (!val.isEmpty()) {
                        double amount = Double.parseDouble(val);
                        topUpWallet(amount);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void topUpWallet(double amount) {
        setLoading(true);
        userRef.child("walletBalance").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double current = 0;
                if (snapshot.exists()) {
                    Double val = snapshot.getValue(Double.class);
                    if (val != null) current = val;
                }
                userRef.child("walletBalance").setValue(current + amount)
                        .addOnSuccessListener(aVoid -> {
                            setLoading(false);
                            Toast.makeText(WalletActivity.this, "R" + amount + " added successfully!", Toast.LENGTH_SHORT).show();
                            recordTransaction("Top Up", amount);
                        });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { setLoading(false); }
        });
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }
}
