package com.previntagestudios.skylinelugecarts;

import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.ArrayList;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

public interface CartDAO {

    @Update
    void updateCart(Cart cart);

    @Query("SELECT * FROM cartmanager")
    ArrayList<Cart> importCarts();
    @Insert
    void insertCartstoChecksheet();

    @Query("SELECT * FROM cartlist ")
}
