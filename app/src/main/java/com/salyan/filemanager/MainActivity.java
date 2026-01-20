package com.salyan.filemanager;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.*;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.*;
import java.nio.channels.FileChannel;
import java.util.*;

public class MainActivity extends AppCompatActivity {

    private File currentDir, clipboardFile;
    private boolean isMoveAction = false, isDarkMode = false;
    private ListView listView;
    private List<File> fileList = new ArrayList<>();
    private List<File> filteredList = new ArrayList<>();
    private TextView pathDisplay;
    private EditText searchBar;
    private LinearLayout root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);

        // --- BARRA SUPERIOR (TOOLBAR CUSTOMIZADA) ---
        LinearLayout toolbar = new LinearLayout(this);
        toolbar.setPadding(30, 20, 30, 20);
        toolbar.setGravity(Gravity.CENTER_VERTICAL);
        toolbar.setBackgroundColor(Color.parseColor("#F5F5F5"));

        TextView title = new TextView(this);
        title.setText("Salyan File");
        title.setTextSize(20);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(Color.BLACK);
        title.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1));

        // Botão Lupa (Pesquisar)
        TextView btnSearch = new TextView(this);
        btnSearch.setText("🔍");
        btnSearch.setTextSize(22);
        btnSearch.setPadding(20, 0, 30, 0);
        btnSearch.setOnClickListener(v -> {
            if (searchBar.getVisibility() == View.GONE) searchBar.setVisibility(View.VISIBLE);
            else {
                searchBar.setVisibility(View.GONE);
                searchBar.setText("");
            }
        });

        // Botão Configurações (Menu)
        TextView btnMenu = new TextView(this);
        btnMenu.setText("⋮");
        btnMenu.setTextSize(28);
        btnMenu.setPadding(10, 0, 10, 0);
        btnMenu.setOnClickListener(v -> showSettingsMenu(v));

        toolbar.addView(title);
        toolbar.addView(btnSearch);
        toolbar.addView(btnMenu);
        root.addView(toolbar);

        // --- BARRA DE BUSCA (INICIALMENTE ESCONDIDA) ---
        searchBar = new EditText(this);
        searchBar.setHint("Pesquisar...");
        searchBar.setVisibility(View.GONE); // Escondida
        searchBar.setPadding(40, 30, 40, 30);
        searchBar.setBackgroundColor(Color.parseColor("#EEEEEE"));
        searchBar.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int b, int c) { filter(s.toString()); }
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            public void afterTextChanged(Editable s) {}
        });
        root.addView(searchBar);

        pathDisplay = new TextView(this);
        pathDisplay.setPadding(40, 15, 40, 15);
        pathDisplay.setTextSize(12);
        root.addView(pathDisplay);

        listView = new ListView(this);
        root.addView(listView);

        setContentView(root);
        if (checkPermission()) initApp(); else requestPermission();
    }

    private void showSettingsMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.getMenu().add("Nova Pasta");
        popup.getMenu().add("Colar").setEnabled(clipboardFile != null);
        popup.getMenu().add(isDarkMode ? "Modo Claro" : "Modo Dark");
        
        popup.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            if (title.equals("Nova Pasta")) createFolderDialog();
            else if (title.equals("Colar")) pasteFile();
            else if (title.contains("Modo")) toggleDarkMode();
            return true;
        });
        popup.show();
    }

    private void toggleDarkMode() {
        isDarkMode = !isDarkMode;
        int bg = isDarkMode ? Color.parseColor("#121212") : Color.WHITE;
        int txt = isDarkMode ? Color.WHITE : Color.BLACK;
        
        root.setBackgroundColor(bg);
        pathDisplay.setTextColor(txt);
        listView.setBackgroundColor(bg);
        loadFiles(); // Atualiza a lista com as novas cores
        Toast.makeText(this, "Modo " + (isDarkMode ? "Dark" : "Claro") + " ativado", Toast.LENGTH_SHORT).show();
    }

    // --- MÉTODOS DE ARQUIVO (REVISADOS) ---
    private void initApp() {
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

    class FileAdapter extends BaseAdapter {
        public int getCount() { return filteredList.size(); }
        public Object getItem(int i) { return filteredList.get(i); }
        public long getItemId(int i) { return i; }
        public View getView(int i, View v, ViewGroup vg) {
            LinearLayout l = new LinearLayout(MainActivity.this);
            l.setPadding(40, 35, 40, 35);
            File f = filteredList.get(i);
            TextView t = new TextView(MainActivity.this);
            t.setText((f.isDirectory() ? "📁 " : "📄 ") + f.getName());
            t.setTextColor(isDarkMode ? Color.WHITE : Color.BLACK);
            t.setTextSize(16);
            l.addView(t);
            return l;
        }
    }

    private void showContextMenu(File file) {
        String[] options = {"Copiar", "Mover", "Renomear", "Excluir"};
        new AlertDialog.Builder(this).setTitle(file.getName()).setItems(options, (d, which) -> {
            if (which == 0) { clipboardFile = file; isMoveAction = false; }
            else if (which == 1) { clipboardFile = file; isMoveAction = true; }
            else if (which == 2) renameDialog(file);
            else if (which == 3) deleteConfirm(file);
        }).show();
    }

    private void pasteFile() {
        File dest = new File(currentDir, clipboardFile.getName());
        try {
            if (isMoveAction) clipboardFile.renameTo(dest);
            else copyFile(clipboardFile, dest);
            clipboardFile = null;
            loadFiles();
        } catch (Exception e) { Toast.makeText(this, "Erro", 0).show(); }
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
        new AlertDialog.Builder(this).setMessage("Excluir?").setPositiveButton("Sim", (d, w) -> {
            deleteRecursive(file); loadFiles();
        }).show();
    }

    private void deleteRecursive(File f) {
        if (f.isDirectory()) { File[] c = f.listFiles(); if (c != null) for (File a : c) deleteRecursive(a); }
        f.delete();
    }

    private boolean checkPermission() { return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED; }
    private void requestPermission() { ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1); }
    @Override public void onRequestPermissionsResult(int r, String[] p, int[] g) { if (g.length > 0 && g[0] == PackageManager.PERMISSION_GRANTED) initApp(); }

    @Override
    public void onBackPressed() {
        if (currentDir != null && !currentDir.equals(Environment.getExternalStorageDirectory())) {
            currentDir = currentDir.getParentFile(); loadFiles();
        } else super.onBackPressed();
    }
}
