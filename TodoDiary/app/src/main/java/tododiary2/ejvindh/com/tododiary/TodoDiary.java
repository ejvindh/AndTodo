package tododiary2.ejvindh.com.tododiary;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import static android.view.accessibility.AccessibilityNodeInfo.CollectionInfo.SELECTION_MODE_NONE;
import static com.prolificinteractive.materialcalendarview.MaterialCalendarView.SELECTION_MODE_SINGLE;
import static java.lang.String.format;


public class TodoDiary extends AppCompatActivity implements OnClickListener, OnDateSelectedListener {

    final static private String db_name = "db";
    final static private String jumbleFileName = "jumble.txt";
    final static private String filesEndfolder = "TodoDiary";
    final static private String date_flag = "||||";
    final static private String todo_flag_sign = "++++";
    final static private String endline = "\r\n";
    final static private String encoding = "UTF-16";
    private String todo_flag;
    private boolean enable_todo;
    private int activity_requestCode_jumble=1;
    private int activity_requestCode_browsefiles=2;
    private int activity_requestCode_raw_db=3;
    private Button save_button;
    private Button goto_today;
    private Button markdone_button;
    private Button prevtodo_button;
    private Button nexttodo_button;
    private MaterialCalendarView date_picked;
    private TextView todocount;
    private EditText entrytext;
    private String prev_entrytext = "";
    private String prev_marked_done_by_button_entrytext = "";
    private String date_marker;
    private int count_date_position_in_text;
    private String dbFilepath;
    private String db_name_full;
    private String returneret_result;
    private String returneret_result_raw;
    private int undone_count = -1;
    private boolean marked_todo_in_file = false;
    private boolean entrytext_changed = false;
    private boolean marked_done_by_button = false;
    private boolean entrytext_changed_after_marked_done_by_button = false;
    private boolean changes = false;
    private boolean dbAccess;
    private ArrayList<Integer> todo_day_of_years = new ArrayList<>();
    private int thisYear; //det år, som vi starter op i -- ændrer sig ikke, når der skiftes år.
    @SuppressWarnings("FieldCanBeLocal")
    private int thisMonth;
    private int thisDayOfYear;
    @SuppressWarnings("FieldCanBeLocal")
    private int thisDayOfMonth;
    private int selectedDayOfMonth;
    private int selectedDayOfYear;
    private int selectedMonth;
    private int selectedYear; // Det år som aktuelt vises
    private boolean loadingFlag;
    private boolean endingFlag;
    private int externalReadWritePermissionCheckFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tododiary);
        thisYear = Calendar.getInstance().get(Calendar.YEAR);
        thisMonth = Calendar.getInstance().get(Calendar.MONTH);
        thisDayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        thisDayOfMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        dbAccess = false;
        boolean firstTimeRun = true;

        //Hent gemte settings, og hvis de ikke findes, så loade default
        SharedPreferences settings = getSharedPreferences("DBTodo_settings", 0);
        enable_todo = settings.getBoolean("enable_todo", true);
        String tmp_filePath = Environment.getExternalStorageDirectory().toString() + "/" + filesEndfolder;
        dbFilepath = settings.getString("dbFilepath", tmp_filePath);

        if (enable_todo) todo_flag = todo_flag_sign; else todo_flag = "";

        save_button = findViewById(R.id.save_button);
        save_button.setOnClickListener(this);
        goto_today = findViewById(R.id.goto_today);
        goto_today.setOnClickListener(this);
        markdone_button = findViewById(R.id.markdone_button);
        markdone_button.setOnClickListener(this);
        prevtodo_button = findViewById(R.id.prevtodo_button);
        prevtodo_button.setOnClickListener(this);
        nexttodo_button = findViewById(R.id.nexttodo_button);
        nexttodo_button.setOnClickListener(this);
        todocount = findViewById(R.id.todocount);
        date_picked = findViewById(R.id.calendarView);
        date_picked.setOnDateChangedListener(this);
        entrytext = findViewById(R.id.editText1);

        if (savedInstanceState != null) {
            //Hvis telefonen er blevet drejet portrait/horisontal el. lign, læses variable ind igen
            firstTimeRun = false;
            String saved_entrytext = savedInstanceState.getString("entrytext");
            entrytext.setText(saved_entrytext);
            prev_entrytext = savedInstanceState.getString("prev_entrytext");
            prev_marked_done_by_button_entrytext =
                    savedInstanceState.getString("prev_marked_done_by_button_entrytext");
            selectedDayOfYear = savedInstanceState.getInt("selecteddayofyear");
            selectedDayOfMonth = savedInstanceState.getInt("selectedday");
            selectedMonth = savedInstanceState.getInt("selectedmonth");
            selectedYear = savedInstanceState.getInt("selectedyear");
            date_marker = create_date_marker(selectedYear, selectedMonth, selectedDayOfMonth);
            count_date_position_in_text = savedInstanceState.getInt("count_date_position_in_text");
            enable_todo = savedInstanceState.getBoolean("enable_todo");
            marked_todo_in_file = savedInstanceState.getBoolean("marked_todo_in_file");
            entrytext_changed = savedInstanceState.getBoolean("entrytext_changed");
            marked_done_by_button = savedInstanceState.getBoolean("marked_done_by_button");
            entrytext_changed_after_marked_done_by_button = savedInstanceState.getBoolean(
                    "entrytext_changed_after_marked_done_by_button");
            changes = savedInstanceState.getBoolean("changes");
            loadingFlag = savedInstanceState.getBoolean("loadingFlag");
            endingFlag = savedInstanceState.getBoolean("endingFlag");
            externalReadWritePermissionCheckFlag = savedInstanceState.getInt("externalReadWritePermissionCheckFlag");
            dbAccess = savedInstanceState.getBoolean("dbAccess");
            dbFilepath = savedInstanceState.getString("dbFilepath");

        } else {
            //Ved første kørsel af onCreate
            selectedYear = thisYear;
            selectedMonth = thisMonth;
            selectedDayOfMonth = thisDayOfMonth;
            Calendar tmpSelectedDay = Calendar.getInstance();
            tmpSelectedDay.set(selectedYear, selectedMonth, selectedDayOfMonth);
            selectedDayOfYear = tmpSelectedDay.get(Calendar.DAY_OF_YEAR);
        }

        assert dbFilepath != null;
        externalReadWritePermissionCheckFlag = 1;
        if (permissionCheck()) {
            File directory_tmp = new File(dbFilepath);
            dbAccess = true;
            if (! directory_tmp.exists()){
                boolean mkdirResult = directory_tmp.mkdirs();
                if (!mkdirResult) {
                    showToast(getString(R.string.notCreateFolder));
                    dbAccess = false;
                }
            }
        }
        setDBNames(selectedYear);//hent databasefilernes navn udfra årstal
        if (firstTimeRun && dbAccess) {
            loadDB( selectedYear, db_name_full);
        }


        entrytext.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                // Auto-generated method stub
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Auto-generated method stub
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //Hvis teksten er blevet ændret, så sæt de relevante ændringsflag, og opdater kna
                entrytext_changed = entrytext.getText().toString().compareTo(prev_entrytext) != 0;
                entrytext_changed_after_marked_done_by_button =
                        entrytext.getText().toString().compareTo(prev_marked_done_by_button_entrytext) != 0;
                undone_count = countUndone(selectedYear);
                ButtonTexts();
            }
        });
    }

    @Override
    public void onDateSelected(@NonNull MaterialCalendarView date_picked, @NonNull CalendarDay date, boolean selected) {
        //selected is no value on logcat
        if (selected) {
            makeChangesOnDateChange(date);
        }

    }

    public void makeChangesOnDateChange(CalendarDay date) {
        save_changed_entrytext();//Gem teksten, hvis den er blevet ændret
        int oldSelectedYear = selectedYear;
        selectedDayOfMonth = date.getDay();
        selectedMonth = date.getMonth();
        selectedYear = date.getYear();
        Calendar tmpSelectedDay = Calendar.getInstance();
        tmpSelectedDay.setTime(date.getDate());
        selectedDayOfYear = tmpSelectedDay.get(Calendar.DAY_OF_YEAR);
        //Når en ny dato er valgt
        entrytext.setEnabled(true);
        if (oldSelectedYear != selectedYear) {
            //Har der været årsskift i Datepicker?
            change_year(selectedYear);
        }
        undone_count = countUndone(selectedYear);
        ButtonTexts();
        populateText(selectedYear, selectedMonth, selectedDayOfMonth);//Sæt indhold ind i edittext for den valgte dato
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //Settings-menu -- sæt de rigtige flueben, og vis/skjul knappen til at vælge mappe
        boolean visibilityFlag = !loadingFlag;
        MenuItem itemLoading = menu.findItem(R.id.loadingMessage);
        MenuItem itemJumble = menu.findItem(R.id.goto_jumble);
        MenuItem itemRaw = menu.findItem(R.id.raw_db);
        MenuItem itemFindDict = menu.findItem(R.id.find_dict);
        MenuItem itemTodo = menu.findItem(R.id.enabletodo);
        itemLoading.setVisible(!visibilityFlag);
        itemJumble.setVisible(visibilityFlag);
        itemRaw.setVisible(visibilityFlag);
        itemTodo.setVisible(visibilityFlag);
        itemTodo.setChecked(enable_todo);
        if (loadingFlag) {
            itemFindDict.setVisible(visibilityFlag);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Settings-menu -- hvad gør de enkelte valgmuligheder
        if (item.getItemId()== R.id.goto_jumble) {
            //Jumble-knappen. Start en ny aktivitet til at håndtere rode-filen
            loadDB(0, jumbleFileName);
        }
        if (item.getItemId()== R.id.raw_db) {
            //Vis den rå db-fil
            save_changed_entrytext();
            Intent raw_db = new Intent(this, Raw_db_Activity.class);
            String raw_db_text="";
            externalReadWritePermissionCheckFlag = 0; // No success/failure handling on the permissionCheck...
            if (permissionCheck()) {
                File raw_db_file = new File(dbFilepath, db_name_full);
                if (!raw_db_file.exists()) {
                    raw_db_text=getResources().getString(R.string.failed_db_load);
                } else {
                    try {
                        BufferedReader raw_db_br = new BufferedReader
                                (new InputStreamReader(new FileInputStream(raw_db_file), encoding));
                        StringBuilder raw_db_txt = new StringBuilder();
                        String thisLine;
                        while ((thisLine = raw_db_br.readLine()) != null) {
                            raw_db_txt.append(thisLine).append("\n");
                        }
                        raw_db_br.close();
                        raw_db_text = raw_db_txt.toString();
                    } catch (IOException e) {
                        // Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                raw_db.putExtra("raw_db_text", raw_db_text);
                raw_db.putExtra("raw_db_position", count_date_position_in_text);
                startActivityForResult(raw_db, activity_requestCode_raw_db);

            }
        }
        if (item.getItemId()== R.id.find_dict) {
            //Vælge mappe til lokal backup
            externalReadWritePermissionCheckFlag = 5; // Change DBFolder
            if (permissionCheck()) {
                //Only opens if permissions are given. If permissions have been revoked, they can be given again
                //...however will not open the intent the first time anyway.
                openChangeLocalCopyFolderIntent();
            }
        }
        if (item.getItemId()== R.id.enabletodo) {
            //Skal Todofunktionaliteten være tændt?
            item.setChecked(!item.isChecked());
            enable_todo = item.isChecked();
            if (enable_todo) todo_flag = todo_flag_sign; else todo_flag = "";
            undone_count = countUndone(selectedYear);
            ButtonTexts();
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onSaveInstanceState (Bundle outState) {
        //B.la. hvis der skiftes fra landscape til portrait -- så skal alle variabler gemmes
        // (genindlæses i onCreate)
        super.onSaveInstanceState(outState);
        outState.putString("entrytext", entrytext.getText().toString());
        outState.putInt("count_date_position_in_text", count_date_position_in_text);
        outState.putString("prev_entrytext", prev_entrytext);
        outState.putString("prev_marked_done_by_button_entrytext", prev_marked_done_by_button_entrytext);
        outState.putInt("selectedday", selectedDayOfMonth);
        outState.putInt("selecteddayofyear", selectedDayOfYear);
        outState.putInt("selectedmonth", selectedMonth);
        outState.putInt("selectedyear", selectedYear);
        outState.putBoolean("enable_todo", enable_todo);
        outState.putBoolean("marked_todo_in_file", marked_todo_in_file);
        outState.putBoolean("entrytext_changed", entrytext_changed);
        outState.putBoolean("marked_done_by_button", marked_done_by_button);
        outState.putBoolean("entrytext_changed_after_marked_done_by_button", entrytext_changed_after_marked_done_by_button);
        outState.putBoolean("changes", changes);
        outState.putBoolean("loadingFlag", loadingFlag);
        outState.putBoolean("endingFlag", endingFlag);
        outState.putInt("externalReadWritePermissionCheckFlag", externalReadWritePermissionCheckFlag);
        outState.putBoolean("dbAccess", dbAccess);
        outState.putString("dbFilepath", dbFilepath);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @SuppressLint("ApplySharedPref")
    @Override
    protected void onPause() {
        super.onPause();
        // Når app'en lukkes ned, gemmes de settings, der skal huskes på tværs af denne kørsel
        SharedPreferences settings = getApplicationContext().getSharedPreferences("DBTodo_settings", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("enable_todo", enable_todo);
        editor.putString("dbFilepath", dbFilepath);
        editor.commit(); //Android Studio wants editor.apply() here, but then it doesn't seem to save the changes...
        if (endingFlag) {
            System.exit(1);
        }
    }



    public void onClick(View v) {
        // Hvordan opfører knapperne sig
        if (v.getId() == R.id.save_button) {
            //Save-knappen
            save_changed_entrytext();
            populateText(selectedYear, selectedMonth, selectedDayOfMonth);
            undone_count = countUndone(selectedYear);
            ButtonTexts();
        } else if (v.getId() == R.id.goto_today) {
            //Find dagen-i-dag
            save_changed_entrytext();//Gem teksten, hvis den er blevet ændret
            setPickedDayOfYear(thisYear, thisDayOfYear);
        } else if (v.getId() == R.id.markdone_button) {
            // Mark-done button
            marked_done_by_button = true;
            entrytext_changed_after_marked_done_by_button = false;
            prev_marked_done_by_button_entrytext = entrytext.getText().toString();
            ButtonTexts();
        } else if (v.getId() == R.id.prevtodo_button) {
            //prevtodo-button
            int prev_day=0;
            for (int parsed_days : todo_day_of_years) {
                if (parsed_days<selectedDayOfYear) prev_day=parsed_days;
            }
            if (prev_day !=0) {
                setPickedDayOfYear(selectedYear, prev_day);
            }
            ButtonTexts();
        } else if (v.getId() == R.id.nexttodo_button) {
            //nexttodo-button
            int next_day=0;
            for (int parsed_days : todo_day_of_years) {
                if (next_day==0 && parsed_days>selectedDayOfYear) next_day=parsed_days;
            }
            if (next_day !=0) {
                setPickedDayOfYear(selectedYear, next_day);
            }
            ButtonTexts();
        }
    }

    @Override
    public void onBackPressed() {
        // onPause aktiveres af mange andre ting end egentlig nedlukning af appen
        // denne onBackPressed samler op, når onPause er aktiveret for at stoppe programmet helt
        if (!loadingFlag) {
            save_changed_entrytext();
            endingFlag = true;
            super.onBackPressed();
        }
    }

    public void openChangeLocalCopyFolderIntent() {
        Intent intent = new Intent(this, BrowseFiles.class);
        intent.putExtra("dbFilepath", dbFilepath);
        startActivityForResult(intent, activity_requestCode_browsefiles);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Her samles op fra de aktiviteter, der skal give resultat tilbage
        // (dvs. JumbleActivity, RawView og BrowseFiles)
        // Check which request we're responding to
        if (requestCode == activity_requestCode_jumble) {
            // JumbleActivity
            if (resultCode == RESULT_OK) {
                //changes = true;
                save_changed_entrytext();
                ButtonTexts();
                returneret_result=data.getStringExtra("returningresult");
                externalReadWritePermissionCheckFlag = 6;
                if (permissionCheck()) {
                    File jumblefile_write = new File(dbFilepath, jumbleFileName);
                    try {
                        BufferedWriter bw = new BufferedWriter
                                (new OutputStreamWriter(new FileOutputStream(jumblefile_write), encoding));
                        bw.write(returneret_result.replaceAll("\n", endline));
                        bw.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if (requestCode == activity_requestCode_browsefiles) {
            //BrowseFiles
            if (resultCode == RESULT_OK) {
                dbFilepath = data.getStringExtra("RESULT_STRING");
                loadDB(selectedYear, db_name_full);
            }
        }
        if (requestCode == activity_requestCode_raw_db) {
            //Return from Raw_db_view
            if (resultCode == RESULT_OK) {
                save_changed_entrytext();
                returneret_result_raw=data.getStringExtra("returningresult");
                externalReadWritePermissionCheckFlag = 7;
                if (permissionCheck()) {
                    File raw_db_write = new File(dbFilepath, db_name_full);
                    try {
                        BufferedWriter bw = new BufferedWriter
                                (new OutputStreamWriter(new FileOutputStream(raw_db_write), encoding));
                        bw.write(returneret_result_raw.replaceAll("\n", endline));
                        bw.close();
                    } catch (IOException e) {
                        // Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            populateText(selectedYear, selectedMonth, selectedDayOfMonth);
            ButtonTexts();
        }
    }


    private void loadDB(final int year, final String filename) {
        // Check om fil eksisterer, og hvis nej, så opret den. Ellers hent-ind
        loadingFlag = true;
        lockScreenOrientation();
        date_picked.setSelectionMode(SELECTION_MODE_NONE);
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setMessage(getString(R.string.loading));
        dialog.show();
        if (year == 0) {
            externalReadWritePermissionCheckFlag = 2; //Jumblefile
        } else {
            externalReadWritePermissionCheckFlag = 3; //Trad. db
        }
        if (permissionCheck()) {
            final File file = new File(dbFilepath, filename);
            dbAccess = true;
            boolean fileExists = file.exists();
            if (!fileExists) {
                fileExists = createNewDBFile(filename, year);

            }
            if (fileExists) {
                if (year != 0) {
                    undone_count = countUndone(year);//Hvor mange undone's er der i år?
                    populateText(selectedYear, selectedMonth, selectedDayOfMonth);
                    ButtonTexts();
                }
            } else {
                dbAccess = false;
            }
        }
        unlockScreenOrientation();
        loadingFlag = false;
        dialog.dismiss();
        if (dbAccess) {
            date_picked.setSelectionMode(SELECTION_MODE_SINGLE);
            date_picked.setSelectedDate(CalendarDay.from(selectedYear, selectedMonth, selectedDayOfMonth));
            if (year > 0) {
                ButtonTexts();
            } else {
                File jumblefile = new File(dbFilepath, jumbleFileName);
                openJumbleActivity(jumblefile);
            }
        }
    }


    private boolean createNewDBFile(String filename, int year) {
        boolean result = false;
        int writeDBResult = writeDB(year, filename, dbFilepath);
        if (writeDBResult == 0) {
            date_marker = create_date_marker(selectedYear, selectedMonth, selectedDayOfMonth);
            changes = true;
            undone_count = countUndone(year);//Hvor mange undone's er der i år?
            populateText(selectedYear, selectedMonth, selectedDayOfMonth);
            ButtonTexts();
            loadingFlag = false;
            unlockScreenOrientation();
            date_picked.setSelectionMode(SELECTION_MODE_SINGLE);
            date_picked.setSelectedDate(CalendarDay.from(selectedYear, selectedMonth, selectedDayOfMonth));
            result = true;
        } else {
            //Error creating new DB => you have serious trouble.
            showToast(getString(R.string.notCreateDB) + filename);
            dbAccess = false;
        }
        if (year == 0 && dbAccess) {
            //Create jumblefile
            File jumblefile = new File(dbFilepath, jumbleFileName);
            openJumbleActivity(jumblefile);
        }
        return result;
    }


    int writeDB(Integer year, String db_name, String filePath) {
        int returnResult = 0;
        try {
            File dbFile = new File(filePath + "/" + db_name);
            if (!dbFile.exists()) {
                if (!dbFile.createNewFile()) {
                    returnResult = 1;
                }
            }
            BufferedWriter bw = new BufferedWriter
                    (new OutputStreamWriter(new FileOutputStream(dbFile), TodoDiary.encoding));
            if (year == 0) {
                bw.write(getString(R.string.newJF) + TodoDiary.endline);
            } else {
                String[] dayNames = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
                String parseDayName;
                Calendar parseDays = Calendar.getInstance();
                parseDays.set(Calendar.YEAR, year);
                Integer DayOfYear = 1;
                parseDays.set(Calendar.DAY_OF_YEAR, DayOfYear);
                while (parseDays.get(Calendar.YEAR) == year) {
                    parseDayName = dayNames[parseDays.get(Calendar.DAY_OF_WEEK) - 1];
                    bw.write(TodoDiary.date_flag + " " + parseDayName + " "
                            + parseDays.get(Calendar.YEAR) + "-"
                            + format(Locale.US, "%02d", (parseDays.get(Calendar.MONTH) + 1))
                            + "-" + format(Locale.US, "%02d", parseDays.get(Calendar.DAY_OF_MONTH)) + TodoDiary.endline);
                    bw.write(TodoDiary.endline);
                    DayOfYear++;
                    parseDays.set(Calendar.DAY_OF_YEAR, DayOfYear);
                }
            }
            bw.flush();
            bw.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            returnResult = 2;
        }
        return returnResult;
    }


    private void openJumbleActivity(File jumblefile) {
        if (changes || entrytext_changed || marked_done_by_button) {
            save_changed_entrytext();
        }
        Intent jumble = new Intent(this, JumbleActivity.class);
        String jumbletext="";
        try {
            BufferedReader jumble_br = new BufferedReader
                    (new InputStreamReader(new FileInputStream(jumblefile), encoding));
            StringBuilder jumbletxt = new StringBuilder();
            String thisLine;
            while ((thisLine = jumble_br.readLine()) != null) {
                jumbletxt.append(thisLine).append("\n");
            }
            jumble_br.close();
            jumbletext = jumbletxt.toString();
        } catch (IOException e) {
            // Auto-generated catch block
            e.printStackTrace();
        }
        externalReadWritePermissionCheckFlag = 0; // No success/failure handling
        if (permissionCheck()) {
            jumble.putExtra("jumbletext", jumbletext);
            startActivityForResult(jumble, activity_requestCode_jumble);
        }
    }

    private boolean permissionCheck() {
        int PERMISSION_EXTERNAL_READ_WRITE = 1;
        boolean permissionSuccess = false;
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            permissionSuccess = true;
        } else {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE,
                                  Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_EXTERNAL_READ_WRITE
            );
        }
        return permissionSuccess;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        final int PERMISSION_EXTERNAL_READ_WRITE = 1;
        switch (requestCode) {
            case PERMISSION_EXTERNAL_READ_WRITE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (externalReadWritePermissionCheckFlag == 1) { // Create folder
                        dbAccess = true;
                        File directory_tmp = new File(dbFilepath);
                        if (! directory_tmp.exists()) {
                            boolean mkdirResult = directory_tmp.mkdirs();
                            if (!mkdirResult) {
                                showToast(getString(R.string.notCreateFolder));
                                dbAccess = false;
                            }
                        }
                        if (dbAccess) {
                            loadDB(selectedYear, db_name_full);
                            ButtonTexts();
                        }
                    } else if (externalReadWritePermissionCheckFlag >= 2 &&
                                externalReadWritePermissionCheckFlag <=3) { // loadDB (and perhaps create DB)
                        if (externalReadWritePermissionCheckFlag == 2) { //JumbleFileLoad
                            loadDB(0, jumbleFileName);
                        } else { //trad. DB-file
                            loadDB(selectedYear, db_name_full);
                        }
                    } else if (externalReadWritePermissionCheckFlag == 4) { // Save Changes
                        save_changed_entrytext();
                        populateText(selectedYear, selectedMonth, selectedDayOfMonth);
                        undone_count = countUndone(selectedYear);
                        ButtonTexts();
                    } else if (externalReadWritePermissionCheckFlag == 5) { // Change DBFolder
                        openChangeLocalCopyFolderIntent();
                    } else if (externalReadWritePermissionCheckFlag == 6) { // Save result from jumbleActivity
                        File jumblefile_write = new File(dbFilepath, jumbleFileName);
                        try {
                            BufferedWriter bw = new BufferedWriter
                                    (new OutputStreamWriter(new FileOutputStream(jumblefile_write), encoding));
                            bw.write(returneret_result.replaceAll("\n", endline));
                            bw.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    } else if (externalReadWritePermissionCheckFlag == 7) { // Save result from xxxxxxx
                        File raw_db_write = new File(dbFilepath, db_name_full);
                        try {
                            BufferedWriter bw = new BufferedWriter
                                    (new OutputStreamWriter(new FileOutputStream(raw_db_write),encoding));
                            bw.write(returneret_result_raw.replaceAll("\n", endline));
                            bw.close();
                        } catch (IOException e) {
                            // Auto-generated catch block
                            e.printStackTrace();
                        }

                    }
                } else {
                    if (externalReadWritePermissionCheckFlag >= 1 && externalReadWritePermissionCheckFlag <= 7) {
                        showToast(getString(R.string.goingToReadOnly));
                        dbAccess = false;
                        ButtonTexts();
                    }
                }
            }
        }
    }


    private void ButtonTexts() {
        //Opdatere udseendet af de forskellige knapper o.lign.
        //*************** SaveButton
        save_button.setEnabled(changes || entrytext_changed || marked_done_by_button);

        //*************** TodayButton
        //Grey hvis vi allerede er på idag
        if (dbAccess) {
            goto_today.setEnabled(!(selectedDayOfYear == thisDayOfYear && selectedYear == thisYear));
        } else {
            goto_today.setEnabled(false);
        }


        //*************** Entrytext
        entrytext.setFocusable(dbAccess);
        entrytext.setFocusableInTouchMode(dbAccess);

        //*************** DatePicker
        if (dbAccess) {
            date_picked.setSelectionMode(SELECTION_MODE_SINGLE);
        } else {
            date_picked.setSelectionMode(SELECTION_MODE_NONE);
        }

        //For hele Todoknapperiet: Skal kun vises, hvis enable_todo
        if (enable_todo) {
            //*************** MarkDoneButton
            markdone_button.setVisibility(View.VISIBLE);
            if ((marked_todo_in_file && !marked_done_by_button) || entrytext_changed_after_marked_done_by_button) {
                markdone_button.setText(R.string.mark_done);
                markdone_button.setTextColor(Color.rgb(128,0,0));
                markdone_button.setEnabled(dbAccess); //tændt hvis access til DB
            } else if (marked_done_by_button && marked_todo_in_file || marked_done_by_button && entrytext_changed) {
                markdone_button.setText(R.string.done);
                markdone_button.setTextColor(Color.rgb(0,107,0));
                markdone_button.setEnabled(dbAccess); //tændt hvis access til DB
            } else {
                markdone_button.setText(R.string.allset);
                markdone_button.setTextColor(Color.rgb(179,179,179));
                markdone_button.setEnabled(false);
            }

            //*************** PrevButton + NextButton
            prevtodo_button.setVisibility(View.VISIBLE);
            nexttodo_button.setVisibility(View.VISIBLE);
            if (todo_day_of_years.isEmpty()) {
                prevtodo_button.setEnabled(false);
                nexttodo_button.setEnabled(false);
            } else {
                if (selectedDayOfYear > todo_day_of_years.get(0)) {
                    prevtodo_button.setEnabled(true);
                } else {
                    prevtodo_button.setEnabled(false);
                }
                if (selectedDayOfYear < todo_day_of_years.get(todo_day_of_years.size()-1)) {
                    nexttodo_button.setEnabled(true);
                } else {
                    nexttodo_button.setEnabled(false);
                }
            }

            //*************** Todocounter
            if (dbAccess) {
                todocount.setVisibility(View.VISIBLE);
                if (undone_count != -1) {
                    if (selectedYear <= thisYear) {
                        todocount.setEnabled(true);
                        int temp_counter = undone_count;
                        if ((selectedYear < thisYear) || (selectedDayOfYear < thisDayOfYear)) {
                            if (marked_todo_in_file && marked_done_by_button) {
                                temp_counter = temp_counter - 1;
                            }
                        }
                        String tmpString = getString(R.string.leftovers) + " " + Integer.toString(selectedYear) +
                                ":\n" + Integer.toString(temp_counter) + " " + getString(R.string.entries);
                        todocount.setText(tmpString);
                    } else {
                        todocount.setEnabled(false);
                        String tmpString = getString(R.string.leftovers) + " " + Integer.toString(selectedYear) +
                                ":\n" + getString(R.string.emptyTodoCounter);
                        todocount.setText(tmpString);
                    }
                }
            } else {
                todocount.setVisibility(View.INVISIBLE);
            }
        } else {
            //Todoknapperne skjules, hvis todofunktionaliteten er slået fra
            markdone_button.setVisibility(View.INVISIBLE);
            prevtodo_button.setVisibility(View.INVISIBLE);
            nexttodo_button.setVisibility(View.INVISIBLE);
            todocount.setVisibility(View.INVISIBLE);
        }
    }

    private String create_date_marker(int yy, int mm, int dd) {
        // Lave en streng med dato-stemplet ud fra den valgte dato (som den findes i database-filerne)
        String[] dayNames = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        String parseDayName;
        Calendar parseDays = Calendar.getInstance();
        parseDays.set(Calendar.YEAR, yy);
        parseDays.set(Calendar.MONTH, mm);
        parseDays.set(Calendar.DAY_OF_MONTH, dd);
        parseDayName = dayNames[parseDays.get(Calendar.DAY_OF_WEEK)-1];
        return date_flag + " " +
                parseDayName + " " +
                parseDays.get(Calendar.YEAR) + "-" +
                String.format(Locale.US, "%02d", (parseDays.get(Calendar.MONTH) + 1)) + "-" +
                String.format(Locale.US, "%02d", parseDays.get(Calendar.DAY_OF_MONTH));
    }

    private void populateText(int yy, int mm, int dd) {
        // fyld indhold i entrytext
        count_date_position_in_text=0;
        boolean date_position_in_text_found = false;
        date_marker = create_date_marker(yy, mm, dd);
        externalReadWritePermissionCheckFlag = 0;
        if (permissionCheck()) {
            File file = new File(dbFilepath, db_name_full);
            try {
                BufferedReader br = new BufferedReader
                        (new InputStreamReader(new FileInputStream(file), encoding));
                boolean inside_date = false;
                StringBuilder date_text = new StringBuilder();
                String thisLine;
                while ((thisLine = br.readLine()) != null) {
                    if (!date_position_in_text_found) {
                        count_date_position_in_text = count_date_position_in_text + thisLine.length() + 1;
                    }
                    if (inside_date) {
                        if (thisLine.contains(date_flag)) {
                            inside_date = false;
                        } else {
                            date_text.append(thisLine).append("\n");
                        }
                    }
                    if (thisLine.contains(date_marker)) {
                        date_position_in_text_found = true;
                        marked_todo_in_file = thisLine.contains(todo_flag_sign);
                        inside_date = true;
                    }
                }
                br.close();
                prev_entrytext = date_text.toString();
                prev_marked_done_by_button_entrytext = date_text.toString();
                entrytext.setText(date_text.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void change_year(int new_year) {
        setDBNames(new_year);
        loadDB(new_year, db_name_full);
        undone_count = countUndone(new_year);
        ButtonTexts();
    }

    private int countUndone(int currentyear) {
        // Tæl hvor mange undone-markeringer der er i den valgte årgang.
        // Der tælles kun datoer, der ligger før "i dag"
        int counting = 0;
        todo_day_of_years.clear();
        externalReadWritePermissionCheckFlag = 0;
        if (permissionCheck()) {
            File file = new File(dbFilepath, db_name_full);
            try {
                BufferedReader br = new BufferedReader
                        (new InputStreamReader(new FileInputStream(file), encoding));
                String thisLine;
                if (currentyear < thisYear) {
                    while ((thisLine = br.readLine()) != null) {
                        if (thisLine.contains(todo_flag_sign) && thisLine.contains(date_flag)) {
                            todo_day_of_years.add(convertDateMarkerToDayOfYear(thisLine));
                            counting++;
                        }
                    }
                } else if (currentyear == thisYear) {
                    int currentday;
                    while ((thisLine = br.readLine()) != null) {
                        if (thisLine.contains(todo_flag_sign) && thisLine.contains(date_flag)) {
                            currentday = convertDateMarkerToDayOfYear(thisLine);
                            todo_day_of_years.add(currentday);
                            if (currentday <= thisDayOfYear) {
                                counting++;
                            }
                        }
                    }
                } else {
                    while ((thisLine = br.readLine()) != null) {
                        if (thisLine.contains(todo_flag_sign) && thisLine.contains(date_flag)) {
                            todo_day_of_years.add(convertDateMarkerToDayOfYear(thisLine));
                        }
                    }
                }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return counting;
        } else {
            return 0;
        }
    }

    private int convertDateMarkerToDayOfYear(String thisLine) {
        // Udregne hvilken dag-på-året som den pågældende date-marker repræsenterer
        int parsedmonth;
        int parsedday;
        Calendar parseDays = Calendar.getInstance();
        parseDays.set(Calendar.YEAR, selectedYear);

        String pmonth = thisLine.substring(
                thisLine.length()-(todo_flag_sign.length()+5),
                thisLine.length()-(todo_flag_sign.length()+3));
        try {
            parsedmonth = Integer.parseInt(pmonth)-1;
        } catch(NumberFormatException nfe) {
            parsedmonth = 0;
        }
        String pday = thisLine.substring(
                thisLine.length()-(todo_flag_sign.length()+2),
                thisLine.length()-todo_flag_sign.length());
        try {
            parsedday = Integer.parseInt(pday);
        } catch(NumberFormatException nfe) {
            parsedday = 1;
        }
        parseDays.set(Calendar.MONTH, parsedmonth);
        parseDays.set(Calendar.DAY_OF_MONTH, parsedday);
        return parseDays.get(Calendar.DAY_OF_YEAR);
    }

    private void setPickedDayOfYear(int yy, int dd) {
        // Ændre datepicker til at være den valgte day-of-year
        Calendar tmpSelectedDay = Calendar.getInstance();
        tmpSelectedDay.set(Calendar.YEAR, yy);
        tmpSelectedDay.set(Calendar.DAY_OF_YEAR, dd);
        date_picked.setSelectedDate(tmpSelectedDay);
        date_picked.setCurrentDate(tmpSelectedDay);
        makeChangesOnDateChange(date_picked.getSelectedDate());
    }

    private void setDBNames(int year) {
        //lave en streng med database-filnavnet ud fra det valgte årstal
        db_name_full = db_name+year+".txt";
    }

    private void save_changed_entrytext() {
        // Gem den tekst, der ligger i entrytext til den lokale databasefil (ikke dropbox endnu)
        externalReadWritePermissionCheckFlag = 4;
        if (permissionCheck()) {
            String current_entrytext = entrytext.getText().toString();
            if (!current_entrytext.endsWith("\n")) {
                current_entrytext = current_entrytext + "\n\n";
            }
            if (!current_entrytext.equals(prev_entrytext) || marked_done_by_button){
                //Hvis teksten er blevet ændret, skal ændringer gemmes
                changes = true;
                ButtonTexts();
                File file_read = new File(dbFilepath, db_name_full);
                File file_write = new File(dbFilepath, "_"+db_name_full);
                try {
                    BufferedReader br = new BufferedReader
                            (new InputStreamReader(new FileInputStream(file_read),encoding));
                    BufferedWriter bw = new BufferedWriter
                            (new OutputStreamWriter(new FileOutputStream(file_write),encoding));
                    boolean inside_date = false;
                    String thisLine;
                    while ((thisLine = br.readLine()) != null) {
                        if (inside_date) {
                            if (thisLine.contains(date_flag)) {
                                inside_date = false;
                                bw.write(thisLine + endline);
                            }
                        } else {
                            if(thisLine.contains(date_marker)) {
                                String writeline;
                                String temp_marked_done;
                                if ((marked_todo_in_file && !marked_done_by_button)
                                        || entrytext_changed_after_marked_done_by_button) {
                                    temp_marked_done = todo_flag;
                                } else {
                                    temp_marked_done = "";
                                }
                                writeline = thisLine.replace(todo_flag,  "") + temp_marked_done + endline;
                                bw.write(writeline);
                                inside_date = true;
                                current_entrytext = current_entrytext.split("\\n+$", 2)[0];
                                bw.write(current_entrytext.replaceAll("\n", endline).split("\\n+$", 2)[0] + endline + endline);
                                //Replace all newlines with the endline-sequence defined +
                                //...make certain that the entry ends with only one extra newline
                                // (removing all the last endlines + inserting two new ones
                            } else {
                                bw.write(thisLine + endline);
                            }
                        }
                    }
                    br.close();
                    bw.close();
                    marked_done_by_button = false;
                    changes = false;
                    if (!file_read.delete()) {
                        showToast(getString(R.string.cannotAccessDrive));
                        changes = true;
                    }
                    if (!file_write.renameTo(file_read)) {
                        showToast(getString(R.string.cannotAccessDrive));
                        changes = true;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    changes = true;
                }
            }
        }
    }

    public void showToast(String msg) {
        //Vise kortvarige advarsler
        Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        error.show();
    }

    private void lockScreenOrientation() {
        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    private void unlockScreenOrientation() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }
}
