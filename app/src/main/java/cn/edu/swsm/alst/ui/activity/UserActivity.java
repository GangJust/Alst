package cn.edu.swsm.alst.ui.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import cn.edu.swsm.alst.R;
import cn.edu.swsm.alst.databinding.ActivityUserBinding;
import cn.edu.swsm.alst.entity.CollapsedEntity;
import cn.edu.swsm.alst.entity.FirstPageEntity;
import cn.edu.swsm.alst.entity.InformEntity;
import cn.edu.swsm.alst.net.NetUtil;
import cn.edu.swsm.alst.ui.adapter.InformAdapter;
import cn.edu.swsm.alst.ui.adapter.SingleCardAdapter;
import cn.edu.swsm.alst.ui.base.BaseActivity;
import cn.edu.swsm.alst.ui.receiver.AlarmReceiver;
import cn.edu.swsm.alst.ui.service.CheckInNotifyService;
import cn.edu.swsm.alst.ui.view.SimpleDialogView;
import cn.edu.swsm.alst.uitls.SharedPreferencesUtil;

public class UserActivity extends BaseActivity {
    private ActivityUserBinding binding;

    private String session = "";
    private String userName = "";
    private FirstPageEntity firstPage;

    private final int USER_NAME_SUCCESS = 1000;
    private final int FIRST_PAGE_SUCCESS = 1001;

