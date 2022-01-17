package cn.edu.ccibe.alst.entity;

public class InformEntity {
    private String href; //公告链接
    private String title; //公告标题

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "InformEntity{" +
                "href='" + href + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}