package com.salyan.filemanager;

import android.content.Intent;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import android.graphics.Color;
import android.graphics.Typeface;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends AppCompatActivity {
    private File currentDir;
    private ListView listView;
    private List<File> fileList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Layout Principal
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);

        // Header (Toolbar)
        TextView header = new TextView(this);
        header.setText("Salyan File Manager");
        header.setTextSize(22);
        header.setPadding(40, 50, 40, 50);
        header.setTextColor(Color.BLACK);
        header.setTypeface(null, Typeface.BOLD);
        root.addView(header);

        // Abas de Navegação (Estilo LG/Samsung)
        LinearLayout tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        String[] locations = {"Início", "Android", "Download"};
        for (String loc : locations) {
            Button btn = new Button(this, null, android.R.attr.borderlessButtonStyle);
            btn.setText(loc);
            btn.setOnClickListener(v -> navigateTo(loc));
            tabs.addView(btn);
        }
        root.addView(tabs);

        // Lista de Arquivos
        listView = new ListView(this);
        root.addView(listView);

        setContentView(root);

        currentDir = Environment.getExternalStorageDirectory();
        checkPermissions();
        loadFiles();

        listView.setOnItemClickListener((p, v, pos, id) -> {
            File f = fileList.get(pos);
            if (f.isDirectory()) {
                currentDir = f;
                loadFiles();
            }
        });
    }

    private void navigateTo(String loc) {
        if (loc.equals("Início")) currentDir = Environment.getExternalStorageDirectory();
        else if (loc.equals("Android")) currentDir = new File(Environment.getExternalStorageDirectory(), "Android");
        else if (loc.equals("Download")) currentDir = new File(Environment.getExternalStorageDirectory(), "Download");
        loadFiles();
    }

    private void loadFiles() {
        File[] files = currentDir.listFiles();
        fileList.clear();
        if (files != null) {
            fileList.addAll(Arrays.asList(files));
            Collections.sort(fileList, (a, b) -> a.isDirectory() && !b.isDirectory() ? -1 : 1);
        }

        listView.setAdapter(new BaseAdapter() {
            @Override public int getCount() { return fileList.size(); }
            @Override public Object getItem(int i) { return fileList.get(i); }
            @Override public long getItemId(int i) { return i; }
            @Override public View getView(int i, View view, ViewGroup vg) {
                LinearLayout itemLayout = new LinearLayout(MainActivity.this);
                itemLayout.setPadding(30, 30, 30, 30);
                
                TextView icon = new TextView(MainActivity.this);
                File f = fileList.get(i);
                icon.setText(f.isDirectory() ? "📁 " : "📄 ");
                icon.setTextSize(24);
                
                TextView name = new TextView(MainActivity.this);
                name.setText(f.getName());
                name.setTextSize(16);
                name.setTextColor(Color.BLACK);

                itemLayout.addView(icon);
                itemLayout.addView(name);
                return itemLayout;
            }
        });
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
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
