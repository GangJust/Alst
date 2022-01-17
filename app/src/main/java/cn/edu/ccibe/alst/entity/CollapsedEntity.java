package cn.edu.ccibe.alst.entity;

import java.util.ArrayList;

public class CollapsedEntity {
    private String title;
    private ArrayList<OptionLinkEntity> linkEntities;
    private String semester; //xq_xz 学期学制
    private String year; //nd 年度

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ArrayList<OptionLinkEntity> getLinkEntities() {
        return linkEntities;
    }

    public void setLinkEntities(ArrayList<OptionLinkEntity> linkEntities) {
        this.linkEntities = linkEntities;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    @Override
    public String toString() {
        return "CollapsedEntity{" +
                "title='" + title + '\'' +
                ", linkEntities=" + linkEntities +
                ", semester='" + semester + '\'' +
                ", year='" + year + '\'' +
                '}';
    }

    /**
     * 子选项，链接
     */
    public static class OptionLinkEntity {
        private String optionId; // 暂且叫做 操作id吧， xxdm%d
        private String linkId; // 暂且叫做 链接id吧， a即km
        private String title; // b
        private String semesterId; //应该是学期id吧 c，1=第一学期, 2=第二学期;
        private String tmfl; //我实在猜不出来 d;

        public String getOptionId() {
            return optionId;
        }

        public void setOptionId(String optionId) {
            this.optionId = optionId;
        }

        public String getLinkId() {
            return linkId;
        }

        public void setLinkId(String linkId) {
            this.linkId = linkId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getSemesterId() {
            return semesterId;
        }

        public void setSemesterId(String semesterId) {
            this.semesterId = semesterId;
        }

        public String getTmfl() {
            return tmfl;
        }

        public void setTmfl(String tmfl) {
            this.tmfl = tmfl;
        }


        @Override
        public String toString() {
            return "OptionLinkEntity{" +
                    "optionId='" + optionId + '\'' +
                    ", linkId='" + linkId + '\'' +
                    ", title='" + title + '\'' +
                    ", semesterId='" + semesterId + '\'' +
                    ", tmfl='" + tmfl + '\'' +
                    '}';
        }
    }
}
