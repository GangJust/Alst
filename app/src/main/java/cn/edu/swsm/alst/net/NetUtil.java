package cn.edu.swsm.alst.net;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.edu.swsm.alst.entity.CollapsedEntity;
import cn.edu.swsm.alst.entity.FirstPageEntity;
import cn.edu.swsm.alst.entity.InformEntity;
import cn.edu.swsm.alst.entity.LoginParamEntity;

public class NetUtil {
    public static final String HOST = "alst.swsm.edu.cn";
    public static final String BASE_URL = "http://alst.swsm.edu.cn/";
    public static final String URL_LOGIN = "http://alst.swsm.edu.cn/Mobile/LOGIN.ASPX";
    public static final String URL_V_CODE = "http://alst.swsm.edu.cn/vcode.aspx";
    public static final String URL_FIRST = "http://alst.swsm.edu.cn/Mobile/first.aspx";

    private NetHeaderUtil netHeaderUtil;

    //登录参数
    private LoginParamEntity loginParam;

    /**
     * 初始化 Cookie, LoginParam
     *
     * @throws IOException
     */
    public void initAlst() throws IOException {
        netHeaderUtil = new NetHeaderUtil();
        //设置为 html 类型
        netHeaderUtil.setContentType(NetHeaderUtil.ContentTypes.HtmlUtf8);
        Connection connect = connect(URL_LOGIN, netHeaderUtil, Connection.Method.GET);
        Connection.Response response = connect.execute();

        //System.out.println(response.body());

        //保留请求后的 Cookie
        netHeaderUtil.setCookies(response.cookies());

        //保留登录参数
        Document document = response.parse();

        loginParam = new LoginParamEntity();
        loginParam.set__VIEWSTATE(document.getElementById("__VIEWSTATE").val());
        loginParam.set__VIEWSTATEGENERATOR(document.getElementById("__VIEWSTATEGENERATOR").val());
        loginParam.set__VIEWSTATEENCRYPTED(document.getElementById("__VIEWSTATEENCRYPTED").val());
        loginParam.setUserbh(document.getElementById("userbh").val());
        loginParam.setVcode(document.getElementById("vcode").val());
        loginParam.setCw(document.getElementById("cw").val()); //登录提示
        loginParam.setXzbz(document.getElementById("xzbz").val());// 写死在js中，下方正则获取
        loginParam.setPas2s(document.getElementById("pas2s").val());
        loginParam.setYxdm(document.getElementById("yxdm").val());

        //如果取到 xzbz 为空
        if (loginParam.getXzbz().equals("")) {
            String headHtml = document.head().outerHtml();
            Pattern pattern = Pattern.compile("form1.xzbz.value=\"(.+)\"".trim());
            Matcher matcher = pattern.matcher(headHtml);
            loginParam.setXzbz("1"); //默认
            if (matcher.find()) {
                loginParam.setXzbz(matcher.group(1));
            }
        }
    }

    /**
     * 获取验证码
     *
     * @throws IOException
     */
    public InputStream getVCode() throws IOException {
        //替换为 Jpeg 类型
        netHeaderUtil.setContentType(NetHeaderUtil.ContentTypes.JpegUtf8);
        netHeaderUtil.setHost(HOST);
        netHeaderUtil.setReferer(URL_LOGIN);

        //System.out.println(getHeader());

        Connection connect = connect(URL_V_CODE, netHeaderUtil, Connection.Method.GET);

        return connect.execute().bodyStream();
    }

    /**
     * 请求登录
     *
     * @param loginParam
     * @return
     */
    public String loginIn(LoginParamEntity loginParam) throws IOException {
        //替换提交类型
        netHeaderUtil.setContentType(NetHeaderUtil.ContentTypes.FromApplication);

        Connection connect = connect(URL_LOGIN, netHeaderUtil, Connection.Method.POST);
        connect.requestBody(loginParam.getParamString());
        Document document = connect.execute().parse();

        //如果已经登陆成功
        if (document.html().contains("top_1.aspx") && document.html().contains("first.aspx")) {
            return "success";
        }
        loginParam.setCw(document.getElementById("cw").val()); //登录提示
        return loginParam.getCw().trim();
    }

    public Map<String, String> getHeader() {
        return netHeaderUtil.getHeader();
    }

    /**
     * 获取登录 Cookie
     *
     * @return
     */
    public String getCookie() {
        return getHeader().get("Cookie");
    }

    /**
     * 获取用户名
     *
     * @param cookie
     * @return
     * @throws IOException
     */
    public String getUserName(String cookie) throws IOException {
        String userName = "";

        //设置为 html 类型
        if (netHeaderUtil == null) netHeaderUtil = new NetHeaderUtil();
        netHeaderUtil.setContentType(NetHeaderUtil.ContentTypes.HtmlUtf8);
        netHeaderUtil.setHost(HOST);
        netHeaderUtil.setReferer(URL_LOGIN);
        if (cookie != null) netHeaderUtil.setCookies(cookie);


        Connection connect = connect(BASE_URL + "top_1.aspx", netHeaderUtil, Connection.Method.GET);
        String body = connect.execute().parse().body().outerHtml();

        Pattern pattern = Pattern.compile("<b>欢迎你:(.*)</b>");
        Matcher matcher = pattern.matcher(body.replace(" ", ""));
        if (matcher.find()) {
            userName = matcher.group(1);
        }

        return userName;
    }

