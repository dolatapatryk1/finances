package patrykd.finances;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import patrykd.finances.patrykd.finances.activities.AddAccountActivity;
import patrykd.finances.patrykd.finances.activities.AddExpenseActivity;
import patrykd.finances.patrykd.finances.activities.ExpenseActivity;
import patrykd.finances.patrykd.finances.activities.LoginActivity;
import patrykd.finances.patrykd.finances.activities.StatementActivity;
import patrykd.finances.patrykd.finances.controllers.AccountController;
import patrykd.finances.patrykd.finances.database.DatabaseHelper;
import patrykd.finances.patrykd.finances.activities.CategoryActivity;
import patrykd.finances.patrykd.finances.models.Account;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener{

    private final AppCompatActivity activity = MainActivity.this;

    private AppCompatButton appCompatButtonAdd;
    private AppCompatButton appCompatButtonDelete;
    private AppCompatButton appCompatButtonIncome;
    private AppCompatButton appCompatButtonExpense;

    private TextView textViewAmount;

    private ListView listViewAccount;
    private ArrayAdapter<Account> adapter;

    private DatabaseHelper db;

    public static String userLogin;
    private int positionOnList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);




        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Intent myIntent = getIntent();
        userLogin = myIntent.getStringExtra("login");
        System.out.println(userLogin);
        initViews();
        initListeners();
        initObjects();
        displayAccounts();

    }

    private void initViews(){
        appCompatButtonAdd = findViewById(R.id.appCompatButtonAdd);
        appCompatButtonDelete = findViewById(R.id.appCompatButtonDelete);
        appCompatButtonIncome = findViewById(R.id.appCompatButtonIncome);
        appCompatButtonExpense = findViewById(R.id.appCompatButtonExpense);
        listViewAccount = findViewById(R.id.listViewAccount);
        textViewAmount = findViewById(R.id.textViewAmount);
    }

    private void initListeners(){
        appCompatButtonAdd.setOnClickListener(this);
        appCompatButtonDelete.setOnClickListener(this);
        appCompatButtonIncome.setOnClickListener(this);
        appCompatButtonExpense.setOnClickListener(this);
        listViewAccount.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                view.setSelected(true);
                positionOnList = position;
            }
        });
    }

    private void initObjects(){
        db = new DatabaseHelper(activity);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(loginIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        android.app.FragmentManager fragmentManager = getFragmentManager();
        if (id == R.id.accounts) {
            displayAccounts();
        } else if (id == R.id.expenses) {
            Intent exIntent = new Intent(getApplicationContext(), ExpenseActivity.class);
            exIntent.putExtra("login", userLogin);
            startActivity(exIntent);
        } else if (id == R.id.categories) {
            Intent catIntent = new Intent(getApplicationContext(), CategoryActivity.class);
            catIntent.putExtra("login", userLogin);
            startActivity(catIntent);
        } else if(id == R.id.monthly_statement){
            Intent statementIntent = new Intent(getApplicationContext(), StatementActivity.class);
            statementIntent.putExtra("login", userLogin);
            startActivity(statementIntent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.appCompatButtonAdd:
                Intent intentAdd = new Intent(getApplicationContext(), AddAccountActivity.class);
                intentAdd.putExtra("login", userLogin);
                startActivity(intentAdd);
                break;
            case R.id.appCompatButtonDelete:
                deleteAccount(positionOnList);
                break;
            case R.id.appCompatButtonIncome:
                displayPrompt();
                break;
            case R.id.appCompatButtonExpense:
                Account acc = (Account)listViewAccount.getItemAtPosition(positionOnList);
                Intent intentExpense = new Intent(getApplicationContext(), AddExpenseActivity.class);
                intentExpense.putExtra("login", userLogin);
                intentExpense.putExtra("accountId", acc.getId());
                startActivity(intentExpense);
                break;
        }
    }

    private void displayAccounts(){
        List<Account> accounts = AccountController.getAllAcounts(userLogin, db.getReadableDatabase());
        adapter = new ArrayAdapter<>(this, R.layout.row_account,
                accounts);
        listViewAccount.setAdapter(adapter);
        double amount = 0;
        for(Account acc:accounts){
            amount += acc.getAmount();
        }
        textViewAmount.setText(String.format("%.2f zł", amount));
    }

    private void deleteAccount(int position){
        Account acc = (Account)listViewAccount.getItemAtPosition(position);
        AccountController.deleteAccount(acc, db.getWritableDatabase());
        displayAccounts();
    }

    private void displayPrompt(){
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.prompt, null);

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setView(promptsView);

        final EditText userInput = promptsView.findViewById(R.id.editTextDialogUserInput);

        alertDialogBuilder.setCancelable(false);
        final AlertDialog alertDialog = alertDialogBuilder.create();
        Button button = promptsView.findViewById(R.id.button);
        Button button2 = promptsView.findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println(positionOnList);
                Account acc = (Account) listViewAccount.getItemAtPosition(positionOnList);
                double money = Double.parseDouble(userInput.getText().toString()) + acc.getAmount();
                AccountController.addMoneyToAccount(acc, money, db.getWritableDatabase());
                displayAccounts();
                alertDialog.cancel();
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.cancel();
            }
        });



        alertDialog.show();
    }


}
