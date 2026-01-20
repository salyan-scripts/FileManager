package com.salyan.filemanager;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> {
    private final List<File> files;
    private final OnFileClickListener listener;
    public interface OnFileClickListener { void onFileClick(File file); }
    public FileAdapter(List<File> files, OnFileClickListener listener) {
        this.files = files; this.listener = listener;
    }
    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int t) {
        View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_file, p, false);
        return new ViewHolder(v);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int p) {
        File f = files.get(p);
        h.txtName.setText(f.getName());
        h.txtDate.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date(f.lastModified())));
        h.imgIcon.setImageResource(f.isDirectory() ? android.R.drawable.ic_menu_archive : android.R.drawable.ic_menu_report_image);
        h.itemView.setOnClickListener(v -> listener.onFileClick(f));
    }
    @Override public int getItemCount() { return files.size(); }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgIcon; TextView txtName, txtDate;
        public ViewHolder(View i) { super(i);
            imgIcon = i.findViewById(R.id.img_icon);
            txtName = i.findViewById(R.id.txt_name);
            txtDate = i.findViewById(R.id.txt_date);
        }
    }
}
