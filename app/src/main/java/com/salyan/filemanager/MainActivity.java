package com.salyan.filemanager;

import android.content.Intent;
import android.net.Uri;
import android.os.*;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.*;
import android.graphics.Color;
import android.graphics.Typeface;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.io.*;
import java.nio.channels.FileChannel;
import java.util.*;

public class MainActivity extends AppCompatActivity {
    private File currentDir, clipboardFile;
    private boolean isMoveAction = false;
    private ListView listView;
    private List<File> fileList = new ArrayList<>();
    private List<File> filteredList = new ArrayList<>();
    private TextView pathDisplay;
    private EditText searchBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);

        // Barra Superior: Nova Pasta e Colar
        LinearLayout topActions = new LinearLayout(this);
        topActions.setPadding(20, 20, 20, 20);
        
        Button btnNewFolder = new Button(this);
        btnNewFolder.setText("NOVA PASTA");
        btnNewFolder.setOnClickListener(v -> createFolderDialog());
        
        Button btnPaste = new Button(this);
        btnPaste.setText("COLAR");
        btnPaste.setOnClickListener(v -> pasteFile());
        
        topActions.addView(btnNewFolder);
        topActions.addView(btnPaste);
        root.addView(topActions);

        // Barra de Busca (Fiel ao print)
        searchBar = new EditText(this);
        searchBar.setHint("Buscar...");
        searchBar.setPadding(40, 20, 40, 20);
        searchBar.setBackgroundColor(Color.parseColor("#F5F5F5"));
        searchBar.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) { filter(s.toString()); }
            public void afterTextChanged(Editable s) {}
        });
        root.addView(searchBar);

        pathDisplay = new TextView(this);
        pathDisplay.setPadding(40, 10, 40, 10);
        pathDisplay.setTextSize(12);
        root.addView(pathDisplay);

        listView = new ListView(this);
        root.addView(listView);
        setContentView(root);

        currentDir = Environment.getExternalStorageDirectory();

        listView.setOnItemClickListener((p, v, pos, id) -> {
            File f = filteredList.get(pos);
            if (f.isDirectory()) { currentDir = f; loadFiles(); }
        });

        listView.setOnItemLongClickListener((p, v, pos, id) -> {
            showContextMenu(filteredList.get(pos));
            return true;
        });

        loadFiles();
    }

    private void loadFiles() {
        pathDisplay.setText(currentDir.getAbsolutePath());
        File[] files = currentDir.listFiles();
        fileList.clear();
        if (files != null) fileList.addAll(Arrays.asList(files));
        Collections.sort(fileList, (a, b) -> a.isDirectory() && !b.isDirectory() ? -1 : 1);
        filter(searchBar.getText().toString());
    }

    private void filter(String text) {
        filteredList.clear();
        for (File f : fileList) {
            if (f.getName().toLowerCase().contains(text.toLowerCase())) filteredList.add(f);
        }
        listView.setAdapter(new FileAdapter());
    }

    private void showContextMenu(File file) {
        String[] options = {"Copiar", "Mover", "Renomear", "Excluir"};
        new AlertDialog.Builder(this).setTitle(file.getName())
            .setItems(options, (d, which) -> {
                if (which == 0) { clipboardFile = file; isMoveAction = false; }
                else if (which == 1) { clipboardFile = file; isMoveAction = true; }
                else if (which == 2) renameDialog(file);
                else if (which == 3) deleteConfirm(file);
            }).show();
    }

    private void pasteFile() {
        if (clipboardFile == null) return;
        File dest = new File(currentDir, clipboardFile.getName());
        try {
            if (isMoveAction) {
                clipboardFile.renameTo(dest);
                clipboardFile = null;
            } else {
                copyFile(clipboardFile, dest);
            }
            loadFiles();
        } catch (IOException e) { Toast.makeText(this, "Erro ao colar", 0).show(); }
    }

    private void copyFile(File src, File dst) throws IOException {
        try (FileChannel in = new FileInputStream(src).getChannel(); 
             FileChannel out = new FileOutputStream(dst).getChannel()) {
            in.transferTo(0, in.size(), out);
        }
    }

    private void createFolderDialog() {
        EditText input = new EditText(this);
        new AlertDialog.Builder(this).setTitle("Nova Pasta").setView(input)
            .setPositiveButton("Criar", (d, w) -> {
                new File(currentDir, input.getText().toString()).mkdir();
                loadFiles();
            }).show();
    }

    private void renameDialog(File file) {
        EditText input = new EditText(this);
        input.setText(file.getName());
        new AlertDialog.Builder(this).setTitle("Renomear").setView(input)
            .setPositiveButton("OK", (d, w) -> {
                file.renameTo(new File(file.getParent(), input.getText().toString()));
                loadFiles();
            }).show();
    }

    private void deleteConfirm(File file) {
        new AlertDialog.Builder(this).setMessage("Excluir " + file.getName() + "?")
            .setPositiveButton("Sim", (d, w) -> { deleteRecursive(file); loadFiles(); }).show();
    }

    private void deleteRecursive(File f) {
        if (f.isDirectory()) for (File c : f.listFiles()) deleteRecursive(c);
        f.delete();
    }

    class FileAdapter extends BaseAdapter {
        public int getCount() { return filteredList.size(); }
        public Object getItem(int i) { return filteredList.get(i); }
        public long getItemId(int i) { return i; }
        public View getView(int i, View v, ViewGroup vg) {
            LinearLayout l = new LinearLayout(MainActivity.this);
            l.setPadding(40, 30, 40, 30);
            File f = filteredList.get(i);
            TextView t = new TextView(MainActivity.this);
            t.setText((f.isDirectory() ? "📁 " : "📄 ") + f.getName());
            t.setTextSize(16); t.setTextColor(Color.BLACK);
            l.addView(t);
            return l;
        }
    }

    @Override
    public void onBackPressed() {
        if (!currentDir.equals(Environment.getExternalStorageDirectory())) {
            currentDir = currentDir.getParentFile(); loadFiles();
        } else super.onBackPressed();
    }
}
