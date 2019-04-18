package com.previntagestudios.skylinelugecarts;

import android.app.Activity;
import android.os.Bundle;

public class ManageCartsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_managecarts);

        MainActivity mainActivity = (MainActivity)getParent();
    }

}

