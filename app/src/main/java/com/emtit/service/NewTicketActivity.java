package com.emtit.service;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.ToneGenerator;
import android.media.AudioManager;
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
    private ProgressBar progressBar;
    private Button btnSubmit, btnCamera, btnGallery;

    private Uri cameraImageUri;
    private File photoFile;

    private final ActivityResultLauncher<Intent> cameraLauncher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && cameraImageUri != null) {
                ivScreenshot.setImageURI(cameraImageUri);
                ivScreenshot.setVisibility(View.VISIBLE);
            }
        });

    private final ActivityResultLauncher<Intent> galleryLauncher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Uri selectedImage = result.getData().getData();
                if (selectedImage != null) {
                    cameraImageUri = selectedImage;
                    ivScreenshot.setImageURI(selectedImage);
                    ivScreenshot.setVisibility(View.VISIBLE);
                }
            }
        });

    private final ActivityResultLauncher<String> permissionLauncher =
        registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
            if (granted) {
                openCamera();
            } else {
                Toast.makeText(this, "נדרשת הרשאת מצלמה", Toast.LENGTH_SHORT).show();
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
        progressBar = findViewById(R.id.progressBar);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnCamera = findViewById(R.id.btnCamera);
        btnGallery = findViewById(R.id.btnGallery);

        String[] categories = {"קטגוריה", "תמיכה טכנית", "תקלת רשת", "תקלת חומרה", "תקלת תוכנה", "אחר"};
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item, categories);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(catAdapter);

        String[] priorities = {"עדיפות", "נמוכה", "בינונית", "גבוהה", "דחופה"};
        ArrayAdapter<String> prioAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item, priorities);
        prioAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPriority.setAdapter(prioAdapter);

        btnCamera.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                permissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });

        btnGallery.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            galleryLauncher.launch(intent);
        });

        btnSubmit.setOnClickListener(v -> submitTicket());
    }

    private void openCamera() {
        try {
            photoFile = createImageFile();
            cameraImageUri = FileProvider.getUriForFile(this,
                getPackageName() + ".fileprovider", photoFile);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
            cameraLauncher.launch(intent);
        } catch (IOException e) {
            Toast.makeText(this, "שגיאה בפתיחת מצלמה", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(null);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void submitTicket() {
        String name = etName.getText().toString().trim();
        String title = etTitle.getText().toString().trim();
        String desc = etDesc.getText().toString().trim();

        if (name.isEmpty() || title.isEmpty() || desc.isEmpty()) {
            Toast.makeText(this, "אנא מלא את כל השדות המסומנים *", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmit.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        Ticket ticket = new Ticket();
        ticket.setReporterName(name);
        ticket.setReporterPhone(etPhone.getText().toString().trim());
        ticket.setCompany(etCompany.getText().toString().trim());
        ticket.setTitle(title);
        ticket.setDescription(desc);
        ticket.setCategory(spCategory.getSelectedItem().toString());
        ticket.setPriority(spPriority.getSelectedItem().toString());

        String imageUriStr = cameraImageUri != null ? cameraImageUri.toString() : null;

        new EmailSender().sendEmail(ticket, imageUriStr, new EmailSender.Callback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    playSuccessSound();
                    startActivity(new Intent(NewTicketActivity.this, SuccessActivity.class));
                    finish();
                });
            }
            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnSubmit.setEnabled(true);
                    Toast.makeText(NewTicketActivity.this,
                        "שגיאה בשליחה: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void playSuccessSound() {
        try {
            ToneGenerator toneGen = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
            toneGen.startTone(ToneGenerator.TONE_PROP_BEEP2, 400);
            android.os.Handler handler = new android.os.Handler(getMainLooper());
            handler.postDelayed(() -> {
                toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 300);
            }, 450);
            handler.postDelayed(toneGen::release, 800);
        } catch (Exception e) {
            // ignore if sound fails
        }
    }
}
