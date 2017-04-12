package com.practical2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View mainView = getLayoutInflater().inflate(R.layout.activity_main, null);
        LinearLayout container = (LinearLayout) mainView.findViewById(R.id.boardView);
        BoardView boardView = new BoardView(this);
        container.addView(boardView);
        setContentView(mainView);
    }

}
