package com.example.cloudenotes;

import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.Key;
import java.security.KeyStore;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class NoteSecurityManager {

    private static final String KEY_ALIAS = "^9NdXFFmTTX893wk"; // Un alias unico per la tua chiave
    private static final String KEYSTORE_PROVIDER = "AndroidKeyStore";
    private static final String ALGORITHM = "AES/CBC/PKCS7Padding"; // Aggiunta del modo e del padding
    private static final String TRANSFORMATION = "AES/CBC/PKCS7Padding";
    private static final String CHARSET_NAME = "UTF-8";
    private IvParameterSpec ivSpec; // Per salvare il vettore di inizializzazione

    private KeyStore keyStore;

    public NoteSecurityManager() throws Exception {
        keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER);
        keyStore.load(null);
        createNewKeyIfNotExists();
    }

    private void createNewKeyIfNotExists() throws Exception {
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER);

            KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .setRandomizedEncryptionRequired(true) // Sicurezza migliorata
                    .build();

            keyGenerator.init(keyGenParameterSpec);
            keyGenerator.generateKey();
        }
    }

    private Cipher getCipherInstance(int mode, Key key) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        if (mode == Cipher.ENCRYPT_MODE) {
            cipher.init(mode, key);
            ivSpec = new IvParameterSpec(cipher.getIV()); // Ottiene il vettore di inizializzazione durante la cifratura
        } else {
            cipher.init(mode, key, ivSpec); // Utilizza il vettore di inizializzazione per la decifratura
        }
        return cipher;
    }

    public void saveNote(Context context, String noteTitle, String noteContent) throws Exception {
        Key key = keyStore.getKey(KEY_ALIAS, null);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] iv = cipher.getIV(); // Ottieni il vettore di inizializzazione generato

        byte[] encryptedData = cipher.doFinal(noteContent.getBytes(CHARSET_NAME));

        // Salva il vettore di inizializzazione insieme ai dati cifrati
        try (FileOutputStream fos = new FileOutputStream(new File(context.getFilesDir(), noteTitle))) {
            fos.write(iv); // Salva l'IV all'inizio del file
            fos.write(encryptedData); // Poi salva i dati cifrati
        }
        loadNote(context, noteTitle);
    }

    public String loadNote(Context context, String noteTitle) throws Exception {
        Key key = keyStore.getKey(KEY_ALIAS, null);

        File noteFile = new File(context.getFilesDir(), noteTitle);
        int length = (int) noteFile.length();

        if (length <= 16) {
            // Se il file è troppo corto, logga l'errore e cancella il file.
            Log.e("NoteSecurityManager", "File troppo corto, non contiene dati validi: " + noteTitle);
            if (!noteFile.delete()) {
                Log.e("NoteSecurityManager", "Impossibile eliminare il file non valido: " + noteTitle);
            }
            throw new IOException("Il file è troppo corto per contenere l'IV e i dati cifrati.");
        }

        byte[] fileData = new byte[length];

        try (FileInputStream fis = new FileInputStream(noteFile)) {
            int readResult = fis.read(fileData);
            if (readResult != length) {
                throw new IOException("Non tutti i byte sono stati letti dal file.");
            }
        } catch (IOException e) {
            // Se c'è un errore nella lettura, logga e cancella il file.
            Log.e("NoteSecurityManager", "Errore nella lettura del file: " + noteTitle, e);
            if (!noteFile.delete()) {
                Log.e("NoteSecurityManager", "Impossibile eliminare il file non valido: " + noteTitle);
            }
            throw e;
        }

        // Estrai l'IV e i dati cifrati dal fileData
        byte[] iv = Arrays.copyOfRange(fileData, 0, 16); // Assumendo che l'IV sia di 16 byte
        byte[] encryptedData = Arrays.copyOfRange(fileData, 16, fileData.length);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));

        byte[] decryptedData = cipher.doFinal(encryptedData);
        return new String(decryptedData, CHARSET_NAME);
    }


    // Metodi aggiuntivi per gestire il vettore di inizializzazione
}