    /**
     * 获取用户页内容
     *
     * @param cookie
     * @return
     * @throws IOException
     */
    public FirstPageEntity getFirst(String cookie) throws IOException {
        //设置为 html 类型
        if (netHeaderUtil == null) netHeaderUtil = new NetHeaderUtil();
        netHeaderUtil.setContentType(NetHeaderUtil.ContentTypes.HtmlUtf8);
        netHeaderUtil.setHost(HOST);
        netHeaderUtil.setReferer(URL_LOGIN);
        if (cookie != null) netHeaderUtil.setCookies(cookie);

        // 请求网络
        Connection connect = connect(URL_FIRST, netHeaderUtil, Connection.Method.GET);
        Document document = connect.execute().parse();

        String html = document.outerHtml().replaceAll("\\s", ""); //删除所有空白字符
        html = html.replaceAll("&quot;", "\""); //还原双引号

        //////// 获取公告
        // 匹配公告
        String informRegex = "<ahref=\"(.*?)\"title=\"(.*?)\"target=\"_self\">(.*?)/a>";
        Pattern patternI = Pattern.compile(informRegex);
        Matcher matcherI = patternI.matcher(html);
        ArrayList<InformEntity> informEntities = new ArrayList<>();
        while (matcherI.find()) {
            InformEntity inform = new InformEntity();
            inform.setHref(matcherI.group(1));
            inform.setTitle(matcherI.group(2));
            informEntities.add(inform);
        }

        /////// 获取标题
        // 匹配整个标题块
        String titleBlockRegex = "<div(class=\"collapsed\")?>(.*?)</div>";
        Pattern pattern = Pattern.compile(titleBlockRegex);
        Matcher matcher = pattern.matcher(html);
        ArrayList<String> titleBlocks = new ArrayList<>();
        while (matcher.find()) {
            //System.out.println(matcher.group());
            titleBlocks.add(matcher.group());
        }

        //从标题中匹配标题文字，及子项
        ArrayList<CollapsedEntity> collapsedEntities = new ArrayList<>();
        for (String titleBlock : titleBlocks) {
            CollapsedEntity collapsed = new CollapsedEntity();

            // 匹配标题 （诸如：基本、事务……）
            String collapsedRegex = "<spanstyle=\"font-weight:bold;background:#7399E0\">(.*?)</span>";
            pattern = Pattern.compile(collapsedRegex);
            matcher = pattern.matcher(titleBlock);
            if (matcher.find()) collapsed.setTitle(matcher.group(1));

            //System.out.println(titleBlock);

            // 匹配子项列表（诸如：基本修改、家庭成员……）
            ArrayList<CollapsedEntity.OptionLinkEntity> optionLinkEntities = new ArrayList<>();
            String optionRegex = "javascript:(.*?)\\(\"(.*?)\",\"(.*?)\",\"(.*?)\",\"(.*?)\"\\)";
            pattern = Pattern.compile(optionRegex);
            matcher = pattern.matcher(titleBlock);
            while (matcher.find()) {
                CollapsedEntity.OptionLinkEntity optionLink = new CollapsedEntity.OptionLinkEntity();
                optionLink.setOptionId(matcher.group(1));
                optionLink.setLinkId(matcher.group(2));
                optionLink.setTitle(matcher.group(3));
                optionLink.setSemesterId(matcher.group(4));
                optionLink.setTmfl(matcher.group(5));
                optionLinkEntities.add(optionLink);
            }
            collapsed.setLinkEntities(optionLinkEntities);
            //其他必要信息 (暂时未完全匹配，后续如有需要，再行添加)
            collapsed.setSemester(document.getElementById("xq_xz").val());
            collapsed.setYear(document.getElementById("nd").val());

            //整合
            collapsedEntities.add(collapsed);
        }

        //整合
        FirstPageEntity firstPageEntity = new FirstPageEntity();
        firstPageEntity.setInformEntitiesl(informEntities);
        firstPageEntity.setCollapsedEntities(collapsedEntities);

        return firstPageEntity;
    }

    /**
     * 登录参数
     *
     * @return
     * @throws IOException
     */
    public LoginParamEntity getLoginParam() throws IOException {
        if (loginParam == null) initAlst();
        return loginParam;
    }

    /**
     * 通用请求
     *
     * @return
     */
    private Connection connect(String url, NetHeaderUtil netHeaderUtil, Connection.Method method) throws IOException {
        Connection connection = Jsoup.connect(url)
                .ignoreContentType(true)
                .ignoreHttpErrors(true)
                .headers(netHeaderUtil.getHeader())
                .timeout(2000)
                .method(method);

        return connection;
    }
}
