package com.example.myapplication.ui.assesments;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.MainActivity;
import com.example.myapplication.receivers.AssessmentNotificationReceiver;
import com.example.myapplication.R;
import com.example.myapplication.data.StudentDatabase;
import com.example.myapplication.ui.courses.CourseDetailsActivity;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ViewAssessmentActivity extends AppCompatActivity {

    private static final String TAG = "ViewAssessmentActivity";
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assessment_details);

        // Setup action bar back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize UI with passed extras
        initViews();
    }

    private void initViews() {
        TextView titleView = findViewById(R.id.assessmentTitleTextView);
        TextView startDateView = findViewById(R.id.datePickerString);
        TextView endDateView = findViewById(R.id.datePickerString2);
        TextView statusView = findViewById(R.id.assessmentStatusTextView);

        Intent intent = getIntent();

        titleView.setText(intent.getStringExtra("assessmentTitle"));
        startDateView.setText(intent.getStringExtra("assessmentStart"));
        endDateView.setText(intent.getStringExtra("assessmentEnd"));
        statusView.setText(intent.getStringExtra("assessmentType"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_assessments, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent;
        int assessmentId = getIntent().getIntExtra("assessmentId", 0);
        int itemId = item.getItemId();

        if (itemId == android.R.id.home) {
            // Back pressed: go to CourseDetailsActivity if assessment exists
            StudentDatabase sdb2 = new StudentDatabase(this);
            try (Cursor c2 = sdb2.getAssessmentsById(assessmentId)) {
                if (c2.moveToNext()) {
                    intent = new Intent(this, CourseDetailsActivity.class);
                    putCourseAndTermExtras(intent);
                    startActivity(intent);
                }
            }
            return true;

        } else if (itemId == R.id.action_zero_assessments) {
            // Set assessment alerts
            Log.i(TAG, "Setting assessment alerts");

            intent = new Intent(this, AssessmentNotificationReceiver.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

            putAssessmentExtras(intent);
            putCourseAndTermExtras(intent);

            intent.putExtra("assessmentTitleCopy", getIntent().getStringExtra("assessmentTitle"));
            intent.putExtra("assessmentStartCopy", getIntent().getStringExtra("assessmentStart"));
            intent.putExtra("assessmentEndCopy", getIntent().getStringExtra("assessmentEnd"));

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
                Toast.makeText(this, "Assessment Alerts Set", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to set alerts: AlarmManager unavailable", Toast.LENGTH_LONG).show();
            }
            return true;

        } else if (itemId == R.id.action_one_assessments) {
            // Edit assessment
            StudentDatabase sdb1 = new StudentDatabase(this);
            try (Cursor c1 = sdb1.getAssessmentsById(assessmentId)) {
                if (c1.moveToNext()) {
                    intent = new Intent(this, EditAssessmentActivity.class);
                    putAssessmentExtras(intent);
                    putCourseAndTermExtras(intent);

                    intent.putExtra("monthValueCourse", getIntent().getStringExtra("monthValueCourse"));
                    intent.putExtra("courseTitle", getIntent().getStringExtra("courseTitle"));
                    intent.putExtra("courseStart", getIntent().getStringExtra("courseStart"));
                    intent.putExtra("courseEnd", getIntent().getStringExtra("courseEnd"));
                    intent.putExtra("courseType", getIntent().getStringExtra("courseType"));

                    startActivity(intent);
                }
            }
            return true;

        } else if (itemId == R.id.action_two_assessments) {
            // Delete assessment asynchronously
            deleteAssessmentInBackground(assessmentId);
            return true;

        } else {
            return super.onOptionsItemSelected(item);
        }
    }


    private void deleteAssessmentInBackground(int assessmentId) {
        executor.execute(() -> {
            StudentDatabase db = new StudentDatabase(this);
            db.removeAssessment(assessmentId);

            mainHandler.post(() -> {
                Toast.makeText(this, "Assessment Deleted!", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(this, CourseDetailsActivity.class);
                putCourseAndTermExtras(intent);
                startActivity(intent);
            });
        });
    }

    private void putCourseAndTermExtras(Intent intent) {
        intent.putExtra("termMonthValue", getIntent().getStringExtra("termMonthValue"));
        intent.putExtra("termName", getIntent().getStringExtra("termName"));
        intent.putExtra("termStart", getIntent().getStringExtra("termStart"));
        intent.putExtra("termEnd", getIntent().getStringExtra("termEnd"));
        intent.putExtra("termNameAndDate", getIntent().getStringExtra("termNameAndDate"));
        intent.putExtra("termId", getIntent().getStringExtra("termId"));

        intent.putExtra("courseMonthValue", getIntent().getStringExtra("courseMonthValue"));
        intent.putExtra("courseNameAndDate", getIntent().getStringExtra("courseNameAndDate"));
        intent.putExtra("courseId", getIntent().getStringExtra("courseId"));
        intent.putExtra("courseOptionalNotes", getIntent().getStringExtra("courseOptionalNotes"));
    }

    private void putAssessmentExtras(Intent intent) {
        intent.putExtra("assessmentMonthValue", getIntent().getStringExtra("assessmentMonthValue"));
        intent.putExtra("assessmentId", getIntent().getIntExtra("assessmentId", 0));
        intent.putExtra("assessmentTitle", getIntent().getStringExtra("assessmentTitle"));
        intent.putExtra("assessmentStart", getIntent().getStringExtra("assessmentStart"));
        intent.putExtra("assessmentEnd", getIntent().getStringExtra("assessmentEnd"));
        intent.putExtra("assessmentType", getIntent().getStringExtra("assessmentType"));
    }
}
