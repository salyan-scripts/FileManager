package com.salyan.filemanager;
import android.os.*;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;
import java.io.File;
import java.util.*;

public class MainActivity extends AppCompatActivity {
    private RecyclerView rv; private TextView tv; private File current;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = findViewById(R.id.path_view);
        rv = findViewById(R.id.recycler_view);
        rv.setLayoutManager(new LinearLayoutManager(this));
        updateList(Environment.getExternalStorageDirectory());
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
