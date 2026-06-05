package com.example.documentfilter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.opencv.android.OpenCVLoader;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    // ── Hằng số request code cho gallery ───────────────────────
    private static final int IMAGE_REQUEST = 1;

    // ── Các thành phần UI ───────────────────────────────────────
    DocumentFilter documentFilter;
    Button load, g1, g2, g3, g4, g5, btnFaceDetection;
    ImageView imageView;

    // g0 lưu ảnh gốc được chọn từ gallery
    Bitmap g0;

    // ── Khởi tạo Activity ───────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Khởi tạo OpenCV
        if (!OpenCVLoader.initDebug()) {
            android.widget.Toast.makeText(this,
                    "OpenCV load thất bại!", android.widget.Toast.LENGTH_LONG).show();
        }

        setContentView(R.layout.activity_main);

        // Ánh xạ các view
        load             = findViewById(R.id.btn_load);
        g1               = findViewById(R.id.btn_g1);
        g2               = findViewById(R.id.btn_g2);
        g3               = findViewById(R.id.btn_g3);
        g4               = findViewById(R.id.btn_g4);
        g5               = findViewById(R.id.btn_g5);
        imageView        = findViewById(R.id.img_view);
        btnFaceDetection = findViewById(R.id.btn_face_detection);

        documentFilter = new DocumentFilter();

        // ── Nút Exercise 1: mở Face Detection ─────────────────
        btnFaceDetection.setOnClickListener(v ->
                startActivity(new Intent(this, FaceDetectionActivity.class))
        );

        // ── Nút Load: mở thư viện ảnh ─────────────────────────
        load.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(
                    Intent.createChooser(intent, "Select Picture"),
                    IMAGE_REQUEST
            );
        });

        // ── Nút g1: Shadow Removal ─────────────────────────────
        g1.setOnClickListener(v -> {
            if (g0 == null) return;
            documentFilter.getShadowRemoval(g0, bitmap -> imageView.setImageBitmap(bitmap));
        });

        // ── Nút g2: Grayscale ──────────────────────────────────
        g2.setOnClickListener(v -> {
            if (g0 == null) return;
            documentFilter.getGrayScale(g0, bitmap -> imageView.setImageBitmap(bitmap));
        });

        // ── Nút g3: Adaptive Threshold ─────────────────────────
        g3.setOnClickListener(v -> {
            if (g0 == null) return;
            documentFilter.getAdaptiveThreshold(g0, bitmap -> imageView.setImageBitmap(bitmap));
        });

        // ── Nút g4: Canny Edge ─────────────────────────────────
        g4.setOnClickListener(v -> {
            if (g0 == null) return;
            documentFilter.getCannyEdge(g0, bitmap -> imageView.setImageBitmap(bitmap));
        });

        // ── Nút g5: Hiện lại ảnh gốc ──────────────────────────
        g5.setOnClickListener(v -> {
            if (g0 == null) return;
            imageView.setImageBitmap(g0);
        });
    }

    // ── Xử lý kết quả trả về từ gallery ────────────────────────
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            try {
                g0 = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            imageView.setImageBitmap(g0);
        }
    }
}
