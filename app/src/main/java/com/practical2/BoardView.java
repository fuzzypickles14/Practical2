package com.practical2;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.View;
import android.graphics.Paint;

/**
 * Created by robbylagen on 4/12/17.
 */

public class BoardView extends View {

    public int startingX;
    public int startingY;
    public int endingX;
    public int endingY;
    public int iconSize;
    public int numTileTypes = 12;
    public final MainGame game;

    private Bitmap background = null;
    private final Paint paint = new Paint();
    private final BitmapDrawable[] bitmapCell = new BitmapDrawable[numTileTypes];

    private Drawable boardBackgroud;
    private Drawable lightUpRec;
    private Drawable fadeRec;

    private int tileSize = 0;
    private int boardWidth = 0;
    private float textSize = 0;

    public BoardView(Context context) {
        super(context);

        // Load Resources
        game = new MainGame(context, this);
        try {
            boardBackgroud = getDrawable(R.drawable.board_backgroud);
            lightUpRec = getDrawable(R.drawable.light_up_rec);
            fadeRec  = getDrawable(R.drawable.fade_rec);
            this.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.board_background_color, null));
        } catch (Exception e) {
            Log.e("BoardView", "Error getting assets", e);
        }

        // set on touch listner

    }

    @Override
    public void onDraw(Canvas canvas) {
        // Reset the transparency of the screen
        canvas.drawBitmap(background, 0, 0, paint);

        drawCells(canvas);
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldW, int oldH) {
        super.onSizeChanged(width, height, oldW, oldH);
        getLayout(width, height);
        createBitmapTiles();
        createBitmapBackgroud(width, height);
    }

    private void drawCells(Canvas canvas) {
        paint.setTextSize(textSize);
        paint.setTextAlign(Paint.Align.CENTER);
        // Outputting the individual cells
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                int startX = startingX + boardWidth + (tileSize + boardWidth) * x;
                int endX = startX + tileSize;
                int startY = startingY + boardWidth + (tileSize + boardWidth) * y;
                int endY = startY + tileSize;

                Tile currentTile = game.board.getSpotContent(x, y);
                if (currentTile != null) {
                    // Get and represent the value of the tile
                    int value = currentTile.getValue();
                    int index = log2(value);

                    bitmapCell[index].setBounds(startX, startY, endX, endY);
                    bitmapCell[index].draw(canvas);
                }
            }
        }
    }


    private void getLayout(int width, int height) {
        tileSize = Math.min(width / (game.numTilesX + 1), height / (game.numTilesY + 3));
        boardWidth = tileSize / 7;
        int screenMidX = width / 2;
        int screenMidY = height / 2;
        int boardMidY = screenMidY + tileSize / 2;
        iconSize = tileSize / 2;

        // Board Dimensions
        double halfNumSquaresX = 2;
        double halfNumSquaresY = 2;
        startingX = (int) (screenMidX - (tileSize + boardWidth) * halfNumSquaresX - boardWidth / 2);
        endingX = (int) (screenMidX + (tileSize + boardWidth) * halfNumSquaresX + boardWidth / 2);
        startingY = (int) (boardMidY - (tileSize + boardWidth) * halfNumSquaresY - boardWidth / 2);
        endingY = (int) (boardMidY + (tileSize + boardWidth) * halfNumSquaresY + boardWidth / 2);

        textSize = tileSize * tileSize / Math.max(tileSize, paint.measureText("0000"));
    }

    private void createBitmapTiles() {
        Resources res = getResources();
        int[] tileIds = getTileIds();
        for (int i = 1; i < bitmapCell.length; i++) {
            int val = (int) Math.pow(2, i);
            Bitmap bitmap = Bitmap.createBitmap(tileSize, tileSize, Bitmap.Config.ALPHA_8);
            Canvas canvas = new Canvas(bitmap);
            drawDrawable(canvas, getDrawable(tileIds[i]), 0, 0, tileSize, tileSize);
            drawTileText(canvas, val);
            bitmapCell[i] = new BitmapDrawable(res, bitmap);
        }
    }

    private void createBitmapBackgroud(int width, int height) {
        background = Bitmap.createBitmap(width, height, Bitmap.Config.ALPHA_8);
        Canvas canvas = new Canvas(background);
        drawBackground(canvas);
        drawBackgroundBoard(canvas);
    }

    private Drawable getDrawable(int resId) {
        return ResourcesCompat.getDrawable(getResources(), resId, null);
    }

    private void drawDrawable(Canvas canvas, Drawable draw, int startingX, int startingY, int endingX, int endingY) {
        draw.setAlpha(88);
        draw.setBounds(startingX, startingY, endingX, endingY);
        draw.draw(canvas);
    }

    private void drawTileText(Canvas canvas, int value) {
        int textShiftY = centerText();
        if (value >= 8) {
            paint.setColor(ResourcesCompat.getColor(getResources(), R.color.text_white, null));
        } else {
            paint.setColor(ResourcesCompat.getColor(getResources(), R.color.text_black, null));
        }
        paint.setTextSize(100);
        canvas.drawText("" + value, tileSize / 2, tileSize / 2 - textShiftY, paint);
    }

    private void drawBackground(Canvas canvas) {
        drawDrawable(canvas, boardBackgroud, startingX, startingY, endingX, endingY);
    }

    private void drawBackgroundBoard(Canvas canvas) {
        Drawable backgroundTile = getDrawable(R.drawable.tile);
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                int startX = startingX + boardWidth + (tileSize + boardWidth) * x;
                int endX = startX + tileSize;
                int startY = startingY + boardWidth + (tileSize + boardWidth) * y;
                int endY = startY + tileSize;
                drawDrawable(canvas, backgroundTile, startX, startY, endX, endY);
            }
        }
    }

    private int[] getTileIds() {
        int[] tileIds = new int[numTileTypes];
        tileIds[0] = R.drawable.tile;
        tileIds[1] = R.drawable.tile_2;
        tileIds[2] = R.drawable.tile_4;
        tileIds[3] = R.drawable.tile_8;
        tileIds[4] = R.drawable.tile_16;
        tileIds[5] = R.drawable.tile_32;
        tileIds[6] = R.drawable.tile_64;
        tileIds[7] = R.drawable.tile_128;
        tileIds[8] = R.drawable.tile_256;
        tileIds[9] = R.drawable.tile_512;
        tileIds[10] = R.drawable.tile_1024;
        tileIds[11] = R.drawable.tile_2048;
        return tileIds;
    }

    private int centerText() {
        return (int) ((paint.descent() + paint.ascent()) / 2);
    }

    private static int log2(int n) {
        if (n <= 0) throw new IllegalArgumentException();
        return 31 - Integer.numberOfLeadingZeros(n);
    }


}
