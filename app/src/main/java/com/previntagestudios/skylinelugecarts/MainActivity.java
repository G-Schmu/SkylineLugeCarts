package com.previntagestudios.skylinelugecarts;


import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Populate Toolbar
        android.support.v7.widget.Toolbar mainToolbar = findViewById(R.id.maintoolbar);
        setSupportActionBar(mainToolbar);
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

        //Add Cartlist Fragment to main screen
        CartListFragment cartList = new CartListFragment();

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().add(R.id.fragment_container,cartList).commit();

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop () {
        super.onStop();
    }

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
            //doWebViewPrint();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*
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
    */
}


