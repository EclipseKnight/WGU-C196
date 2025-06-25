package com.example.myapplication.ui.courses;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.ui.assessments.AddAssessmentActivity;
import com.example.myapplication.MainActivity;
import com.example.myapplication.receivers.CourseNotificationReceiver;
import com.example.myapplication.R;
import com.example.myapplication.ui.common.SimpleMessageDialogFragment;
import com.example.myapplication.ui.assessments.AssessmentDetailsActivity;
import com.example.myapplication.data.StudentDatabase;
import com.example.myapplication.ui.dashboard.DashboardActivity;
import com.example.myapplication.ui.terms.TermDetailsActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CourseDetailsActivity extends AppCompatActivity {

    private final int courseId = 0;
    private GestureDetector mDetector;
    private static final String TAG = "CourseDetailsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_details);
        mDetector = new GestureDetector(this, new MyGestureListener());

        StudentDatabase sdb = new StudentDatabase(getApplicationContext());
        int courseId = Integer.parseInt(getIntent().getStringExtra("courseId"));
        Cursor assessmentCursor = sdb.getAssessmentsByCourseId(courseId);

        setupFab(courseId);
        setupTextViews();
        setupInstructorInfo();  // <-- Added instructor info setup here

        Button[] buttons = setupButtons();

        for (Button btn : buttons) {
            setupAssessmentClick(btn);
            btn.setVisibility(View.INVISIBLE);
        }

        if (assessmentCursor != null && assessmentCursor.getCount() > 0) {
            int index = 0;
            while (assessmentCursor.moveToNext() && index < buttons.length) {
                buttons[index].setText(assessmentCursor.getString(1));
                buttons[index].setVisibility(View.VISIBLE);
                index++;
            }
        }
    }

    private void setupFab(int courseId) {
        FloatingActionButton fab = findViewById(R.id.floatingActionButton3);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(this, AddAssessmentActivity.class);
            String[] extras = {
                    "termMonthValue", "termName", "termStart", "termEnd",
                    "termNameAndDate", "termId", "courseMonthValue",
                    "courseNameAndDate", "courseOptionalNotes"
            };

            for (String key : extras) {
                intent.putExtra(key, getIntent().getStringExtra(key));
            }

            intent.putExtra("course_id", courseId);
            intent.putExtra("courseId", String.valueOf(courseId));
            startActivity(intent);
        });
    }

    private void setupTextViews() {
        ((TextView) findViewById(R.id.remainingWeeksValue2))
                .setText(getIntent().getStringExtra("courseMonthValue"));

        ((TextView) findViewById(R.id.termView2))
                .setText(getIntent().getStringExtra("courseNameAndDate"));

        TextView notesView = findViewById(R.id.termViewNotes);
        String notes = getIntent().getStringExtra("courseOptionalNotes");
        notesView.setText(notes);
        if (notes == null || notes.isEmpty()) {
            notesView.setVisibility(View.GONE);
        }
    }

    /**
     * New method to populate instructor info TextViews.
     */
    private void setupInstructorInfo() {
        String instructorName = getIntent().getStringExtra("termInstructorName");
        String instructorPhone = getIntent().getStringExtra("termInstructorPhone");
        String instructorEmail = getIntent().getStringExtra("termInstructorEmail");

        TextView nameView = findViewById(R.id.instructorName);
        TextView phoneView = findViewById(R.id.instructorPhone);
        TextView emailView = findViewById(R.id.instructorEmail);

        if (instructorName != null && !instructorName.isEmpty()) {
            nameView.setText(instructorName);
        } else {
            nameView.setText("No instructor name");
        }

        if (instructorPhone != null && !instructorPhone.isEmpty()) {
            phoneView.setText(instructorPhone);
        } else {
            phoneView.setText("No phone number");
        }

        if (instructorEmail != null && !instructorEmail.isEmpty()) {
            emailView.setText(instructorEmail);
        } else {
            emailView.setText("No email");
        }
    }

    private Button[] setupButtons() {
        return new Button[]{
                findViewById(R.id.assessment_button_first),
                findViewById(R.id.assessment_button_second),
                findViewById(R.id.assessment_button_third),
                findViewById(R.id.assessment_button_fourth),
                findViewById(R.id.assessment_button_fifth),
                findViewById(R.id.assessment_button_sixth),
                findViewById(R.id.assessment_button_seventh),
                findViewById(R.id.assessment_button_eighth),
                findViewById(R.id.assessment_button_ninth),
                findViewById(R.id.assessment_button_tenth)
        };
    }

    private void setupAssessmentClick(Button button) {
        button.setOnClickListener(view -> {
            try (StudentDatabase sdb = new StudentDatabase(getApplicationContext())) {
                Cursor cursor = sdb.getAssessments();
                Intent intent = new Intent(this, AssessmentDetailsActivity.class);

                while (cursor.moveToNext()) {
                    String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                    if (button.getText().toString().equals(title)) {
                        int assessmentId = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
                        String startDate = cursor.getString(cursor.getColumnIndexOrThrow("start_date"));
                        String endDate = cursor.getString(cursor.getColumnIndexOrThrow("end_date"));
                        String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));

                        findRemainingWeeks(title, startDate, endDate, intent, assessmentId, type);
                        break;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading assessment", e);
                Toast.makeText(this, "Failed to load assessment.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(@NonNull MotionEvent event) {
            Log.i(TAG, "onDown: ");
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
            Log.i(TAG, "onFling: ");
            if (velocityX > 500) {
                startActivity(new Intent(CourseDetailsActivity.this, DashboardActivity.class));
            }
            return true;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_courses, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        String courseIdStr = getIntent().getStringExtra("courseId");
        int courseId = -1;
        try {
            assert courseIdStr != null;
            courseId = Integer.parseInt(courseIdStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid course ID", Toast.LENGTH_SHORT).show();
            return super.onOptionsItemSelected(item);
        }

        try (StudentDatabase db = new StudentDatabase(getApplicationContext())) {
            Intent intent;

            if (itemId == android.R.id.home) {
                try (Cursor courseCursor = db.getCoursesById(courseId)) {
                    if (courseCursor != null && courseCursor.moveToNext()) {
                        intent = new Intent(this, TermDetailsActivity.class);
                        putTermExtras(intent);
                        intent.putExtra("termProgress", courseCursor.getString(4));
                        startActivity(intent);
                    }
                }
                return true;

            } else if (itemId == R.id.action_zero_courses) {
                Toast.makeText(this, "Course Alerts Set", Toast.LENGTH_SHORT).show();

                intent = new Intent(this, CourseNotificationReceiver.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                String[] keys = {
                        "termMonthValue", "termName", "termStart", "termEnd",
                        "termNameAndDate", "termId", "courseMonthValue",
                        "courseNameAndDate", "courseId", "courseOptionalNotes",
                        "courseTitleCopy", "courseStartCopy", "courseEndCopy"
                };
                for (String key : keys) {
                    String value = getIntent().getStringExtra(key);
                    if (value != null) {
                        intent.putExtra(key, value);
                    }
                }

                PendingIntent sender = PendingIntent.getBroadcast(
                        this,
                        MainActivity.numAlert++,
                        intent,
                        PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
                );

                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                if (alarmManager != null) {
                    alarmManager.setRepeating(
                            AlarmManager.RTC_WAKEUP,
                            System.currentTimeMillis(),
                            AlarmManager.INTERVAL_HALF_DAY,
                            sender
                    );
                }
                return true;

            } else if (itemId == R.id.action_one_courses) {
                try (Cursor courseCursor = db.getCoursesById(courseId)) {
                    if (courseCursor != null && courseCursor.moveToNext()) {
                        intent = new Intent(this, EditCourseActivity.class);
                        intent.putExtra("courseId", courseIdStr);
                        intent.putExtra("termName", courseCursor.getString(1));
                        intent.putExtra("termStart", courseCursor.getString(2));
                        intent.putExtra("termEnd", courseCursor.getString(3));
                        intent.putExtra("termProgress", courseCursor.getString(4));
                        intent.putExtra("termInstructorName", courseCursor.getString(5));
                        intent.putExtra("termInstructorPhone", courseCursor.getString(6));
                        intent.putExtra("termInstructorEmail", courseCursor.getString(7));
                        intent.putExtra("optionalNotes", courseCursor.getString(9));
                        startActivity(intent);
                    }
                }
                return true;

            } else if (itemId == R.id.action_two_courses) {
                try (Cursor assessmentCursor = db.getAssessmentsByCourseId(courseId)) {
                    if (assessmentCursor != null && assessmentCursor.moveToNext()) {
                        SimpleMessageDialogFragment dialog = SimpleMessageDialogFragment.newInstance(
                                "Deletion Error",
                                "Please delete all assessments before deleting this course."
                        );
                        dialog.show(getSupportFragmentManager(), "DeleteErrorDialog");
                    } else {
                        confirmAndDeleteCourse(courseId);
                    }
                }
                return true;

            } else if (itemId == R.id.action_three_courses) {
                intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, getIntent().getStringExtra("courseOptionalNotes"));
                intent.setType("text/plain");
                startActivity(Intent.createChooser(intent, null));
                return true;

            } else {
                return super.onOptionsItemSelected(item);
            }
        } catch (Exception e) {
            Log.e(TAG, "Database operation failed in onOptionsItemSelected", e);
            Toast.makeText(this, "Database error occurred.", Toast.LENGTH_SHORT).show();
            return super.onOptionsItemSelected(item);
        }
    }


    /**
     * Show confirmation dialog and delete course if confirmed.
     */
    private void confirmAndDeleteCourse(int courseId) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete this course?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    Handler handler = new Handler(Looper.getMainLooper());

                    executor.execute(() -> {
                        try (StudentDatabase db = new StudentDatabase(getApplicationContext())) {
                            db.removeCourse(courseId);

                            handler.post(() -> {
                                Toast.makeText(getApplicationContext(), "Course Deleted!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(this, TermDetailsActivity.class);
                                putTermExtras(intent);
                                startActivity(intent);
                                finish();
                            });
                        } catch (Exception e) {
                            Log.e(TAG, "Error deleting course", e);
                            handler.post(() -> Toast.makeText(this, "Failed to delete course", Toast.LENGTH_SHORT).show());
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Helper to put all term extras into the intent from the current intent.
     */
    private void putTermExtras(Intent intent) {
        String[] termExtras = {
                "termMonthValue", "termName", "termStart", "termEnd",
                "termNameAndDate", "termId"
        };
        for (String key : termExtras) {
            String value = getIntent().getStringExtra(key);
            if (value != null) {
                intent.putExtra(key, value);
            }
        }
    }


    public void findRemainingWeeks(String termTitle, String startDate, String endDate, Intent intent, int termId, String type) {
        int startMonth = getMonthFromDateString(startDate);
        int endMonth = getMonthFromDateString(endDate);

        String[] startParts = startDate.split(" ");
        String[] endParts = endDate.split(" ");

        int startDay = Integer.parseInt(startParts[1]);
        int startYear = Integer.parseInt(startParts[2]);
        int endDay = Integer.parseInt(endParts[1]);
        int endYear = Integer.parseInt(endParts[2]);

        int numOfWeeks = calculateWeeksBetweenDates(startYear, startMonth, startDay, endYear, endMonth, endDay);

        // Copy over relevant extras if they exist
        String[] extrasToCopy = {
                "termMonthValue", "termName", "termStart", "termEnd",
                "termNameAndDate", "termId", "courseMonthValue",
                "courseNameAndDate", "courseId", "courseOptionalNotes"
        };

        for (String key : extrasToCopy) {
            String value = getIntent().getStringExtra(key);
            if (value != null) {
                intent.putExtra(key, value);
            }
        }

        intent.putExtra("assessmentMonthValue", String.valueOf(numOfWeeks));
        intent.putExtra("assessmentId", termId);
        intent.putExtra("assessmentTitle", termTitle);
        intent.putExtra("assessmentStart", startDate);
        intent.putExtra("assessmentEnd", endDate);
        intent.putExtra("assessmentType", type);

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
