package org.lolobored.dao.memos;

import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Strings;
import org.lolobored.dao.bear.BearAttachment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

@Data
public class MemosAttachment {
    private String filename;
    private String type;
    private byte[] content;
    private String memo;

    public MemosAttachment(String bearFolder, BearAttachment bearAttachment, String memo) throws IOException {
        String attachmentImageFolder = "/Application Data/Local Files/Note Images";
        String attachmentFileFolder = "/Application Data/Local Files/Note Files";

        // determine where the file is
        File attachmentFile= new File(bearFolder+attachmentImageFolder+"/"+bearAttachment.getFileFolderName()+"/"+bearAttachment.getFileName());
        if (!attachmentFile.exists()){
            attachmentFile= new File(bearFolder+attachmentFileFolder+"/"+bearAttachment.getFileFolderName()+"/"+bearAttachment.getFileName());
            if (!attachmentFile.exists()){
                throw new FileNotFoundException("File ["+bearAttachment.getFileFolderName()+"/"+bearAttachment.getFileName()+"] " +
                        "was not found in ["+bearFolder+attachmentImageFolder+"/"+bearAttachment.getFileFolderName()+"/"+bearAttachment.getFileName()+"] " +
                        "or ["+bearFolder+attachmentFileFolder+"/"+bearAttachment.getFileFolderName()+"/"+bearAttachment.getFileName()+"]");
            }
        }
       // this.name="attachments/"+bearAttachment.getFileName();
        this.type= Files.probeContentType(attachmentFile.toPath());
        this.filename=bearAttachment.getFileName();
        // read the file as bytes
        content= FileUtils.readFileToByteArray(attachmentFile);
        this.memo= memo;
    }
}
