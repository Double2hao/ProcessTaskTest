package com.example.processtasktest;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.processtasktest.test.TaskStackManager;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

  //ui
  private Button btnStart;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    initViews();
    TaskStackManager.getInstance().init();
  }

  private void initViews() {
    btnStart = findViewById(R.id.btn_start_main);
    btnStart.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        TaskStackManager.getInstance().startTestActivity(MainActivity.this);
      }
    });
  }
}