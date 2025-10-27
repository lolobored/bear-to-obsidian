package org.lolobored.dao.bear;

import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Data
public class BearNoteSQL {
    private BigInteger id;
    private String title;
    private String text;
    private boolean trashed;
    private BearAttachment bearAttachment;
    private LocalDateTime creationDate;
    private LocalDateTime updateDate;

    public BearNoteSQL(String id,
                       String title,
                       String text,
                       String fileFolderName,
                       String fileName,
                       String trashed,
                       String creationDate,
                       String updateDate){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


        this.id= new BigInteger(id);
        this.title= title;
        this.text=text;
        this.trashed="1".equals(trashed);
        if (fileFolderName!=null && fileName!= null){
            BearAttachment bearAttachment = new BearAttachment();
            bearAttachment.setFileName(fileName);
            bearAttachment.setFileFolderName(fileFolderName);
            this.bearAttachment = bearAttachment;
        }
        this.creationDate= LocalDateTime.parse(creationDate, formatter);
        this.creationDate.atOffset(ZoneOffset.UTC);
        this.updateDate= LocalDateTime.parse(updateDate, formatter);
        this.updateDate.atOffset(ZoneOffset.UTC);
    }
}

