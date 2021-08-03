package cn.edu.swsm.alst.ui.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import cn.edu.swsm.alst.ui.service.CheckInNotifyService;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("GLog", "onReceive: 定时任务执行。");
        Intent intent1 = new Intent(context, CheckInNotifyService.class);
        context.startService(intent1);
    }
}
