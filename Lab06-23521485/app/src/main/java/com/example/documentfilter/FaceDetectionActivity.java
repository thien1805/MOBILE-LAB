package com.example.documentfilter;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.io.IOException;
import java.util.List;

/**
 * FaceDetectionActivity – Exercise 1 của Lab 6.
 * Nhận diện khuôn mặt từ ảnh gallery bằng ML Kit Face Detection.
 * Vẽ hình chữ nhật xanh xung quanh mỗi khuôn mặt được phát hiện.
 */
public class FaceDetectionActivity extends AppCompatActivity {

    private static final int IMAGE_REQUEST = 1;
    private static final int PERMISSION_REQUEST = 200;

    private ImageView imgView;
    private ProgressBar progressBar;
    private TextView tvResult;
    private Button btnLoad, btnDetect, btnBack;

    private Bitmap selectedBitmap = null;
    private FaceDetector faceDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_detection);

        // ── Cấu hình ML Kit Face Detector ───────────────────────
        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .setMinFaceSize(0.10f)  // phát hiện khuôn mặt nhỏ tới 10% ảnh
                .enableTracking()
                .build();

        faceDetector = FaceDetection.getClient(options);

        // ── Ánh xạ view ─────────────────────────────────────────
        imgView     = findViewById(R.id.fd_img_view);
        progressBar = findViewById(R.id.fd_progress);
        tvResult    = findViewById(R.id.fd_tv_result);
        btnLoad     = findViewById(R.id.fd_btn_load);
        btnDetect   = findViewById(R.id.fd_btn_detect);
        btnBack     = findViewById(R.id.fd_btn_back);

        // ── Nút Load ─────────────────────────────────────────────
        btnLoad.setOnClickListener(v -> checkPermissionAndPick());

        // ── Nút Detect ───────────────────────────────────────────
        btnDetect.setOnClickListener(v -> {
            if (selectedBitmap == null) {
                Toast.makeText(this, "Vui lòng tải ảnh khuôn mặt trước!", Toast.LENGTH_SHORT).show();
                return;
            }
            runFaceDetection(selectedBitmap);
        });

        // ── Nút Back ─────────────────────────────────────────────
        btnBack.setOnClickListener(v -> finish());
    }

    // ── Chạy nhận diện khuôn mặt ────────────────────────────────
    private void runFaceDetection(Bitmap bitmap) {
        progressBar.setVisibility(View.VISIBLE);
        tvResult.setText("Đang nhận diện...");
        btnDetect.setEnabled(false);

        InputImage image = InputImage.fromBitmap(bitmap, 0);

        faceDetector.process(image)
                .addOnSuccessListener(faces -> {
                    progressBar.setVisibility(View.GONE);
                    btnDetect.setEnabled(true);

                    if (faces.isEmpty()) {
                        tvResult.setText("❌ Không tìm thấy khuôn mặt nào!");
                        Toast.makeText(this,
                                "Không phát hiện khuôn mặt. Thử ảnh rõ hơn.",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        // Vẽ hộp bao quanh khuôn mặt
                        Bitmap resultBitmap = drawFaceBoxes(bitmap, faces);
                        imgView.setImageBitmap(resultBitmap);
                        String msg = "✅ Phát hiện " + faces.size() + " khuôn mặt!";
                        tvResult.setText(msg);
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnDetect.setEnabled(true);
                    tvResult.setText("❌ Lỗi: " + e.getMessage());
                    Toast.makeText(this, "Lỗi nhận diện: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    // ── Vẽ hộp bao quanh khuôn mặt ──────────────────────────────
    private Bitmap drawFaceBoxes(Bitmap original, List<Face> faces) {
        // Tạo bản sao bitmap để vẽ lên
        Bitmap mutableBitmap = original.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);

        // Cấu hình Paint cho hộp nhận diện
        Paint boxPaint = new Paint();
        boxPaint.setColor(Color.parseColor("#00FF88")); // Xanh lá neon
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(6f);
        boxPaint.setAntiAlias(true);

        // Cấu hình Paint cho text label
        Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(36f);
        textPaint.setAntiAlias(true);
        textPaint.setStyle(Paint.Style.FILL);

        // Cấu hình Paint nền text
        Paint bgPaint = new Paint();
        bgPaint.setColor(Color.parseColor("#CC00FF88"));
        bgPaint.setStyle(Paint.Style.FILL);

        for (int i = 0; i < faces.size(); i++) {
            Face face = faces.get(i);
            RectF rect = new RectF(face.getBoundingBox());

            // Vẽ hộp
            canvas.drawRoundRect(rect, 12, 12, boxPaint);

            // Label text: "Face 1", "Face 2"...
            String label = "Face " + (i + 1);

            // Nền label
            float textWidth = textPaint.measureText(label);
            RectF labelBg = new RectF(
                    rect.left,
                    rect.top - 46,
                    rect.left + textWidth + 16,
                    rect.top
            );
            canvas.drawRoundRect(labelBg, 6, 6, bgPaint);

            // Vẽ text label
            canvas.drawText(label, rect.left + 8, rect.top - 12, textPaint);

            // Thêm thông tin smilingProbability nếu có
            if (face.getSmilingProbability() != null) {
                float smileProb = face.getSmilingProbability();
                String smile = String.format("%.0f%%😊", smileProb * 100);
                canvas.drawText(smile, rect.left + 8, rect.top + 42, textPaint);
            }
        }

        return mutableBitmap;
    }

    // ── Kiểm tra quyền và mở gallery ────────────────────────────
    private void checkPermissionAndPick() {
        String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(this, permission)
                == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            ActivityCompat.requestPermissions(
                    this, new String[]{permission}, PERMISSION_REQUEST);
        }
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Chọn ảnh khuôn mặt"), IMAGE_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            Toast.makeText(this, "Cần quyền truy cập ảnh!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri == null) return;
            try {
                selectedBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                imgView.setImageBitmap(selectedBitmap);
                tvResult.setText("Ảnh đã tải – nhấn \"Nhận Diện\" để bắt đầu");
            } catch (IOException e) {
                Toast.makeText(this, "Lỗi đọc ảnh: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (faceDetector != null) faceDetector.close();
    }
}
