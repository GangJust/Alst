package cn.edu.swsm.alst.ui.base;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.permissionx.guolindev.PermissionX;

import java.util.ArrayList;
import java.util.List;

import cn.edu.swsm.alst.R;
import cn.edu.swsm.alst.databinding.ToastLayoutBinding;
import cn.edu.swsm.alst.net.NetUtil;
import cn.edu.swsm.alst.ui.utils.StatusBarUtil;
import cn.edu.swsm.alst.ui.view.SimpleDialogView;

public abstract class BaseActivity extends AppCompatActivity {
    private long timer = 0;
    private static List<Activity> activityList = new ArrayList<>();
    private static NetUtil netUtil = new NetUtil();
    private static Toast mToast;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.Theme_Alst);
        super.onCreate(savedInstanceState);
        _initActivityList();
        _initActivity();
        _initPermission();
    }

    @Override
    protected void onDestroy() {
        activityList.remove(this);
        super.onDestroy();
    }

    private void _initActivityList() {
        activityList.add(this);
    }

    private void _initActivity() {
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        //当FitsSystemWindows设置 true 时，会在屏幕最上方预留出状态栏高度的 padding
        StatusBarUtil.setRootViewFitsSystemWindows(this, false);
        //设置状态栏透明
        StatusBarUtil.setTranslucentStatus(this);
        //一般的手机的状态栏文字和图标都是白色的, 可如果你的应用也是纯白色的, 或导致状态栏文字看不清
        //所以如果你是这种情况,请使用以下代码, 设置状态使用深色文字图标风格, 否则你可以选择性注释掉这个if内容
        if (!StatusBarUtil.setStatusBarDarkTheme(this, true)) {
            //如果不支持设置深色风格 为了兼容总不能让状态栏白白的看不清, 于是设置一个状态栏颜色为半透明,
            //这样半透明+白=灰, 状态栏的文字能看得清
            StatusBarUtil.setStatusBarColor(this, 0x55000000);
        }
    }

    private void _initPermission() {
        PermissionX
                .init(this)
                .permissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                .onExplainRequestReason((scope, deniedList) -> {
                    String message = "需要以下权限(保存图片、上传文件)";
                    scope.showRequestReasonDialog(deniedList, message, "允许");
                })
                .request((allGranted, grantedList, deniedList) -> {
                    if (!allGranted) {
                        SimpleDialogView simpleDialogView = new SimpleDialogView();
                        simpleDialogView.setCancelable(false);
                        simpleDialogView.setTitle("权限提示");
                        simpleDialogView.setContentText("你没有允许必要权限，后续部分操作将无法使用，望悉知。");
                        simpleDialogView.setAgreeText("我已了解");
                        simpleDialogView.setAgreeOnClickListener(dialog -> dialog.dismiss());
                        simpleDialogView.show(getSupportFragmentManager(), "NotPermissionDialog");
                    }
                });
    }

    public static NetUtil getNetUtil() {
        return netUtil;
    }

    protected void showToast(String message) {
        ToastLayoutBinding toastBinding = ToastLayoutBinding.inflate(LayoutInflater.from(this));
        toastBinding.toastText.setText(message);

        mToast = Toast.makeText(this, "", Toast.LENGTH_LONG);
        mToast.setView(toastBinding.getRoot());
        mToast.show();
    }

    protected void toActivity(Class<? extends Activity> activity) {
        startActivity(new Intent(getApplicationContext(), activity));
    }

    protected void toActivity(Class<? extends Activity> activity, Bundle bundle) {
        Intent intent = new Intent(getApplicationContext(), activity);
        intent.putExtra("bundle", bundle);
        startActivity(intent);
    }

    protected void exitApp(boolean finishAndRemoveTask) {
        if (!finishAndRemoveTask && System.currentTimeMillis() - timer > 2000) {
            timer = System.currentTimeMillis();
            showToast("再按一次退出应用。");
            return;
        }
        for (Activity activity : activityList) {
            if (activity != null) {
                if (finishAndRemoveTask && activityList.indexOf(activity) == activityList.size() - 1) {
                    activity.finishAndRemoveTask();
                } else {
                    activity.finish();
                }
            }
        }
    }
}
