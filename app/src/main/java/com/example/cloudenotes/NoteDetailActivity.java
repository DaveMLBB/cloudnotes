package com.example.cloudenotes;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class NoteDetailActivity extends AppCompatActivity {

    private EditText editTextNoteTitle;
    private EditText editTextNoteContent;
    private Button buttonSave;

    private static final int MIN_TITLE_LENGTH = 8;
    private static final int MIN_CONTENT_LENGTH = 10;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_note_detail); // Assicurati che il nome qui corrisponda al nome del file XML del layout

        editTextNoteTitle = findViewById(R.id.edit_text_note_title);
        editTextNoteContent = findViewById(R.id.edit_text_note_content);
        buttonSave = findViewById(R.id.button_save);

        if (getIntent().hasExtra("EXTRA_NOTE_TITLE") && getIntent().hasExtra("EXTRA_NOTE_CONTENT")) {
            String title = getIntent().getStringExtra("EXTRA_NOTE_TITLE");
            String content = getIntent().getStringExtra("EXTRA_NOTE_CONTENT");
            editTextNoteTitle.setText(title);
            editTextNoteContent.setText(content);
        }

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveNote();
            }
        });
    }



    private void saveNote() {
        String title = editTextNoteTitle.getText().toString().trim();
        String content = editTextNoteContent.getText().toString().trim();

        if (title.isEmpty() || title.length() < MIN_TITLE_LENGTH) {
            Toast.makeText(this, "Il titolo deve essere di almeno " + MIN_TITLE_LENGTH + " caratteri", Toast.LENGTH_SHORT).show();
            return;
        }

        if (content.isEmpty() || content.length() < MIN_CONTENT_LENGTH) {
            Toast.makeText(this, "Il contenuto deve essere di almeno " + MIN_CONTENT_LENGTH + " caratteri", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!title.isEmpty() && !content.isEmpty()) {
            NoteSecurityManager noteSecurityManager;
            try {
                noteSecurityManager = new NoteSecurityManager();
                noteSecurityManager.saveNote(this, title, content);
                Log.d("NoteDetailActivity", "Nota salvata: " + title);
                Toast.makeText(NoteDetailActivity.this, "Nota salvata", Toast.LENGTH_SHORT).show();

                finish(); // Chiude l'activity e torna indietro
            } catch (Exception e) {
                Toast.makeText(NoteDetailActivity.this, "Errore nel salvataggio della nota: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("NoteDetailActivity", "Errore nel salvataggio della nota", e);
            }
        } else {
            Toast.makeText(NoteDetailActivity.this, "Il titolo e il contenuto non possono essere vuoti", Toast.LENGTH_SHORT).show();
        }
    }


}
