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
    private Button btnPaste;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);

        // Barra superior de Ações (Fiel ao seu print)
        LinearLayout topActions = new LinearLayout(this);
        topActions.setPadding(20, 20, 20, 20);
        topActions.setGravity(Gravity.CENTER_HORIZONTAL);

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

        // Campo de Busca
        searchBar = new EditText(this);
        searchBar.setHint("Buscar...");
        searchBar.setPadding(40, 30, 40, 30);
        searchBar.setBackgroundColor(Color.parseColor("#F5F5F5"));
        searchBar.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }
            public void afterTextChanged(Editable s) {}
        });
        root.addView(searchBar);

        // Exibição do Caminho Atual
        pathDisplay = new TextView(this);
        pathDisplay.setPadding(40, 15, 40, 15);
        pathDisplay.setTextSize(13);
        pathDisplay.setTextColor(Color.DKGRAY);
        pathDisplay.setBackgroundColor(Color.parseColor("#EEEEEE"));
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

    // =================== LOGICA DE ARQUIVOS ===================

    private void loadFiles() {
        pathDisplay.setText("Pasta: " + currentDir.getAbsolutePath());
        File[] files = currentDir.listFiles();

        fileList.clear();
        if (files != null) fileList.addAll(Arrays.asList(files));

        // Organizar: Pastas primeiro, depois arquivos por ordem alfabetica
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

    // =================== MENU DE CONTEXTO ===================

    private void showContextMenu(File file) {
        List<String> options = new ArrayList<>();
        options.add("Copiar");
        options.add("Mover");
        options.add("Renomear");
        options.add("Apagar");
        options.add("Detalhes");

        new AlertDialog.Builder(this)
                .setTitle(file.getName())
                .setItems(options.toArray(new String[0]), (d, which) -> {
                    String opt = options.get(which);
                    switch (opt) {
                        case "Copiar":
                            clipboardFile = file;
                            isMoveAction = false;
                            btnPaste.setEnabled(true);
                            Toast.makeText(this, "Copiado: " + file.getName(), Toast.LENGTH_SHORT).show();
                            break;
                        case "Mover":
                            clipboardFile = file;
                            isMoveAction = true;
                            btnPaste.setEnabled(true);
                            Toast.makeText(this, "Pronto para mover", Toast.LENGTH_SHORT).show();
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
                    }
                }).show();
    }

    // =================== ACOES (COLAR, CRIAR, DELETAR) ===================

    private void pasteFile() {
        if (clipboardFile == null) return;
        File dest = new File(currentDir, clipboardFile.getName());

        try {
            if (isMoveAction) {
                if (clipboardFile.renameTo(dest)) {
                    Toast.makeText(this, "Movido com sucesso", Toast.LENGTH_SHORT).show();
                }
            } else {
                copyFile(clipboardFile, dest);
                Toast.makeText(this, "Copiado com sucesso", Toast.LENGTH_SHORT).show();
            }
            btnPaste.setEnabled(false);
            clipboardFile = null;
            loadFiles();
        } catch (Exception e) {
            Toast.makeText(this, "Erro na operação", Toast.LENGTH_SHORT).show();
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
        input.setHint("Nome da pasta");
        new AlertDialog.Builder(this)
                .setTitle("Nova Pasta")
                .setView(input)
                .setPositiveButton("Criar", (d, w) -> {
                    File folder = new File(currentDir, input.getText().toString());
                    if (folder.mkdir()) loadFiles();
                }).show();
    }

    private void renameDialog(File file) {
        EditText input = new EditText(this);
        input.setText(file.getName());
        new AlertDialog.Builder(this)
                .setTitle("Renomear")
                .setView(input)
                .setPositiveButton("OK", (d, w) -> {
                    if (file.renameTo(new File(file.getParent(), input.getText().toString()))) loadFiles();
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
            if (children != null) for (File c : children) deleteRecursive(c);
        }
        f.delete();
    }

    private void showDetails(File f) {
        String info = "Nome: " + f.getName() + "\n" +
                     "Tamanho: " + (f.length() / 1024) + " KB\n" +
                     "Modificado: " + new Date(f.lastModified());
        new AlertDialog.Builder(this).setTitle("Detalhes").setMessage(info).setPositiveButton("OK", null).show();
    }

    // =================== ADAPTER (VISUAL DA LISTA) ===================

    class FileAdapter extends BaseAdapter {
        public int getCount() { return filteredList.size(); }
        public Object getItem(int i) { return filteredList.get(i); }
        public long getItemId(int i) { return i; }

        public View getView(int i, View v, ViewGroup vg) {
            LinearLayout itemLayout = new LinearLayout(MainActivity.this);
            itemLayout.setPadding(40, 35, 40, 35);
            itemLayout.setGravity(Gravity.CENTER_VERTICAL);

            File f = filteredList.get(i);

            // Icone Recriado
            TextView icon = new TextView(MainActivity.this);
            icon.setText(f.isDirectory() ? "📁 " : "📄 ");
            icon.setTextSize(22);
            icon.setTextColor(f.isDirectory() ? Color.parseColor("#FFA000") : Color.GRAY);

            // Nome do arquivo
            TextView name = new TextView(MainActivity.this);
            name.setText(f.getName());
            name.setTextSize(16);
            name.setTextColor(Color.BLACK);
            name.setPadding(20, 0, 0, 0);

            itemLayout.addView(icon);
            itemLayout.addView(name);
            return itemLayout;
        }
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
