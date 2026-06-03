package com.example.flappybird;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;

public class Bird {
    private Bitmap birdBitmap;
    private int x, y;
    private int velocity = 0;
    private int gravity = 3;
    private int jumpVelocity = -30;
    private float rotation = 0;

    public Bird(Bitmap rawBitmap, int x, int y, int size) {
        // Requirement 3a: Character Rounding (Masking) into a perfect circle
        // First scale the raw f1.jpg to the appropriate size
        Bitmap scaled = Bitmap.createScaledBitmap(rawBitmap, size, size, true);
        this.birdBitmap = getCircularBitmap(scaled);
        this.x = x;
        this.y = y;
    }

    /**
     * Requirement 3a: Programmatic Circular Masking using BitmapShader
     */
    private Bitmap getCircularBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        
        // Use BitmapShader to create the circular effect
        BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        paint.setShader(shader);
        paint.setAntiAlias(true);
        
        float radius = bitmap.getWidth() / 2f;
        canvas.drawCircle(radius, radius, radius, paint);
        return output;
    }

    public void update() {
        velocity += gravity;
        y += velocity;

        // Requirement 3a: Rotation effect based on velocity (jump or fall)
        rotation = velocity * 2;
        if (rotation > 90) rotation = 90;
        if (rotation < -30) rotation = -30;
    }

    public void jump() {
        velocity = jumpVelocity;
    }

    public void draw(Canvas canvas) {
        Matrix matrix = new Matrix();
        // Translate to position (x,y) as center
        matrix.postTranslate(x - birdBitmap.getWidth() / 2f, y - birdBitmap.getHeight() / 2f);
        // Rotate around the bird center
        matrix.postRotate(rotation, x, y);
        canvas.drawBitmap(birdBitmap, matrix, null);
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return birdBitmap.getWidth(); }
    public int getHeight() { return birdBitmap.getHeight(); }
    public void setY(int y) { this.y = y; }
    public void setVelocity(int velocity) { this.velocity = velocity; }
}
