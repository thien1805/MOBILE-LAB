package com.example.documentfilter;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Helper: Clone a Bitmap asynchronously to avoid blocking the UI thread.
 */
public class Clone {

    public interface CallBack<T> {
        void onComplete(T result);
    }

    /**
     * Tạo một bản sao (mutable) của Bitmap trên background thread,
     * rồi trả về trên main thread thông qua callback.
     */
    public static void getCloneImage(Bitmap original, CallBack<Bitmap> callBack) {
        Executor executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            Bitmap copy = original.copy(original.getConfig(), true);
            handler.post(() -> callBack.onComplete(copy));
        });
    }
}
