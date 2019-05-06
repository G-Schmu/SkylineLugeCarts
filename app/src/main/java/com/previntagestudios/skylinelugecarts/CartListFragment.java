package com.previntagestudios.skylinelugecarts;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CartListFragment extends Fragment implements View.OnClickListener {

    static private SimpleDateFormat format = new SimpleDateFormat("EEEE MMMM d, yyyy");
    static Calendar dateSelected = Calendar.getInstance();
    public CartListAdapter cartListAdapter;
    static private Button dateview;
    CalendarDialog calendardialog;
    static private ArrayList<Cart> selectedCarts;
    private boolean dataChanged;
    private String pmcheckerSave;
    private String amcheckerSave;
    private boolean ischeckfiltered;
    private boolean ismaintfiltered;
    private boolean isremainfiltered;
    private ArrayList<Cart> filteredCarts;
    private DatabaseHelper dbhelper;

    public CartListFragment() {
        // Required empty public constructor
    }

    public static CartListFragment newInstance() {
        CartListFragment cartListFragment = new CartListFragment();
        return cartListFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cart_list, container, false);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        selectedCarts = new ArrayList<Cart>();
        filteredCarts = new ArrayList<Cart>();
        dbhelper = new DatabaseHelper(getActivity().getBaseContext());

        //date handler stuff
        dateview = getView().findViewById(R.id.dateview);
        dateview.setText(format.format(dateSelected.getTime()));
        calendardialog = new CalendarDialog();
        checkforSheet();
        loadcartcount();

        EditText pmstaff = getView().findViewById(R.id.pmchecker);
        EditText amstaff = getView().findViewById(R.id.amchecker);
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
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.yesterdaybutton :
                yesterday();
                break;
            case R.id.dateview :
                if (dataChanged)
                    saveCheckSheet();
                calendardialog.show(getFragmentManager(),"calendar");
                checkforSheet();
                break;
            case R.id.tomorrowbutton :
                tomorrow();
                break;
            case R.id.startCheckSheet :
                startCheckSheet(view);
                break;
            case R.id.filterChecked :
                filterChecked(view);
                break;
            case R.id.filterMaint :
                filterMaint(view);
                break;
            case R.id.filterRemain :
                filterRemain(view);
                break;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        saveCheckSheet();
    }

    @Override
    public void onResume() {
        super.onResume();

        //List Jumping From Number
        final EditText searchbox = getView().findViewById(R.id.jumptotext);
        searchbox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                ListView cartlist = getView().findViewById(R.id.cartlistcontainer);
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
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
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
            Toast.makeText(getActivity(), "DB Entry Error", Toast.LENGTH_LONG).show();
            lugedb.delete("metasheet","formID = '"+newdate+"'", null);
            lugedb.delete("checklist","formID = '"+newdate+"'", null);
        }
        else {
            Toast.makeText(getActivity(), "New Checksheet Created", Toast.LENGTH_SHORT).show();
            getView().findViewById(R.id.createchecksheet).setVisibility(View.GONE);
        }

        getView().findViewById(R.id.locked).setVisibility(View.GONE);
        cartsC.close();
        lugedb.close();
        checkforSheet();
    }

    private void checkforSheet () {
        //setting list view from DB
        String date = format.format(dateSelected.getTime());
        getView().findViewById(R.id.locked).setVisibility(View.GONE);
        getView().findViewById(R.id.createchecksheet).setVisibility(View.GONE);
        getView().findViewById(R.id.cartlistcontainer).setVisibility(View.GONE);
        EditText amchecker = getView().findViewById(R.id.amchecker);
        EditText pmchecker = getView().findViewById(R.id.pmchecker);
        //check for created sheet
        SQLiteDatabase lugedb = dbhelper.getReadableDatabase();
        String querytest = "SELECT * FROM metaSheet WHERE formID = '" + date + "'";
        Cursor metacursor = lugedb.rawQuery(querytest,null);
        //if sheet does not exist display prompt
        if (metacursor.getCount()==0) {
            getView().findViewById(R.id.createchecksheet).setVisibility(View.VISIBLE);
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
            ListView cartlist = getView().findViewById(R.id.cartlistcontainer);
            cartlist.setVisibility(View.VISIBLE);
            loadCarts();
            loadcartcount();
            metacursor.moveToFirst();
            if (metacursor.getInt(3)==1)
                getView().findViewById(R.id.locked).setVisibility(View.VISIBLE);
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
            Toast.makeText(getActivity(),"Failed to load check sheet",Toast.LENGTH_LONG);
        }
        cartListAdapter = new CartListAdapter(getActivity(),R.layout.cart_container,selectedCarts);
        RecyclerView cartListContainer = getView().findViewById(R.id.cartlistcontainer);
        cartListContainer.setAdapter(cartListAdapter);


        EditText amchecker = getView().findViewById(R.id.amchecker);
        EditText pmchecker = getView().findViewById(R.id.pmchecker);
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
        EditText amstaff = getView().findViewById(R.id.amchecker);
        String amstaffname = amstaff.getText().toString();
        EditText pmstaff = getView().findViewById(R.id.pmchecker);
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

    public static class Boolean {
        public static boolean getBoolean(int num) {
            if (num == 1)
                return true;
            else
                return false;
        }
    }

    public void tomorrow () {
        if (dataChanged)
            saveCheckSheet();
        dateSelected.add(dateSelected.DAY_OF_YEAR,1);
        dateview.setText(format.format(dateSelected.getTime()));
        checkforSheet();
    }
    public void yesterday () {
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
        LinearLayout remainView = getView().findViewById(R.id.filterRemain);
        LinearLayout checkedView = getView().findViewById(R.id.filterChecked);
        LinearLayout maintView = getView().findViewById(R.id.filterMaint);
        maintView.setBackgroundColor(getResources().getColor(R.color.cartmaintenance));
        checkedView.setBackgroundColor(getResources().getColor(R.color.cartchecked));
        remainView.setBackgroundColor(getResources().getColor(R.color.lightGrey));

        maintView.setTranslationZ(0);
        checkedView.setTranslationZ(0);
        remainView.setTranslationZ(0);

        //TODO optimize filtering color algorithm
    }

    public void filterMaint (View view) {
        RecyclerView cartListView = getView().findViewById(R.id.cartlistcontainer);
        clearFilters("maint");
        if (selectedCarts != null) {
            if (!ismaintfiltered) {
                filteredCarts.clear();
                for (int i = 0; i < selectedCarts.size(); i++)
                    if (selectedCarts.get(i).isMaintenance())
                        filteredCarts.add(selectedCarts.get(i));
                cartListAdapter.setCarts(filteredCarts);
                ismaintfiltered = true;
                view.setTranslationZ(2);
                LinearLayout remainView = getView().findViewById(R.id.filterRemain);
                LinearLayout checkedView = getView().findViewById(R.id.filterChecked);
                checkedView.setBackgroundColor(getResources().getColor(R.color.grey));
                remainView.setBackgroundColor(getResources().getColor(R.color.grey));
            }
            else {
                cartListAdapter.setCarts(selectedCarts);
                ismaintfiltered = false;
            }
            cartListView.setAdapter(cartListAdapter);
        }
    }

    public void filterChecked (View view) {
        RecyclerView cartListView = getView().findViewById(R.id.cartlistcontainer);
        clearFilters("checked");
        if (selectedCarts != null) {
            if (!ischeckfiltered) {
                filteredCarts.clear();
                for (int i = 0; i < selectedCarts.size(); i++)
                    if (selectedCarts.get(i).isChecked())
                        filteredCarts.add(selectedCarts.get(i));
                cartListAdapter.setCarts(filteredCarts);
                ischeckfiltered=true;
                view.setTranslationZ(2);
                LinearLayout remainView = getView().findViewById(R.id.filterRemain);
                LinearLayout maintView = getView().findViewById(R.id.filterMaint);
                maintView.setBackgroundColor(getResources().getColor(R.color.grey));
                remainView.setBackgroundColor(getResources().getColor(R.color.grey));
            }
            else {
                cartListAdapter.setCarts(selectedCarts);
                ischeckfiltered = false;
            }
        }
    }

    public void filterRemain (View view) {
        RecyclerView cartListView = getView().findViewById(R.id.cartlistcontainer);
        clearFilters("remain");
        if (selectedCarts != null) {
            if (!isremainfiltered) {
                filteredCarts.clear();
                for (int i = 0; i < selectedCarts.size(); i++)
                    if (!selectedCarts.get(i).isChecked()&&!selectedCarts.get(i).isMaintenance())
                        filteredCarts.add(selectedCarts.get(i));
                cartListAdapter.setCarts(filteredCarts);
                isremainfiltered = true;
                view.setTranslationZ(2);

                LinearLayout checkedView = getView().findViewById(R.id.filterChecked);
                LinearLayout maintView = getView().findViewById(R.id.filterMaint);
                maintView.setBackgroundColor(getResources().getColor(R.color.grey));
                checkedView.setBackgroundColor(getResources().getColor(R.color.grey));
            }
            else {
                cartListAdapter.setCarts(selectedCarts);
                isremainfiltered = false;
            }
            cartListView.setAdapter(cartListAdapter);
        }
    }

    public void cartDialog (View view) {
        ListView list = getView().findViewById(R.id.cartlistcontainer);
        int cartnum = list.getPositionForView(view);
        Cart temp = getCartAt(cartnum);
        Bundle args = new Bundle();
        args.putInt("cartnum",temp.getCartnum());
        cartInfoDialog cartDialog = new cartInfoDialog();
        cartDialog.setArguments(args);
        cartDialog.show(getFragmentManager(),"cartDialog");
        cartListAdapter.notifyDataSetChanged();
    }

    public void cartMaintenance (View view) {
        //collect data to send to fragment
        dataChanged = true;
        ListView list = getView().findViewById(R.id.cartlistcontainer);
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
        ListView list = getView().findViewById(R.id.cartlistcontainer);
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
        TextView remainingcarts = getView().findViewById(R.id.remainingcarts);
        remainingcarts.setText(String.valueOf(amtremainingcarts));
        TextView checkedcarts = getView().findViewById(R.id.checkedcarts);
        checkedcarts.setText(String.valueOf(amtcheckedcarts));
        TextView maintenancecarts = getView().findViewById(R.id.maintenancecarts);
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
        }
    }

    public class CartListAdapter extends RecyclerView.Adapter<CartHolder> {

        private ArrayList<Cart> carts;
        private Context context;
        private int layout;

        public CartListAdapter (Context context, int layout, ArrayList<Cart> carts) {
            this.context = context;
            this.carts = carts;
            this.layout = layout;
        }

        public void setCarts (ArrayList<Cart> carts) {
            this.carts = carts;
        }

        @Override
        public CartHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext()).inflate(layout,parent,false);

            CartHolder cartHolder = new CartHolder(this.context,view);
            return cartHolder;

        }

        @Override
        public void onBindViewHolder(CartHolder cartHolder, int position) {

            Cart currcart = carts.get(position);
            ImageView infobutton = cartHolder.itemView.findViewById(R.id.cartinfobutton);
            ImageButton indicator = cartHolder.itemView.findViewById(R.id.indicator);
            ImageButton maintenance = cartHolder.itemView.findViewById(R.id.maintenancebutton);
            if(cartHolder.isInRotation()==0) {
                cartHolder.itemView.setBackgroundColor(getResources().getColor(R.color.glassdark));
                //todo remove button clickability
            }
            else {
                if(cartHolder.isOverdue())
                    cartHolder.itemView.setBackgroundColor(getResources().getColor(R.color.cartoverdue));
                if(cartHolder.isMaintenance())
                    cartHolder.itemView.setBackgroundColor(getResources().getColor(R.color.cartmaintenance));
                if (cartHolder.isChecked())
                    cartHolder.itemView.setBackgroundColor(getResources().getColor(R.color.cartchecked));
            }
            Cart cart = this.carts.get(position);
            cartHolder.bindCart(cart);
        }

        @Override
        public int getItemCount() {
            return this.carts.size();
        }

        public void cartDialog (View view) {
            ListView list = getView().findViewById(R.id.cartlistcontainer);
            int cartnum = list.getPositionForView(view);
            Cart temp = getCartAt(cartnum);
            Bundle args = new Bundle();
            args.putInt("cartnum",temp.getCartnum());
            cartInfoDialog cartDialog = new cartInfoDialog();
            cartDialog.setArguments(args);
            cartDialog.show(getFragmentManager(),"cartDialog");
            cartListAdapter.notifyDataSetChanged();
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
        CartListFragment CLF;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final CartListFragment.CartListAdapter cLA = CLF.getCartListAdapter();
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            Bundle args = getArguments();
            final int cartnum = args.getInt("cartnum");
            builder.setTitle("Cart " + (cartnum));
            final Cart currcart;
            if (CLF.isremainfiltered||CLF.ismaintfiltered||CLF.ischeckfiltered)
                currcart = CLF.filteredCarts.get(cartnum-1);
            else
                currcart = CLF.selectedCarts.get(cartnum-1);

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


}
