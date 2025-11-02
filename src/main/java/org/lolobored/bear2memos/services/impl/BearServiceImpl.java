package org.lolobored.bear2memos.services.impl;

import org.apache.commons.io.FileUtils;
import org.lolobored.bear2memos.dao.bear.BearAttachment;
import org.lolobored.bear2memos.dao.bear.BearAttachmentSQL;
import org.lolobored.bear2memos.dao.bear.BearNote;
import org.lolobored.bear2memos.dao.bear.BearNoteSQL;
import org.lolobored.bear2memos.repository.SyncRepository;
import org.lolobored.bear2memos.services.BearService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.util.*;

@Service
public class BearServiceImpl implements BearService {

    @Autowired
    private SyncRepository syncRepository;

    @Override
    public Map<BigInteger, BearNote> retrieveBearNotes(String bearFolder) throws IOException {
        // reinit JDBC connection dynamically
        DriverManagerDataSource dataSourceBear = new DriverManagerDataSource();
        dataSourceBear.setDriverClassName("org.sqlite.JDBC");
        dataSourceBear.setUrl("jdbc:sqlite:" + bearFolder + "/Application Data/database.sqlite");
        JdbcTemplate jdbcTemplateBear = new JdbcTemplate(dataSourceBear);

        List<BearNoteSQL> notesSQL = jdbcTemplateBear.query("SELECT\n" +
                        "      ZSFNOTE.Z_PK AS id,\n" +
                        "      ZSFNOTE.ZTITLE AS title,\n" +
                        "\t  ZSFNOTE.ZTEXT AS text,\n" +
                        "\t  ZSFNOTEFILE.ZFILENAME AS filename,\n" +
                        "      ZSFNOTEFILE.ZUNIQUEIDENTIFIER AS folder,\n" +
                        "\t  ZSFNOTE.ZTRASHED AS trashed,\n" +
                        // weirdly the timestamp starts not in 1970 but
                        // on 2001-01-01 at 8:00AM
                        "\t  DATETIME(978336000+ZSFNOTE.ZCREATIONDATE , 'unixepoch') AS creation_date,\n" +
                        "\t  DATETIME(978336000+ZSFNOTE.ZMODIFICATIONDATE , 'unixepoch') AS update_date \n" +
                        "  FROM ZSFNOTE\n" +
                        "  LEFT JOIN ZSFNOTEFILE ON ZSFNOTE.Z_PK = ZSFNOTEFILE.ZNOTE",
                (rs, rowNum) -> new BearNoteSQL(
                        rs.getString("id"),
                        rs.getString("title"),
                        rs.getString("text"),
                        rs.getString("folder"),
                        rs.getString("filename"),
                        rs.getString("trashed"),
                        rs.getString("creation_date"),
                        rs.getString("update_date")
                ));

        Map<BigInteger, BearNote> notesById = new HashMap<>();

        // collect all the notes (note that notes can be appearing multiple times because of multiple tasgs
        for (BearNoteSQL bearNoteSQL : notesSQL) {

            BearNote bearNote = notesById.getOrDefault(bearNoteSQL.getId(), new BearNote());
            bearNote.setId(bearNoteSQL.getId());
            bearNote.setText(bearNoteSQL.getText());
            bearNote.setTitle(bearNoteSQL.getTitle());
            bearNote.setCreationDate(bearNoteSQL.getCreationDate());
            bearNote.setUpdateDate(bearNoteSQL.getUpdateDate());
            bearNote.setDeleted(bearNoteSQL.isTrashed());
            if (bearNoteSQL.getBearAttachmentSQL() != null) {
                bearNote.addAttachment(buildBearAttachment(bearFolder, bearNoteSQL.getBearAttachmentSQL()));
            }
            notesById.put(bearNoteSQL.getId(), bearNote);
        }
        return notesById;
    }

    private BearAttachment buildBearAttachment(String bearFolder, BearAttachmentSQL bearAttachmentSQL) throws IOException {
        String attachmentImageFolder = "/Application Data/Local Files/Note Images";
        String attachmentFileFolder = "/Application Data/Local Files/Note Files";
        BearAttachment bearAttachment = new BearAttachment();
        // determine where the file is
        File attachmentFile = new File(bearFolder + attachmentImageFolder + "/" + bearAttachmentSQL.getFileFolderName() + "/" + bearAttachmentSQL.getFileName());
        if (!attachmentFile.exists()) {
            attachmentFile = new File(bearFolder + attachmentFileFolder + "/" + bearAttachmentSQL.getFileFolderName() + "/" + bearAttachmentSQL.getFileName());
            if (!attachmentFile.exists()) {
                throw new FileNotFoundException("File [" + bearAttachmentSQL.getFileFolderName() + "/" + bearAttachmentSQL.getFileName() + "] " +
                        "was not found in [" + bearFolder + attachmentImageFolder + "/" + bearAttachmentSQL.getFileFolderName() + "/" + bearAttachmentSQL.getFileName() + "] " +
                        "or [" + bearFolder + attachmentFileFolder + "/" + bearAttachmentSQL.getFileFolderName() + "/" + bearAttachmentSQL.getFileName() + "]");
            }
        }
        // this.name="attachments/"+bearAttachment.getFileName();
        bearAttachment.setType(Files.probeContentType(attachmentFile.toPath()));
        bearAttachment.setFilename(bearAttachmentSQL.getFileName());
        // read the file as bytes
        bearAttachment.setContent(FileUtils.readFileToByteArray(attachmentFile));
        return bearAttachment;
    }
}