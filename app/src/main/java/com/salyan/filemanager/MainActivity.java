package com.salyan.filemanager;

import android.os.Bundle;
import android.os.Environment;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {
    private File currentDir;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listView);
        currentDir = Environment.getExternalStorageDirectory();
        loadFiles();

        // Clique Curto: Navegar
        listView.setOnItemClickListener((parent, view, position, id) -> {
            String name = (String) parent.getItemAtPosition(position);
            File clickedFile = new File(currentDir, name);
            if (clickedFile.isDirectory()) {
                currentDir = clickedFile;
                loadFiles();
            }
        });

        // Clique Longo: Menu de Opções (Renomear/Deletar)
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
        String[] options = {"Renomear", "Deletar"};
        new AlertDialog.Builder(this)
            .setTitle(fileName)
            .setItems(options, (dialog, which) -> {
                if (which == 0) showRenameDialog(fileName);
                else showDeleteConfirm(fileName);
            }).show();
    }

    private void showRenameDialog(String oldName) {
        EditText input = new EditText(this);
        input.setText(oldName);
        new AlertDialog.Builder(this)
            .setTitle("Novo nome")
            .setView(input)
            .setPositiveButton("OK", (d, w) -> {
                File oldF = new File(currentDir, oldName);
                File newF = new File(currentDir, input.getText().toString());
                if (oldF.renameTo(newF)) {
                    loadFiles();
                } else {
                    Toast.makeText(this, "Erro ao renomear", Toast.LENGTH_SHORT).show();
                }
            }).show();
    }

    private void showDeleteConfirm(String fileName) {
        new AlertDialog.Builder(this)
            .setTitle("Excluir")
            .setMessage("Deseja apagar " + fileName + "?")
            .setPositiveButton("Sim", (d, w) -> {
                File f = new File(currentDir, fileName);
                if (f.delete()) {
                    loadFiles();
                } else {
                    Toast.makeText(this, "Erro ao deletar", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Não", null).show();
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
