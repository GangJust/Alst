package cn.edu.ccibe.alst.net;

import java.util.HashMap;
import java.util.Map;

/**
 * 不完善的 HeaderUtil
 *
 * @author Gang
 */
public class NetHeaderUtil {
    private Map<String, String> header;

    public NetHeaderUtil() {
        initReqHeader();
    }

    private void initReqHeader() {
        if (header == null) {
            header = new HashMap<>();
            header.put("Accept-Language", "zh-CN,zh;q=0.9");
            header.put("Accept-Encoding", "gzip, deflate");
            header.put("Connection", "keep-alive");
            header.put("User-Agent", UserAgents.PhoneQQ_8_5_5);
            header.put("X-Requested-With", "cn.edu.swsm.alst");
        }
    }

    public NetHeaderUtil setAccept(String accept) {
        setOther("Accept", accept);
        return this;
    }

    public NetHeaderUtil setAcceptEncoding(String acceptEncoding) {
        setOther("Accept-Encoding", acceptEncoding);
        return this;
    }

    public NetHeaderUtil setAcceptLanguage(String acceptLanguage) {
        setOther("Accept-Language", acceptLanguage);
        return this;
    }

    public NetHeaderUtil setConnection(String connection) {
        setOther("Connection", connection);
        return this;
    }

    public NetHeaderUtil setContentType(String contentType) {
        setOther("Content-Type", contentType);

        return this;
    }

    public NetHeaderUtil setCookies(Map<String, String> cookies) {
        //遍历重组
        StringBuffer stringBuffer = new StringBuffer();
        for (Map.Entry<String, String> entry : cookies.entrySet()) {
            String temp = entry.getKey() + "=" + entry.getValue() + "; ";
            stringBuffer.append(temp);
        }
        //删除最后一个分号
        stringBuffer.deleteCharAt(stringBuffer.length() - 2);
        setOther("Cookie", stringBuffer.toString().trim());

        return this;
    }

    public NetHeaderUtil setCookies(String cookie) {
        setOther("Cookie", cookie);

        return this;
    }

    public NetHeaderUtil appendCookie(String key, String value) {

        StringBuffer stringBuffer = new StringBuffer();

        // Cookie 不为空则在末尾添加分号
        String cookies = header.get("Cookie");
        if (cookies != null || !cookies.trim().equals("")) {
            stringBuffer.append(cookies).append("; ");
        }
        //组合Cookie
        stringBuffer.append(key).append("=").append(value);

        setOther("Cookie", stringBuffer.toString().trim());

        return this;
    }

    public NetHeaderUtil appendCookie(String cookie) {

        StringBuffer stringBuffer = new StringBuffer();

        // Cookie 不为空则在末尾添加分号
        String cookies = header.get("Cookie");
        if (cookie != null || !cookie.trim().equals("")) {
            stringBuffer.append(cookie).append("; ");
        }
        //组合Cookie
        stringBuffer.append(cookie);

        setOther("Cookie", stringBuffer.toString().trim());

        return this;
    }

    public NetHeaderUtil setHost(String host) {
        setOther("Host", host);
        return this;
    }

    public NetHeaderUtil setReferer(String referer) {
        setOther("Referer", referer);
        return this;
    }

    public NetHeaderUtil setUserAgent(String userAgent) {
        setOther("User-Agent", userAgent);
        return this;
    }

    public NetHeaderUtil setOther(String name, String value) {
        header.put(name, value);
        return this;
    }

    public Map<String, String> getHeader() {
        return header;
    }

    /**
     * 部分常用 User-Agent
     */
    public static class UserAgents {
        /// 数字代表版本号 _1_0

        //常用软件
        public static final String PhoneQQ_8_5_5 = "Mozilla/5.0 (Linux; Android 11; Mi 10 Pro Build/RKQ1.200826.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/77.0.3865.120 MQQBrowser/6.2 TBS/045426 Mobile Safari/537.36 V1_AND_SQ_8.5.5_1630_YYB_D A_8050500 QQ/8.5.5.5105 NetType/4G WebP/0.3.0 Pixel/1080 StatusBarHeight/91 SimpleUISwitch/1 QQTheme/2921 InMagicWin/0";
        public static final String PhoneWeChat_8_0_11 = "Mozilla/5.0 (Linux; Android 11; Mi 10 Pro Build/RKQ1.200826.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/78.0.3904.62 XWEB/2759 MMWEBSDK/201201 Mobile Safari/537.36 MMWEBID/8438 MicroMessenger/8.0.11.1980(0x28000037) Process/toolsmp WeChat/arm64 Weixin NetType/4G Language/zh_CN ABI/arm64";
        public static final String PhoneWeiBo_11_1_3 = "Mozilla/5.0 (Linux; Android 11; Mi 10 Pro Build/RKQ1.200826.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/86.0.4240.185 Mobile Safari/537.36 Weibo (Xiaomi-Mi 10 Pro__weibo__11.1.3__android__android11)";
        public static final String PhoneDouYin_14_6_2 = "com.ss.android.ugc.aweme/140603 (Linux; U; Android 11; zh_CN; Mi 10 Pro; Build/RKQ1.200826.002; Cronet/TTNetVersion:3078b6b4 2021-01-18 QuicVersion:47946d2a 2020-10-14)";

        //常见浏览器
        public static final String PhoneQQBrowser_10_8_0 = "Mozilla/5.0 (Linux; U; Android 11; zh-cn; Mi 10 Pro Build/RKQ1.200826.002) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/66.0.3359.126 MQQBrowser/10.8 Mobile Safari/537.36";
        public static final String PhoneQuarkBrowser_4_6_2 = "Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_3_2 like Mac OS X; en-us) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8H7 Safari/6533.18.5 Quark/4.6.2.161";
        public static final String PhoneMiUiBrowser_13_8_13 = "Mozilla/5.0 (Linux; U; Android 11; zh-cn; Mi 10 Pro Build/RKQ1.200826.002) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/79.0.3945.147 Mobile Safari/537.36 XiaoMi/MiuiBrowser/13.8.13";
        //Dalvik
        public static final String Android11_MI_10_Pro_Dalvik = "Dalvik/2.1.0 (Linux; U; Android 11; Mi 10 Pro Build/RKQ1.200826.002)";
    }

    /**
     * 常用 Content-Type
     */
    public static class ContentTypes {
        public static final String FromApplication = "application/x-www-form-urlencoded";
        public static final String FormMultipart = "multipart/form-data";
        public static final String Json = "application/json";
        public static final String Html = "text/html";
        public static final String HtmlUtf8 = "text/html; charset=utf-8";
        public static final String JavaScript = "application/x-javascript";
        public static final String Css = "text/css";

        public static final String Jpeg = "image/Jpeg";
        public static final String JpegUtf8 = "image/Jpeg; charset=utf-8";


    }
}
