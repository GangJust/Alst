package cn.edu.swsm.alst.ui.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import cn.edu.swsm.alst.R;
import cn.edu.swsm.alst.ui.activity.LoginActivity;

public class CheckInNotifyService extends Service {

    String channelId = "checkIn";
    String channelName = "打卡提醒";
    int importance = NotificationManager.IMPORTANCE_HIGH;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("GLog", "onCreate: ");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("GLog", "onStartCommand: ");
        sendNotify();
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        Log.d("GLog", "onDestroy: ");
        super.onDestroy();
    }


    private void sendNotify() {
        // 获取通知管理器
        NotificationManager notificationManager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);

        // 创建点击通知跳转意图
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplication(), 0, intent, 0);

        // Android 8 之后需要设置 NotificationChannel
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            channel.enableVibration(true);
            channel.enableLights(true);
            channel.setShowBadge(true);
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            channel.setImportance(NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }


        // 创建通知
        Notification notification = new NotificationCompat.Builder(getApplicationContext(), channelId)
                .setContentTitle(channelName) //设置通知标题
                .setContentText("今天该打卡了。") //设置通知内容
                .setContentIntent(pendingIntent) //设置点击意图
                .setSmallIcon(R.mipmap.ic_launcher_alpha) //设置通知小图标
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher)) //设置大图标
                .setPriority(NotificationCompat.PRIORITY_HIGH) //设置显示优先级
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE) //设置使用系统默认的声音、默认震动
                .setAutoCancel(true) //点击后自动消失
                //.setOngoing(true) //锁定不可滑动删除
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setShowWhen(true)
                .build();

        //发送通知
        notificationManager.notify(1, notification);
    }
}
