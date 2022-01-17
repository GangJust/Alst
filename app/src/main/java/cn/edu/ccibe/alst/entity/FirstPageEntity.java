package cn.edu.ccibe.alst.entity;

import java.util.ArrayList;

public class FirstPageEntity {
    private ArrayList<InformEntity> informEntitiesl;
    private ArrayList<CollapsedEntity> collapsedEntities;

    public ArrayList<InformEntity> getInformEntitiesl() {
        return informEntitiesl;
    }

    public void setInformEntitiesl(ArrayList<InformEntity> informEntitiesl) {
        this.informEntitiesl = informEntitiesl;
    }

    public ArrayList<CollapsedEntity> getCollapsedEntities() {
        return collapsedEntities;
    }

    public void setCollapsedEntities(ArrayList<CollapsedEntity> collapsedEntities) {
        this.collapsedEntities = collapsedEntities;
    }
}
