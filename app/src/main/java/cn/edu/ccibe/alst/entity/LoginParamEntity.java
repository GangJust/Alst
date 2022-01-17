package cn.edu.ccibe.alst.entity;

import java.net.URLEncoder;

public class LoginParamEntity {
    private String __VIEWSTATE;
    private String __VIEWSTATEGENERATOR;
    private String __VIEWSTATEENCRYPTED;
    private String userbh;
    private String vcode;
    private String cw;
    private String xzbz;
    private String pas2s;
    private String yxdm;

    public String get__VIEWSTATE() {
        return __VIEWSTATE;
    }

    public void set__VIEWSTATE(String __VIEWSTATE) {
        this.__VIEWSTATE = __VIEWSTATE;
    }

    public String get__VIEWSTATEGENERATOR() {
        return __VIEWSTATEGENERATOR;
    }

    public void set__VIEWSTATEGENERATOR(String __VIEWSTATEGENERATOR) {
        this.__VIEWSTATEGENERATOR = __VIEWSTATEGENERATOR;
    }

    public String get__VIEWSTATEENCRYPTED() {
        return __VIEWSTATEENCRYPTED;
    }

    public void set__VIEWSTATEENCRYPTED(String __VIEWSTATEENCRYPTED) {
        this.__VIEWSTATEENCRYPTED = __VIEWSTATEENCRYPTED;
    }

    public String getUserbh() {
        return userbh;
    }

    public void setUserbh(String userbh) {
        this.userbh = userbh;
    }

    public String getVcode() {
        return vcode;
    }

    public void setVcode(String vcode) {
        this.vcode = vcode;
    }

    public String getCw() {
        return cw;
    }

    public void setCw(String cw) {
        this.cw = cw;
    }

    public String getXzbz() {
        return xzbz;
    }

    public void setXzbz(String xzbz) {
        this.xzbz = xzbz;
    }

    public String getPas2s() {
        return pas2s;
    }

    public void setPas2s(String pas2s) {
        this.pas2s = pas2s;
    }

    public String getYxdm() {
        return yxdm;
    }

    public void setYxdm(String yxdm) {
        this.yxdm = yxdm;
    }

    @Override
    public String toString() {
        return "LoginEntity{" +
                "__VIEWSTATE='" + __VIEWSTATE + '\'' +
                ", __VIEWSTATEGENERATOR='" + __VIEWSTATEGENERATOR + '\'' +
                ", __VIEWSTATEENCRYPTED='" + __VIEWSTATEENCRYPTED + '\'' +
                ", userbh='" + userbh + '\'' +
                ", vcode='" + vcode + '\'' +
                ", cw='" + cw + '\'' +
                ", xzbz='" + xzbz + '\'' +
                ", pas2s='" + pas2s + '\'' +
                ", yxdm='" + yxdm + '\'' +
                '}';
    }

    public String getParamString() {
        return "__VIEWSTATE=" + URLEncoder.encode(__VIEWSTATE) +
                "&__VIEWSTATEGENERATOR=" + URLEncoder.encode(__VIEWSTATEGENERATOR) +
                "&__VIEWSTATEENCRYPTED=" + URLEncoder.encode(__VIEWSTATEENCRYPTED) +
                "&userbh=" + URLEncoder.encode(userbh) +
                "&vcode=" + URLEncoder.encode(vcode) +
                "&cw=" + URLEncoder.encode(cw) +
                "&xzbz=" + URLEncoder.encode(xzbz) +
                "&pas2s=" + URLEncoder.encode(pas2s) +
                "&yxdm=" + URLEncoder.encode(yxdm);
    }

}
