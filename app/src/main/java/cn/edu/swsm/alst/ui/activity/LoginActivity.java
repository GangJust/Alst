package cn.edu.swsm.alst.ui.activity;


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Html;
import android.text.InputType;

import androidx.annotation.NonNull;

import java.io.IOException;

import cn.edu.swsm.alst.databinding.ActivityLoginBinding;
import cn.edu.swsm.alst.entity.LoginParamEntity;
import cn.edu.swsm.alst.net.NetUtil;
import cn.edu.swsm.alst.ui.base.BaseActivity;
import cn.edu.swsm.alst.ui.service.CheckInNotifyService;
import cn.edu.swsm.alst.ui.view.ReadmeView;
import cn.edu.swsm.alst.ui.view.SimpleDialogView;
import cn.edu.swsm.alst.uitls.MD5Util;
import cn.edu.swsm.alst.uitls.SharedPreferencesUtil;

public class LoginActivity extends BaseActivity {
    private ActivityLoginBinding binding;

    private Bitmap codeBitmap;
    private String loginMsg = "";

    private final int INIT_ERROR = -1000;
    private final int LOGIN_ERROR = -1002;
    private final int LOAD_CODE_IMG_REQUEST = 1000;
    private final int LOGIN_CODE_REQUEST = 1001;

