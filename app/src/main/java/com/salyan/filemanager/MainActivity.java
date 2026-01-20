package com.salyan.filemanager;

import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.io.*;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {
    private File currentDir;
    private ListView listView;
    private File fileToCopy = null;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> fileList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Layout Principal
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16, 16, 16, 16);

        // Barra de Busca
        EditText searchBar = new EditText(this);
        searchBar.setHint("Buscar arquivos...");
        layout.addView(searchBar);

        // Botões de Ação
        LinearLayout topBar = new LinearLayout(this);
        Button btnNewFolder = new Button(this);
        btnNewFolder.setText("Nova Pasta");
        btnNewFolder.setOnClickListener(v -> showNewFolderDialog());
        
        Button btnPaste = new Button(this);
        btnPaste.setText("Colar");
        btnPaste.setOnClickListener(v -> pasteFile());
        
        topBar.addView(btnNewFolder);
        topBar.addView(btnPaste);
        layout.addView(topBar);

        listView = new ListView(this);
        layout.addView(listView);
        setContentView(layout);

        currentDir = Environment.getExternalStorageDirectory();
        loadFiles();

        // Lógica de Busca
        searchBar.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void afterTextChanged(Editable s) {}
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String name = (String) parent.getItemAtPosition(position);
            File clickedFile = new File(currentDir, name);
            if (clickedFile.isDirectory()) {
                currentDir = clickedFile;
                loadFiles();
                searchBar.setText(""); // Limpa busca ao navegar
            }
        });

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            showOptionsDialog((String) parent.getItemAtPosition(position));
            return true;
        });
    }

    private void loadFiles() {
        File[] files = currentDir.listFiles();
        fileList.clear();
        if (files != null) {
            for (File file : files) fileList.add(file.getName());
        }
        Collections.sort(fileList);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, fileList);
        listView.setAdapter(adapter);
    }

    private void showOptionsDialog(String fileName) {
        String[] options = {"Copiar", "Renomear", "Deletar"};
        new AlertDialog.Builder(this).setTitle(fileName).setItems(options, (dialog, which) -> {
            if (which == 0) {
                fileToCopy = new File(currentDir, fileName);
                Toast.makeText(this, "Copiado: " + fileName, Toast.LENGTH_SHORT).show();
            } else if (which == 1) showRenameDialog(fileName);
            else if (which == 2) showDeleteConfirm(fileName);
        }).show();
    }

    private void pasteFile() {
        if (fileToCopy == null) return;
        File dest = new File(currentDir, fileToCopy.getName());
        try {
            if (fileToCopy.isDirectory()) Toast.makeText(this, "Pasta não suportada no copiar ainda", Toast.LENGTH_SHORT).show();
            else {
                copyFile(fileToCopy, dest);
                loadFiles();
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void copyFile(File src, File dst) throws IOException {
        try (FileChannel in = new FileInputStream(src).getChannel();
             FileChannel out = new FileOutputStream(dst).getChannel()) {
            out.transferFrom(in, 0, in.size());
        }
    }

    private void showNewFolderDialog() {
        EditText input = new EditText(this);
        new AlertDialog.Builder(this).setTitle("Nova Pasta").setView(input)
            .setPositiveButton("Criar", (d, w) -> {
                if (new File(currentDir, input.getText().toString()).mkdir()) loadFiles();
            }).show();
    }

    private void showRenameDialog(String oldName) {
        EditText input = new EditText(this);
        input.setText(oldName);
        new AlertDialog.Builder(this).setTitle("Renomear").setView(input)
            .setPositiveButton("OK", (d, w) -> {
                if (new File(currentDir, oldName).renameTo(new File(currentDir, input.getText().toString()))) loadFiles();
            }).show();
    }

    private void showDeleteConfirm(String fileName) {
        new AlertDialog.Builder(this).setTitle("Excluir").setMessage("Apagar " + fileName + "?")
            .setPositiveButton("Sim", (d, w) -> {
                if (new File(currentDir, fileName).delete()) loadFiles();
            }).show();
    }

    @Override
    public void onBackPressed() {
        if (!currentDir.equals(Environment.getExternalStorageDirectory())) {
            currentDir = currentDir.getParentFile();
            loadFiles();
        } else super.onBackPressed();
    }
}
