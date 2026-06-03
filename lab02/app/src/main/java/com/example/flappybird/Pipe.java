package com.example.flappybird;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import java.util.Random;

public class Pipe {
    private Bitmap pipeBitmap;
    private int x, y; // y is the top of the gap
    private int width;
    private int screenHeight;
    private int gap = 450;
    private int speed = 12;
    private boolean isScored = false;

    // Requirement 3b: Pre-processed scaled bitmaps for the top and bottom sections
    private Bitmap topPipeScaled;
    private Bitmap bottomPipeScaled;

    public Pipe(Bitmap pipeBitmap, int x, int screenHeight) {
        this.pipeBitmap = pipeBitmap;
        this.x = x;
        this.width = pipeBitmap.getWidth();
        this.screenHeight = screenHeight;
        
        Random random = new Random();
        // y is the vertical position where the gap starts
        this.y = random.nextInt(screenHeight - gap - 600) + 300;

        preparePipes();
    }

    /**
     * Requirement 3b: Scaling and Flipping logic (Stretching to edges, No Tiling)
     */
    private void preparePipes() {
        // 1. Top Pipe: Flip vertically and Stretch to fit from top (0) down to gap start (y)
        Matrix matrix = new Matrix();
        matrix.postScale(1, -1); // Flip vertically
        Bitmap flippedPipe = Bitmap.createBitmap(pipeBitmap, 0, 0, pipeBitmap.getWidth(), pipeBitmap.getHeight(), matrix, true);
        
        // Stretch flipping pipe to fill the entire height 'y' from the absolute top edge
        if (y > 0) {
            topPipeScaled = Bitmap.createScaledBitmap(flippedPipe, width, y, true);
        }

        // 2. Bottom Pipe: Stretch to fit from gap end (y + gap) down to the absolute bottom edge
        int bottomPipeHeight = screenHeight - (y + gap);
        if (bottomPipeHeight > 0) {
            bottomPipeScaled = Bitmap.createScaledBitmap(pipeBitmap, width, bottomPipeHeight, true);
        }
    }

    public void update() {
        x -= speed;
    }

    public void draw(Canvas canvas) {
        // Requirement 3b: Rendering the single, vertically stretched pipe assets
        if (topPipeScaled != null) {
            canvas.drawBitmap(topPipeScaled, x, 0, null);
        }
        if (bottomPipeScaled != null) {
            canvas.drawBitmap(bottomPipeScaled, x, y + gap, null);
        }
    }

    public boolean getCollision(int birdX, int birdY, int birdWidth, int birdHeight) {
        // Hitbox logic using absolute screen coordinates
        Rect birdRect = new Rect(birdX - birdWidth/2 + 15, birdY - birdHeight/2 + 15, 
                                 birdX + birdWidth/2 - 15, birdY + birdHeight/2 - 15);

        Rect topRect = new Rect(x, 0, x + width, y);
        Rect bottomRect = new Rect(x, y + gap, x + width, screenHeight);
        
        return Rect.intersects(birdRect, topRect) || Rect.intersects(birdRect, bottomRect);
    }

    public int getX() { return x; }
    public int getWidth() { return width; }
    public boolean isScored() { return isScored; }
    public void setScored(boolean scored) { isScored = scored; }
}
