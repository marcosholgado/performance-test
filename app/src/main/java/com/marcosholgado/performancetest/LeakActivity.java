package com.marcosholgado.performancetest;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;

public class LeakActivity extends Activity {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_leak);

    View button = findViewById(R.id.button);
    button.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startAsyncWork();
      }
    });
  }

  @SuppressLint("StaticFieldLeak")
  void startAsyncWork() {
    // This runnable is an anonymous class and therefore has a hidden reference to the outer
    // class MainActivity. If the activity gets destroyed before the thread finishes (e.g. rotation),
    // the activity instance will leak.
    Runnable work = new Runnable() {
      @Override public void run() {
        // Do some slow work in background
        SystemClock.sleep(20000);
      }
    };
    new Thread(work).start();
  }
}


