package cn.edu.ccibe.alst.uitls;

import java.io.UnsupportedEncodingException;

public class URLParamsUtil {

    /**
     * URL编码
     *
     * @param paramName 参数名
     * @param paramVal  参数值
     * @param symbol    是否添加 & 符合
     * @return
     */
    @Deprecated
    public static String encode(String paramName, String paramVal, boolean symbol) {

        StringBuffer stringBuffer = new StringBuffer(paramName).append("=").append(paramVal);
        if (symbol) stringBuffer.append("&");

        return stringBuffer.toString();
    }

    /**
     * URL编码
     *
     * @param paramName
     * @param paramVal
     * @param enc
     * @param symbol
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String encode(String paramName, String paramVal, String enc, boolean symbol)
            throws UnsupportedEncodingException {

        StringBuffer stringBuffer = new StringBuffer(paramName).append("=").append(paramVal);
        if (symbol) stringBuffer.append("&");

        return stringBuffer.toString();
    }
}
