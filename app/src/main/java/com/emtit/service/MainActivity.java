package com.emtit.service;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnNewTicket = findViewById(R.id.btnNewTicket);
        Button btnCall1 = findViewById(R.id.btnCall1);
        Button btnCall2 = findViewById(R.id.btnCall2);

        btnNewTicket.setOnClickListener(v ->
            startActivity(new Intent(MainActivity.this, NewTicketActivity.class)));

        btnCall1.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(android.net.Uri.parse("tel:035235989"));
            startActivity(intent);
        });

        btnCall2.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(android.net.Uri.parse("tel:0509166011"));
            startActivity(intent);
        });
    }
}
