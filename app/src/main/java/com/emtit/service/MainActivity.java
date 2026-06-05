package com.emtit.service;

import android.content.Intent;
import android.os.Bundle;
import android.net.Uri;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnNewTicket = findViewById(R.id.btnNewTicket);
        Button btnCall1 = findViewById(R.id.btnCall1);

        btnNewTicket.setOnClickListener(v ->
            startActivity(new Intent(MainActivity.this, NewTicketActivity.class)));

        btnCall1.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:0509166011"));
            startActivity(intent);
        });
    }
}
