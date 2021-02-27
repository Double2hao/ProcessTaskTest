package com.example.processtasktest.test;

import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import com.example.processtasktest.R;

import androidx.annotation.Nullable;

/**
 * author: xujiajia
 * created on: 2021/1/26 10:56 AM
 * description:
 */
public class BaseTestActivity extends Activity {

  //ui
  private TextView tvActivityName;
  private TextView tvProcessName;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_test);

    initViews();
  }

  private void initViews() {
    tvActivityName = findViewById(R.id.tv_activity_name_test);
    tvProcessName = findViewById(R.id.tv_process_name_test);

    tvActivityName.setText("activity name:\n" + this.getLocalClassName());
    tvProcessName.setText("process name:\n" + getProcessName(this));
  }

  public static String getProcessName(Context cxt) {
    int pid = android.os.Process.myPid();
    ActivityManager am = (ActivityManager) cxt.getSystemService(Context.ACTIVITY_SERVICE);
    List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
    if (runningApps == null) {
      return null;
    }
    for (ActivityManager.RunningAppProcessInfo procInfo : runningApps) {
      if (procInfo.pid == pid) {
        return procInfo.processName;
      }
    }
    return null;
  }
}
