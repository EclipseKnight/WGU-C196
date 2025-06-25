package com.example.myapplication.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class StudentDatabase extends SQLiteOpenHelper {

    private static final String TAG = "StudentDatabase";
    private static final String DATABASE_NAME = "terms.db";
    private static final int VERSION = 1;

    public StudentDatabase(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    private static final class TermsTable {
        private static final String TABLE = "terms";
        private static final String COL_ID = "_id";
        private static final String COL_TITLE = "title";
        private static final String COL_START_DATE = "start_date";
        private static final String COL_END_DATE = "end_date";
    }

    private static final class CoursesTable {
        private static final String TABLE = "courses";
        private static final String COL_ID = "_id";
        private static final String COL_TITLE = "title";
        private static final String COL_START_DATE = "start_date";
        private static final String COL_END_DATE = "end_date";
        private static final String COL_STATUS = "status";
        private static final String COL_INS_NAME = "instructor_name";
        private static final String COL_INS_PHONE = "instructor_phone";
        private static final String COL_INS_EMAIL = "instructor_email";
        private static final String COL_TERM_ID = "term_id";
        private static final String COL_OPTIONAL_NOTE = "optional_note";
    }

    private static final class AssessmentsTable {
        private static final String TABLE = "assessments";
        private static final String COL_ID = "_id";
        private static final String COL_TITLE = "title";
        private static final String COL_START_DATE = "start_date";
        private static final String COL_END_DATE = "end_date";
        private static final String COL_TYPE = "type";
        private static final String COL_COURSE_ID = "course_id";
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + StudentDatabase.TermsTable.TABLE + " (" +
                StudentDatabase.TermsTable.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                StudentDatabase.TermsTable.COL_TITLE + " TEXT, " +
                StudentDatabase.TermsTable.COL_START_DATE + " DATE, " +
                StudentDatabase.TermsTable.COL_END_DATE + " DATE" + ")");

        Log.i(TAG, "onCreate: terms table created");

        db.execSQL("CREATE TABLE " + StudentDatabase.CoursesTable.TABLE + " (" +
                StudentDatabase.CoursesTable.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                StudentDatabase.CoursesTable.COL_TITLE + " TEXT, " +
                StudentDatabase.CoursesTable.COL_START_DATE + " DATE, " +
                StudentDatabase.CoursesTable.COL_END_DATE + " DATE, " +
                StudentDatabase.CoursesTable.COL_STATUS + " TEXT, " +
                StudentDatabase.CoursesTable.COL_INS_NAME + " TEXT, " +
                StudentDatabase.CoursesTable.COL_INS_PHONE + " TEXT, " +
                StudentDatabase.CoursesTable.COL_INS_EMAIL + " TEXT, " +
                StudentDatabase.CoursesTable.COL_TERM_ID + " INTEGER, " +
                StudentDatabase.CoursesTable.COL_OPTIONAL_NOTE + " TEXT " + ")");

        Log.i(TAG, "onCreate: courses table created");

        db.execSQL("CREATE TABLE " + StudentDatabase.AssessmentsTable.TABLE + " (" +
                StudentDatabase.AssessmentsTable.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                StudentDatabase.AssessmentsTable.COL_TITLE + " TEXT, " +
                StudentDatabase.AssessmentsTable.COL_START_DATE + " DATE, " +
                StudentDatabase.AssessmentsTable.COL_END_DATE + " DATE, " +
                StudentDatabase.AssessmentsTable.COL_TYPE + " TEXT, " +
                StudentDatabase.AssessmentsTable.COL_COURSE_ID + " INTEGER " + ")");

        Log.i(TAG, "onCreate: assessments table created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + StudentDatabase.TermsTable.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + StudentDatabase.CoursesTable.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + StudentDatabase.AssessmentsTable.TABLE);
        onCreate(db);
    }

    public void addTerm(String title, String start, String end) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(StudentDatabase.TermsTable.COL_TITLE, title);
            values.put(StudentDatabase.TermsTable.COL_START_DATE, start);
            values.put(StudentDatabase.TermsTable.COL_END_DATE, end);
            db.insert(StudentDatabase.TermsTable.TABLE, null, values);
        } catch (Exception e) {
            Log.e(TAG, "addTerm: error", e);
        }
    }

    public void updateTerm(String id, String title, String start, String end) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(StudentDatabase.TermsTable.COL_TITLE, title);
            values.put(StudentDatabase.TermsTable.COL_START_DATE, start);
            values.put(StudentDatabase.TermsTable.COL_END_DATE, end);
            db.update(StudentDatabase.TermsTable.TABLE, values, StudentDatabase.TermsTable.COL_ID + " = ?", new String[]{id});
        } catch (Exception e) {
            Log.e(TAG, "updateTerm: error", e);
        }
    }

    public void updateAssessment(String id, String title, String start, String end, String type) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(StudentDatabase.AssessmentsTable.COL_TITLE, title);
            values.put(StudentDatabase.AssessmentsTable.COL_START_DATE, start);
            values.put(StudentDatabase.AssessmentsTable.COL_END_DATE, end);
            values.put(StudentDatabase.AssessmentsTable.COL_TYPE, type);
            db.update(StudentDatabase.AssessmentsTable.TABLE, values, StudentDatabase.AssessmentsTable.COL_ID + " = ?", new String[]{id});
        } catch (Exception e) {
            Log.e(TAG, "updateAssessment: error", e);
        }
    }

    public void updateCourse(String id, String title, String start, String end, String status,
                             String iName, String iPhone, String iEmail, String optionalNote) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(StudentDatabase.CoursesTable.COL_TITLE, title);
            values.put(StudentDatabase.CoursesTable.COL_START_DATE, start);
            values.put(StudentDatabase.CoursesTable.COL_END_DATE, end);
            values.put(StudentDatabase.CoursesTable.COL_STATUS, status);
            values.put(StudentDatabase.CoursesTable.COL_INS_NAME, iName);
            values.put(StudentDatabase.CoursesTable.COL_INS_PHONE, iPhone);
            values.put(StudentDatabase.CoursesTable.COL_INS_EMAIL, iEmail);
            values.put(StudentDatabase.CoursesTable.COL_OPTIONAL_NOTE, optionalNote);
            db.update(StudentDatabase.CoursesTable.TABLE, values, StudentDatabase.CoursesTable.COL_ID + " = ?", new String[]{id});
        } catch (Exception e) {
            Log.e(TAG, "updateCourse: error", e);
        }
    }

    public void removeTerm(int termId) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            db.delete(StudentDatabase.TermsTable.TABLE, StudentDatabase.TermsTable.COL_ID + " = ?", new String[]{String.valueOf(termId)});
        } catch (Exception e) {
            Log.e(TAG, "removeTerm: error", e);
        }
    }

    public void removeCourse(int courseId) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            db.delete(StudentDatabase.CoursesTable.TABLE, StudentDatabase.CoursesTable.COL_ID + " = ?", new String[]{String.valueOf(courseId)});
        } catch (Exception e) {
            Log.e(TAG, "removeCourse: error", e);
        }
    }

    public void removeAssessment(int assessmentId) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            db.delete(StudentDatabase.AssessmentsTable.TABLE, StudentDatabase.AssessmentsTable.COL_ID + " = ?", new String[]{String.valueOf(assessmentId)});
        } catch (Exception e) {
            Log.e(TAG, "removeAssessment: error", e);
        }
    }

    public Cursor getTerms() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + StudentDatabase.TermsTable.TABLE, null);
    }

    public Cursor getTermsById(int termId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + StudentDatabase.TermsTable.TABLE + " WHERE " + StudentDatabase.TermsTable.COL_ID + " = ?", new String[]{String.valueOf(termId)});
    }

    public void addCourse(String title, String start, String end, String status,
                          String iName, String iPhone, String iEmail, int termId, String optionalNote) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(StudentDatabase.CoursesTable.COL_TITLE, title);
            values.put(StudentDatabase.CoursesTable.COL_START_DATE, start);
            values.put(StudentDatabase.CoursesTable.COL_END_DATE, end);
            values.put(StudentDatabase.CoursesTable.COL_STATUS, status);
            values.put(StudentDatabase.CoursesTable.COL_INS_NAME, iName);
            values.put(StudentDatabase.CoursesTable.COL_INS_PHONE, iPhone);
            values.put(StudentDatabase.CoursesTable.COL_INS_EMAIL, iEmail);
            values.put(StudentDatabase.CoursesTable.COL_TERM_ID, termId);
            values.put(StudentDatabase.CoursesTable.COL_OPTIONAL_NOTE, optionalNote);
            db.insert(StudentDatabase.CoursesTable.TABLE, null, values);
        } catch (Exception e) {
            Log.e(TAG, "addCourse: error", e);
        }
    }

    public void addAssessment(String title, String start, String end, String type, int courseId) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(StudentDatabase.AssessmentsTable.COL_TITLE, title);
            values.put(StudentDatabase.AssessmentsTable.COL_START_DATE, start);
            values.put(StudentDatabase.AssessmentsTable.COL_END_DATE, end);
            values.put(StudentDatabase.AssessmentsTable.COL_TYPE, type);
            values.put(StudentDatabase.AssessmentsTable.COL_COURSE_ID, courseId);
            db.insert(StudentDatabase.AssessmentsTable.TABLE, null, values);
        } catch (Exception e) {
            Log.e(TAG, "addAssessment: error", e);
        }
    }

    public Cursor getCourses() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + StudentDatabase.CoursesTable.TABLE, null);
    }

    public Cursor getCoursesById(int courseId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + StudentDatabase.CoursesTable.TABLE + " WHERE " + StudentDatabase.CoursesTable.COL_ID + " = ?", new String[]{String.valueOf(courseId)});
    }

    public Cursor getCoursesByTermId(int termId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + StudentDatabase.CoursesTable.TABLE + " WHERE " + StudentDatabase.CoursesTable.COL_TERM_ID + " = ?", new String[]{String.valueOf(termId)});
    }

    public Cursor getAssessments() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + StudentDatabase.AssessmentsTable.TABLE, null);
    }

    public Cursor getAssessmentsById(int assessmentId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + StudentDatabase.AssessmentsTable.TABLE + " WHERE " + StudentDatabase.AssessmentsTable.COL_ID + " = ?", new String[]{String.valueOf(assessmentId)});
    }

    public Cursor getAssessmentsByCourseId(int courseId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + StudentDatabase.AssessmentsTable.TABLE + " WHERE " + StudentDatabase.AssessmentsTable.COL_COURSE_ID + " = ?", new String[]{String.valueOf(courseId)});
    }

}
