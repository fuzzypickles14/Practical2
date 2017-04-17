package com.practical2;

import android.media.midi.MidiReceiver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements IMainGame {


    public MainGame currentGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View mainView = getLayoutInflater().inflate(R.layout.activity_main, null);
        System.out.println(mainView.getWidth() + "  " + mainView.getHeight());
        BoardView boardView = new BoardView(this);
        System.out.println(boardView.getWidth() + "  " + boardView.getHeight());

        setContentView(mainView);
        setSwipeButtonListener(mainView);
        setRestartButtonListener(mainView);
        setUpButtons();
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
}
