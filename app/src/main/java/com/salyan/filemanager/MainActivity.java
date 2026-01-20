package com.salyan.filemanager;

import android.content.Intent;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends AppCompatActivity {
    private File currentDir;
    private ListView listView;
    private TextView pathDisplay;
    private File fileToMoveOrCopy = null;
    private boolean isCut = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Layout Principal (Cópia fiel do seu modelo)
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setBackgroundColor(0xFFF0F0F0);

        // Toolbar superior (Adm. de arquivos)
        TextView toolbar = new TextView(this);
        toolbar.setText(" Adm. de arquivos");
        toolbar.setTextSize(20);
        toolbar.setPadding(30, 40, 30, 40);
        toolbar.setBackgroundColor(0xFFFFFFFF);
        mainLayout.addView(toolbar);

        // Sistema de Abas (Tabs)
        HorizontalScrollView tabScroll = new HorizontalScrollView(this);
        LinearLayout tabs = new LinearLayout(this);
        String[] tabNames = {"↑", "Memória interna", "Android", "data"};
        for (String tabName : tabNames) {
            Button b = new Button(this, null, android.R.attr.borderlessButtonStyle);
            b.setText(tabName);
            tabs.addView(b);
            if(tabName.equals("Memória interna")) b.setOnClickListener(v -> {
                currentDir = Environment.getExternalStorageDirectory();
                loadFiles();
            });
        }
        tabScroll.addView(tabs);
        mainLayout.addView(tabScroll);

        // Área de conteúdo
        listView = new ListView(this);
        listView.setBackgroundColor(0xFFFFFFFF);
        mainLayout.addView(listView);

        setContentView(mainLayout);
        checkPermissions();
        currentDir = Environment.getExternalStorageDirectory();
        loadFiles();

        listView.setOnItemClickListener((p, v, pos, id) -> {
            File f = (File) v.getTag();
            if(f.isDirectory()) { currentDir = f; loadFiles(); }
        });

        listView.setOnItemLongClickListener((p, v, pos, id) -> {
            showContextMenu((File) v.getTag());
            return true;
        });
    }

    private void loadFiles() {
        File[] files = currentDir.listFiles();
        List<File> fileList = new ArrayList<>();
        if(files != null) Collections.addAll(fileList, files);
        
        BaseAdapter adapter = new BaseAdapter() {
            @Override public int getCount() { return fileList.size(); }
            @Override public Object getItem(int i) { return fileList.get(i); }
            @Override public long getItemId(int i) { return i; }
            @Override public View getView(int i, View view, ViewGroup vg) {
                if(view == null) view = getLayoutInflater().inflate(R.layout.line_item, null);
                File f = fileList.get(i);
                ((TextView)view.findViewById(R.id.item_icon)).setText(f.isDirectory() ? "📁" : "📄");
                ((TextView)view.findViewById(R.id.item_name)).setText(f.getName());
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                ((TextView)view.findViewById(R.id.item_date)).setText(sdf.format(new Date(f.lastModified())));
                view.setTag(f);
                return view;
            }
        };
        listView.setAdapter(adapter);
    }

    private void showContextMenu(File f) {
        String[] opts = {"Apagar", "Mover", "Copiar", "Renomear"};
        new AlertDialog.Builder(this).setTitle(f.getName()).setItems(opts, (d, w) -> {
            if(w == 0) { f.delete(); loadFiles(); }
            else if(w == 1) { fileToMoveOrCopy = f; isCut = true; Toast.makeText(this, "Selecione o destino e use o menu", Toast.LENGTH_LONG).show(); }
            else if(w == 2) { fileToMoveOrCopy = f; isCut = false; }
            else if(w == 3) showRenameDialog(f);
        }).show();
    }

    private void showRenameDialog(File f) {
        EditText input = new EditText(this);
        input.setText(f.getName());
        new AlertDialog.Builder(this).setTitle("Renomear").setView(input)
            .setPositiveButton("OK", (d, w) -> {
                f.renameTo(new File(f.getParent(), input.getText().toString()));
                loadFiles();
            }).show();
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            startActivity(new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.fromParts("package", getPackageName(), null)));
        }
    }
}
