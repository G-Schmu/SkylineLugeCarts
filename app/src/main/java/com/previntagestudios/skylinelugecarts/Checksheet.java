package com.previntagestudios.skylinelugecarts;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "checksheet")
public class Checksheet {
    @PrimaryKey
    @ColumnInfo(name = "checksheetID")
    private String checksheetdate;

    @ColumnInfo
    private String pmstaff;
    @ColumnInfo
    private String amstaff;
    @ColumnInfo
    private CartListFragment.Boolean locked;

    public Checksheet(String checksheetdate) {
        this.checksheetdate = checksheetdate;
    }

    public String getChecksheetdate() {
        return checksheetdate;
    }

    public String getPmstaff() {
        return pmstaff;
    }

    public void setPmstaff(String pmstaff) {
        this.pmstaff = pmstaff;
    }

    public String getAmstaff() {
        return amstaff;
    }

    public void setAmstaff(String amstaff) {
        this.amstaff = amstaff;
    }

    public CartListFragment.Boolean getLocked() {
        return locked;
    }

    public void setLocked(CartListFragment.Boolean locked) {
        this.locked = locked;
    }
}
