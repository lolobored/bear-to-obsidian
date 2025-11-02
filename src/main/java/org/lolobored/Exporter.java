package org.lolobored;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.lolobored.dao.bear.BearNote;
import org.lolobored.dao.memos.MemosNote;
import org.lolobored.dao.sync.Sync;
import org.lolobored.repository.SyncRepository;
import org.lolobored.services.BearService;
import org.lolobored.services.MemosService;
import org.lolobored.utils.ChecksumUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@SpringBootApplication
public class Exporter implements ApplicationRunner {

    @Autowired
    private BearService bearService;

    @Autowired
    private MemosService memosService;

    @Autowired
    private SyncRepository syncRepository;

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
        if (!args.containsOption("token")) {
            throw new Exception("Option --token is mandatory and should contain the token created in Memos to import notes");
        }
        if (!args.containsOption("url")) {
            throw new Exception("Option --url is mandatory and should contain the url to memos");
        }

        String bearFolder= args.getOptionValues("bear").get(0);
        String memosToken= args.getOptionValues("token").get(0);
        String memosUrl= args.getOptionValues("url").get(0);

        memosService.setMemosSettings(memosUrl, memosToken);
        importIntoMemos(bearFolder, memosUrl, memosToken);
    }


    private void importIntoMemos(String bearFolder, String memosUrl, String memosToken) throws IOException {
        List<BearNote> bearNotes = bearService.retrieveBearNotes(bearFolder);

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
        sync.setMemosId(memosNote.getName());
        sync.setMemosChecksum(ChecksumUtil.memosChecksum(memosNote));
        sync.setDeleted(bearNote.isDeleted());
        syncRepository.save(sync);
    }


}
