package com.salyan.filemanager;

import android.os.Bundle;
import android.os.Environment;
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
    private File fileToCopy = null; // Memória para copiar/colar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Layout dinâmico simples para incluir botão de "Nova Pasta" e "Colar"
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        
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

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String name = (String) parent.getItemAtPosition(position);
            File clickedFile = new File(currentDir, name);
            if (clickedFile.isDirectory()) {
                currentDir = clickedFile;
                loadFiles();
            }
        });

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            String fileName = (String) parent.getItemAtPosition(position);
            showOptionsDialog(fileName);
            return true;
        });
    }

    private void loadFiles() {
        File[] files = currentDir.listFiles();
        ArrayList<String> fileList = new ArrayList<>();
        if (files != null) {
            for (File file : files) fileList.add(file.getName());
        }
        Collections.sort(fileList);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, fileList);
        listView.setAdapter(adapter);
    }

    private void showOptionsDialog(String fileName) {
        String[] options = {"Copiar", "Renomear", "Deletar"};
        new AlertDialog.Builder(this)
            .setTitle(fileName)
            .setItems(options, (dialog, which) -> {
                if (which == 0) {
                    fileToCopy = new File(currentDir, fileName);
                    Toast.makeText(this, "Copiado para a área de transferência", Toast.LENGTH_SHORT).show();
                }
                else if (which == 1) showRenameDialog(fileName);
                else if (which == 2) showDeleteConfirm(fileName);
            }).show();
    }

    private void showNewFolderDialog() {
        EditText input = new EditText(this);
        new AlertDialog.Builder(this).setTitle("Nova Pasta").setView(input)
            .setPositiveButton("Criar", (d, w) -> {
                File newDir = new File(currentDir, input.getText().toString());
                if (newDir.mkdir()) loadFiles();
                else Toast.makeText(this, "Erro ao criar pasta", Toast.LENGTH_SHORT).show();
            }).show();
    }

    private void pasteFile() {
        if (fileToCopy == null) {
            Toast.makeText(this, "Nada para colar", Toast.LENGTH_SHORT).show();
            return;
        }
        File dest = new File(currentDir, fileToCopy.getName());
        try {
            copyFile(fileToCopy, dest);
            loadFiles();
            Toast.makeText(this, "Colado!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Erro ao colar: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void copyFile(File source, File dest) throws IOException {
        try (FileChannel in = new FileInputStream(source).getChannel();
             FileChannel out = new FileOutputStream(dest).getChannel()) {
            out.transferFrom(in, 0, in.size());
        }
    }

    private void showRenameDialog(String oldName) {
        EditText input = new EditText(this);
        input.setText(oldName);
        new AlertDialog.Builder(this).setTitle("Renomear").setView(input)
            .setPositiveButton("OK", (d, w) -> {
                File oldF = new File(currentDir, oldName);
                File newF = new File(currentDir, input.getText().toString());
                if (oldF.renameTo(newF)) loadFiles();
            }).show();
    }

    private void showDeleteConfirm(String fileName) {
        new AlertDialog.Builder(this).setTitle("Excluir").setMessage("Apagar " + fileName + "?")
            .setPositiveButton("Sim", (d, w) -> {
                if (new File(currentDir, fileName).delete()) loadFiles();
            }).setNegativeButton("Não", null).show();
    }

    @Override
    public void onBackPressed() {
        if (!currentDir.equals(Environment.getExternalStorageDirectory())) {
            currentDir = currentDir.getParentFile();
            loadFiles();
        } else {
            super.onBackPressed();
        }
    }
}
