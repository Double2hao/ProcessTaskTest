package com.example.processtasktest.test;

/**
 * author: xujiajia
 * created on: 2021/1/26 11:37 AM
 * description:
 */
public class TaskStackInfo {
  public String nameProcess;
  public Class<? extends BaseTestActivity> activityClass;

  public TaskStackInfo(String nameProcess,
      Class<? extends BaseTestActivity> activityClass) {
    this.nameProcess = nameProcess;
    this.activityClass = activityClass;
  }
}
