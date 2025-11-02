package org.lolobored.dao.bear;

import lombok.Data;

import java.math.BigInteger;
import java.time.*;
import java.time.format.DateTimeFormatter;

@Data
public class BearNoteSQL {
    private BigInteger id;
    private String title;
    private String text;
    private boolean trashed;
    private BearAttachmentSQL bearAttachmentSQL;
    private ZonedDateTime creationDate;
    private ZonedDateTime updateDate;

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
            BearAttachmentSQL bearAttachmentSQL = new BearAttachmentSQL();
            bearAttachmentSQL.setFileName(fileName);
            bearAttachmentSQL.setFileFolderName(fileFolderName);
            this.bearAttachmentSQL = bearAttachmentSQL;
        }
        this.creationDate= LocalDateTime.parse(creationDate, formatter).atZone(ZoneId.systemDefault());
        this.updateDate= LocalDateTime.parse(updateDate, formatter).atZone(ZoneId.systemDefault());
    }
}

