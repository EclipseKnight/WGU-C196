package com.example.myapplication.ui.courses;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.data.StudentDatabase;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class EditCourseActivity extends AppCompatActivity {

    private static final String TAG = "EditCourseActivity";

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM d yyyy", Locale.US);

    private Button startDateButton, endDateButton;
    private StudentDatabase db;

    private String courseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_course);

        db = new StudentDatabase(getApplicationContext());

        // View references
        EditText titleEdit = findViewById(R.id.courseTitleTextEdit);
        Spinner statusSpinner = findViewById(R.id.spinner);
        EditText instructorNameEdit = findViewById(R.id.courseInstructorNameEdit);
        EditText instructorPhoneEdit = findViewById(R.id.courseInstructorPhoneEdit);
        EditText instructorEmailEdit = findViewById(R.id.courseInstructorEmailEdit);
        EditText notesEdit = findViewById(R.id.courseOptionalNoteTextEdit);
        startDateButton = findViewById(R.id.datePickerButton);
        endDateButton = findViewById(R.id.datePickerButton2);
        Button saveButton = findViewById(R.id.editCourseButton);

        // Intent data
        Intent intent = getIntent();
        courseId = intent.getStringExtra("courseId");

        titleEdit.setText(intent.getStringExtra("termName"));
        instructorNameEdit.setText(intent.getStringExtra("termInstructorName"));
        instructorPhoneEdit.setText(intent.getStringExtra("termInstructorPhone"));
        instructorEmailEdit.setText(intent.getStringExtra("termInstructorEmail"));
        notesEdit.setText(intent.getStringExtra("optionalNotes"));

        String status = intent.getStringExtra("termProgress");
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.course_status, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(adapter);
        statusSpinner.setSelection(Arrays.asList(getResources().getStringArray(R.array.course_status)).indexOf(status));

        setupDatePicker(startDateButton, intent.getStringExtra("termStart"));
        setupDatePicker(endDateButton, intent.getStringExtra("termEnd"));

        saveButton.setOnClickListener(v -> {
            String updatedTitle = titleEdit.getText().toString();
            String updatedStart = startDateButton.getText().toString();
            String updatedEnd = endDateButton.getText().toString();
            String updatedStatus = statusSpinner.getSelectedItem().toString();
            String updatedInstructorName = instructorNameEdit.getText().toString();
            String updatedInstructorPhone = instructorPhoneEdit.getText().toString();
            String updatedInstructorEmail = instructorEmailEdit.getText().toString();
            String updatedNotes = notesEdit.getText().toString();

            db.updateCourse(
                    courseId,
                    updatedTitle,
                    updatedStart,
                    updatedEnd,
                    updatedStatus,
                    updatedInstructorName,
                    updatedInstructorPhone,
                    updatedInstructorEmail,
                    updatedNotes
            );

            Toast.makeText(this, "Course Updated!", Toast.LENGTH_SHORT).show();
            getOnBackPressedDispatcher().onBackPressed();
        });

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }

    private void setupDatePicker(Button button, String initialDate) {
        try {
            LocalDate date = LocalDate.parse(initialDate, dateFormatter);
            button.setText(initialDate);

            button.setOnClickListener(v -> {
                Calendar calendar = Calendar.getInstance();
                calendar.set(date.getYear(), date.getMonthValue() - 1, date.getDayOfMonth());

                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        this,
                        (view, year, month, dayOfMonth) -> {
                            LocalDate selectedDate = LocalDate.of(year, month + 1, dayOfMonth);
                            button.setText(selectedDate.format(dateFormatter));
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                );

                datePickerDialog.show();
            });
        } catch (DateTimeParseException e) {
            Log.e(TAG, "Failed to parse date: " + initialDate, e);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        db = null;
        super.onDestroy();
    }
}
