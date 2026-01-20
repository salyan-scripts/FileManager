package com.salyan.filemanager;

import android.content.Intent;
import android.net.Uri;
import android.os.*;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.*;
import android.graphics.Color;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.*;

public class MainActivity extends AppCompatActivity {

    private File currentDir;
    private File clipboardFile;
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

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);

        // Barra superior
        LinearLayout topActions = new LinearLayout(this);
        topActions.setPadding(20, 20, 20, 20);

        Button btnNewFolder = new Button(this);
        btnNewFolder.setText("NOVA PASTA");
        btnNewFolder.setOnClickListener(v -> createFolderDialog());

        btnPaste = new Button(this);
        btnPaste.setText("COLAR");
        btnPaste.setEnabled(false);
        btnPaste.setOnClickListener(v -> pasteFile());

        topActions.addView(btnNewFolder);
        topActions.addView(btnPaste);
        root.addView(topActions);

        // Busca
        searchBar = new EditText(this);
        searchBar.setHint("Buscar...");
        searchBar.setPadding(40, 20, 40, 20);
        searchBar.setBackgroundColor(Color.parseColor("#F5F5F5"));
        searchBar.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }
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

    // =================== FILE LIST ===================

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
            if (f.getName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(f);
            }
        }
        listView.setAdapter(new FileAdapter());
    }

    // =================== CONTEXT MENU ===================

    private void showContextMenu(File file) {
        List<String> options = new ArrayList<>();

        if (file.isDirectory()) {
            options.add("Apagar");
            options.add("Renomear");
            options.add("Detalhes");
            options.add("Adicionar à tela inicial");
        } else {
            options.add("Copiar");
            options.add("Renomear");
            options.add("Apagar");
            options.add("Detalhes");
        }

        new AlertDialog.Builder(this)
                .setTitle(file.getName())
                .setItems(options.toArray(new String[0]), (d, which) -> {
                    String opt = options.get(which);
                    switch (opt) {
                        case "Copiar":
                            clipboardFile = file;
                            isMoveAction = false;
                            btnPaste.setEnabled(true);
                            break;
                        case "Apagar":
                            deleteConfirm(file);
                            break;
                        case "Renomear":
                            renameDialog(file);
                            break;
                        case "Detalhes":
                            showDetails(file);
                            break;
                        case "Adicionar à tela inicial":
                            addShortcut(file);
                            break;
                    }
                }).show();
    }

    // =================== ACTIONS ===================

    private void pasteFile() {
        if (clipboardFile == null) return;

        File dest = new File(currentDir, clipboardFile.getName());
        try {
            if (isMoveAction) {
                clipboardFile.renameTo(dest);
            } else {
                copyFile(clipboardFile, dest);
            }
            clipboardFile = null;
            btnPaste.setEnabled(false);
            loadFiles();
        } catch (IOException e) {
            Toast.makeText(this, "Erro ao colar", Toast.LENGTH_SHORT).show();
        }
    }

    private void copyFile(File src, File dst) throws IOException {
        try (FileChannel in = new FileInputStream(src).getChannel();
             FileChannel out = new FileOutputStream(dst).getChannel()) {
            in.transferTo(0, in.size(), out);
        }
    }

    private void createFolderDialog() {
        EditText input = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle("Nova pasta")
                .setView(input)
                .setPositiveButton("Criar", (d, w) -> {
                    new File(currentDir, input.getText().toString()).mkdir();
                    loadFiles();
                }).show();
    }

    private void renameDialog(File file) {
        EditText input = new EditText(this);
        input.setText(file.getName());
        new AlertDialog.Builder(this)
                .setTitle("Renomear")
                .setView(input)
                .setPositiveButton("OK", (d, w) -> {
                    file.renameTo(new File(file.getParent(), input.getText().toString()));
                    loadFiles();
                }).show();
    }

    private void deleteConfirm(File file) {
        new AlertDialog.Builder(this)
                .setMessage("Excluir " + file.getName() + "?")
                .setPositiveButton("Sim", (d, w) -> {
                    deleteRecursive(file);
                    loadFiles();
                }).setNegativeButton("Não", null).show();
    }

    private void deleteRecursive(File f) {
        if (f.isDirectory()) {
            File[] children = f.listFiles();
            if (children != null) {
                for (File c : children) deleteRecursive(c);
            }
        }
        f.delete();
    }

    private void showDetails(File f) {
        String info =
                "Nome: " + f.getName() + "\n\n" +
                "Caminho:\n" + f.getAbsolutePath() + "\n\n" +
                "Tamanho: " + f.length() + " bytes\n\n" +
                "Última modificação:\n" + new Date(f.lastModified());

        new AlertDialog.Builder(this)
                .setTitle("Detalhes")
                .setMessage(info)
                .setPositiveButton("OK", null)
                .show();
    }

    private void addShortcut(File f) {
        Toast.makeText(this,
                "Atalho criado para:\n" + f.getName(),
                Toast.LENGTH_SHORT).show();
    }

    // =================== LIST ADAPTER ===================

    class FileAdapter extends BaseAdapter {
        public int getCount() { return filteredList.size(); }
        public Object getItem(int i) { return filteredList.get(i); }
        public long getItemId(int i) { return i; }

        public View getView(int i, View v, ViewGroup vg) {
            LinearLayout l = new LinearLayout(MainActivity.this);
            l.setPadding(40, 30, 40, 30);
            l.setOrientation(LinearLayout.VERTICAL);

            File f = filteredList.get(i);

            TextView t = new TextView(MainActivity.this);
            t.setText(f.getName());
            t.setTextSize(16);
            t.setTextColor(Color.BLACK);

            l.addView(t);
            return l;
        }
    }

    // =================== OPTIONS MENU (⋮) ===================

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Nova pasta");
        menu.add("Ordenar por nome");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getTitle().toString()) {
            case "Nova pasta":
                createFolderDialog();
                return true;
            case "Ordenar por nome":
                Collections.sort(fileList, Comparator.comparing(File::getName));
                filter(searchBar.getText().toString());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // =================== BACK ===================

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

