package com.salyan.filemanager;

import androidx.documentfile.provider.DocumentFile;

public class FileItem {
    public DocumentFile file;
    public FileItem(DocumentFile f) { file = f; }
    public String toString() { return file.getName(); }
}
