package com.practical2;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements IMainGame, MainGame.MainGameInterface {


    public MainGame currentGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View mainView = getLayoutInflater().inflate(R.layout.activity_main, null);
        System.out.println(mainView.getWidth() + "  " + mainView.getHeight());
        BoardView boardView = new BoardView(this);
        System.out.println(boardView.getWidth() + "  " + boardView.getHeight());

        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean("hasState")) {
                this.load();
            }
        }

        setContentView(mainView);
        setSwipeButtonListener(mainView);
        setRestartButtonListener(mainView);
        setDemoButtonListener(mainView);
        setUpButtons();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("hasState", true);
        this.save();
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.load();
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.save();
    }

    private void setUpButtons() {
        View upButton = findViewById(R.id.upButton);
        View rightButton = findViewById(R.id.rightButton);
        View leftButton = findViewById(R.id.leftButton);
        View downButton = findViewById(R.id.downButton);
        rightButton.setRotation(90);
        downButton.setRotation(180);
        leftButton.setRotation(-90);
        setHighScore(currentGame.highscore);
    }

    @Override
    public void setCurrentGame(MainGame game) {
        currentGame = game;
        currentGame.setGameInterface(this);
    }

    private View.OnClickListener swipeButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Button swipeButton = ((Button) v);
            int direction = Integer.parseInt((String)swipeButton.getTag());
            moveAndSetScore(direction);
        }
    };

    private void setScore(int score) {
        TextView scoreTextView = (TextView) findViewById(R.id.scoreTextView);
        currentGame.score = score;
        String scoreText = String.format(getResources().getString(R.string.basicScore), score);
        scoreTextView.setText(scoreText);
    }

    private void setHighScore(int highScore) {
        TextView highScoreTextView = (TextView) findViewById(R.id.highScoreTextView);
        String highScoreText = String.format(getResources().getString(R.string.basicHighScore), highScore);
        highScoreTextView.setText(highScoreText);
    }

    private View.OnClickListener restartButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setScore(0);
            setHighScore(currentGame.highscore);
            currentGame.newGame();
        }
    };

    @Override
    public void onNewGameStarted() {
        setScores(currentGame.score, currentGame.highscore);
    }

    private void setScores(int score, int highscore) {
        setScore(score);
        setHighScore(highscore);
    }

    private void setSwipeButtonListener(View view) {
        Button upButton = (Button) view.findViewById(R.id.upButton);
        Button downButton = (Button) view.findViewById(R.id.downButton);
        Button leftButton = (Button) view.findViewById(R.id.leftButton);
        Button rightButton = (Button) view.findViewById(R.id.rightButton);
        upButton.setOnClickListener(swipeButtonListener);
        downButton.setOnClickListener(swipeButtonListener);
        leftButton.setOnClickListener(swipeButtonListener);
        rightButton.setOnClickListener(swipeButtonListener);
    }

    private void setRestartButtonListener(View view) {
        Button restartButton = (Button) view.findViewById(R.id.restartButton);
        restartButton.setOnClickListener(restartButtonListener);
    }

    private void setDemoButtonListener(View view) {
        Button demoButton = (Button) view.findViewById(R.id.demoButton);
        demoButton.setOnClickListener(demoButtonListener);
    }

    private View.OnClickListener demoButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            currentGame.startDemo();
            setScores(currentGame.score, currentGame.highscore);
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            //Do nothing
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            moveAndSetScore(DIRECTIONS.DOWN.ordinal());
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            moveAndSetScore(DIRECTIONS.UP.ordinal());
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            moveAndSetScore(DIRECTIONS.LEFT.ordinal());
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            moveAndSetScore(DIRECTIONS.RIGHT.ordinal());
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void moveAndSetScore(int direction) {
        currentGame.move(direction);
        setHighScore(currentGame.highscore);
        setScore(currentGame.score);
    }

    private static final String WIDTH = "pref_width";
    private static final String HEIGHT = "pref_height";
    private static final String SCORE = "pref_score";
    private static final String HIGH_SCORE = "pref_highScore";
    private static final String GAME_STATE = "pref_gameState";

    private void save() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();

        Tile[][] currentBoard = currentGame.board.board;
        editor.putInt(WIDTH, currentBoard.length);
        editor.putInt(HEIGHT, currentBoard[0].length);
        for (int x = 0; x < currentBoard.length; x++) {
            for (int y = 0; y < currentBoard[0].length; y++) {
                if (currentBoard[x][y] != null) {
                    editor.putInt(x + " " + y, currentBoard[x][y].getValue());
                } else {
                    editor.putInt(x + " " + y, 0);
                }
            }
        }
        editor.putFloat(SCORE, currentGame.score);
        editor.putFloat(HIGH_SCORE, currentGame.highscore);
        editor.putInt(GAME_STATE, currentGame.gameState);
        editor.commit();

    }

    private void load() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        for (int x = 0; x < currentGame.board.board.length; x++) {
            for (int y = 0; y < currentGame.board.board[0].length; y++) {
                int value = settings.getInt(x + " " + y, -1);
                if (value > 0) {
                    currentGame.board.board[x][y] = new Tile(x, y, value);
                } else if (value == 0) {
                    currentGame.board.board[x][y] = null;
                }
            }
        }

        setScores((int) settings.getFloat(SCORE, currentGame.score), (int) settings.getFloat(HIGH_SCORE, currentGame.highscore));
        currentGame.gameState = settings.getInt(GAME_STATE, currentGame.gameState);
    }
}
