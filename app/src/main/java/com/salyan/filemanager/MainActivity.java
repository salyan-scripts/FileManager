package com.salyan.filemanager;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.widget.*;
import androidx.documentfile.provider.DocumentFile;
import java.util.ArrayList;

public class MainActivity extends Activity {

    private static final int REQ_TREE = 1;
    private Uri currentUri;
    private DocumentFile clipboard;
    private ListView listView;
    private TextView path;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listView);
        path = findViewById(R.id.pathDisplay);

        findViewById(R.id.btnOpen).setOnClickListener(v -> openTree());
        findViewById(R.id.btnPaste).setOnClickListener(v -> paste());

        listView.setOnItemClickListener((a, v, p, i) -> {
            FileItem item = (FileItem) a.getItemAtPosition(p);
            if (item.file.isDirectory()) {
                currentUri = item.file.getUri();
                load();
            } else {
                openWith(item.file);
            }
        });

        listView.setOnItemLongClickListener((a, v, p, i) -> {
            FileItem item = (FileItem) a.getItemAtPosition(p);
            clipboard = item.file;
            return true;
        });
    }

    private void openTree() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(i, REQ_TREE);
    }

    @Override
    protected void onActivityResult(int r, int c, Intent d) {
        if (r == REQ_TREE && c == RESULT_OK) {
            currentUri = d.getData();
            load();
        }
    }

    private void load() {
        DocumentFile dir = DocumentFile.fromTreeUri(this, currentUri);
        ArrayList<FileItem> list = new ArrayList<>();
        for (DocumentFile f : dir.listFiles())
            list.add(new FileItem(f));
        listView.setAdapter(new FileAdapter(this, list));
        path.setText(currentUri.getPath());
    }

    private void paste() {
        if (clipboard == null || currentUri == null) return;
        DocumentFile dest = DocumentFile.fromTreeUri(this, currentUri);
        clipboard.copyTo(dest);
        load();
    }

    private void openWith(DocumentFile f) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(f.getUri(), f.getType());
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(i);
    }
}
