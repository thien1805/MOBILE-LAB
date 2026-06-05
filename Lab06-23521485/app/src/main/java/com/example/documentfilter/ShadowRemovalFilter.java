package com.example.documentfilter;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * ShadowRemovalFilter – Thuật toán loại bỏ bóng tối theo đúng yêu cầu Lab 6.
 *
 * Thuật toán (theo PDF trang 7-8):
 *  1. Chuyển ảnh sang HSV  (COLOR_BGR2HSV)
 *  2. Tách kênh V (độ sáng)
 *  3. Dilate kênh V với kernel MORPH_RECT 7x7
 *  4. MedianBlur kích thước 21
 *  5. AbsDiff(V_gốc, V_làm mờ)
 *  6. Bitwise NOT
 *  7. Normalize [0, 255]
 *  8. Ghép H + S + V_mới → COLOR_HSV2BGR
 */
public class ShadowRemovalFilter {

    public interface MyCallBack {
        void onComplete(Bitmap bitmap);
    }

    // ── Phiên bản Bitmap (dùng trong Android) ───────────────────
    public static Bitmap shadowRemoval(Bitmap bitmap) {
        Mat src = new Mat();
        Utils.bitmapToMat(bitmap, src);

        // Bước 1: Chuyển sang HSV
        Mat hsv = new Mat();
        Imgproc.cvtColor(src, hsv, Imgproc.COLOR_BGR2HSV);

        // Bước 2: Tách kênh H, S, V
        List<Mat> channels = new ArrayList<>();
        Core.split(hsv, channels);

        // Bước 3: Lấy kênh V
        Mat v = channels.get(2);

        // Bước 4: Dilate với kernel MORPH_RECT 7x7
        Mat dilate = new Mat();
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(7, 7));
        Imgproc.dilate(v, dilate, kernel);

        // Bước 5: Median Blur kernel 21
        Mat bg = new Mat();
        Imgproc.medianBlur(dilate, bg, 21);

        // Bước 6: AbsDiff
        Mat diff = new Mat();
        Core.absdiff(v, bg, diff);

        // Bước 7: Bitwise NOT
        Mat diff_not = new Mat();
        Core.bitwise_not(diff, diff_not);

        // Bước 8: Normalize về [0, 255]
        Mat normalized = new Mat();
        Core.normalize(diff_not, normalized, 0, 255, Core.NORM_MINMAX, CvType.CV_8UC1);

        // Bước 9: Ghép lại và chuyển về BGR
        channels.set(2, normalized);
        Core.merge(channels, hsv);

        Mat dst = new Mat();
        Imgproc.cvtColor(hsv, dst, Imgproc.COLOR_HSV2BGR);

        // Tạo Bitmap kết quả
        Bitmap result = Bitmap.createBitmap(
                bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(dst, result);

        // Giải phóng bộ nhớ
        src.release(); hsv.release(); v.release();
        dilate.release(); kernel.release(); bg.release();
        diff.release(); diff_not.release(); normalized.release(); dst.release();

        return result;
    }

    // ── Phiên bản async (gọi từ DocumentFilter) ─────────────────
    public static void getShadowFilteredImage(Bitmap bitmap, final MyCallBack callBack) {
        Clone.getCloneImage(bitmap, cloned -> {
            Executor executor = Executors.newSingleThreadExecutor();
            Handler handler  = new Handler(Looper.getMainLooper());

            executor.execute(() -> {
                Bitmap result = shadowRemoval(cloned);
                handler.post(() -> callBack.onComplete(result));
            });
        });
    }

    // ── Phiên bản Mat (dùng khi cần pipeline với Mat khác) ──────
    public static Mat shadowRemovalMat(Mat src) {
        Mat hsv = new Mat();
        Imgproc.cvtColor(src, hsv, Imgproc.COLOR_BGR2HSV);

        List<Mat> channels = new ArrayList<>();
        Core.split(hsv, channels);

        Mat vChannel = channels.get(2);

        Mat dilated = new Mat();
        Mat kernel  = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(7, 7));
        Imgproc.dilate(vChannel, dilated, kernel);

        Mat blurred = new Mat();
        Imgproc.medianBlur(dilated, blurred, 21);

        Mat diff = new Mat();
        Core.absdiff(vChannel, blurred, diff);

        Mat not = new Mat();
        Core.bitwise_not(diff, not);

        Mat normalized = new Mat();
        Core.normalize(not, normalized, 0, 255, Core.NORM_MINMAX, CvType.CV_8UC1);

        channels.set(2, normalized);
        Core.merge(channels, hsv);

        Mat result = new Mat();
        Imgproc.cvtColor(hsv, result, Imgproc.COLOR_HSV2BGR);
        return result;
    }
}
