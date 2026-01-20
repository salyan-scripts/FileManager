package com.salyan.filemanager;
import android.os.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;
import java.io.File;
import java.util.*;

public class MainActivity extends AppCompatActivity {
    private RecyclerView rv; private TextView tv, storageText; 
    private ProgressBar storageBar; private File current;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = findViewById(R.id.path_view);
        storageText = findViewById(R.id.storage_text);
        storageBar = findViewById(R.id.storage_bar);
        rv = findViewById(R.id.recycler_view);
        rv.setLayoutManager(new LinearLayoutManager(this));
        
        updateStorageInfo();
        updateList(Environment.getExternalStorageDirectory());
    }

    private void updateStorageInfo() {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long blockSize = stat.getBlockSizeLong();
        long totalBlocks = stat.getBlockCountLong();
        long availableBlocks = stat.getAvailableBlocksLong();

        long totalSpace = (totalBlocks * blockSize) / (1024 * 1024 * 1024);
        long freeSpace = (availableBlocks * blockSize) / (1024 * 1024 * 1024);
        long usedSpace = totalSpace - freeSpace;

        storageText.setText(usedSpace + " GB usados de " + totalSpace + " GB");
        storageBar.setMax((int)totalSpace);
        storageBar.setProgress((int)usedSpace);
    }

    private void updateList(File folder) {
        current = folder; tv.setText(folder.getAbsolutePath());
        File[] fa = folder.listFiles();
        List<File> fl = new ArrayList<>();
        if (fa != null) {
            fl.addAll(Arrays.asList(fa));
            Collections.sort(fl, (f1, f2) -> {
                if (f1.isDirectory() && !f2.isDirectory()) return -1;
                if (!f1.isDirectory() && f2.isDirectory()) return 1;
                return f1.getName().toLowerCase().compareTo(f2.getName().toLowerCase());
            });
        }
        rv.setAdapter(new FileAdapter(fl, f -> { if (f.isDirectory()) updateList(f); }));
    }

    @Override
    public void onBackPressed() {
        if (!current.equals(Environment.getExternalStorageDirectory())) updateList(current.getParentFile());
        else super.onBackPressed();
    }
}
