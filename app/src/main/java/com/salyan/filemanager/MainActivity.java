package com.salyan.filemanager;

import android.os.Bundle;
import android.os.Environment;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView pathView;
    private File currentFolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pathView = findViewById(R.id.path_view);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Ponto de partida: Memória Interna
        File root = Environment.getExternalStorageDirectory();
        updateList(root);
    }

    private void updateList(File folder) {
        currentFolder = folder;
        pathView.setText(folder.getAbsolutePath());

        File[] filesArray = folder.listFiles();
        List<File> fileList = new ArrayList<>();

        if (filesArray != null) {
            fileList.addAll(Arrays.asList(filesArray));
            
            // Ordenação: Pastas primeiro, depois ordem alfabética
            Collections.sort(fileList, (f1, f2) -> {
                if (f1.isDirectory() && !f2.isDirectory()) return -1;
                if (!f1.isDirectory() && f2.isDirectory()) return 1;
                return f1.getName().toLowerCase().compareTo(f2.getName().toLowerCase());
            });
        }

        FileAdapter adapter = new FileAdapter(fileList, file -> {
            if (file.isDirectory()) {
                updateList(file);
            } else {
                Toast.makeText(this, "Arquivo: " + file.getName(), Toast.LENGTH_SHORT).show();
            }
        });
        
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        // Se puder voltar para a pasta pai, volta. Senão, fecha o app.
        File parent = currentFolder.getParentFile();
        if (parent != null && currentFolder.getAbsolutePath().contains(Environment.getExternalStorageDirectory().getAbsolutePath())) {
            if (currentFolder.equals(Environment.getExternalStorageDirectory())) {
                super.onBackPressed();
            } else {
                updateList(parent);
            }
        } else {
            super.onBackPressed();
        }
    }
}
