package com.example.myapplication.ui.terms;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.ui.courses.AddCourseActivity;
import com.example.myapplication.ui.courses.CourseDetailsActivity;
import com.example.myapplication.R;
import com.example.myapplication.ui.common.SimpleMessageDialogFragment;
import com.example.myapplication.data.StudentDatabase;
import com.example.myapplication.ui.dashboard.DashboardActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TermDetailsActivity extends AppCompatActivity {

    private static final String TAG = "TermDetailsActivity";
    private static int termId = 0;
    private GestureDetector mDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_term_details);
        mDetector = new GestureDetector(this, new MyGestureListener());

        FloatingActionButton fab = findViewById(R.id.floatingActionButton2);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(TermDetailsActivity.this, AddCourseActivity.class);
            String[] keys = { "termMonthValue", "termName", "termStart", "termEnd", "termNameAndDate", "termId" };
            for (String key : keys) {
                intent.putExtra(key, getIntent().getStringExtra(key));
            }
            startActivity(intent);
        });

        String weeks = getIntent().getStringExtra("termMonthValue");
        TextView textView = findViewById(R.id.remainingWeeksValue);
        textView.setText(weeks);
        TextView textViewTitle = findViewById(R.id.termView);
        textViewTitle.setText(getIntent().getStringExtra("termNameAndDate"));

        Button[] courseButtons = {
                findViewById(R.id.course_button_first),
                findViewById(R.id.course_button_second),
                findViewById(R.id.course_button_third),
                findViewById(R.id.course_button_fourth),
                findViewById(R.id.course_button_fifth),
                findViewById(R.id.course_button_sixth),
                findViewById(R.id.course_button_seventh),
                findViewById(R.id.course_button_eighth),
                findViewById(R.id.course_button_ninth),
                findViewById(R.id.course_button_tenth)
        };

        for (Button btn : courseButtons) {
            btn.setVisibility(View.INVISIBLE);
        }

        try (StudentDatabase sdb = new StudentDatabase(getApplicationContext())) {
            termId = Integer.parseInt(Objects.requireNonNull(getIntent().getStringExtra("termId")));
            Log.d(TAG, "onCreate: term id " + termId);
            try (Cursor cursor = sdb.getCoursesByTermId(termId)) {
                if (cursor != null) {
                    int index = 0;
                    while (cursor.moveToNext() && index < courseButtons.length) {
                        String courseTitle = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                        Button btn = courseButtons[index];
                        btn.setText(courseTitle);
                        btn.setVisibility(View.VISIBLE);
                        btn.setOnClickListener(view -> openCourseDetails(courseTitle));
                        index++;
                    }
                }
            }
        } catch (Exception e) {
            Log.i(TAG, "ERROR onCreate: exception occurred", e);
        }
    }

    private void openCourseDetails(String courseTitle) {
        try (StudentDatabase db = new StudentDatabase(getApplicationContext());
             Cursor c = db.getCourses()) {

            Intent intent = new Intent(TermDetailsActivity.this, CourseDetailsActivity.class);

            while (c.moveToNext()) {
                if (courseTitle.equals(c.getString(c.getColumnIndexOrThrow("title")))) {
                    int courseId = Integer.parseInt(c.getString(c.getColumnIndexOrThrow("_id")));
                    String startDate = c.getString(c.getColumnIndexOrThrow("start_date"));
                    String endDate = c.getString(c.getColumnIndexOrThrow("end_date"));
                    String optionalNotes = c.getString(c.getColumnIndexOrThrow("optional_note"));

                    // Get instructor info from database columns, replace column names if different
                    String instructorName = "";
                    String instructorPhone = "";
                    String instructorEmail = "";

                    // Check if these columns exist, add null checks as needed
                    int nameIndex = c.getColumnIndex("instructor_name");
                    if (nameIndex != -1) instructorName = c.getString(nameIndex);

                    int phoneIndex = c.getColumnIndex("instructor_phone");
                    if (phoneIndex != -1) instructorPhone = c.getString(phoneIndex);

                    int emailIndex = c.getColumnIndex("instructor_email");
                    if (emailIndex != -1) instructorEmail = c.getString(emailIndex);

                    // Put instructor extras into the intent
                    intent.putExtra("termInstructorName", instructorName);
                    intent.putExtra("termInstructorPhone", instructorPhone);
                    intent.putExtra("termInstructorEmail", instructorEmail);

                    findRemainingWeeks(courseTitle, startDate, endDate, intent, courseId, optionalNotes);
                    break;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error opening course details", e);
            Toast.makeText(this, "Error loading course details", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        mDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    public void findRemainingWeeks(String termTitle, String startDate, String endDate, Intent intent, int courseId, String optionalNotes) {
        int startMonth = getMonthFromDateString(startDate);
        int endMonth = getMonthFromDateString(endDate);

        String[] startParts = startDate.split(" ");
        String[] endParts = endDate.split(" ");

        int startDay = Integer.parseInt(startParts[1]);
        int startYear = Integer.parseInt(startParts[2]);
        int endDay = Integer.parseInt(endParts[1]);
        int endYear = Integer.parseInt(endParts[2]);

        int numOfWeeks = calculateWeeksBetweenDates(startYear, startMonth, startDay, endYear, endMonth, endDay);

        // Copy relevant term extras
        String[] extrasToCopy = {
                "termMonthValue", "termName", "termStart", "termEnd",
                "termNameAndDate", "termId"
        };
        for (String key : extrasToCopy) {
            String value = getIntent().getStringExtra(key);
            if (value != null) {
                intent.putExtra(key, value);
            }
        }

        // Set course-specific extras
        intent.putExtra("courseMonthValue", String.valueOf(numOfWeeks));
        intent.putExtra("courseNameAndDate", termTitle + "\n" + startDate + " - \n" + endDate);
        intent.putExtra("courseId", String.valueOf(courseId));
        intent.putExtra("courseOptionalNotes", optionalNotes);
        intent.putExtra("courseStartCopy", startDate);
        intent.putExtra("courseEndCopy", endDate);
        intent.putExtra("courseTitleCopy", termTitle);

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


    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent event) {
            Log.i(TAG, "onDown: ");
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Log.i(TAG, "onFling: ");
            if (velocityX > 500) {
                startActivity(new Intent(TermDetailsActivity.this, DashboardActivity.class));
            }
            return true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == android.R.id.home) {
            startActivity(new Intent(TermDetailsActivity.this, DashboardActivity.class));
            return true;

        } else if (itemId == R.id.action_one) {
            try (StudentDatabase sdb1 = new StudentDatabase(getApplicationContext());
                 Cursor c1 = sdb1.getTermsById(termId)) {

                if (c1.moveToNext()) {
                    Intent intent = new Intent(TermDetailsActivity.this, EditTermActivity.class);
                    putTermExtras(intent);
                    startActivity(intent);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error in action_one menu item", e);
                Toast.makeText(this, "Error loading term data", Toast.LENGTH_SHORT).show();
            }
            return true;

        } else if (itemId == R.id.action_two) {
            try (StudentDatabase sdb = new StudentDatabase(getApplicationContext());
                 Cursor c = sdb.getCoursesByTermId(termId)) {

                if (c.moveToNext()) {
                    SimpleMessageDialogFragment dialog = SimpleMessageDialogFragment.newInstance(
                            "Deletion Error",
                            "Please delete all assessments before deleting a course."
                    );
                    dialog.show(getSupportFragmentManager(), "DeleteErrorDialog");
                } else {
                    confirmAndDeleteTerm(termId);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error in action_two menu item", e);
                Toast.makeText(this, "Error checking courses", Toast.LENGTH_SHORT).show();
            }
            return true;

        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void putTermExtras(Intent intent) {
        intent.putExtra("termMonthValue", getIntent().getStringExtra("termMonthValue"));
        intent.putExtra("termName", getIntent().getStringExtra("termName"));
        intent.putExtra("termStart", getIntent().getStringExtra("termStart"));
        intent.putExtra("termEnd", getIntent().getStringExtra("termEnd"));
        intent.putExtra("termNameAndDate", getIntent().getStringExtra("termNameAndDate"));
        intent.putExtra("termId", getIntent().getStringExtra("termId"));
    }

    private void confirmAndDeleteTerm(int termId) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete this term?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    Handler handler = new Handler(Looper.getMainLooper());

                    executor.execute(() -> {
                        StudentDatabase dbConnector = new StudentDatabase(getApplicationContext());
                        dbConnector.removeTerm(termId);

                        handler.post(() -> {
                            Toast.makeText(getApplicationContext(), "Term Deleted!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(TermDetailsActivity.this, DashboardActivity.class));
                            finish();
                        });
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
