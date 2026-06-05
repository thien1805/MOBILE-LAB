package com.example.documentfilter;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * DocumentFilter – Facade class tập hợp các bộ lọc ảnh.
 * Theo Lab 6: g1=Shadow Removal, g2=Grayscale, g3=Threshold, g4=Edge, g5=Original
 */
public class DocumentFilter {

    public interface MyCallBack {
        void onComplete(Bitmap bitmap);
    }

    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler  handler  = new Handler(Looper.getMainLooper());

    // ── g1: Shadow Removal (bài tập chính của Lab 6) ─────────────
    public void getShadowRemoval(Bitmap bitmap, MyCallBack callBack) {
        ShadowRemovalFilter.getShadowFilteredImage(bitmap, callBack::onComplete);
    }

    // ── g2: Grayscale ─────────────────────────────────────────────
    public void getGrayScale(Bitmap bitmap, MyCallBack callBack) {
        Clone.getCloneImage(bitmap, cloned -> executor.execute(() -> {
            Mat src  = new Mat();
            Utils.bitmapToMat(cloned, src);

            Mat gray = new Mat();
            Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);

            Mat out = new Mat();
            Imgproc.cvtColor(gray, out, Imgproc.COLOR_GRAY2BGR);

            Bitmap result = Bitmap.createBitmap(
                    out.cols(), out.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(out, result);

            src.release(); gray.release(); out.release();
            handler.post(() -> callBack.onComplete(result));
        }));
    }

    // ── g3: Adaptive Threshold (làm nổi bật văn bản tài liệu) ────
    public void getAdaptiveThreshold(Bitmap bitmap, MyCallBack callBack) {
        Clone.getCloneImage(bitmap, cloned -> executor.execute(() -> {
            Mat src  = new Mat();
            Utils.bitmapToMat(cloned, src);

            Mat gray = new Mat();
            Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
            Imgproc.GaussianBlur(gray, gray, new Size(5, 5), 0);

            Mat thresh = new Mat();
            Imgproc.adaptiveThreshold(
                    gray, thresh, 255,
                    Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                    Imgproc.THRESH_BINARY,
                    11, 2
            );

            Mat out = new Mat();
            Imgproc.cvtColor(thresh, out, Imgproc.COLOR_GRAY2BGR);

            Bitmap result = Bitmap.createBitmap(
                    out.cols(), out.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(out, result);

            src.release(); gray.release(); thresh.release(); out.release();
            handler.post(() -> callBack.onComplete(result));
        }));
    }

    // ── g4: Canny Edge Detection ──────────────────────────────────
    public void getCannyEdge(Bitmap bitmap, MyCallBack callBack) {
        Clone.getCloneImage(bitmap, cloned -> executor.execute(() -> {
            Mat src  = new Mat();
            Utils.bitmapToMat(cloned, src);

            Mat gray = new Mat();
            Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
            Imgproc.GaussianBlur(gray, gray, new Size(5, 5), 1.5);

            Mat edges = new Mat();
            Imgproc.Canny(gray, edges, 50, 150);

            Mat out = new Mat();
            Imgproc.cvtColor(edges, out, Imgproc.COLOR_GRAY2BGR);

            Bitmap result = Bitmap.createBitmap(
                    out.cols(), out.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(out, result);

            src.release(); gray.release(); edges.release(); out.release();
            handler.post(() -> callBack.onComplete(result));
        }));
    }
}
