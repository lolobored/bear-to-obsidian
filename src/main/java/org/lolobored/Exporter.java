package org.lolobored;

import org.lolobored.dao.bear.BearNote;
import org.lolobored.services.BearService;
import org.lolobored.services.MemosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.math.BigInteger;
import java.util.Map;

@SpringBootApplication
public class Exporter implements ApplicationRunner {

    @Autowired
    private BearService bearService;

    @Autowired
    private MemosService memosService;

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

        String bearFolder= args.getOptionValues("bear").get(0);

        Map<BigInteger, BearNote> notesById = bearService.exportBearNotes(bearFolder);
        memosService.importNotes("http://localhost:5230",
                "eyJhbGciOiJIUzI1NiIsImtpZCI6InYxIiwidHlwIjoiSldUIn0.eyJuYW1lIjoibGF1cmVudGxhYm9yZGUiLCJpc3MiOiJtZW1vcyIsInN1YiI6IjEiLCJhdWQiOlsidXNlci5hY2Nlc3MtdG9rZW4iXSwiaWF0IjoxNzYxNTQyNzU0fQ.tePDGaV3-eqV8ta8_1JhOexUQSG9gTSZ2-A0gBnGJyQ", notesById, bearFolder);
    }


}
