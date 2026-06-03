package com.example.flappybird;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {
    private MainThread thread;
    private Bird bird;
    private Bitmap background;
    private Bitmap title;
    private Bitmap playButton;
    private Bitmap pipeBitmap;
    private List<Pipe> pipes;
    private int score = 0;
    private boolean isPlaying = false;
    private boolean isGameOver = false;

    public GameView(Context context) {
        super(context);
        getHolder().addCallback(this);
        thread = new MainThread(getHolder(), this);
        setFocusable(true);
    }

    private void init() {
        // Load and scale assets relative to screen size from res/drawable
        try {
            // background.png
            Bitmap rawBackground = BitmapFactory.decodeResource(getResources(), R.drawable.background);
            background = Bitmap.createScaledBitmap(rawBackground, getWidth(), getHeight(), true);

            // title.png
            Bitmap rawTitle = BitmapFactory.decodeResource(getResources(), R.drawable.title);
            int titleWidth = (int) (getWidth() * 0.7);
            int titleHeight = (int) (rawTitle.getHeight() * ((float) titleWidth / rawTitle.getWidth()));
            title = Bitmap.createScaledBitmap(rawTitle, titleWidth, titleHeight, true);

            // play.png
            Bitmap rawPlay = BitmapFactory.decodeResource(getResources(), R.drawable.play);
            int playWidth = (int) (getWidth() * 0.3);
            int playHeight = (int) (rawPlay.getHeight() * ((float) playWidth / rawPlay.getWidth()));
            playButton = Bitmap.createScaledBitmap(rawPlay, playWidth, playHeight, true);

            // Requirement 3a: f1.jpg (The Bird) - Passing to constructor for circular masking
            Bitmap rawBird = BitmapFactory.decodeResource(getResources(), R.drawable.f1);
            int birdSize = (int) (getWidth() * 0.15);
            bird = new Bird(rawBird, getWidth() / 4, getHeight() / 2, birdSize);

            // Requirement 3b: pipe.png - Programmatic Background Removal
            Bitmap rawPipe = BitmapFactory.decodeResource(getResources(), R.drawable.pipe);
            Bitmap transparentPipe = removeBackgroundColor(rawPipe, Color.WHITE);
            int pipeWidth = getWidth() / 5;
            int pipeHeight = (int) (transparentPipe.getHeight() * ((float) pipeWidth / transparentPipe.getWidth()));
            pipeBitmap = Bitmap.createScaledBitmap(transparentPipe, pipeWidth, pipeHeight, true);

        } catch (Exception e) {
            // Fallback for missing assets
            background = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            bird = new Bird(Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888), getWidth() / 4, getHeight() / 2, 100);
            pipeBitmap = Bitmap.createBitmap(200, 800, Bitmap.Config.ARGB_8888);
        }
        pipes = new ArrayList<>();
    }

    // Requirement 3b: Programmatic Background Removal logic
    private Bitmap removeBackgroundColor(Bitmap src, int colorToRemove) {
        Bitmap result = src.copy(Bitmap.Config.ARGB_8888, true);
        int width = result.getWidth();
        int height = result.getHeight();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pixel = result.getPixel(x, y);
                // Threshold check to catch "near-white" or near-target colors
                if (Math.abs(Color.red(pixel) - Color.red(colorToRemove)) < 30 &&
                    Math.abs(Color.green(pixel) - Color.green(colorToRemove)) < 30 &&
                    Math.abs(Color.blue(pixel) - Color.blue(colorToRemove)) < 30) {
                    result.setPixel(x, y, Color.TRANSPARENT);
                }
            }
        }
        return result;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        init();
        thread.setRunning(true);
        if (thread.getState() == Thread.State.NEW) {
            thread.start();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        while (retry) {
            try {
                thread.setRunning(false);
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            retry = false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (!isPlaying && !isGameOver) {
                isPlaying = true;
                resetGame();
            } else if (isGameOver) {
                isGameOver = false;
                isPlaying = false;
                resetGame();
            } else {
                bird.jump();
            }
            performClick();
        }
        return true;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    private void resetGame() {
        score = 0;
        pipes.clear();
        bird.setY(getHeight() / 2);
        bird.setVelocity(0);
        spawnPipe();
    }

    private void spawnPipe() {
        pipes.add(new Pipe(pipeBitmap, getWidth(), getHeight()));
    }

    public void update() {
        if (isPlaying && !isGameOver) {
            bird.update();

            // Adjusted pipe spawning frequency to increase horizontal distance (from 0.6 to 0.8)
            if (pipes.isEmpty() || pipes.get(pipes.size() - 1).getX() < getWidth() - (getWidth() * 0.8)) {
                spawnPipe();
            }

            for (int i = 0; i < pipes.size(); i++) {
                Pipe p = pipes.get(i);
                p.update();

                if (p.getCollision(bird.getX(), bird.getY(), bird.getWidth(), bird.getHeight())) {
                    isGameOver = true;
                }

                if (!p.isScored() && bird.getX() > p.getX() + p.getWidth()) {
                    score++;
                    p.setScored(true);
                }

                if (p.getX() + p.getWidth() < 0) {
                    pipes.remove(i);
                    i--;
                }
            }

            if (bird.getY() < 0 || bird.getY() > getHeight()) {
                isGameOver = true;
            }
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (canvas != null) {
            // Draw custom background asset
            canvas.drawBitmap(background, 0, 0, null);

            // Draw pipes using pipe.png asset (Requirement 3b: Logic is inside Pipe.draw)
            for (Pipe p : pipes) {
                p.draw(canvas);
            }

            // Draw bird using f1.jpg asset with rotation (Requirement 3a: Circle masking is inside Bird)
            bird.draw(canvas);

            Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setFakeBoldText(true);
            paint.setShadowLayer(5, 0, 0, Color.BLACK);
            
            if (!isPlaying && !isGameOver) {
                // Centered Start Screen with custom title and play assets
                canvas.drawBitmap(title, getWidth()/2 - title.getWidth()/2, getHeight()/4, null);
                canvas.drawBitmap(playButton, getWidth()/2 - playButton.getWidth()/2, getHeight()/2, null);
                
                paint.setTextSize(getWidth() / 12);
                paint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText("FLAPPY LE TU NHAN", getWidth()/2, getHeight()/4 - 50, paint);
            } else if (isGameOver) {
                // Centered Game Over Screen
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setTextSize(getWidth() / 8);
                canvas.drawText("GAME OVER", getWidth()/2, getHeight()/2 - 100, paint);
                paint.setTextSize(getWidth() / 12);
                canvas.drawText("Score: " + score, getWidth()/2, getHeight()/2, paint);
                paint.setTextSize(getWidth() / 15);
                canvas.drawText("TAP TO RESTART", getWidth()/2, getHeight()/2 + 150, paint);
            } else {
                // Gameplay Score at the top center
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setTextSize(getWidth() / 6);
                canvas.drawText(String.valueOf(score), getWidth()/2, 250, paint);
            }
        }
    }
}
