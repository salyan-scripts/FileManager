package com.salyan.filemanager;

import android.content.Intent;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import android.graphics.Color;
import android.graphics.Typeface;
import androidx.appcompat.app.AppCompatActivity;
import java.io.*;
import java.util.*;

public class MainActivity extends AppCompatActivity {
    private File currentDir;
    private ListView listView;
    private List<File> fileList = new ArrayList<>();
    private TextView pathDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);

        // Título do App
        TextView header = new TextView(this);
        header.setText("Salyan File Manager");
        header.setTextSize(24);
        header.setPadding(40, 50, 40, 10);
        header.setTextColor(Color.parseColor("#212121"));
        header.setTypeface(null, Typeface.BOLD);
        root.addView(header);

        // Indicador de Pasta Atual
        pathDisplay = new TextView(this);
        pathDisplay.setPadding(40, 0, 40, 20);
        pathDisplay.setTextSize(12);
        pathDisplay.setTextColor(Color.GRAY);
        root.addView(pathDisplay);

        // Botões de Atalho
        LinearLayout tabs = new LinearLayout(this);
        tabs.setPadding(20, 10, 20, 10);
        String[] locations = {"INÍCIO", "ANDROID", "DOWNLOAD"};
        for (String loc : locations) {
            Button btn = new Button(this, null, android.R.attr.borderlessButtonStyle);
            btn.setText(loc);
            btn.setOnClickListener(v -> navigateTo(loc));
            tabs.addView(btn);
        }
        root.addView(tabs);

        listView = new ListView(this);
        root.addView(listView);

        setContentView(root);

        currentDir = Environment.getExternalStorageDirectory();
        
        listView.setOnItemClickListener((p, v, pos, id) -> {
            File f = fileList.get(pos);
            if (f.isDirectory()) {
                currentDir = f;
                loadFiles();
            } else {
                Toast.makeText(this, "Arquivo: " + f.getName(), Toast.LENGTH_SHORT).show();
            }
        });

        checkStoragePermission();
    }

    private void checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.addCategory("android.intent.category.DEFAULT");
                    intent.setData(Uri.parse(String.format("package:%s", getPackageName())));
                    startActivityForResult(intent, 100);
                } catch (Exception e) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivityForResult(intent, 100);
                }
            } else {
                loadFiles();
            }
        } else {
            loadFiles(); // Versões antigas não precisam dessa tela
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            loadFiles(); // Tenta carregar os arquivos após voltar da configuração
        }
    }

    private void navigateTo(String loc) {
        File root = Environment.getExternalStorageDirectory();
        if (loc.equals("INÍCIO")) currentDir = root;
        else if (loc.equals("ANDROID")) currentDir = new File(root, "Android");
        else if (loc.equals("DOWNLOAD")) currentDir = new File(root, "Download");
        loadFiles();
    }

    private void loadFiles() {
        pathDisplay.setText(currentDir.getAbsolutePath());
        File[] files = currentDir.listFiles();
        fileList.clear();
        
        if (files != null && files.length > 0) {
            fileList.addAll(Arrays.asList(files));
            // Organiza: Pastas primeiro, depois arquivos
            Collections.sort(fileList, (a, b) -> {
                if (a.isDirectory() && !b.isDirectory()) return -1;
                if (!a.isDirectory() && b.isDirectory()) return 1;
                return a.getName().toLowerCase().compareTo(b.getName().toLowerCase());
            });
        }

        listView.setAdapter(new BaseAdapter() {
            @Override public int getCount() { return fileList.size(); }
            @Override public Object getItem(int i) { return fileList.get(i); }
            @Override public long getItemId(int i) { return i; }
            @Override public View getView(int i, View view, ViewGroup vg) {
                LinearLayout item = new LinearLayout(MainActivity.this);
                item.setPadding(40, 35, 40, 35);
                item.setGravity(Gravity.CENTER_VERTICAL);
                
                TextView icon = new TextView(MainActivity.this);
                File f = fileList.get(i);
                icon.setText(f.isDirectory() ? "📁 " : "📄 ");
                icon.setTextSize(22);
                
                TextView name = new TextView(MainActivity.this);
                name.setText(f.getName());
                name.setTextSize(16);
                name.setTextColor(Color.BLACK);

                item.addView(icon);
                item.addView(name);
                return item;
            }
        });
        
        if (fileList.isEmpty()) {
            Toast.makeText(this, "Pasta vazia ou sem permissão", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        File parent = currentDir.getParentFile();
        if (parent != null && currentDir.getAbsolutePath().contains(Environment.getExternalStorageDirectory().getAbsolutePath()) 
            && !currentDir.equals(Environment.getExternalStorageDirectory())) {
            currentDir = parent;
            loadFiles();
        } else {
            super.onBackPressed();
        }
    }
}
