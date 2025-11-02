package org.lolobored.bear2memos;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Strings;
import org.lolobored.bear2memos.dao.bear.BearNote;
import org.lolobored.bear2memos.dao.memos.MemosNote;
import org.lolobored.bear2memos.dao.sync.Sync;
import org.lolobored.bear2memos.repository.SyncRepository;
import org.lolobored.bear2memos.services.BearService;
import org.lolobored.bear2memos.services.MemosService;
import org.lolobored.bear2memos.utils.ChecksumUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@SpringBootApplication
public class Synchronizer implements ApplicationRunner {

    @Autowired
    private BearService bearService;

    @Autowired
    private MemosService memosService;

    @Autowired
    private SyncRepository syncRepository;

    @Autowired
    private Environment environment;

    static void main(String[] args) {
        SpringApplication application = new SpringApplication(Synchronizer.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        application.run(args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        String bearFolder= environment.getProperty("bear.folder");
        String memosToken= environment.getProperty("memos.token");
        String memosUrl= environment.getProperty("memos.url");
        String memosDir= environment.getProperty("memos.folders.root");

        // set up configuration automatically
        memosService.setMemosSettings(memosUrl, memosToken);
        importIntoMemos(bearFolder, memosUrl, memosToken);
        deleteFromMemos(bearFolder, memosUrl, memosToken);
        detectChangesInMemos(bearFolder, memosUrl, memosToken, memosDir);
    }

    private void detectChangesInMemos(String bearFolder, String memosUrl, String memosToken, String memosDir) throws IOException {
        List<MemosNote> listMemos= memosService.listMemoNotes(memosUrl, memosToken);
        Map<BigInteger, BearNote> bearNotesById = bearService.retrieveBearNotes(bearFolder);
        File createdFolder=Path.of(memosDir+"/modifications/created").toFile();
        File modifiedFolder=Path.of(memosDir+"/modifications/modified").toFile();
        File deletedFolder=Path.of(memosDir+"/modifications/deleted").toFile();
        // create folders
        FileUtils.deleteDirectory(createdFolder);
        FileUtils.deleteDirectory(modifiedFolder);
        FileUtils.deleteDirectory(deletedFolder);
        createdFolder.mkdirs();
        modifiedFolder.mkdirs();
        deletedFolder.mkdirs();

        int created=1;
        int modified=1;
        int deleted=1;

        // retrieve all the ids of the memosNote
        Set<String> memosNoteIds= new HashSet<>();

        for (MemosNote memo : listMemos) {

            String memoName= Strings.CI.removeStart(memo.getName(),"memos/");
            memosNoteIds.add(memoName);
            Optional<Sync> sync = syncRepository.findByMemosId(memoName);
            if (sync.isEmpty()){
                String content="Created: "+memo.getCreateTime()+"\n";
                content+="Modified: "+memo.getUpdateTime()+"\n\n";
                content+=memo.getContent();
                FileUtils.writeStringToFile(new File(createdFolder.getAbsolutePath()+"/created-"+created+".md"), content, Charset.defaultCharset() );
                created++;
            }
            else{
                String newChecksum = ChecksumUtil.memosChecksum(memo);
                if (!newChecksum.equals(sync.get().getMemosChecksum())){
                    String content="Created: "+memo.getCreateTime()+"\n";
                    content+="Modified: "+memo.getUpdateTime()+"\n\n";
                    content+=memo.getContent();
                    FileUtils.writeStringToFile(new File(modifiedFolder.getAbsolutePath()+"/modified-"+modified+".md"), content, Charset.defaultCharset() );
                    modified++;
                }
            }
        }

        List<Sync> syncs = syncRepository.findAll();
        // if in sync but no longer in memo this has been deleted
        for (Sync sync : syncs) {
            if (!memosNoteIds.contains(sync.getMemosId())){
                BearNote bearNote= bearNotesById.get(sync.getBearId());
                String content="Created: "+bearNote.getCreationDate()+"\n";
                content+="Modified: "+bearNote.getUpdateDate()+"\n\n";
                content+=bearNote.getText();
                FileUtils.writeStringToFile(new File(deletedFolder.getAbsolutePath()+"/deleted-"+deleted+".md"), content, Charset.defaultCharset() );
                deleted++;
                syncRepository.save(sync);
            }
        }

    }

    private void deleteFromMemos(String bearFolder, String memosUrl, String memosToken) throws IOException {
        List<Sync> syncs = syncRepository.findAll();
        Map<BigInteger, BearNote> bearNotes = bearService.retrieveBearNotes(bearFolder);
        BearNote deletedNote = new BearNote();
        deletedNote.setDeleted(true);

        for (Sync sync : syncs) {
            // check if bearNotes exists
            BearNote bearNote = bearNotes.getOrDefault(sync.getBearId(), deletedNote);
            if (bearNote.isDeleted()){
                memosService.deleteNote(memosUrl,
                        memosToken,
                        sync.getMemosId());
                sync.setDeleted(true);
                syncRepository.save(sync);
            }
        }
    }


    private void importIntoMemos(String bearFolder, String memosUrl, String memosToken) throws IOException {
        Map<BigInteger, BearNote> bearNotesById = bearService.retrieveBearNotes(bearFolder);
        List<BearNote> bearNotes = new ArrayList<>(bearNotesById.values());
        Collections.sort(bearNotes);
        for (BearNote bearNote : bearNotes) {
            Optional<Sync> optionalSync = syncRepository.findById(bearNote.getId());
            if (optionalSync.isEmpty()){
                if (!bearNote.isDeleted()){
                    MemosNote memosNote = memosService.createNote(memosUrl,
                            memosToken,
                            bearNote);
                    recordChange(bearNote, memosNote);
                }
            }
            else{
                if (bearNote.isDeleted() && !optionalSync.get().isDeleted()){
                    memosService.deleteNote(memosUrl,
                            memosToken,
                            optionalSync.get().getMemosId());
                    recordChange(bearNote, new MemosNote());
                }
                // compare checksum
                String newChecksum = ChecksumUtil.bearChecksum(bearNote);
                // if needs update
                if (!newChecksum.equals(optionalSync.get().getBearChecksum())){
                    MemosNote memosNote= memosService.updateNote(memosUrl,
                            memosToken,
                            optionalSync.get().getMemosId(),
                            bearNote);
                    recordChange(bearNote, memosNote);
                }
            }
        }
    }

    private void recordChange(BearNote bearNote, MemosNote memosNote) throws JsonProcessingException {
        Sync sync = new Sync();
        sync.setBearId(bearNote.getId());
        sync.setBearChecksum(ChecksumUtil.bearChecksum(bearNote));
        sync.setMemosId(Strings.CI.removeStart(memosNote.getName(),"memos/"));
        sync.setMemosChecksum(ChecksumUtil.memosChecksum(memosNote));
        sync.setDeleted(bearNote.isDeleted());
        syncRepository.save(sync);
    }


}
