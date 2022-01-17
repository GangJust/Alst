package cn.edu.ccibe.alst.ui.activity;


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
import android.util.Log;

import androidx.annotation.NonNull;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.GeneralBasicParams;
import com.baidu.ocr.sdk.model.GeneralResult;
import com.baidu.ocr.sdk.model.WordSimple;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.GenericDeclaration;

import cn.edu.ccibe.alst.databinding.ActivityLoginBinding;
import cn.edu.ccibe.alst.entity.LoginParamEntity;
import cn.edu.ccibe.alst.net.NetUtil;
import cn.edu.ccibe.alst.ui.base.BaseActivity;
import cn.edu.ccibe.alst.ui.view.ReadmeView;
import cn.edu.ccibe.alst.ui.view.SimpleDialogView;
import cn.edu.ccibe.alst.uitls.BitmapUtil;
import cn.edu.ccibe.alst.uitls.MD5Util;
import cn.edu.ccibe.alst.uitls.SharedPreferencesUtil;

public class LoginActivity extends BaseActivity {
    private ActivityLoginBinding binding;

    private Bitmap codeBitmap;
    private String loginMsg = "";
    private File vCodeImageFile;

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
        // 加载本地信息
        if (loadPref()) return;
        initNetData();
    }

    @Override
    protected void onDestroy() {
        spEditor.remove("loginOut").commit();  // 移除主动注销登录，同步执行
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
        binding.autoLogin.setOnCheckedChangeListener((v, checked) -> {
            //同步更新是否自动登录
            spEditor.putBoolean("autoLogin", checked).commit();
        });
    }

    //浏览器打开
    private void openBrowser() {
        SimpleDialogView simpleDialogView = new SimpleDialogView()
                .setTitle("提醒")
                .setCancelableDismiss(false)
                .setContentText("通过浏览器打开网页版奥蓝系统。")
                .setAgreeText("已了解")
                .setAgreeOnClickListener(dialog -> {
                    dialog.dismiss();

                    Uri uri = Uri.parse(NetUtil.URL_LOGIN);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                });
        simpleDialogView.show(getSupportFragmentManager(), "BrowseDialog");
    }

    //初始化网络数据
    private void initNetData() {
        //toActivity(BrowserActivity.class);

        //请求系统，初始化获取必要信息
        new Thread(() -> {
            try {
                getNetUtil().initAlst();

                Thread.sleep(100);  //延迟100毫秒，加载验证码
                codeBitmap = BitmapFactory.decodeStream(getNetUtil().getVCode());
                saveVCode();  // 保存验证码
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
        int showCode = 4;
        SharedPreferences sp = SharedPreferencesUtil.getSp(this);
        SharedPreferences.Editor editor = SharedPreferencesUtil.getEditor(this);
        String html = "1. 更新学校新域名。<br/>" +
                "2. 自动识别验证码登录。<br/>" +
                "3. 移除打卡提示服务组件。";
        SimpleDialogView simpleDialogView = new SimpleDialogView()
                .setTitle("1.4-Bate 更新说明")
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

    // 保存验证码图片
    private void saveVCode() {
        vCodeImageFile = new File(getCacheDir(), "vcode.jpeg");
        BitmapUtil.bitmapToImage(codeBitmap, vCodeImageFile, Bitmap.CompressFormat.JPEG);
    }

    // 存储用户信息到本地
    private void storagePref() {
        String uName = binding.editUserName.getText().toString().trim();
        String uPass = binding.editUserPass.getText().toString().trim();
        Boolean autoLogin = binding.autoLogin.isChecked();

        spEditor.putString("user", uName);
        spEditor.putString("pass", uPass);
        spEditor.putString("session", getNetUtil().getCookie());
        spEditor.putBoolean("autoLogin", autoLogin);
        spEditor.putLong("time", System.currentTimeMillis());
        spEditor.apply();
    }

    // 加载用户信息
    private boolean loadPref() {
        String uName = sp.getString("user", "");
        String uPass = sp.getString("pass", "");
        String session = sp.getString("session", "");
        Boolean autoLogin = sp.getBoolean("autoLogin", false);
        Long time = sp.getLong("time", 0L);

        binding.editUserName.setText(uName);
        binding.editUserPass.setText(uPass);
        binding.autoLogin.setChecked(autoLogin);

        //判断是否存在登录历史, 且距离上次登录时间未超过10分钟。
        if (!session.equals("") && System.currentTimeMillis() - time < 600000L) {
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

        // 判断是否主动注销登录、是否勾选了自动登录
        SharedPreferences sp = SharedPreferencesUtil.getSp(getApplicationContext());
        Boolean loginOut = sp.getBoolean("loginOut", false);
        Boolean autoLogin = sp.getBoolean("autoLogin", false);
        if (loginOut || !autoLogin) return;

        //识别验证码
        GeneralBasicParams params = new GeneralBasicParams();
        params.setDetectDirection(true);
        params.setImageFile(vCodeImageFile);
        OCR.getInstance(this).recognizeGeneralBasic(params, new OnResultListener<GeneralResult>() {
            @Override
            public void onResult(GeneralResult result) {
                // 如果验证码识别成功，自动填充到文本框内。
                if (result.getWordsResultNumber() == 1) {
                    WordSimple wordSimple = result.getWordList().get(0);
                    binding.editVCode.setText(wordSimple.toString());
                    onLoginInClick();
                }
            }

            @Override
            public void onError(OCRError ocrError) {
                Log.d("GLog", "OCRError: " + ocrError);
            }
        });
    }

    // 错误回调
    private void callBackErrorMsg(String msg, int code) {
        showToast("ERROR：" + msg);
    }

    @Override
    public void onBackPressed() {
        exitApp(false);
    }
}