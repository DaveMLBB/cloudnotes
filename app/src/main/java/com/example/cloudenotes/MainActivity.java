package com.example.cloudenotes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NotesAdapter.OnNoteListener {

    private static final int WRITE_STORAGE_GRANT = 1;
    private static final String THREAD_INFO = "render_thread_info";
    private static final String APP_INFO = "app_info";
    private RecyclerView recyclerView;
    private FloatingActionButton fabAddNote;
    private FloatingActionButton fabRefreshNotes; // Aggiungi questa variabile per il nuovo pulsante di aggiornamento

    private NotesAdapter adapter;
    private List<Note> noteList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler_view_notes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        noteList = new ArrayList<>();
        adapter = new NotesAdapter(noteList, this);
        recyclerView.setAdapter(adapter);

        loadNotes();

        fabAddNote = findViewById(R.id.fab_add_note);
        fabAddNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent intent = new Intent(MainActivity.this, NoteDetailActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace(); // Log dell'eccezione
                }
            }
        });

        fabRefreshNotes = findViewById(R.id.fab_refresh_notes);
        fabRefreshNotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Chiama il metodo per ricaricare le note
                loadNotes();
            }
        });
    }

    private void loadNotes() {
        NoteSecurityManager noteSecurityManager;
        try {
            noteSecurityManager = new NoteSecurityManager();
            File[] files = getFilesDir().listFiles(); // Assume all files here are notes

            List<Note> newNotes = new ArrayList<>();
            for (File file : files) {
                String name = file.getName();
                if (!(name.equals(THREAD_INFO) || name.equals(APP_INFO) )){
                    newNotes.add(new Note(file.getName()));
                }
            }

            // Update the list in the adapter
            adapter.setNotes(newNotes);
            adapter.notifyDataSetChanged();

        } catch (Exception e) {
            e.printStackTrace();
            // Handle the exception, such as showing an error message
        }
    }

    @Override
    public void onNoteClick(int position) {
        NoteSecurityManager noteSecurityManager = null;
        Intent intent = new Intent(MainActivity.this, NoteDetailActivity.class);
        try {
            noteSecurityManager = new NoteSecurityManager();
            Note clickedNote = noteList.get(position);
            String noteContent = noteSecurityManager.loadNote(this, clickedNote.getTitle());
            intent.putExtra("EXTRA_NOTE_TITLE", clickedNote.getTitle());
            intent.putExtra("EXTRA_NOTE_CONTENT", noteContent);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotes();
    }
}