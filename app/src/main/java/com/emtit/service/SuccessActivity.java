package com.emtit.service;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class SuccessActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);

        int ticketId = getIntent().getIntExtra("ticketId", 0);
        TextView tvTicketId = findViewById(R.id.tvTicketId);
        if (tvTicketId != null) {
            tvTicketId.setText("מספר קריאה: #" + ticketId);
        }

        Button btnBack = findViewById(R.id.btnBackHome);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                Intent intent = new Intent(SuccessActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            });
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(SuccessActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
