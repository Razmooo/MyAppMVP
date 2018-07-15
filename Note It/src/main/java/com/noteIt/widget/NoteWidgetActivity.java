package com.noteIt.widget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.noteIt.R;
import com.noteIt.data.Note;
import com.noteIt.data.local.NoteRepository;
import com.noteIt.notes.NoteAdapter;

import java.util.ArrayList;

public class NoteWidgetActivity extends Activity
{

    private static final String PREFS_NAME = "com.myappmvp.widget.NoteWidgetActivity";
    public static final String PREF_TITLE = "Title";
    public static final String PREF_DESCRIPTION = "Description";
    public static final String PREF_ID = "Id";

    private GridView mGridView;
    private NoteRepository mTaskRepository;
    private NoteAdapter mNoteAdapter;

    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        setResult(RESULT_CANCELED);
        setContentView(R.layout.widget_note_menu);
        mGridView = findViewById(R.id.widgetNoteGridView);
        mTaskRepository = mTaskRepository.getINSTANCE(getApplication());
        mNoteAdapter = new NoteAdapter(new ArrayList<Note>(0) );
        mGridView.setAdapter(mNoteAdapter);
        mNoteAdapter.replaceNoteList(mTaskRepository.getNotesList());

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if(extras != null)
        {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        if(mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID)
            finish();


        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Note note = mNoteAdapter.getItem(i);
                String title = note.getTitle();
                String description = note.getDescription();
                String id = note.getId();

                Context context = NoteWidgetActivity.this;
                saveTask(context, mAppWidgetId, note);
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                NoteWidgetProvider.updateAppWidget(context,appWidgetManager,mAppWidgetId,title,description,id);

                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                setResult(RESULT_OK,resultValue);
                finish();



            }
        });

    }

    static void saveTask(Context context, int appwidgetId, Note note)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME,0).edit();
        prefs.putString(PREF_TITLE + appwidgetId, note.getTitle());
        prefs.putString(PREF_DESCRIPTION+ appwidgetId, note.getDescription());
        prefs.putString(PREF_ID + appwidgetId, note.getId());
        prefs.apply();
    }

    static String loadTitlePref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME,0);
        String title = prefs.getString(PREF_TITLE +appWidgetId,null);
        if(title != null)
        {
            return title;
        }
        else
        {
            return "Example";
        }
    }

    static String loadDescPref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String description = prefs.getString(PREF_DESCRIPTION + appWidgetId, null);
        if(description != null)
        {
            return description;
        }
        else
        {
            return "Example";
        }
    }

    static String loadIdPref(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String id = prefs.getString(PREF_ID + appWidgetId, null);
        if(id != null)
        {
            return id;
        }
        else
        {
            return "Example";
        }
    }
}
