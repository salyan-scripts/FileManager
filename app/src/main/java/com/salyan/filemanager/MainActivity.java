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

        // Clique curto: Navegar
        listView.setOnItemClickListener((parent, view, position, id) -> {
            String name = (String) parent.getItemAtPosition(position);
            File clickedFile = new File(currentDir, name);
            if (clickedFile.isDirectory()) {
                currentDir = clickedFile;
                loadFiles();
            }
        });

        // Clique LONGO: Renomear
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            String oldName = (String) parent.getItemAtPosition(position);
            showRenameDialog(oldName);
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

    private void showRenameDialog(String oldName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Renomear: " + oldName);
        
        final EditText input = new EditText(this);
        input.setText(oldName);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String newName = input.getText().toString();
            File oldFile = new File(currentDir, oldName);
            File newFile = new File(currentDir, newName);
            if (oldFile.renameTo(newFile)) {
                Toast.makeText(this, "Renomeado!", Toast.LENGTH_SHORT).show();
                loadFiles();
            } else {
                Toast.makeText(this, "Erro ao renomear", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }
}
