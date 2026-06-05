package com.emtit.service;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import com.emtit.service.model.Ticket;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NewTicketActivity extends AppCompatActivity {
    private EditText etName, etPhone, etCompany, etTitle, etDesc;
    private Spinner spCategory, spPriority;
    private ImageView ivScreenshot;
    private Button btnCamera, btnGallery, btnSubmit;
    private ProgressBar progressBar;
    private Bitmap selectedBitmap;
    private Uri photoUri;

    private final ActivityResultLauncher<Intent> cameraLauncher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                try {
                    selectedBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), photoUri);
                    ivScreenshot.setImageBitmap(selectedBitmap);
                    ivScreenshot.setVisibility(View.VISIBLE);
                } catch (Exception e) { e.printStackTrace(); }
            }
        });

    private final ActivityResultLauncher<Intent> galleryLauncher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                try {
                    Uri uri = result.getData().getData();
                    selectedBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    ivScreenshot.setImageBitmap(selectedBitmap);
                    ivScreenshot.setVisibility(View.VISIBLE);
                } catch (Exception e) { e.printStackTrace(); }
            }
        });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_ticket);

        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etCompany = findViewById(R.id.etCompany);
        etTitle = findViewById(R.id.etTitle);
        etDesc = findViewById(R.id.etDesc);
        spCategory = findViewById(R.id.spCategory);
        spPriority = findViewById(R.id.spPriority);
        ivScreenshot = findViewById(R.id.ivScreenshot);
        btnCamera = findViewById(R.id.btnCamera);
        btnGallery = findViewById(R.id.btnGallery);
        btnSubmit = findViewById(R.id.btnSubmit);
        progressBar = findViewById(R.id.progressBar);

        String[] categories = {"תמיכה טכנית", "תקלת חומרה", "תקלת תוכנה", "רשת ואינטרנט", "אחר"};
        spCategory.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories));

        String[] priorities = {"גבוהה", "בינונית", "נמוכה"};
        spPriority.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, priorities));

        btnCamera.setOnClickListener(v -> openCamera());
        btnGallery.setOnClickListener(v -> openGallery());
        btnSubmit.setOnClickListener(v -> submitTicket());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("קריאת שירות חדשה");
        }
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
            return;
        }
        try {
            File photoFile = createImageFile();
            photoUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", photoFile);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            cameraLauncher.launch(intent);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private File createImageFile() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File storageDir = getExternalFilesDir(null);
        return File.createTempFile("IMG_" + timestamp, ".jpg", storageDir);
    }

    private void submitTicket() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String title = etTitle.getText().toString().trim();
        String desc = etDesc.getText().toString().trim();

        if (name.isEmpty() || title.isEmpty() || desc.isEmpty()) {
            Toast.makeText(this, "אנא מלא את כל השדות החובה", Toast.LENGTH_SHORT).show();
            return;
        }

        Ticket ticket = new Ticket();
        ticket.setReporterName(name);
        ticket.setReporterPhone(phone);
        ticket.setCompany(etCompany.getText().toString().trim());
        ticket.setTitle(title);
        ticket.setDescription(desc);
        ticket.setCategory(spCategory.getSelectedItem().toString());
        ticket.setPriority(spPriority.getSelectedItemPosition());

        btnSubmit.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        EmailSender.send(ticket, selectedBitmap, error -> {
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                btnSubmit.setEnabled(true);
                if (error == null) {
                    Intent intent = new Intent(NewTicketActivity.this, SuccessActivity.class);
                    intent.putExtra("ticketId", ticket.getId());
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "שגיאה בשליחה: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    @Override
    public boolean onSupportNavigateUp() { finish(); return true; }
}
