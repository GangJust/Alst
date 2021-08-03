package cn.edu.swsm.alst.entity;

import android.graphics.Bitmap;

public class GridCardEntity {
    private Bitmap icon;
    private String title;

    public GridCardEntity(Bitmap icon, String title) {
        this.icon = icon;
        this.title = title;
    }

    public Bitmap getIcon() {
        return icon;
    }

    public void setIcon(Bitmap icon) {
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
