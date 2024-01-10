package org.lolobored.dao;

import lombok.Data;

@Data
public class NoteSQL {
    private int id;
    private String title;
    private String text;
    private boolean trashed;
    private String fileFolderName;
    private String fileName;

    public NoteSQL(String id,
                   String title,
                   String text,
                   String fileFolderName,
                   String fileName,
                   String trashed){
        this.id= Integer.parseInt(id);
        this.title= title;
        this.text=text;
        this.trashed="1".equals(trashed);
        this.fileFolderName=fileFolderName;
        this.fileName=fileName;
    }
}
