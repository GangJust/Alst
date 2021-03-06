package cn.edu.ccibe.alst.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;

import cn.edu.ccibe.alst.R;
import cn.edu.ccibe.alst.databinding.ActivityUserBinding;
import cn.edu.ccibe.alst.entity.CollapsedEntity;
import cn.edu.ccibe.alst.entity.FirstPageEntity;
import cn.edu.ccibe.alst.entity.InformEntity;
import cn.edu.ccibe.alst.net.NetUtil;
import cn.edu.ccibe.alst.ui.adapter.InformAdapter;
import cn.edu.ccibe.alst.ui.adapter.SingleCardAdapter;
import cn.edu.ccibe.alst.ui.base.BaseActivity;
import cn.edu.ccibe.alst.ui.view.SimpleDialogView;

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
        binding.loginOut.setOnClickListener(v -> onLoginOutClick());
    }

    // ????????????
    private void onLoginOutClick() {
        SimpleDialogView simpleDialogView = new SimpleDialogView()
                .setTitle("???????????????????????????")
                .setContentText(Html.fromHtml("????????????????????????????????????????????????????????????????????????<font color='red'>??????????????????????????????</font>"))
                .setAgreeText("??????")
                .setAgreeOnClickListener(dialog -> {
                    dialog.dismiss();
                    removeLoginSession();
                });
        simpleDialogView.show(getSupportFragmentManager(), "LoginOutSimpleDialog");
    }

    // ???????????????????????????????????????
    private void removeLoginSession() {
        spEditor.remove("session");
        spEditor.putBoolean("loginOut", true); //??????????????????
        spEditor.apply();
        this.finish();
    }

    // ????????????????????????
    private void hasLoginHistory() {
        session = sp.getString("session", "");
        new Thread(() -> {
            try {
                userName = getNetUtil().getUserName(session);
                handler.sendEmptyMessage(USER_NAME_SUCCESS);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // ????????????????????????
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

    // ??????????????????
    private void setInform(FirstPageEntity firstPage) {
        TextView notInform = binding.informLayout.notInform;
        RecyclerView informList = binding.informLayout.informList;

        //????????????
        ArrayList<InformEntity> list = firstPage.getInformEntitiesl();

        // ????????????
        if (list == null || list.size() == 0) {
            notInform.setVisibility(View.VISIBLE);
            informList.setVisibility(View.GONE);
            return;
        }

        // ?????????
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

    // ??????????????????
    private void setOptionsList(FirstPageEntity firstPage) {
        RecyclerView optionsList = binding.optionsListLayout.optionsList;

        //Grid??????(??????????????????????????????????????????????????????)
        //????????????
        /*ArrayList<GridCardEntity> list = new ArrayList<>();
        for (int i = 0; i < 13; i++) {
            list.add(new GridCardEntity(null, "?????? " + i));
        }
        optionsList.setLayoutManager(new GridLayoutManager(this, 4));
        //optionsList.addItemDecoration(new GridSpaceItemDecoration(4, 10, 10));
        GridCardAdapter gridCardAdapter = new GridCardAdapter();
        gridCardAdapter.setGridCardEntities(list);
        optionsList.setAdapter(gridCardAdapter);*/

        //Single??????
        //????????????
        ArrayList<CollapsedEntity> list = firstPage.getCollapsedEntities();

        optionsList.setLayoutManager(new LinearLayoutManager(this));
        optionsList.setItemAnimator(new DefaultItemAnimator());
        SingleCardAdapter singleCardAdapter = new SingleCardAdapter();
        singleCardAdapter.setCollapsedEntities(list);
        //?????????????????????
        singleCardAdapter.setItemOnClickListener((view, collapsed, sublistSingleCardAdapter, position) -> {
            //?????????????????????
            sublistSingleCardAdapter.setSublistItemOnClickListener((view1, optionLink, position1) -> {
                openBrowseActivity(collapsed, optionLink);
            });
        });
        optionsList.setAdapter(singleCardAdapter);
    }

    // ?????? WebView ??????
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

    // ??????????????????
    private void callBackIsLogin() {
        // ?????????????????????
        if (!userName.trim().equals("")) {
            binding.nickname.setText(String.format(getString(R.string.welcome), userName));
            if (firstPage == null) initFirstPageData();
            return;
        }
        // ?????????????????????
        removeLoginSession();
        showToast("?????????????????????????????????????????????");
    }

    // ?????????????????????
    private void callBackFirstPage() {
        setInform(firstPage);
        setOptionsList(firstPage);
    }

    @Override
    public void onBackPressed() {
        exitApp(false);
    }
}
