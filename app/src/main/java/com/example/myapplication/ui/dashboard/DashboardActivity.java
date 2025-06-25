package com.example.myapplication.ui.dashboard;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.ui.terms.AddTermActivity;
import com.example.myapplication.R;
import com.example.myapplication.ui.terms.TermDetailsActivity;
import com.example.myapplication.data.StudentDatabase;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity {

    private static final String TAG = "DashboardActivity";
    public static int term_id;

    private Button[] buttons;
    private StudentDatabase studentDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        studentDatabase = new StudentDatabase(this);

        buttons = new Button[]{
                findViewById(R.id.button_first),
                findViewById(R.id.button_second),
                findViewById(R.id.button_third),
                findViewById(R.id.button_fourth),
                findViewById(R.id.button_fifth),
                findViewById(R.id.button_sixth),
                findViewById(R.id.button_seventh),
                findViewById(R.id.button_eighth),
                findViewById(R.id.button_ninth),
                findViewById(R.id.button_tenth)
        };

        for (Button btn : buttons) {
            btn.setVisibility(View.INVISIBLE);
        }

        loadTerms();

        FloatingActionButton fab = findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(view -> startActivity(
                new Intent(DashboardActivity.this, AddTermActivity.class)
        ));
    }

    private void loadTerms() {
        try (Cursor cursor = studentDatabase.getTerms()) {
            if (cursor != null && cursor.moveToFirst()) {
                int index = 0;
                do {
                    if (index >= buttons.length) break;
                    String termTitle = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                    buttons[index].setText(termTitle);
                    buttons[index].setVisibility(View.VISIBLE);

                    final int btnIndex = index;
                    buttons[btnIndex].setOnClickListener(view ->
                            handleTermButtonClick(termTitle));
                    index++;
                } while (cursor.moveToNext());
            }
        }
    }

    private void handleTermButtonClick(String termTitle) {
        try (Cursor cursor = studentDatabase.getTerms()) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                    if (termTitle.equals(title)) {
                        term_id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
                        String startDate = cursor.getString(cursor.getColumnIndexOrThrow("start_date"));
                        String endDate = cursor.getString(cursor.getColumnIndexOrThrow("end_date"));

                        Intent intent = new Intent(this, TermDetailsActivity.class);
                        findRemainingWeeks(termTitle, startDate, endDate, intent, term_id);
                        break;
                    }
                } while (cursor.moveToNext());
            }
        }
    }

    private void findRemainingWeeks(String termTitle, String startDate, String endDate, Intent intent, int termId) {
        int startMonth = getMonthFromDateString(startDate);
        int endMonth = getMonthFromDateString(endDate);

        String[] startParts = startDate.split(" ");
        String[] endParts = endDate.split(" ");

        int startDay = Integer.parseInt(startParts[1]);
        int startYear = Integer.parseInt(startParts[2]);
        int endDay = Integer.parseInt(endParts[1]);
        int endYear = Integer.parseInt(endParts[2]);

        int numOfWeeks = calculateWeeksBetweenDates(startYear, startMonth, startDay, endYear, endMonth, endDay);

        intent.putExtra("termMonthValue", String.valueOf(numOfWeeks));
        intent.putExtra("termName", termTitle);
        intent.putExtra("termStart", startDate);
        intent.putExtra("termEnd", endDate);
        intent.putExtra("termNameAndDate", termTitle + "\n" + startDate + " - \n" + endDate);
        intent.putExtra("termId", String.valueOf(termId));

        startActivity(intent);
    }

    private int calculateWeeksBetweenDates(int startYear, int startMonth, int startDay,
                                           int endYear, int endMonth, int endDay) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy MM dd", Locale.US);
        try {
            Date startDate = sdf.parse(startYear + " " + startMonth + " " + startDay);
            Date endDate = sdf.parse(endYear + " " + endMonth + " " + endDay);
            if (startDate != null && endDate != null) {
                long diffMillis = endDate.getTime() - startDate.getTime();
                return (int) (diffMillis / (1000 * 60 * 60 * 24)) / 7;
            }
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing dates", e);
        }
        return 0;
    }

    private int getMonthFromDateString(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return 0;

        Map<String, Integer> monthMap = new HashMap<String, Integer>() {{
            put("JAN", 1); put("FEB", 2); put("MAR", 3); put("APR", 4);
            put("MAY", 5); put("JUN", 6); put("JUL", 7); put("AUG", 8);
            put("SEP", 9); put("OCT", 10); put("NOV", 11); put("DEC", 12);
        }};

        String[] parts = dateStr.split(" ");
        if (parts.length < 1) return 0;

        Integer month = monthMap.get(parts[0].toUpperCase(Locale.US));
        return (month != null) ? month : 0;
    }
}
