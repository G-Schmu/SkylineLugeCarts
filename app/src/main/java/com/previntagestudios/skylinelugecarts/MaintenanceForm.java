package com.previntagestudios.skylinelugecarts;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "maintenance")
public class MaintenanceForm {
    @PrimaryKey
    @ColumnInfo(name = "formID")
    private String date;
    @ColumnInfo(name = "cartID")
    private int cartnum;
    private String reason;
    private String comment;
    //used to determine if the form is a new submission and needs to be saved, or loaded from before
    private boolean recent;

    public MaintenanceForm (int cartnum, String date, String reason, Boolean recent) {
        this.date = date;
        this.cartnum = cartnum;
        this.reason = reason;
        this.recent = recent;
    }

    public void addComment (String comment) {
        this.comment = comment;
    }

    public String getReason() {
        return reason;
    }

    public String getComment() {
        return comment;
    }

    public String getDate() {
        return date;
    }

    public Boolean isRecent () {
        return recent;
    }
}