    private Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case INIT_ERROR:
                    callBackErrorMsg("无法访问到系统。", INIT_ERROR);
                    break;
                case LOGIN_ERROR:
                    callBackErrorMsg("无法登录成功。", LOGIN_ERROR);
                    break;
                case LOAD_CODE_IMG_REQUEST:
                    callBackCodeBitmap();
                    break;
                case LOGIN_CODE_REQUEST:
                    callBackLoginIn();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        initNetData();
    }

    @Override
    protected void onDestroy() {
        /*Intent intent = new Intent(getApplicationContext(), CheckInNotifyService.class);
        stopService(intent);*/
        super.onDestroy();
    }

    private void initView() {
        hasShowUpdateDescDialog();

        binding.btnLogin.setOnClickListener(v -> onLoginInClick());
        binding.forgotPassword.setOnClickListener(v -> forgotPassword());
        binding.textReadme.setOnClickListener(v -> showReadmeDialog());
        binding.editVCode.setFocusable(true);
        binding.editVCode.setFocusableInTouchMode(true);
        binding.editVCode.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                // 如果没有成功加载验证码，文本框获取焦点后重新加载。
                if (binding.imgVCode.getDrawable() == null) {
                    initNetData();
                }
            }
        });
        binding.openBrowser.setOnClickListener(v -> openBrowser());

        // 测试通知
        /*binding.testNotify.setOnClickListener(v -> {
            Intent intent1 = new Intent(this, CheckInNotifyService.class);
            startService(intent1);
        });*/
    }

    //浏览器打开
    private void openBrowser() {
        SimpleDialogView simpleDialogView = new SimpleDialogView()
                .setTitle("提醒")
                .setCancelableDismiss(false)
                .setContentText("通过浏览打开网页版奥蓝系统。")
                .setAgreeText("已了解")
                .setAgreeOnClickListener(dialog -> {
                    dialog.dismiss();

                    Uri uri = Uri.parse(NetUtil.URL_LOGIN);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                });
        simpleDialogView.show(getSupportFragmentManager(),"BrowseDialog");
    }

    //初始化网络数据
    private void initNetData() {
        //toActivity(BrowserActivity.class);

        //如果存在登录历史
        if (loadPref()) return;

        //请求系统，初始化获取必要信息
        new Thread(() -> {
            try {
                getNetUtil().initAlst();
                //延迟100毫秒，加载验证码
                Thread.sleep(100);
                codeBitmap = BitmapFactory.decodeStream(getNetUtil().getVCode());
                handler.sendEmptyMessage(LOAD_CODE_IMG_REQUEST);
            } catch (IOException e) {
                e.printStackTrace();
                handler.sendEmptyMessage(INIT_ERROR);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

    }

    // 登录事件
    private void onLoginInClick() {
        String uName = binding.editUserName.getText().toString().trim();
        String uPass = binding.editUserPass.getText().toString().trim();
        String vCode = binding.editVCode.getText().toString().trim();

        if (uName.trim().equals("")) {
            showToast("账号不能为空！");
            return;
        }

        if (uPass.trim().equals("")) {
            showToast("密码不能为空！");
            return;
        }

        if (vCode.trim().equals("")) {
            showToast("验证码不能为空！");
            return;
        }
        new Thread(() -> {
            try {
                LoginParamEntity loginParam = getNetUtil().getLoginParam();
                loginParam.setUserbh(uName);
                loginParam.setPas2s(MD5Util.md5(uPass.toUpperCase()).toUpperCase());
                loginParam.setVcode(vCode);
                loginMsg = getNetUtil().loginIn(loginParam);
                handler.sendEmptyMessage(LOGIN_CODE_REQUEST);
            } catch (IOException e) {
                e.printStackTrace();
                handler.sendEmptyMessage(LOGIN_ERROR);
            }
        }).start();
    }

    // 忘记密码
    private void forgotPassword() {
        if (binding.editUserPass.getInputType() != InputType.TYPE_CLASS_TEXT) {
            SimpleDialogView simpleDialogView = new SimpleDialogView()
                    .setTitle("功能说明")
                    .setCancelableDismiss(false)
                    .setContentText("该功能需要密码框内存在密码！")
                    .setAgreeText("已了解")
                    .setAgreeOnClickListener(dialog -> {
                        dialog.dismiss();
                    });
            simpleDialogView.setOnDismissListener(() -> {
                binding.editUserPass.setInputType(InputType.TYPE_CLASS_TEXT);
                showToast("密码框已设置可见！");
            });

            simpleDialogView.show(getSupportFragmentManager(), "forgotPassword");
        }
    }

    // 必读说明
    private void showReadmeDialog() {
        ReadmeView readmeView = new ReadmeView();
        readmeView.show(getSupportFragmentManager(), "ReadmeView");
    }

    // 更新说明
    private boolean hasShowUpdateDescDialog() {
        int showCode = 3;
        SharedPreferences sp = SharedPreferencesUtil.getSp(this);
        SharedPreferences.Editor editor = SharedPreferencesUtil.getEditor(this);

        /*String html =
                "<p>1. 修复登录失效跳回登录页验证码不显示的问题(即验证码框获取焦点后，触发重新请求验证码)。</p>" +
                        "<p>2. 自定义Toast轻量级消息提示。</p>" +
                        "<p>3. 适配奥蓝系统中诸如：问卷考试、网上投票等打开报错。</p>" +
                        "<p>4. 去除了打卡通知固定在通知栏上，但是在部分手机，比如屌丝机(MIUI)上，会被自动归类到不重要通知当中，暂时没找到解决方法。</p>" +
                        "<p>5. 软件内自动检测更新(诶，这个没写，嘿嘿嘿，等后面再看)。</p>" +
                        "<p>6. Flutter重构，适配苹果手机(这个也没写，只是有打算，先占个位置，万一呢，是不是)。</p>";
        SimpleDialogView simpleDialogView = new SimpleDialogView()
                .setTitle("1.1-Bate 更新说明")
                .setCancelableDismiss(false)
                .setContentText(Html.fromHtml(html))
                .setAgreeText("已了解")
                .setAgreeOnClickListener(dialog -> {
                    editor.putInt("upc", showCode);
                    editor.apply();
                    dialog.dismiss();
                });*/

        String html = "1. 增加文件上传功能。<br/>" +
                        "2. 增加浏览器打开方式。";
        SimpleDialogView simpleDialogView = new SimpleDialogView()
                .setTitle("1.3-Bate 更新说明")
                .setCancelableDismiss(false)
                .setContentText(Html.fromHtml(html))
                .setAgreeText("已了解")
                .setAgreeOnClickListener(dialog -> {
                    editor.putInt("upc", showCode);
                    editor.apply();
                    dialog.dismiss();
                });


        //如果软件有更新，则清除登录信息，弹出更新说明。
        if (sp.getInt("upc", -1) < showCode) {
            editor.remove("s");
            editor.apply();
            simpleDialogView.show(getSupportFragmentManager(), "UpdateDescDialog");
            return true;
        }
        return false;
    }

    // 存储用户信息到本地
    private void storagePref() {
        String uName = binding.editUserName.getText().toString().trim();
        String uPass = binding.editUserPass.getText().toString().trim();

        SharedPreferences.Editor editor = SharedPreferencesUtil.getEditor(getApplicationContext());
        editor.putString("u", uName);
        editor.putString("p", uPass);
        editor.putString("s", getNetUtil().getCookie());
        editor.apply();
    }

    // 加载用户信息
    private boolean loadPref() {
        SharedPreferences sp = SharedPreferencesUtil.getSp(getApplicationContext());
        String u = sp.getString("u", "");
        String p = sp.getString("p", "");
        String cookie = sp.getString("s", "");

        if (!u.trim().equals("")) binding.editUserName.setText(u);
        if (!p.trim().equals("")) binding.editUserPass.setText(p);

        //判断是否存在登录历史
        if (!cookie.equals("")) {
            toActivity(UserActivity.class);
            return true;
        }

        return false;
    }

    // 登录回调
    private void callBackLoginIn() {
        if (!loginMsg.equals("success")) {
            showToast(loginMsg);
            initNetData();
        } else {
            showToast("登陆验证成功！");
            storagePref();
            toActivity(UserActivity.class);
        }
    }

    // 验证码回调
    private void callBackCodeBitmap() {
        binding.imgVCode.setImageBitmap(codeBitmap);
    }

    // 错误回调
    private void callBackErrorMsg(String msg, int code) {
        showToast("发生错误，错误信息：" + msg);

    }

    @Override
    public void onBackPressed() {
        exitApp(false);
    }
}