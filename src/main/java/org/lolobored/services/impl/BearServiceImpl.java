package org.lolobored.services.impl;

import org.lolobored.dao.bear.BearNote;
import org.lolobored.dao.bear.BearNoteSQL;
import org.lolobored.services.BearService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BearServiceImpl implements BearService {

    @Override
    public Map<BigInteger, BearNote> exportBearNotes(String bearFolder) throws IOException {
        // reinit JDBC connection dynamically
        DriverManagerDataSource dataSourceBear = new DriverManagerDataSource();
        dataSourceBear.setDriverClassName("org.sqlite.JDBC");
        dataSourceBear.setUrl("jdbc:sqlite:"+bearFolder+"/Application Data/database.sqlite");
        JdbcTemplate jdbcTemplateBear= new JdbcTemplate(dataSourceBear);

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

        Map<BigInteger, BearNote> notesById= new HashMap<>();

        // collect all the notes (note that notes can be appearing multiple times because of multiple tasgs
        for (BearNoteSQL bearNoteSQL : notesSQL) {
            if (bearNoteSQL.isTrashed()) continue;

            BearNote bearNote = notesById.getOrDefault(bearNoteSQL.getId(), new BearNote());
            bearNote.setId(bearNoteSQL.getId());
            bearNote.setText(bearNoteSQL.getText());
            bearNote.setTitle(bearNoteSQL.getTitle());
            bearNote.setCreationDate(bearNoteSQL.getCreationDate());
            bearNote.setUpdateDate(bearNoteSQL.getUpdateDate());
            if (bearNoteSQL.getBearAttachment()!=null) {
                bearNote.addAttachment(bearNoteSQL.getBearAttachment());
            }
            notesById.put(bearNoteSQL.getId(), bearNote);
        }

        return notesById;
    }
}
