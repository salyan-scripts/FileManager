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
    private File fileToCopy = null; // Memória para o arquivo copiado
    private ArrayAdapter<String> adapter;
    private ArrayList<String> fileList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Layout principal
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20, 20, 20, 20);

        // Barra de ferramentas (Botões)
        LinearLayout toolBar = new LinearLayout(this);
        toolBar.setOrientation(LinearLayout.HORIZONTAL);
        
        Button btnNewFolder = new Button(this);
        btnNewFolder.setText("Nova Pasta");
        btnNewFolder.setOnClickListener(v -> showNewFolderDialog());
        
        Button btnPaste = new Button(this);
        btnPaste.setText("Colar");
        btnPaste.setOnClickListener(v -> pasteFile());
        
        toolBar.addView(btnNewFolder);
        toolBar.addView(btnPaste);
        layout.addView(toolBar);

        // Barra de Busca
        EditText searchBar = new EditText(this);
        searchBar.setHint("🔍 Buscar arquivo...");
        layout.addView(searchBar);

        // Lista de Arquivos
        listView = new ListView(this);
        layout.addView(listView);
        
        setContentView(layout);

        // Configuração inicial
        currentDir = Environment.getExternalStorageDirectory();
        loadFiles();

        // Configurar Filtro de Busca
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) adapter.getFilter().filter(s);
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });

        // Clique Curto: Navegar
        listView.setOnItemClickListener((parent, view, position, id) -> {
            String name = (String) parent.getItemAtPosition(position);
            File clickedFile = new File(currentDir, name);
            if (clickedFile.isDirectory()) {
                currentDir = clickedFile;
                searchBar.setText(""); // Limpar busca ao mudar de pasta
                loadFiles();
            }
        });

        // Clique Longo: Menu de Opções
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            String fileName = (String) parent.getItemAtPosition(position);
            showOptionsDialog(fileName);
            return true;
        });
    }

    private void loadFiles() {
        File[] files = currentDir.listFiles();
        fileList.clear();
        if (files != null) {
            for (File file : files) fileList.add(file.getName());
        }
        Collections.sort(fileList);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, fileList);
        listView.setAdapter(adapter);
    }

    private void showOptionsDialog(String fileName) {
        String[] options = {"Copiar", "Renomear", "Deletar"};
        new AlertDialog.Builder(this)
            .setTitle(fileName)
            .setItems(options, (dialog, which) -> {
                if (which == 0) {
                    fileToCopy = new File(currentDir, fileName);
                    Toast.makeText(this, "Copiado! Vá para o destino e clique em Colar.", Toast.LENGTH_LONG).show();
                }
                else if (which == 1) showRenameDialog(fileName);
                else if (which == 2) showDeleteConfirm(fileName);
            }).show();
    }

    private void pasteFile() {
        if (fileToCopy == null) {
            Toast.makeText(this, "Nenhum arquivo copiado.", Toast.LENGTH_SHORT).show();
            return;
        }
        File dest = new File(currentDir, fileToCopy.getName());
        try {
            copyFile(fileToCopy, dest);
            loadFiles();
            Toast.makeText(this, "Arquivo colado com sucesso!", Toast.LENGTH_SHORT).show();
            fileToCopy = null; // Limpa a memória após colar
        } catch (IOException e) {
            Toast.makeText(this, "Erro ao colar: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void copyFile(File source, File dest) throws IOException {
        if (source.isDirectory()) {
            throw new IOException("Cópia de pastas ainda não implementada, apenas arquivos.");
        }
        try (FileChannel in = new FileInputStream(source).getChannel();
             FileChannel out = new FileOutputStream(dest).getChannel()) {
            out.transferFrom(in, 0, in.size());
        }
    }

    private void showNewFolderDialog() {
        EditText input = new EditText(this);
        input.setHint("Nome da pasta");
        new AlertDialog.Builder(this)
            .setTitle("Nova Pasta")
            .setView(input)
            .setPositiveButton("Criar", (d, w) -> {
                File newDir = new File(currentDir, input.getText().toString());
                if (newDir.mkdir()) loadFiles();
                else Toast.makeText(this, "Falha ao criar pasta", Toast.LENGTH_SHORT).show();
            }).show();
    }

    private void showRenameDialog(String oldName) {
        EditText input = new EditText(this);
        input.setText(oldName);
        new AlertDialog.Builder(this)
            .setTitle("Renomear")
            .setView(input)
            .setPositiveButton("Salvar", (d, w) -> {
                File oldF = new File(currentDir, oldName);
                File newF = new File(currentDir, input.getText().toString());
                if (oldF.renameTo(newF)) loadFiles();
            }).show();
    }

    private void showDeleteConfirm(String fileName) {
        new AlertDialog.Builder(this)
            .setTitle("ATENÇÃO")
            .setMessage("Tem certeza que deseja apagar " + fileName + "?")
            .setPositiveButton("SIM, APAGAR", (d, w) -> {
                File f = new File(currentDir, fileName);
                if (f.delete()) loadFiles();
            })
            .setNegativeButton("Cancelar", null)
            .show();
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