    private Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case USER_NAME_SUCCESS:
                    callBackIsLogin();
                    break;
                case FIRST_PAGE_SUCCESS:
                    callBackFirstPage();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        hasLoginHistory();
    }

    private void initView() {
        //Intent intent1 = new Intent(this, CheckInNotifyService.class);
        //startService(intent1);

        binding.loginOut.setOnClickListener(v -> onLoginOutClick());
        binding.nickname.setOnLongClickListener(v -> registerAlarm());
        //setAlarm();
    }

    //退出登陆
    private void onLoginOutClick() {
        SimpleDialogView simpleDialogView = new SimpleDialogView()
                .setTitle("确定要注销登录么？")
                .setContentText(Html.fromHtml("该操作将清除登录历史，下次进入软件需要重新登录，<font color='red'>点击空白处可以取消。</font>"))
                .setAgreeText("注销")
                .setAgreeOnClickListener(dialog -> {
                    dialog.dismiss();
                    removeLoginSession();
                });
        simpleDialogView.show(getSupportFragmentManager(), "LoginOutSimpleDialog");
    }

    //注册定时提醒闹钟
    private boolean registerAlarm() {
        Spanned spanned = Html.fromHtml("你确定要将应用切换至后台并隐藏<font color='red'>(该行为会保持软件运行，但会消耗少许电量)</font>，如果执行该操作，且应用没被系统杀死，每天晚上<font color='red'>08:05</font>会提醒你打卡。<font size='30'>应用并没有强制要求后台运行，如果需要，你可以试试(自启动、后台加锁、关闭电源优化)</font>");
        SimpleDialogView simpleDialogView = new SimpleDialogView();
        simpleDialogView.setTitle("切换后台提示");
        simpleDialogView.setContentText(spanned);
        simpleDialogView.setAgreeText("切换后台并隐藏");
        simpleDialogView.setAgreeOnClickListener(dialog -> {
            dialog.dismiss();
            setAlarm(); //开启定时提醒
            exitApp(true);
        });
        simpleDialogView.show(getSupportFragmentManager(), "FinishAndRemoveTaskDialog");
        return true;
    }

    // 删除登录历史并结束当前活动
    private void removeLoginSession() {
        SharedPreferences.Editor editor = SharedPreferencesUtil.getEditor(getApplicationContext());
        editor.remove("s");
        editor.apply();
        this.finish();
    }

    // 是否存在登录历史
    private void hasLoginHistory() {
        SharedPreferences sp = SharedPreferencesUtil.getSp(getApplicationContext());
        session = sp.getString("s", "");
        new Thread(() -> {
            try {
                userName = getNetUtil().getUserName(session);
                handler.sendEmptyMessage(USER_NAME_SUCCESS);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // 初始化用户页信息
    private void initFirstPageData() {
        new Thread(() -> {
            try {
                firstPage = getNetUtil().getFirst(session);
                handler.sendEmptyMessage(FIRST_PAGE_SUCCESS);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // 设置校级公告
    private void setInform(FirstPageEntity firstPage) {
        TextView notInform = binding.informLayout.notInform;
        RecyclerView informList = binding.informLayout.informList;

        //获取数据
        ArrayList<InformEntity> list = firstPage.getInformEntitiesl();

        // 没有公告
        if (list == null || list.size() == 0) {
            notInform.setVisibility(View.VISIBLE);
            informList.setVisibility(View.GONE);
            return;
        }

        // 有公告
        notInform.setVisibility(View.GONE);
        informList.setVisibility(View.VISIBLE);
        informList.setLayoutManager(new LinearLayoutManager(this));
        InformAdapter adapter = new InformAdapter();
        adapter.setInformList(list);
        adapter.setItemOnClickListener((view, inform, position) -> {
            Bundle bundle = new Bundle();
            bundle.putString("url", NetUtil.BASE_URL + "Mobile/" + inform.getHref());
            bundle.putString("cookie", getNetUtil().getCookie());
            toActivity(BrowserActivity.class, bundle);
        });
        informList.setAdapter(adapter);
    }

    // 设置链接选项
    private void setOptionsList(FirstPageEntity firstPage) {
        RecyclerView optionsList = binding.optionsListLayout.optionsList;

        //Grid布局(暂时淘汰使用，原因：找不到适合的图片)
        //获取数据
        /*ArrayList<GridCardEntity> list = new ArrayList<>();
        for (int i = 0; i < 13; i++) {
            list.add(new GridCardEntity(null, "基本 " + i));
        }
        optionsList.setLayoutManager(new GridLayoutManager(this, 4));
        //optionsList.addItemDecoration(new GridSpaceItemDecoration(4, 10, 10));
        GridCardAdapter gridCardAdapter = new GridCardAdapter();
        gridCardAdapter.setGridCardEntities(list);
        optionsList.setAdapter(gridCardAdapter);*/

        //Single布局
        //获取数据
        ArrayList<CollapsedEntity> list = firstPage.getCollapsedEntities();

        optionsList.setLayoutManager(new LinearLayoutManager(this));
        optionsList.setItemAnimator(new DefaultItemAnimator());
        SingleCardAdapter singleCardAdapter = new SingleCardAdapter();
        singleCardAdapter.setCollapsedEntities(list);
        //列表项点击事件
        singleCardAdapter.setItemOnClickListener((view, collapsed, sublistSingleCardAdapter, position) -> {
            //扩展项点击事件
            sublistSingleCardAdapter.setSublistItemOnClickListener((view1, optionLink, position1) -> {
                openBrowseActivity(collapsed, optionLink);
            });
        });
        optionsList.setAdapter(singleCardAdapter);
    }

    // 打开 WebView 活动
    private void openBrowseActivity(CollapsedEntity collapsed, CollapsedEntity.OptionLinkEntity optionLink) {
        Bundle bundle = new Bundle();
        if (optionLink.getOptionId().equals("xzdm3")) {
            bundle.putString("url", NetUtil.BASE_URL + "txxm/rsbulid/r_3_3_" + optionLink.getLinkId() + ".aspx?mobbz=1&xq=" + collapsed.getSemester() + "&nd=" + collapsed.getYear());
        } else {
            String url = NetUtil.BASE_URL;
            switch (optionLink.getLinkId().substring(0, 6)) {
                case "st_cg_":
                    url += "/txxm/cgsq.aspx?mobbz=1&xq=" + collapsed.getSemester() + "&km=" + optionLink.getLinkId();
                    break;
                case "st_hp_":
                    url += "/txxm/sthp.aspx?mobbz=1&xq=" + collapsed.getSemester() + "&nd=" + collapsed.getYear() + "&km=" + optionLink.getLinkId();
                    break;
                case "tm_py_":
                    url += "/txxm/fdypy.aspx?mobbz=1&xq=" + collapsed.getSemester() + "&nd=" + collapsed.getYear() + "&km=" + optionLink.getLinkId() + "&tmfl=" + URLEncoder.encode(optionLink.getTmfl());
                    break;
                case "tm_ks_":
                    url += "/txxm/dkkt.aspx?mobbz=1&xq=" + collapsed.getSemester() + "&nd=" + collapsed.getYear() + "&km=" + optionLink.getLinkId() + "&tmfl=" + URLEncoder.encode(optionLink.getTmfl());
                    break;
                case "tm_wj_":
                    url += "/wjdc/wjkt.aspx?mobbz=1&tmfl=" + URLEncoder.encode(optionLink.getTmfl()) + "&xq=" + collapsed.getSemester() + "&km=" + optionLink.getLinkId();
                    break;
                case "st_tp_":
                    url += "/txxm/tpkt.aspx?mobbz=1&xq=" + collapsed.getSemester() + "&nd=" + collapsed.getYear() + "&km=" + optionLink.getLinkId();
                    break;
                case "st_cp_":
                    url += "/txxm/khcp.aspx?mobbz=1&xq=" + collapsed.getSemester() + "&nd=" + collapsed.getYear() + "&km=" + optionLink.getLinkId();
                    break;
                case "st_kc_":
                    url += "/txxm/cpkh.aspx?mobbz=1&xq=" + collapsed.getSemester() + "&nd=" + collapsed.getYear() + "&km=" + optionLink.getLinkId();
                    break;
                default:
                    if (optionLink.getLinkId().length() >= 9 && optionLink.getLinkId().substring(0, 9) == "st_rc_zx_") {
                        url += "/xlzx/xlzx.aspx?mobbz=1&xq=" + collapsed.getSemester() + "&km=" + optionLink.getLinkId();
                    } else {
                        url += "Mobile/" + "rsbulid/r_3_3_" + optionLink.getLinkId() + ".aspx";
                    }
            }
            //bundle.putString("url", NetUtil.BASE_URL + "Mobile/rsbulid/r_3_3_" + optionLink.getLinkId() + ".aspx");
            bundle.putString("url", url);
        }

        bundle.putString("cookie", getNetUtil().getCookie());
        toActivity(BrowserActivity.class, bundle);
    }

    // 判断登录回调
    private void callBackIsLogin() {
        // 成功取到用户名
        if (!userName.trim().equals("")) {
            binding.nickname.setText(String.format(getString(R.string.welcome), userName));
            if (firstPage == null) initFirstPageData();
            return;
        }
        // 否则，登录过期
        removeLoginSession();
        showToast("登录信息已经过期，请重新登录！");
    }

    // 用户页信息回调
    private void callBackFirstPage() {
        setInform(firstPage);
        setOptionsList(firstPage);
    }

    // 设置定时提醒签到闹钟
    private void setAlarm() {
        Log.d("GLog", "setAlarm: 设置定时提醒签到闹钟");

        // 进行闹铃注册
        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);

        //得到日历实例，主要是为了下面的获取时间
        Calendar mCalendar = Calendar.getInstance();
        mCalendar.setTimeInMillis(System.currentTimeMillis());

        //获取当前毫秒值
        long systemTime = System.currentTimeMillis();

        //是设置日历的时间，主要是让日历的年月日和当前同步
        mCalendar.setTimeInMillis(System.currentTimeMillis());
        // 这里时区需要设置一下，不然可能个别手机会有8个小时的时间差
        mCalendar.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        //设置在几点提醒  设置的为8点
        mCalendar.set(Calendar.HOUR_OF_DAY, 8);
        //设置在几分提醒  设置的为05分
        mCalendar.set(Calendar.MINUTE, 5);
        //下面这两个看字面意思也知道
        mCalendar.set(Calendar.SECOND, 0);
        mCalendar.set(Calendar.MILLISECOND, 0);

        //上面设置的就是19点05分的时间点

        //获取上面设置的19点05分的毫秒值
        long selectTime = mCalendar.getTimeInMillis();

        // 如果当前时间大于设置的时间，那么就从第二天的设定时间开始
        if (systemTime > selectTime) {
            mCalendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, mCalendar.getTimeInMillis(), sender);
    }

    @Override
    public void onBackPressed() {
        if (!userName.trim().equals("")) {
            exitApp(false);
            return;
        }
        super.onBackPressed();
    }
}
