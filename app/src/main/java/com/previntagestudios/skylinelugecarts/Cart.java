package com.previntagestudios.skylinelugecarts;

import java.util.ArrayList;

public class Cart {
    private int cartnum;
    private int in_rotation;
    private boolean checked;
    private ArrayList<MaintenanceForm> history;
    private boolean maintenance;
    private boolean overdue;

    public Cart (int cartnum, int in_rotation) {
        this.cartnum = cartnum;
        this.in_rotation = in_rotation;
        checked = false;
        maintenance = false;
        overdue = false;
    }

    public String toString () {
        return cartnum + " R:" + in_rotation + " C:" + checked + " M:" + maintenance + " O:" + overdue;
    }

    public void setChecked (boolean checked) {
        this.checked = checked;
    }

    public void setMaintenance(boolean maintenance) {
        this.maintenance = maintenance;
    }

    public void setOverdue(boolean overdue) {
        this.overdue = overdue;
    }

    public void setHistory (ArrayList<MaintenanceForm>history) {
        this.history = history;
    }

    public void addMaintenance (MaintenanceForm mF) {
        history.add(mF);
    }

    public int getCartnum() {
        return cartnum;
    }

    public int isIn_rotation() {
        return in_rotation;
    }

    public boolean isChecked() {
        return checked;
    }

    public ArrayList<MaintenanceForm> getHistory() {
        return history;
    }

    public boolean isMaintenance() {
        return maintenance;
    }

    public boolean isOverdue() {
        return overdue;
    }
}
