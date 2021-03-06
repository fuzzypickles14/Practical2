package com.practical2;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.R.attr.tunerCount;
import static android.R.attr.x;

/**
 * Created by robbylagen on 4/12/17.
 */

public class MainGame {

    public interface MainGameInterface {
        public void onNewGameStarted();
    }

    public Board board = null;
    public final int numTilesX = 4;
    public final int numTilesY = 4;
    public int highscore = 0;
    public int score = 0;
    public int lastScore = 0;
    public int bufScore = 0;
    public boolean canUndo;

    private final Context mainContext;
    private final BoardView mainBoardView;
    private MainGameInterface mainGameInterface = null;
    private static final String HIGH_SCORE = "pref_highScore";
    private static final int GAME_WIN = 1;
    private static final int GAME_LOSE = -1;
    private static final int GAME_CONTINUES = 0;
    int gameState = GAME_CONTINUES;


    public MainGame(Context context, BoardView boardView) {
        mainContext = context;
        mainBoardView = boardView;
    }

    public void setGameInterface(MainGameInterface gameInterface) {
        mainGameInterface = gameInterface;
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
        setNewGameState();
        score = 0;
        addStartTiles();
        mainBoardView.invalidate();
        if (mainGameInterface != null) {
            mainGameInterface.onNewGameStarted();
        }
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

    public void move(int direction) {
        // 0: up, 1: right, 2: down, 3: left
        if (!gameIsActive()) {
            return;
        }
        prepareUndoState();
        BoardSpot vector = getVector(direction);
        ArrayList<Integer> traversalsX = buildTraversalsX(vector);
        ArrayList<Integer> traversalsY = buildTraversalsY(vector);
        boolean moved = false;

        prepareTiles();

        boolean test = canMoveLeft();
        for (int x : traversalsX) {
            for (int y : traversalsY) {
                BoardSpot spot = new BoardSpot(x, y);
                Tile tile = board.getSpotContent(spot);

                if (tile != null) {
                    BoardSpot[] positions = findFarthestPosition(spot, vector);
                    Tile nextTile = board.getSpotContent(positions[1]);

                    if (nextTile != null && nextTile.getValue() == tile.getValue() && nextTile.getTilesMergedWith() == null) {
                        Tile merged = new Tile(positions[1], tile.getValue() * 2);
                        Tile[] tiles = {tile, nextTile};
                        merged.setMergedWith(tiles);

                        board.insertTile(merged);
                        board.removeTile(tile);

                        tile.updatePosition(positions[1]);

                        // Update the score
                        score = score + merged.getValue();
                        highscore = Math.max(score, highscore);

                        // Check for 2048
                        if (merged.getValue() == 2048) {
                            gameState = GAME_WIN; // Set win state
                            endGame();
                        }
                    } else {
                        moveTile(tile, positions[0]);
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

        for (int x = 0; x < numTilesX; x++) {
            traversals.add(x);
        }
        if (vector.getX() == 1) {
            Collections.reverse(traversals);
        }

        return traversals;
    }

    private ArrayList<Integer> buildTraversalsY(BoardSpot vector) {
        ArrayList<Integer> traversals = new ArrayList<>();

        for (int x = 0; x < numTilesY; x++) {
            traversals.add(x);
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
        editor.apply();
    }

    private int getHighScore() {
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(mainContext);
        return (int)p.getFloat(HIGH_SCORE, -1);
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
        Resources resources = mainContext.getResources();
        if (score >= highscore) {
            highscore = score;
            saveHighScore();
        }
        if (gameLost()) {
            new AlertDialog.Builder(mainContext)
                    .setTitle("Game Over")
                    .setMessage(String.format(resources.getString(R.string.finalScore), score))
                    .setPositiveButton(R.string.play_again, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            newGame();
                        }
                    })
                    .show();
        } else {
            new AlertDialog.Builder(mainContext)
                    .setTitle("You Win!")
                    .setMessage(String.format(resources.getString(R.string.finalScore), score))
                    .setPositiveButton(R.string.play_again, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            newGame();
                        }
                    })
                    .show();
        }
    }

    private void setNewGameState() {
        gameState = GAME_CONTINUES;
    }

    public void startDemo() {
        if (canMoveLeft()) {
            move(IMainGame.DIRECTIONS.LEFT.ordinal());
        } else if (canMoveUp()) {
            move(IMainGame.DIRECTIONS.UP.ordinal());
        }else if (canMoveRight()) {
            move(IMainGame.DIRECTIONS.RIGHT.ordinal());
        } else if (canMoveDown()) {
            move(IMainGame.DIRECTIONS.DOWN.ordinal());
        }
    }

    private boolean canMoveLeft() {
        return canMoveInDirection(IMainGame.DIRECTIONS.LEFT);
    }
    private boolean canMoveUp() {
        return canMoveInDirection(IMainGame.DIRECTIONS.UP);
    }
    private boolean canMoveRight() {
        return canMoveInDirection(IMainGame.DIRECTIONS.RIGHT);
    }
    private boolean canMoveDown() {
        return canMoveInDirection(IMainGame.DIRECTIONS.DOWN);
    }

    private boolean canMoveInDirection(IMainGame.DIRECTIONS directions) {
        prepareUndoState();
        BoardSpot vector = getVector(directions.ordinal());
        ArrayList<Integer> traversalsX = buildTraversalsX(vector);
        ArrayList<Integer> traversalsY = buildTraversalsY(vector);
        boolean canMove = false;
        for (int x : traversalsX) {
            for (int y : traversalsY) {
                BoardSpot spot = new BoardSpot(x, y);
                Tile tile = board.getSpotContent(spot);

                if (tile != null) {
                    BoardSpot[] positions = findFarthestPosition(spot, vector);
                    Tile nextTile = board.getSpotContent(positions[1]);
                    if (nextTile != null) {
                        if (nextTile.getValue() == tile.getValue()) {
                            return true;
                        }
                        else if (nextTile.getY() - tile.getY() > 1 || nextTile.getX() - tile.getX() > 1) {
                            return true;
                        } else if (positionsEqual(spot, tile)) {
                            canMove = false;
                        }
                    }else if (directions == IMainGame.DIRECTIONS.LEFT) {
                        if (board.getSpotContent(0, tile.getY()) == null) {
                            return true;
                        }
                    }else if (directions == IMainGame.DIRECTIONS.RIGHT) {
                        if (board.getSpotContent(3, tile.getY()) == null) {
                            return true;
                        }
                    }else if (directions == IMainGame.DIRECTIONS.UP) {
                        if (board.getSpotContent(tile.getX(), 0) == null) {
                            return true;
                        }
                    }else if (directions == IMainGame.DIRECTIONS.DOWN) {
                        if (board.getSpotContent(tile.getX(), 3) == null) {
                            return true;
                        }
                    }
                }
            }
        }
        return canMove;
    }

}


