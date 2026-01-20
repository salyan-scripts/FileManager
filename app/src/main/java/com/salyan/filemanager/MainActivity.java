package com.salyan.filemanager;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import java.util.ArrayList;

public class MainActivity extends Activity {

    private static final int REQ_OPEN_TREE = 100;
    private Uri currentUri;
    private ListView listView;
    private TextView pathDisplay;
    private EditText searchBar;
    private FileAdapter adapter;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listView);
        pathDisplay = findViewById(R.id.pathDisplay);
        searchBar = findViewById(R.id.searchBar);

        findViewById(R.id.btnMenu).setOnClickListener(v -> openTree());
        findViewById(R.id.btnSearch).setOnClickListener(v ->
            searchBar.setVisibility(
                searchBar.getVisibility() == View.GONE ? View.VISIBLE : View.GONE
            )
        );
    }

    private void openTree() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(i, REQ_OPEN_TREE);
    }

    @Override
    protected void onActivityResult(int r, int c, @Nullable Intent d) {
        super.onActivityResult(r, c, d);
        if (r == REQ_OPEN_TREE && c == RESULT_OK && d != null) {
            currentUri = d.getData();
            loadFiles();
        }
    }

    private void loadFiles() {
        ArrayList<FileItem> items = new ArrayList<>();
        Cursor cursor = null;

        try {
            Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
                currentUri,
                DocumentsContract.getTreeDocumentId(currentUri)
            );

            cursor = getContentResolver().query(
                childrenUri,
                null,
                null,
                null,
                null
            );

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String name = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                            DocumentsContract.Document.COLUMN_DISPLAY_NAME
                        )
                    );

                    String mime = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                            DocumentsContract.Document.COLUMN_MIME_TYPE
                        )
                    );

                    boolean isDir =
                        DocumentsContract.Document.MIME_TYPE_DIR.equals(mime);

                    items.add(new FileItem(name, isDir));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
        }

        adapter = new FileAdapter(this, items);
        listView.setAdapter(adapter);
        pathDisplay.setText(
            currentUri != null ? currentUri.getPath() : ""
        );
    }
}
