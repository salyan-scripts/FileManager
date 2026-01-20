package com.salyan.filemanager;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
    private Button btnPaste;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Layout Principal (Raiz)
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#FFFFFF")); // Branco puro para evitar transparência

        // Título Estilizado
        TextView title = new TextView(this);
        title.setText("Salyan Explorer");
        title.setTextSize(22);
        title.setPadding(40, 40, 40, 10);
        title.setTextColor(Color.BLACK);
        title.setTypeface(null, Typeface.BOLD);
        root.addView(title);

        // Barra de Ações (Nova Pasta e Colar)
        LinearLayout actions = new LinearLayout(this);
        actions.setPadding(20, 10, 20, 10);
        
        Button btnNewFolder = new Button(this);
        btnNewFolder.setText("NOVA PASTA");
        
        btnPaste = new Button(this);
        btnPaste.setText("COLAR");
        btnPaste.setEnabled(false);

        actions.addView(btnNewFolder);
        actions.addView(btnPaste);
        root.addView(actions);

        // Barra de Busca
        searchBar = new EditText(this);
        searchBar.setHint("Pesquisar arquivos...");
        searchBar.setPadding(40, 30, 40, 30);
        searchBar.setBackgroundColor(Color.parseColor("#F0F0F0"));
        root.addView(searchBar);

        // Indicador de Caminho
        pathDisplay = new TextView(this);
        pathDisplay.setPadding(40, 15, 40, 15);
        pathDisplay.setBackgroundColor(Color.parseColor("#E0E0E0"));
        pathDisplay.setTextColor(Color.BLACK);
        pathDisplay.setTextSize(14);
        root.addView(pathDisplay);

        // Lista de Arquivos
        listView = new ListView(this);
        listView.setCacheColorHint(Color.WHITE);
        root.addView(listView);

        setContentView(root);

        // Lógica de Permissão para Android 8
        if (checkPermission()) {
            initApp();
        } else {
            requestPermission();
        }

        // Eventos dos Botões
        btnNewFolder.setOnClickListener(v -> createFolderDialog());
        btnPaste.setOnClickListener(v -> pasteFile());
        
        searchBar.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) { filter(s.toString()); }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void afterTextChanged(Editable s) {}
        });
    }

    private void initApp() {
        currentDir = Environment.getExternalStorageDirectory();
        listView.setOnItemClickListener((p, v, pos, id) -> {
            File f = filteredList.get(pos);
            if (f.isDirectory()) {
                currentDir = f;
                loadFiles();
            }
        });
        listView.setOnItemLongClickListener((p, v, pos, id) -> {
            showContextMenu(filteredList.get(pos));
            return true;
        });
        loadFiles();
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initApp();
        } else {
            Toast.makeText(this, "Permissão necessária!", Toast.LENGTH_LONG).show();
        }
    }

    private void loadFiles() {
        pathDisplay.setText(currentDir.getAbsolutePath());
        File[] files = currentDir.listFiles();
        fileList.clear();
        if (files != null) fileList.addAll(Arrays.asList(files));
        
        Collections.sort(fileList, (a, b) -> {
            if (a.isDirectory() && !b.isDirectory()) return -1;
            if (!a.isDirectory() && b.isDirectory()) return 1;
            return a.getName().compareToIgnoreCase(b.getName());
        });
        filter(searchBar.getText().toString());
    }

    private void filter(String text) {
        filteredList.clear();
        for (File f : fileList) {
            if (f.getName().toLowerCase().contains(text.toLowerCase())) filteredList.add(f);
        }
        listView.setAdapter(new FileAdapter());
    }

    // Adaptador compatível com Android antigo
    class FileAdapter extends BaseAdapter {
        public int getCount() { return filteredList.size(); }
        public Object getItem(int i) { return filteredList.get(i); }
        public long getItemId(int i) { return i; }
        public View getView(int i, View v, ViewGroup vg) {
            LinearLayout row = new LinearLayout(MainActivity.this);
            row.setPadding(30, 30, 30, 30);
            row.setGravity(Gravity.CENTER_VERTICAL);
            
            File f = filteredList.get(i);
            TextView icon = new TextView(MainActivity.this);
            icon.setText(f.isDirectory() ? "📁" : "📄");
            icon.setTextSize(24);
            
            TextView name = new TextView(MainActivity.this);
            name.setText(f.getName());
            name.setTextColor(Color.BLACK);
            name.setTextSize(16);
            name.setPadding(30, 0, 0, 0);

            row.addView(icon);
            row.addView(name);
            return row;
        }
    }

    // Funções de Contexto (Copiar, Mover, Renomear, Excluir)
    private void showContextMenu(File file) {
        String[] options = {"Copiar", "Mover", "Renomear", "Excluir"};
        new AlertDialog.Builder(this).setTitle(file.getName()).setItems(options, (d, which) -> {
            if (which == 0) { clipboardFile = file; isMoveAction = false; btnPaste.setEnabled(true); }
            else if (which == 1) { clipboardFile = file; isMoveAction = true; btnPaste.setEnabled(true); }
            else if (which == 2) renameDialog(file);
            else if (which == 3) deleteConfirm(file);
        }).show();
    }

    private void pasteFile() {
        File dest = new File(currentDir, clipboardFile.getName());
        try {
            if (isMoveAction) clipboardFile.renameTo(dest);
            else copyFile(clipboardFile, dest);
            btnPaste.setEnabled(false);
            loadFiles();
        } catch (Exception e) { Toast.makeText(this, "Erro ao processar", 0).show(); }
    }

    private void copyFile(File src, File dst) throws IOException {
        FileChannel in = new FileInputStream(src).getChannel();
        FileChannel out = new FileOutputStream(dst).getChannel();
        in.transferTo(0, in.size(), out);
        in.close(); out.close();
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
        new AlertDialog.Builder(this).setMessage("Apagar " + file.getName() + "?")
            .setPositiveButton("Sim", (d, w) -> { deleteRecursive(file); loadFiles(); }).show();
    }

    private void deleteRecursive(File f) {
        if (f.isDirectory()) for (File c : f.listFiles()) deleteRecursive(c);
        f.delete();
    }

    @Override
    public void onBackPressed() {
        if (currentDir != null && !currentDir.equals(Environment.getExternalStorageDirectory())) {
            currentDir = currentDir.getParentFile();
            loadFiles();
        } else {
            super.onBackPressed();
        }
    }
}
