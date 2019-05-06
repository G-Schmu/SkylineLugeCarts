package com.previntagestudios.skylinelugecarts;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class CartHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private Context context;
    private Cart cart;
    private int isInRotation;
    private boolean isChecked;
    private boolean isMaintenance;
    private boolean isOverdue;

    private final TextView cartnum;


    public CartHolder (Context context, View itemView) {
        super(itemView);
        this.context = context;
        this.cartnum = itemView.findViewById(R.id.cartnum);
        itemView.setOnClickListener(this);
    }

    public void bindCart(Cart incart) {
        this.cart = incart;
        this.cartnum.setText(cart.getCartnum());
        isInRotation = incart.isIn_rotation();
        isChecked = incart.isChecked();
        isMaintenance = incart.isMaintenance();
        isOverdue = incart.isOverdue();
    }

    public int isInRotation() {
        return isInRotation;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public boolean isMaintenance() {
        return isMaintenance;
    }

    public boolean isOverdue() {
        return isOverdue;
    }

    @Override
    public void onClick (View v) {
    }
}
