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
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16, 16, 16, 16);

        // Ferramentas
        LinearLayout topBar = new LinearLayout(this);
        Button btnNew = new Button(this); btnNew.setText("Nova Pasta");
        Button btnPaste = new Button(this); btnPaste.setText("Colar");
        topBar.addView(btnNew); topBar.addView(btnPaste);
        layout.addView(topBar);

        // Busca
        EditText search = new EditText(this);
        search.setHint("Buscar...");
        layout.addView(search);

        listView = new ListView(this);
        layout.addView(listView);
        setContentView(layout);

        currentDir = Environment.getExternalStorageDirectory();
        loadFiles();

        btnNew.setOnClickListener(v -> {
            EditText in = new EditText(this);
            new AlertDialog.Builder(this).setTitle("Nova Pasta").setView(in)
                .setPositiveButton("Criar", (d, w) -> {
                    if(new File(currentDir, in.getText().toString()).mkdir()) loadFiles();
                }).show();
        });

        btnPaste.setOnClickListener(v -> {
            if(fileToCopy != null) {
                try {
                    copy(fileToCopy, new File(currentDir, fileToCopy.getName()));
                    loadFiles();
                    Toast.makeText(this, "Colado!", Toast.LENGTH_SHORT).show();
                } catch (Exception e) { e.printStackTrace(); }
            }
        });

        search.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(adapter != null) adapter.getFilter().filter(s);
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void afterTextChanged(Editable s) {}
        });

        listView.setOnItemClickListener((p, v, pos, id) -> {
            File f = new File(currentDir, (String) p.getItemAtPosition(pos));
            if(f.isDirectory()) { currentDir = f; search.setText(""); loadFiles(); }
        });

        listView.setOnItemLongClickListener((p, v, pos, id) -> {
            String name = (String) p.getItemAtPosition(pos);
            String[] opts = {"Copiar", "Deletar"};
            new AlertDialog.Builder(this).setTitle(name).setItems(opts, (d, w) -> {
                if(w == 0) { fileToCopy = new File(currentDir, name); Toast.makeText(this, "Copiado", Toast.LENGTH_SHORT).show(); }
                else { if(new File(currentDir, name).delete()) loadFiles(); }
            }).show();
            return true;
        });
    }

    private void loadFiles() {
        File[] files = currentDir.listFiles();
        fileList.clear();
        if(files != null) for(File f : files) fileList.add(f.getName());
        Collections.sort(fileList);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, fileList);
        listView.setAdapter(adapter);
    }

    private void copy(File s, File d) throws IOException {
        try (FileChannel i = new FileInputStream(s).getChannel();
             FileChannel o = new FileOutputStream(d).getChannel()) {
            o.transferFrom(i, 0, i.size());
        }
    }

    @Override
    public void onBackPressed() {
        if (!currentDir.equals(Environment.getExternalStorageDirectory())) {
            currentDir = currentDir.getParentFile();
            loadFiles();
        } else super.onBackPressed();
    }
}
