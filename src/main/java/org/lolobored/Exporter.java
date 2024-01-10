package org.lolobored;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.lolobored.dao.Note;
import org.lolobored.dao.NoteSQL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

@SpringBootApplication
public class Exporter implements ApplicationRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(Exporter.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        application.run(args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        if (!args.containsOption("bear")) {
            throw new Exception("Option --bear is mandatory and should contain the path to your Bear directory on MacOS (generally ~/Library/Group Containers/9K33E3U3T4.net.shinyfrog.bear");
        }
        if (!args.containsOption("output")) {
            throw new Exception("Option --output is mandatory and should point to the directory where you want to export your notes");
        }

        String bearFolder= args.getOptionValues("bear").get(0);
        String outputFolder = args.getOptionValues("output").get(0);

        String comparison= compare(outputFolder);
        if (comparison.isEmpty()){
            export(bearFolder, outputFolder);
        }
        else{
            throw new Exception("Unable to export current notes as Obsidian Notes have been modified:\n"+comparison);
        }
    }

    private String compare(String outputFolder) throws IOException {
        StringBuilder result= new StringBuilder();
        FileUtils.forceMkdir(new File(outputFolder));
        Collection<File> mdFilesInBearNotes = FileUtils.listFiles(new File(outputFolder), new String[]{"md"}, true);
        for (File fileInNotes : mdFilesInBearNotes) {
            // check if the file exist already
            String relativePath= StringUtils.remove(fileInNotes.getPath(), outputFolder+"/") ;

            if (relativePath.startsWith(".checksum") ||
                    relativePath.startsWith("attachments")){
                continue;
            }

            File fileInChecksum = new File( outputFolder + "/.checksum/" + relativePath);
            if (!fileInChecksum.exists()){
                result.append("File [").append(relativePath).append("] has been created in notes and was not in Bear at the time of exportation\n");
            }
            else {
                String checksumAtExport = FileUtils.readFileToString(fileInChecksum, Charset.defaultCharset());
                String newChecksum = TagUtils.getChecksum(fileInNotes.getAbsolutePath());
                if (!newChecksum.equals(checksumAtExport)) {
                    result.append("File [").append(relativePath).append("] has been modified in Obsidian\n");
                }
            }
        }
        return result.toString().trim();
    }

    private void export(String bearFolder, String outputFolder) throws IOException {
        FileUtils.forceMkdir(new File(outputFolder));
        for (File file : new File(outputFolder).listFiles()) {
            if (!".obsidian".equalsIgnoreCase(file.getName())){
                if (file.isFile()){
                    FileUtils.delete(file);
                }
                else {
                    FileUtils.deleteDirectory(file);
                }
            }
        }

        List<NoteSQL> notesSQL = jdbcTemplate.query("SELECT\n" +
                        "      ZSFNOTE.Z_PK AS id,\n" +
                        "      ZSFNOTE.ZTITLE AS title,\n" +
                        "\t  ZSFNOTE.ZTEXT AS text,\n" +
                        "\t  ZSFNOTEFILE.ZFILENAME AS filename,\n" +
                        "      ZSFNOTEFILE.ZUNIQUEIDENTIFIER AS folder,\n" +
                        "\t  ZSFNOTE.ZTRASHED AS trashed\n" +
                        "  FROM ZSFNOTE\n" +
                        "  LEFT JOIN ZSFNOTEFILE ON ZSFNOTE.Z_PK = ZSFNOTEFILE.ZNOTE",
                (rs, rowNum) -> new NoteSQL(
                        rs.getString("id"),
                        rs.getString("title"),
                        rs.getString("text"),
                        rs.getString("folder"),
                        rs.getString("filename"),
                        rs.getString("trashed")
                ));

        Map<Integer, Note> notesById= new HashMap<>();

        for (NoteSQL noteSQL : notesSQL) {
            if (noteSQL.isTrashed()) continue;

            Note note = notesById.getOrDefault(noteSQL.getId(), new Note());
            note.setText(noteSQL.getText());
            note.setTitle(noteSQL.getTitle());
            if (noteSQL.getFileName()!= null) {
                note.getListOfFiles().put(noteSQL.getFileName(), noteSQL.getFileFolderName()+"/"+noteSQL.getFileName());
            }
            notesById.put(noteSQL.getId(), note);
        }

        for (Note note : notesById.values()) {
            Set<String> tags = TagUtils.extractTags(note.getText());
            for (String tag : tags) {
                String path = tag;
                path = path + "/" + TagUtils.cleanTitle(note.getTitle()) + ".md";
                // checksum
                String checksumPath=".checksum/"+path;

                path = outputFolder + "/" + path;
                checksumPath= outputFolder + "/" + checksumPath;

                FileUtils.forceMkdirParent(new File(path));
                FileUtils.forceMkdirParent(new File(checksumPath));

                String noteText= TagUtils.replaceImages(note.getText(), note.getListOfFiles());
                noteText= TagUtils.replacePdf(noteText, note.getListOfFiles());
                noteText= TagUtils.cleanText(noteText);
                FileUtils.writeStringToFile(new File(path), noteText, Charset.defaultCharset());
                TagUtils.createChecksum(path, checksumPath);
            }
        }

        // Manage attachments
        FileUtils.deleteDirectory(new File(outputFolder+"/attachments"));
        FileUtils.copyDirectory(new File(bearFolder+"/Application Data/Local Files/Note Images"),
                new File(outputFolder+"/attachments"));
        FileUtils.copyDirectory(new File(bearFolder+"/Application Data/Local Files/Note Files"),
                new File(outputFolder+"/attachments/files"));

    }

}
