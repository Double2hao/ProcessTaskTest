package com.example.processtasktest.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.UiThread;

/**
 * author: xujiajia
 * created on: 2021/1/26 11:34 AM
 * description:
 */
public class TaskStackManager {

  //constants
  private final static String TAG = "TaskStackManager";
  private final static int MAX_PROCESS = 3;
  //data
  private final HashMap<String, TaskStackInfo> mapInfo = new HashMap<>();
  private Handler mainHandler = null;

  private TaskStackManager() {
    mapInfo.put(TestActivityOne.class.getName(),
        new TaskStackInfo("testprocess1", TestActivityOne.class));
    mapInfo.put(TestActivityTwo.class.getName(),
        new TaskStackInfo("testprocess2", TestActivityTwo.class));
    mapInfo.put(TestActivityThree.class.getName(),
        new TaskStackInfo("testprocess3", TestActivityThree.class));
    mapInfo.put(TestActivityFour.class.getName(),
        new TaskStackInfo("testprocess4", TestActivityFour.class));
  }

  private static class Host {
    private static final TaskStackManager instance = new TaskStackManager();
  }

  public static TaskStackManager getInstance() {
    return Host.instance;
  }

  @UiThread
  public void init() {
    mainHandler = new Handler();
  }

  public void startTestActivity(Context context) {
    if (context == null) {
      return;
    }

    ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    if (am == null) {
      return;
    }
    List<String> listCurrentProcess = getCurrentProcessList(am);
    if (listCurrentProcess == null) {
      return;
    }
    chooseActivityToStart(context, listCurrentProcess);

    //如果已经启动了MAX_PROCESS个进程，start时会启动第MAX_PROCESS+1个，并且清理第一个。
    //todo 这里只是demo演示，如果需要更精准的控制，可以在进程启动后通过ipc来调用清理逻辑
    mainHandler.postDelayed(new Runnable() {
      @Override public void run() {
        checkAndClearOldestProcess(context);
      }
    }, 1000);
  }

  private void checkAndClearOldestProcess(Context context) {
    ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    if (am == null) {
      return;
    }
    List<String> listCurrentProcess = getCurrentProcessList(am);
    if (listCurrentProcess == null) {
      return;
    }
    if (listCurrentProcess.size() > MAX_PROCESS) {
      clearOldestProcess(am);
    }
  }

  private void chooseActivityToStart(Context context, List<String> listCurrentProcess) {
    if (context == null) {
      return;
    }
    for (TaskStackInfo info : mapInfo.values()) {
      boolean exist = false;
      for (String currName : listCurrentProcess) {
        if (TextUtils.equals(info.activityClass.getName(), currName)) {
          exist = true;
          break;
        }
      }
      if (!exist) {
        Intent intent = new Intent(context, info.activityClass);
        context.startActivity(intent);
        break;
      }
    }
  }

  private List<String> getCurrentProcessList(ActivityManager am) {
    if (am == null) {
      return null;
    }

    List<String> listCurrentProcess = new ArrayList<>();
    try {
      List<ActivityManager.AppTask> appTasks = am.getAppTasks();
      if (appTasks != null && appTasks.size() > 0) {
        for (ActivityManager.AppTask item : appTasks) {
          if (item == null) {
            continue;
          }
          ComponentName componentName = item.getTaskInfo().baseActivity;
          if (componentName != null && mapInfo.containsKey(componentName.getClassName())) {
            Log.d(TAG, "getCurrentProcessList componentName:" + componentName.getClassName());
            listCurrentProcess.add(componentName.getClassName());
          }
        }
      }
    } catch (Exception e) {
      Log.e(TAG, "getCurrentSize error:" + e.getMessage());
    }
    return listCurrentProcess;
  }

  private void clearOldestProcess(ActivityManager am) {
    if (am == null) {
      return;
    }
    List<Pair<ActivityManager.AppTask, String>> needCleanTask = new ArrayList<>();
    try {
      List<ActivityManager.AppTask> appTasks = am.getAppTasks();
      for (ActivityManager.AppTask item : appTasks) {
        if (item == null) {
          continue;
        }
        ComponentName componentName = item.getTaskInfo().baseActivity;
        if (componentName != null && mapInfo.containsKey(componentName.getClassName())) {
          Log.d(TAG, "clearOldestProcess componentName:" + componentName.getClassName());
          needCleanTask.add(
              Pair.create(item, mapInfo.get(componentName.getClassName()).nameProcess));
        }
      }

      for (int i = MAX_PROCESS; i < needCleanTask.size(); i++) {
        ActivityManager.AppTask item = needCleanTask.get(i).first;
        String processName = needCleanTask.get(i).second;
        item.finishAndRemoveTask();
        killProcess(am, processName);
      }
    } catch (Throwable e) {
      Log.e(TAG, "cleanLastTask" + e.getMessage());
    }
  }

  private void killProcess(ActivityManager am, String processName) {
    ActivityManager.RunningAppProcessInfo info = getProcessInfo(am, processName);
    if (info != null) {
      Process.killProcess(info.pid);
    }
  }

  private ActivityManager.RunningAppProcessInfo getProcessInfo(ActivityManager am,
      String processName) {
    if (TextUtils.isEmpty(processName) || am == null) {
      return null;
    }
    List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfoList =
        am.getRunningAppProcesses();
    if (runningAppProcessInfoList == null || runningAppProcessInfoList.size() <= 0) {
      return null;
    }
    try {
      for (ActivityManager.RunningAppProcessInfo processInfo : runningAppProcessInfoList) {
        if (processInfo == null) {
          continue;
        }
        Log.d(TAG, "getProcessInfo process name:" + processInfo.processName);
        int childProcessFlag = processInfo.processName.lastIndexOf(":");
        if (childProcessFlag >= 0 && childProcessFlag < processInfo.processName.length()) {
          String process =
              processInfo.processName.substring(childProcessFlag, processInfo.processName.length());
          if (process.equals(processName)) {
            return processInfo;
          }
        }
      }
    } catch (Throwable e) {
      Log.e(TAG, "getProcessInfo " + e.getMessage());
    }
    return null;
  }
}

