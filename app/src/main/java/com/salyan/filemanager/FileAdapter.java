package com.salyan.filemanager;

import android.content.Context;
import android.view.*;
import android.widget.*;
import java.util.ArrayList;

public class FileAdapter extends BaseAdapter {

    private Context ctx;
    private ArrayList<FileItem> items;

    public FileAdapter(Context c, ArrayList<FileItem> i) {
        ctx = c;
        items = i;
    }

    @Override public int getCount() { return items.size(); }
    @Override public Object getItem(int i) { return items.get(i); }
    @Override public long getItemId(int i) { return i; }

    @Override
    public View getView(int p, View v, ViewGroup g) {
        if (v == null)
            v = LayoutInflater.from(ctx).inflate(R.layout.item_file, g, false);

        ImageView icon = v.findViewById(R.id.icon);
        TextView name = v.findViewById(R.id.name);

        FileItem f = items.get(p);
        name.setText(f.name);
        icon.setImageResource(
            f.isDir ? R.drawable.ic_folder : R.drawable.ic_file
        );
        return v;
    }
}
