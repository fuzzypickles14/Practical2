package com.practical2;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by robbylagen on 4/12/17.
 */

public class MainGame {

    public Board board = null;
    public final int numTilesX = 4;
    public final int numTilesY = 4;
    public float highscore = 0;
    public float score = 0;
    public float lastScore = 0;
    public float bufScore = 0;
    public boolean canUndo;

    private final Context mainContext;
    private final BoardView mainBoardView;
    private static final String HIGH_SCORE = "pref_highScore";
    private static final int GAME_WIN = 1;
    private static final int GAME_LOSE = -1;
    private static final int GAME_CONTINUES = 0;
    private int gameState = GAME_CONTINUES;


    public MainGame(Context context, BoardView boardView) {
        mainContext = context;
        mainBoardView = boardView;
    }

    public void newGame() {
        if (board == null) {
            board = new Board(numTilesX, numTilesY);
        } else {
            // prepareUndoState()
            // saveUndoState()
            board.clearBoard();
        }
        highscore = getHighScore();
        if (score >= highscore) {
            highscore = score;
            saveHighScore();
        }
        score = 0;
        addStartTiles();
        mainBoardView.invalidate();
    }

    private void prepareTiles() {
        for (Tile[] array : board.board) {
            for (Tile tile : array) {
                if (board.isBoardSpotOccupied(tile)) {
                    tile.setMergedWith(null);
                }
            }
        }
    }

    private void moveTile(Tile tile, BoardSpot spot) {
        board.board[tile.getX()][tile.getY()] = null;
        board.board[spot.getX()][spot.getY()] = tile;
        tile.updatePosition(spot);
    }

    private void saveUndoState() {
        board.saveTiles();
        canUndo = true;
        lastScore = bufScore;
    }

    private void prepareUndoState() {
        board.prepareSaveTiles();
        bufScore = score;
    }

    public void revertUndoState() {
        if (canUndo) {
            canUndo = false;
            board.revertTiles();
            score = lastScore;
            mainBoardView.invalidate();
        }
    }

    public void move(int direction) {
        // 0: up, 1: right, 2: down, 3: left
        if (!gameIsActive()) {
            return;
        }
        prepareUndoState();
        BoardSpot vector = getVector(direction);
        List<Integer> traversalsX = buildTraversalsX(vector);
        List<Integer> traversalsY = buildTraversalsY(vector);
        boolean moved = false;

        prepareTiles();

        for (int x : traversalsX) {
            for (int y : traversalsY) {
                BoardSpot spot = new BoardSpot(x, y);
                Tile tile = board.getSpotContent(spot);

                if (tile != null) {
                    BoardSpot[] positions = findFarthestPosition(spot, vector);
                    Tile nextTile = board.getSpotContent(positions[1]);

                    if (nextTile != null && nextTile.getValue() == tile.getValue() && nextTile.getTilesMergedWith() == null) {
                        Tile merged = new Tile(positions[1], tile.getValue() * 2);
                        Tile[] temp = {tile, nextTile};
                        merged.setMergedWith(temp);

                        board.insertTile(merged);
                        board.removeTile(tile);

                        // Converge the two tiles' positions
                        tile.updatePosition(positions[1]);

                        // Update the score
                        score = score + merged.getValue();
                        highscore = Math.max(score, highscore);

                        // The mighty 2048 tile
                        if (merged.getValue() == 2048) {
                            gameState = GAME_WIN; // Set win state
                            endGame();
                        }
                    } else {
                        moveTile(tile, positions[0]);
                        int[] extras = {x, x, 0};
                    }

                    if (!positionsEqual(spot, tile)) {
                        moved = true;
                    }
                }
            }
        }
        if (moved) {
            saveUndoState();
            addRandomTile();
            checkIfLost();
        }
        mainBoardView.invalidate();
    }

    private BoardSpot getVector(int direction) {
        BoardSpot[] map = {
                new BoardSpot(0, -1), // up
                new BoardSpot(1, 0),  // right
                new BoardSpot(0, 1),  // down
                new BoardSpot(-1, 0)  // left
        };
        return map[direction];
    }

    private ArrayList<Integer> buildTraversalsX(BoardSpot vector) {
        ArrayList<Integer> traversals = new ArrayList<>();

        for (int xx = 0; xx < numTilesX; xx++) {
            traversals.add(xx);
        }
        if (vector.getX() == 1) {
            Collections.reverse(traversals);
        }

        return traversals;
    }

    private ArrayList<Integer> buildTraversalsY(BoardSpot vector) {
        ArrayList<Integer> traversals = new ArrayList<>();

        for (int xx = 0; xx < numTilesY; xx++) {
            traversals.add(xx);
        }
        if (vector.getY() == 1) {
            Collections.reverse(traversals);
        }

        return traversals;
    }

    private BoardSpot[] findFarthestPosition(BoardSpot spot, BoardSpot vector) {
        BoardSpot previous;
        BoardSpot nextSpot = new BoardSpot(spot.getX(), spot.getY());
        do {
            previous = nextSpot;
            nextSpot = new BoardSpot(previous.getX() + vector.getX(),
                    previous.getY() + vector.getY());
        } while (board.isSpotWithinBounds(nextSpot) && board.isBoardSpotAvailable(nextSpot));

        return new BoardSpot[]{previous, nextSpot};
    }

    private void addStartTiles() {
        int startTiles = 2;
        for (int i = 0; i < startTiles; i++) {
            this.addRandomTile();
        }
    }

    private void addRandomTile() {
        if (board.areBoardSpotsAvailable()) {
            int value = Math.random() < 0.75 ? 2 : 4;
            Tile tile = new Tile(board.getRandomAvailableBoardSpot(), value);
            spawnTile(tile);
        }
    }

    private void spawnTile(Tile tile) {
        board.insertTile(tile);
        // add tile animation here later
    }

    private void saveHighScore() {
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(mainContext);
        SharedPreferences.Editor editor = p.edit();
        editor.putFloat(HIGH_SCORE, highscore);
        editor.commit();
    }

    private float getHighScore() {
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(mainContext);
        return p.getFloat(HIGH_SCORE, -1);
    }

    public boolean gameWon() {
        return (gameState == GAME_WIN);
    }

    public boolean gameLost() {
        return (gameState == GAME_LOSE);
    }

    public boolean gameIsActive() {
        return !(gameWon() || gameLost());
    }

    private boolean positionsEqual(BoardSpot first, BoardSpot second) {
        return first.getX() == second.getX() && first.getY() == second.getY();
    }

    private void checkIfLost() {
        if (!movesAvailable() && !gameWon()) {
            gameState = GAME_LOSE;
            endGame();
        }
    }

    private boolean movesAvailable() {
        return board.areBoardSpotsAvailable() || checkPossibleMoves();
    }

    private boolean checkPossibleMoves() {
        Tile tile;

        for (int x = 0; x < numTilesX; x++) {
            for (int y = 0; y < numTilesY; y++) {
                tile = board.getSpotContent(new BoardSpot(x, y));

                if (tile != null) {
                    for (int direction = 0; direction < 4; direction++) {
                        BoardSpot vector = getVector(direction);
                        BoardSpot spot = new BoardSpot(x + vector.getX(), y + vector.getY());

                        Tile other = board.getSpotContent(spot);

                        if (other != null && other.getValue() == tile.getValue()) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    private void endGame() {
        if (score >= highscore) {
            highscore = score;
            saveHighScore();
        }
    }

    public void setNewGameState() {
        gameState = GAME_CONTINUES;
    }
}


