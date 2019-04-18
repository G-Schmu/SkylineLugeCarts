package com.previntagestudios.skylinelugecarts;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Transition;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewPropertyAnimator;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    static private SimpleDateFormat format = new SimpleDateFormat("EEEE MMMM d, yyyy");
    static Calendar dateSelected = Calendar.getInstance();
    public CartListAdapter cartListAdapter;
    static private Button dateview;
    CalendarDialog calendardialog;
    private DatabaseHelper dbhelper = new DatabaseHelper(this);
    static private ArrayList<Cart> selectedCarts;
    private boolean dataChanged;
    private String pmcheckerSave;
    private String amcheckerSave;
    private boolean ischeckfiltered;
    private boolean ismaintfiltered;
    private boolean isremainfiltered;
    private ArrayList<Cart> filteredCarts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        selectedCarts = new ArrayList<Cart>();
        filteredCarts = new ArrayList<Cart>();

        //date handler stuff
        dateview = findViewById(R.id.dateview);
        dateview.setText(format.format(dateSelected.getTime()));
        calendardialog = new CalendarDialog();
        checkforSheet();

        dateview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dataChanged)
                    saveCheckSheet();
                calendardialog.show(getFragmentManager(),"calendar");
            }

        });
        loadcartcount();
        dbhelper.onUpgrade(dbhelper.getReadableDatabase(),1,2);


        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        Intent managecartsIntent = new Intent(getApplicationContext(),ManageCartsActivity.class);

                        switch (menuItem.getItemId()) {
                            case R.id.eveningcount:
                                Toast.makeText(getBaseContext(), "Evening Count (Coming Soon)", Toast.LENGTH_SHORT).show();
                                break;
                            case R.id.maintenancelist:
                                Toast.makeText(getBaseContext(), "Maintenance List (Coming Soon)", Toast.LENGTH_SHORT).show();
                                break;
                            case R.id.managecartsactivity:
                                startActivity(managecartsIntent);
                                break;
                            default:
                                Toast.makeText(getBaseContext(), "case default", Toast.LENGTH_SHORT).show();
                                break;
                        }

                        return true;
                    }
                });
        EditText pmstaff = findViewById(R.id.pmchecker);
        EditText amstaff = findViewById(R.id.amchecker);
        TextWatcher staffentrycheck = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                dataChanged = true;
            }
        };
        pmstaff.addTextChangedListener(staffentrycheck);
        amstaff.addTextChangedListener(staffentrycheck);
    }

    @Override
    public void onResume() {
        super.onResume();

        //List Jumping From Number
        final EditText searchbox = findViewById(R.id.jumptotext);
        searchbox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                ListView cartlist = findViewById(R.id.cartlistcontainer);
                int cartnuminput;
                try {
                    cartnuminput = Integer.parseInt(searchbox.getText().toString());
                }
                catch (NumberFormatException nfe) {
                    cartnuminput = 1;
                }
                int listnum = findCart(cartnuminput);
                cartlist.setSelection(listnum - 1);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        //End of list jumping from number

    }

    @Override
    public void onStop () {
        super.onStop();
        saveCheckSheet();
    }


    public void startCheckSheet (View view) {
        //Inserting metaSheet
        SQLiteDatabase lugedb = dbhelper.getReadableDatabase();
        String newdate = format.format(dateSelected.getTime());
        ContentValues values = new ContentValues();
        values.put("formID",newdate);
        values.put("locked",0);
        long metacheck = lugedb.insert("metaSheet","formID",values);

        //Inserting carts
        String[] columns = {"cartID","in_rotation"};
        Cursor cartsC = lugedb.query("carts",columns,null,null,null,null,null);
        ContentValues cartsCV = new ContentValues();
        cartsCV.put("formID",newdate);
        cartsC.moveToFirst();
        long cartCheck = -1;
        while(!cartsC.isAfterLast()) {
            DatabaseUtils.cursorStringToContentValues(cartsC, "cartID", cartsCV, "cartID");
            DatabaseUtils.cursorIntToContentValues(cartsC,"in_rotation",cartsCV,"in_rotation");
            cartCheck = lugedb.insert("checklist","cartID, in_rotation",cartsCV);
            cartsC.moveToNext();
        }

        if (metacheck == -1 || cartCheck == -1) {
            Toast.makeText(this, "DB Entry Error", Toast.LENGTH_LONG).show();
            lugedb.delete("metasheet","formID = '"+newdate+"'", null);
            lugedb.delete("checklist","formID = '"+newdate+"'", null);
        }
        else {
            Toast.makeText(this, "New Checksheet Created", Toast.LENGTH_SHORT).show();
            findViewById(R.id.createchecksheet).setVisibility(View.GONE);
        }

        findViewById(R.id.locked).setVisibility(View.GONE);
        cartsC.close();
        lugedb.close();
        checkforSheet();
    }

    private void checkforSheet () {
        //setting list view from DB
        String date = format.format(dateSelected.getTime());
        findViewById(R.id.locked).setVisibility(View.GONE);
        findViewById(R.id.createchecksheet).setVisibility(View.GONE);
        findViewById(R.id.cartlistcontainer).setVisibility(View.GONE);
        EditText amchecker = findViewById(R.id.amchecker);
        EditText pmchecker = findViewById(R.id.pmchecker);
        //check for created sheet
        SQLiteDatabase lugedb = dbhelper.getReadableDatabase();
        String querytest = "SELECT * FROM metaSheet WHERE formID = '" + date + "'";
        Cursor metacursor = lugedb.rawQuery(querytest,null);
        //if sheet does not exist display prompt
        if (metacursor.getCount()==0) {
            findViewById(R.id.createchecksheet).setVisibility(View.VISIBLE);
            selectedCarts.clear();
            loadcartcount();
            amcheckerSave = "";
            pmcheckerSave = "";
            amchecker.setText(amcheckerSave);
            pmchecker.setText(pmcheckerSave);
        }
        //Else retrieve and display carts that belong to that checksheet
        else {
            dataChanged = false;
            ListView cartlist = findViewById(R.id.cartlistcontainer);
            cartlist.setVisibility(View.VISIBLE);
            loadCarts();
            loadcartcount();
            metacursor.moveToFirst();
            if (metacursor.getInt(3)==1)
                findViewById(R.id.locked).setVisibility(View.VISIBLE);
        }
        lugedb.close();
    }

    private void loadCarts () {
        if (selectedCarts!=null)
            selectedCarts.clear();
        SQLiteDatabase lugedb = dbhelper.getReadableDatabase();
        String date = format.format(dateSelected.getTime());
        String cartquery = "SELECT *, cartID as _id FROM checklist WHERE formID = '" + date + "'";
        Cursor cartcursor = lugedb.rawQuery(cartquery, null);
        String historyquery = "SELECT *, cartID as _id FROM maintenance";
        Cursor historycursor = lugedb.rawQuery(historyquery,null);
        if (cartcursor.moveToFirst()) {
            while (!cartcursor.isAfterLast()) {
                Cart temp = new Cart(cartcursor.getInt(1), cartcursor.getInt(2));
                temp.setChecked(Boolean.getBoolean(cartcursor.getInt(3)));
                temp.setOverdue(Boolean.getBoolean(cartcursor.getInt(4)));
                temp.setMaintenance(Boolean.getBoolean(cartcursor.getInt(5)));
                ArrayList<MaintenanceForm> temphistory = new ArrayList<>();
                //check that there is anything in the maintenance history queue
                if (historycursor.getCount()>0) {
                    historycursor.moveToFirst();
                    //loop through cursor
                    while(!historycursor.isAfterLast()) {
                        //if the current history cartnum is the same as the current cartnum, make a maintenance form and add it to the cart's history
                        if(historycursor.getInt(1)==cartcursor.getPosition()+1) {
                            MaintenanceForm tempMF = new MaintenanceForm(historycursor.getInt(1),historycursor.getString(0),historycursor.getString(3),false);
                            //check for comments
                            if(historycursor.getString(4)!=null)
                                tempMF.addComment(historycursor.getString(4));
                            temphistory.add(tempMF);
                        }
                        historycursor.moveToNext();
                    }
                }
                temp.setHistory(temphistory);
                selectedCarts.add(temp);
                cartcursor.moveToNext();
            }
        }
        else {
            Toast.makeText(this,"Failed to load check sheet",Toast.LENGTH_LONG);
        }
        cartListAdapter = new CartListAdapter(this,R.layout.cart_container,selectedCarts);
        ListView cartListContainer = findViewById(R.id.cartlistcontainer);
        cartListContainer.setAdapter(cartListAdapter);


        EditText amchecker = findViewById(R.id.amchecker);
        EditText pmchecker = findViewById(R.id.pmchecker);
        String metaquery = "SELECT * FROM metaSheet WHERE formID = '" + date + "'";
        Cursor metacursor = lugedb.rawQuery(metaquery,null);
        if (metacursor.moveToFirst()) {
            String amname = metacursor.getString(1);
            String pmname = metacursor.getString(2);
            if (amname != null) {
                amcheckerSave = amname;
                amchecker.setText(amname);
            }
            if (pmname != null) {
                pmcheckerSave = pmname;
                pmchecker.setText(pmname);
            }
        }
        lugedb.close();
    }

    private void saveCheckSheet () {
        SQLiteDatabase lugedb = dbhelper.getWritableDatabase();
        String currdate = format.format(dateSelected.getTime());

        //save carts
        ContentValues cartCV = new ContentValues();
        lugedb.beginTransaction();
        for (int i = 0; i<selectedCarts.size(); i++) {
            Cart temp = selectedCarts.get(i);
            cartCV.put("formID", currdate);
            cartCV.put("cartID", temp.getCartnum());
            cartCV.put("in_rotation", Boolean.getBoolean(temp.isIn_rotation()));
            cartCV.put("checked", temp.isChecked());
            cartCV.put("overdue", temp.isOverdue());
            cartCV.put("maintenance", temp.isMaintenance());
            lugedb.update("checklist", cartCV, "formID = '" + currdate + "' AND cartID = " + temp.getCartnum(), null);
        }
        lugedb.setTransactionSuccessful();
        lugedb.endTransaction();

        //save maintenance
        ContentValues historyCV = new ContentValues();
        lugedb.beginTransaction();
        for (int i = 0; i<selectedCarts.size(); i++) {
            if (selectedCarts.get(i).getHistory().size()>0) {
                ArrayList<MaintenanceForm> mfal = selectedCarts.get(i).getHistory();
                for (int j = 0; j < mfal.size(); j++) {
                    MaintenanceForm mf = mfal.get(j);
                    if (mf.isRecent()) {
                        historyCV.put("date", mf.getDate());
                        historyCV.put("formID", currdate);
                        historyCV.put("cartID", selectedCarts.get(i).getCartnum());
                        historyCV.put("reason", mf.getReason());
                        if (mf.getComment() != null)
                            historyCV.put("comment", mf.getComment());
                        lugedb.insert("maintenance","",historyCV);
                    }
                }
            }
        }
        lugedb.setTransactionSuccessful();
        lugedb.endTransaction();

        //save metadata
        EditText amstaff = findViewById(R.id.amchecker);
        String amstaffname = amstaff.getText().toString();
        EditText pmstaff = findViewById(R.id.pmchecker);
        String pmstaffname = pmstaff.getText().toString();
        if (amstaffname!=null||pmstaffname!=null) {
            ContentValues metaCV = new ContentValues();
            if (amstaffname != null)
                metaCV.put("amstaff", amstaffname);
            if (pmstaffname != null)
                metaCV.put("pmstaff", pmstaffname);
            lugedb.update("metaSheet", metaCV, "formID = '" + currdate + "'", null);
        }
        if (dateSelected.before(Calendar.getInstance().getTime())) {
            ContentValues lockedCV = new ContentValues();
            lockedCV.put("locked", true);
            lugedb.update("metaSheet", lockedCV, "formID = '" + currdate + "'", null);
        }
        lugedb.close();
    }

    public class CartListAdapter extends ArrayAdapter<Cart> {

        ArrayList<Cart> carts;
        private Context context;

        public CartListAdapter (Context context, int layout, ArrayList<Cart> carts) {
            super(context,layout,carts);
            this.context = context;
            this.carts = carts;
        }

        public void setCarts (ArrayList<Cart> carts) {
            this.carts = carts;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View mainview = convertView;

            if(mainview==null){
                mainview = LayoutInflater.from(context).inflate(R.layout.cart_container,parent,false);
            }

            Cart currcart = carts.get(position);
            TextView cartnumtext = mainview.findViewById(R.id.cartnum);
            cartnumtext.setText(String.valueOf(currcart.getCartnum()));
            ImageButton indicator = mainview.findViewById(R.id.indicator);
            ImageButton maintenance = mainview.findViewById(R.id.maintenance);

            if (currcart.isIn_rotation()==1) {
                mainview.setBackgroundColor(getResources().getColor(R.color.white));
                indicator.setEnabled(true);
                maintenance.setEnabled(true);
            }
            else {
                mainview.setBackgroundColor(getResources().getColor(R.color.glassdark));
                indicator.setEnabled(false);
                maintenance.setEnabled(false);
            }

            if (currcart.isOverdue())
                mainview.setBackgroundColor(getResources().getColor(R.color.cartoverdue));
            if (currcart.isChecked())
                mainview.setBackgroundColor(getResources().getColor(R.color.cartchecked));
            if (currcart.isMaintenance())
                mainview.setBackgroundColor(getResources().getColor(R.color.cartmaintenance));


            return mainview;

        }
    }

    public static class Boolean {
        public static boolean getBoolean(int num) {
            if (num == 1)
                return true;
            else
                return false;
        }
    }

    public void tomorrow (View view) {
        if (dataChanged)
            saveCheckSheet();
        dateSelected.add(dateSelected.DAY_OF_YEAR,1);
        dateview.setText(format.format(dateSelected.getTime()));
        checkforSheet();
    }
    public void yesterday (View view) {
        if (dataChanged)
            saveCheckSheet();
        dateSelected.add(dateSelected.DAY_OF_YEAR,-1);
        dateview.setText(format.format(dateSelected.getTime()));
        checkforSheet();
    }

    private void clearFilters (String view) {
        if (view != "maint")
            ismaintfiltered = false;
        if (view != "checked")
            ischeckfiltered = false;
        if (view != "remain")
            isremainfiltered = false;
        LinearLayout remainView = findViewById(R.id.filterRemain);
        LinearLayout checkedView = findViewById(R.id.filterChecked);
        LinearLayout maintView = findViewById(R.id.filterMaint);
        maintView.setBackgroundColor(getResources().getColor(R.color.cartmaintenance));
        checkedView.setBackgroundColor(getResources().getColor(R.color.cartchecked));
        remainView.setBackgroundColor(getResources().getColor(R.color.lightGrey));

        maintView.setTranslationZ(0);
        checkedView.setTranslationZ(0);
        remainView.setTranslationZ(0);

        //TODO optimize filtering color algorithm
    }

    public void filterMaint (View view) {
        ListView cartListView = findViewById(R.id.cartlistcontainer);
        clearFilters("maint");
        if (selectedCarts != null) {
            if (!ismaintfiltered) {
                filteredCarts.clear();
                for (int i = 0; i < selectedCarts.size(); i++)
                    if (selectedCarts.get(i).isMaintenance())
                        filteredCarts.add(selectedCarts.get(i));
                cartListAdapter = new CartListAdapter(this, R.id.cartlistcontainer, filteredCarts);
                ismaintfiltered = true;
                view.setTranslationZ(2);
                LinearLayout remainView = findViewById(R.id.filterRemain);
                LinearLayout checkedView = findViewById(R.id.filterChecked);
                checkedView.setBackgroundColor(getResources().getColor(R.color.grey));
                remainView.setBackgroundColor(getResources().getColor(R.color.grey));
            }
            else {
                cartListAdapter = new CartListAdapter(this, R.id.cartlistcontainer, selectedCarts);
                ismaintfiltered = false;
            }
            cartListView.setAdapter(cartListAdapter);
        }
    }

    public void filterChecked (View view) {
        ListView cartListView = findViewById(R.id.cartlistcontainer);
        clearFilters("checked");
        if (selectedCarts != null) {
            if (!ischeckfiltered) {
                filteredCarts.clear();
                for (int i = 0; i < selectedCarts.size(); i++)
                    if (selectedCarts.get(i).isChecked())
                        filteredCarts.add(selectedCarts.get(i));
                cartListAdapter = new CartListAdapter(this, R.id.cartlistcontainer, filteredCarts);
                ischeckfiltered=true;
                view.setTranslationZ(2);
                LinearLayout remainView = findViewById(R.id.filterRemain);
                LinearLayout maintView = findViewById(R.id.filterMaint);
                maintView.setBackgroundColor(getResources().getColor(R.color.grey));
                remainView.setBackgroundColor(getResources().getColor(R.color.grey));
            }
            else {
                cartListAdapter = new CartListAdapter(this, R.id.cartlistcontainer, selectedCarts);
                ischeckfiltered = false;
            }
            cartListView.setAdapter(cartListAdapter);
        }
    }

    public void filterRemain (View view) {
        ListView cartListView = findViewById(R.id.cartlistcontainer);
        clearFilters("remain");
        if (selectedCarts != null) {
            if (!isremainfiltered) {
                filteredCarts.clear();
                for (int i = 0; i < selectedCarts.size(); i++)
                    if (!selectedCarts.get(i).isChecked()&&!selectedCarts.get(i).isMaintenance())
                        filteredCarts.add(selectedCarts.get(i));
                cartListAdapter = new CartListAdapter(this, R.id.cartlistcontainer, filteredCarts);
                isremainfiltered = true;
                view.setTranslationZ(2);

                LinearLayout checkedView = findViewById(R.id.filterChecked);
                LinearLayout maintView = findViewById(R.id.filterMaint);
                maintView.setBackgroundColor(getResources().getColor(R.color.grey));
                checkedView.setBackgroundColor(getResources().getColor(R.color.grey));
            }
            else {
                cartListAdapter = new CartListAdapter(this, R.id.cartlistcontainer, selectedCarts);
                isremainfiltered = false;
            }
            cartListView.setAdapter(cartListAdapter);
        }
    }

    public void cartDialog (View view) {
        ListView list = findViewById(R.id.cartlistcontainer);
        int cartnum = list.getPositionForView(view);
        Cart temp = getCartAt(cartnum);
        Bundle args = new Bundle();
        args.putInt("cartnum",temp.getCartnum());
        cartInfoDialog cartDialog = new cartInfoDialog();
        cartDialog.setArguments(args);
        cartDialog.show(getFragmentManager(),"cartDialog");
    }

    public void cartMaintenance (View view) {
        //collect data to send to fragment
        dataChanged = true;
        ListView list = findViewById(R.id.cartlistcontainer);
        int cartnum = list.getPositionForView(view);
        Cart temp = getCartAt(cartnum);

        //create fragment and set arguments
        Bundle args = new Bundle();
        args.putInt("cartnum",temp.getCartnum());
        cartMaintenanceDialog checkCart = new cartMaintenanceDialog();
        checkCart.setArguments(args);

        checkCart.show(getFragmentManager(),"Cart "+cartnum);
        cartListAdapter.notifyDataSetChanged();
    }

    public void cartPassed (View view) {
        dataChanged = true;
        ListView list = findViewById(R.id.cartlistcontainer);
        int cartnum = list.getPositionForView(view);
        Cart temp = getCartAt(cartnum);
        if (temp.isMaintenance()) {
            temp.setMaintenance(false);
            ArrayList<MaintenanceForm> history = temp.getHistory();
            for (int i = 0; i<history.size(); i++)
                if (history.get(i).isRecent())
                    history.remove(i);
        }
        if (temp.isChecked())
            temp.setChecked(false);
        else
            temp.setChecked(true);
        loadcartcount();
        cartListAdapter.notifyDataSetChanged();
    }

    private void loadcartcount() {
        int amtremainingcarts = 0;
        int amtcheckedcarts = 0;
        int amtmaintenancecarts = 0;
        for (int i = 0; i < selectedCarts.size(); i++) {
            Cart temp = selectedCarts.get(i);
            if (temp.isIn_rotation()==1) {
                if (temp.isChecked())
                    amtcheckedcarts++;
                else if (temp.isMaintenance())
                    amtmaintenancecarts++;
                else
                    amtremainingcarts++;
            }
        }
        TextView remainingcarts = findViewById(R.id.remainingcarts);
        remainingcarts.setText(String.valueOf(amtremainingcarts));
        TextView checkedcarts = findViewById(R.id.checkedcarts);
        checkedcarts.setText(String.valueOf(amtcheckedcarts));
        TextView maintenancecarts = findViewById(R.id.maintenancecarts);
        maintenancecarts.setText(String.valueOf(amtmaintenancecarts));
    }

    private Cart getCartAt(int position) {
        Cart cart;
        if (isremainfiltered||ismaintfiltered||ischeckfiltered)
            cart = filteredCarts.get(position);
        else
            cart = selectedCarts.get(position);
        return cart;
    }

    private int findCart(int cartnum) {
        int cart = 0;
        if (isremainfiltered||ismaintfiltered||ischeckfiltered) {
            for (int i = filteredCarts.size()-1; i >= 0; i--)
                if (filteredCarts.get(i).getCartnum()==cartnum)
                    cart = filteredCarts.get(i).getCartnum();
        }
        else
            for (int i = selectedCarts.size()-1; i >= 0; i--)
                if (selectedCarts.get(i).getCartnum()==cartnum)
                    cart = selectedCarts.get(i).getCartnum();
        return cart;
    }

    //adds print button to top menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.actionbar_menu, menu);
        return true;
    }

    //handles print button press
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.print_button) {
            Toast.makeText(MainActivity.this, "Printing Page", Toast.LENGTH_LONG).show();
            doWebViewPrint();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static class CalendarDialog extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            int year = dateSelected.get(Calendar.YEAR);
            int month = dateSelected.get(Calendar.MONTH);
            int day = dateSelected.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            dateSelected.set(year,month,day);
            dateview.setText(format.format(dateSelected.getTime()));
            ((MainActivity)getActivity()).checkforSheet();
        }
    }

    public class DatabaseHelper extends SQLiteOpenHelper {

        private static final String DB_NAME = "SKYLINELUGECARTS";
        private static final int DATABASE_VERSION = 1;

        private static final String checklistTable = "checklist";
        private static final String cartsTable = "carts";
        private static final String metaSheetTable = "metaSheet";
        private static final String maintenance = "maintenance";

        public DatabaseHelper (Context context) {
            super(context, DB_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase database) {

            database.execSQL("create table " + metaSheetTable + " (formID string PRIMARY KEY, amstaff string, pmstaff string, locked boolean) ");
            database.execSQL("create table " + cartsTable + " (cartID int NOT NULL PRIMARY KEY, in_rotation boolean NOT NULL) ");
            database.execSQL("create table " + checklistTable + " (formID string NOT NULL, cartID int NOT NULL, in_rotation boolean NOT NULL, checked boolean default 0, " +
                    "overdue boolean default 0, maintenance boolean default 0, reason String, FOREIGN KEY(formID) REFERENCES metaSheet(formID), FOREIGN KEY(cartID) REFERENCES carts(cartID))");
            database.execSQL("create table " + maintenance + " (date string NOT NULL PRIMARY KEY, cartID int NOT NULL, formID int, reason string, comment string, " +
                    "FOREIGN KEY(cartID) REFERENCES carts(cartID), FOREIGN KEY(formID) REFERENCES metaSheet(formID))");

            for(int i = 1; i<326; i++) {
                database.execSQL("INSERT INTO " + cartsTable + " VALUES(" + i + ", 1)" );
            }
            ContentValues removefromrotation = new ContentValues();
            removefromrotation.put("in_rotation",false);
            int[] cartsoutofrotation = {1,8,14,23,34,46,49,53,67,81,84,87,
                    94,98,101,102,103,105,107,108,109,110,111,112,113,114,115,116,117,118,119,120,121,122,124,125,126,127,129,130,131,
                    133,134,135,137,138,139,140,144,147,159,162,169,171,172,175,176,177,178,179,180,181,182,183,184,185,186,187,188,189,190,191,192,193,194,195,
                    196,197,198,199,200,201,215,237,238,251,269,272,275,319};
            for(int i=0; i<cartsoutofrotation.length; i++)
                database.execSQL("UPDATE carts SET in_rotation = 0 WHERE cartID = "+cartsoutofrotation[i]);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion==DATABASE_VERSION) {
                int[] cartsoutofrotation = {1,8,14,23,34,46,49,53,67,81,84,87,
                        94,98,101,102,103,105,107,108,109,110,111,112,113,114,115,116,117,118,119,120,121,122,124,125,126,127,129,130,131,
                        133,134,135,137,138,139,140,144,147,159,162,169,171,172,175,176,177,178,179,180,181,182,183,184,185,186,187,188,189,190,191,192,193,194,195,
                        196,197,198,199,200,201,215,237,238,251,269,272,275,319};
                for(int i=0; i<cartsoutofrotation.length; i++)
                    db.execSQL("UPDATE carts SET in_rotation = 0 WHERE cartID = "+cartsoutofrotation[i]);
            }
        }

    }

    public CartListAdapter getCartListAdapter () {
        return cartListAdapter;
    }

    public static class cartMaintenanceDialog extends DialogFragment {

        MainActivity mainActivity;

        @Override
        public AlertDialog onCreateDialog(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setStyle(DialogFragment.STYLE_NORMAL,R.style.FullScreenDialogStyle);
            mainActivity = (MainActivity)getActivity();
            LayoutInflater li = LayoutInflater.from(mainActivity);

            Bundle args = getArguments();
            final int cartnum = args.getInt("cartnum");

            AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
            builder.setTitle("Cart " + (cartnum) + " Maintenance");

            final View cartDialogView = li.inflate(R.layout.maintenance_dialog,null);
            builder.setView(cartDialogView);
            /*
            final Spinner options = cartDialogView.findViewById(R.id.maintenancespinner);
            ArrayAdapter<CharSequence> adapter = new ArrayAdapter(getActivity(),android.R.layout.simple_spinner_item,getResources().getStringArray(R.array.maintenance_options));
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            options.setAdapter(adapter);
            */
            final EditText comment = cartDialogView.findViewById(R.id.comment);

            builder
                    .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            /*
                            Cart temp;
                            temp = mainActivity.selectedCarts.get(cartnum-1);
                            temp.setMaintenance(true);
                            temp.setChecked(false);
                            MaintenanceForm mf = new MaintenanceForm(cartnum, Calendar.getInstance().getTime().toString(), options.getSelectedItem().toString(),true);
                            if (comment.getText().toString()!=null)
                                mf.addComment(comment.getText().toString());
                            temp.addMaintenance(mf);
                            CartListAdapter cartListAdapter = mainActivity.getCartListAdapter();
                            cartListAdapter.notifyDataSetChanged();
                            mainActivity.loadcartcount();
                            */
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });


            return builder.create();
        }

    }

    public static class cartInfoDialog extends DialogFragment {
        MainActivity mainActivity;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            mainActivity = (MainActivity)getActivity();
            final CartListAdapter cLA = mainActivity.getCartListAdapter();
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            Bundle args = getArguments();
            final int cartnum = args.getInt("cartnum");
            builder.setTitle("Cart " + (cartnum));
            final Cart currcart;
            if (mainActivity.isremainfiltered||mainActivity.ismaintfiltered||mainActivity.ischeckfiltered)
                currcart = mainActivity.filteredCarts.get(cartnum-1);
            else
                currcart = mainActivity.selectedCarts.get(cartnum-1);

            LayoutInflater li = LayoutInflater.from(getActivity());
            final View cartDialogView = li.inflate(R.layout.cart_dialog, null);

            final ListView history = cartDialogView.findViewById(R.id.historylist);
            HistoryListAdapter adapter = new HistoryListAdapter(getActivity(),R.id.historylist,currcart.getHistory());
            history.setAdapter(adapter);

            final Button setoverdue = cartDialogView.findViewById(R.id.setoverdue);
            if (!currcart.isOverdue())
                setoverdue.setText(R.string.setoverdue);
            else
                setoverdue.setText(R.string.unsetoverdue);
            setoverdue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!currcart.isOverdue()) {
                        currcart.setOverdue(true);
                        setoverdue.setText(R.string.unsetoverdue);
                        cLA.notifyDataSetChanged();
                    }
                    else {
                        currcart.setOverdue(false);
                        setoverdue.setText(R.string.setoverdue);
                        cLA.notifyDataSetChanged();
                    }

                }
            });

            builder
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });

            builder.setView(cartDialogView);
            AlertDialog cartDialog = builder.create();
            return cartDialog;
        }
    }

    private static class HistoryListAdapter extends ArrayAdapter<MaintenanceForm> {
        private Context context;
        private ArrayList<MaintenanceForm> history;

        public HistoryListAdapter (Context context, int layout, ArrayList<MaintenanceForm> history) {
            super(context,layout,history);
            this.context = context;
            this.history = history;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View mainview = convertView;

            if(mainview==null)
                mainview = LayoutInflater.from(context).inflate(R.layout.history_container,parent,false);

            MaintenanceForm form = history.get(position);

            TextView datetime = mainview.findViewById(R.id.datetime);
            TextView reasontext = mainview.findViewById(R.id.reason);
            TextView commentview = mainview.findViewById(R.id.comment);
            mainview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LinearLayout commcont = view.findViewById(R.id.commcont);
                    if (commcont.getVisibility()==View.GONE)
                        commcont.setVisibility(View.VISIBLE);
                    else
                        commcont.setVisibility(View.GONE);
                }
            });

            if (form.getDate()!=null)
                datetime.setText(form.getDate());
            if (form.getReason()!=null)
                reasontext.setText(form.getReason());
            if (form.getComment()!=null)
                commentview.setText("Comments: " + form.getComment());

            return mainview;

        }

    }

    private WebView mWebView;

    private void doWebViewPrint() {
        // Create a WebView object specifically for printing
        WebView webView = new WebView(this);
        webView.setWebViewClient(new WebViewClient() {

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                saveCheckSheet();
                createWebPrintJob(view);
                mWebView = null;
            }
        });

        TextView checkedcartamt = findViewById(R.id.checkedcarts);
        TextView maintcartamt = findViewById(R.id.maintenancecarts);
        // Generate an HTML document on the fly:
        String htmlDocument = "<html><body><table border=\"0px\" width=100%><tr><td><h1>Cart Checksheet</h1>" +
                "<p>Date: " + format.format(dateSelected.getTime()) + "</p></td>" +
                "<td style=\"text-align:right\"><p>PM Checker: " + pmcheckerSave + " | AM Checker: " + amcheckerSave + "</p>" +
                "<p>Supervisor: _____________________________________</p></td></tr>" +
                "<tr><td><p>Checked Carts: " + checkedcartamt.getText() + " | Maintenance Carts: " + maintcartamt.getText() + "</p></td></tr></table>";
        htmlDocument += "<table border=\"1px solid black\" margin=\"0px\"><tr>";
                for(int i = 0; i<13; i++)
                    htmlDocument += "<th>Cart</th><th>&#x2713;/&#x2717;</th>";
                htmlDocument += "</tr>";
        //loop through cartlist adding cells and a new row every 13 carts.
        for (int row = 0; row < 25; row++) {
            htmlDocument+="<tr>";
            //column loop
            for (int col2 = 0; col2 < 13; col2++) {
                int col = 25 * col2;
                htmlDocument += "<td>" + String.valueOf(selectedCarts.get(col + row).getCartnum()) + "</td>";
                if (selectedCarts.get(col + row).isChecked())
                    htmlDocument += "<td>&#x2713;</td>";
                else if (selectedCarts.get(col + row).isMaintenance())
                    htmlDocument += "<td>&#x2717;</td>";
                else
                    htmlDocument += "<td></td>";
            }
            htmlDocument+="</tr>";
        }
        htmlDocument += "</table></body></html>";
        webView.loadDataWithBaseURL(null, htmlDocument, "text/HTML", "UTF-8", null);

        // Keep a reference to WebView object until you pass the PrintDocumentAdapter
        // to the PrintManager
        mWebView = webView;
    }

    private void createWebPrintJob(WebView webView) {

        // Get a PrintManager instance
        PrintManager printManager = (PrintManager) this.getSystemService(Context.PRINT_SERVICE);

        // Get a print adapter instance
        PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter();

        // Create a print job with name and adapter instance
        String jobName = format.format(dateSelected.getTime()) + " Cart Checksheet";
        PrintJob printJob = printManager.print(jobName, printAdapter, new PrintAttributes.Builder().build());

    }
}


