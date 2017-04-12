package com.practical2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {


    private MainGame currentGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View mainView = getLayoutInflater().inflate(R.layout.activity_main, null);
        LinearLayout container = (LinearLayout) mainView.findViewById(R.id.boardView);
        BoardView boardView = new BoardView(this);
        container.addView(boardView);
        setContentView(mainView);
        boardView.game.newGame();
        currentGame = boardView.game;
        setSwipeButtonListener(mainView);
        setRestartButtonListener(mainView);
    }

    private View.OnClickListener swipeButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Button swipeButton = ((Button) v);
            int direction = Integer.parseInt((String)swipeButton.getTag());
            currentGame.move(direction);
        }
    };

    private View.OnClickListener restartButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
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



}
