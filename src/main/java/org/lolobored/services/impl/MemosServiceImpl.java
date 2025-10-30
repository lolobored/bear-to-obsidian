package org.lolobored.services.impl;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.lolobored.dao.bear.BearAttachment;
import org.lolobored.dao.bear.BearNote;
import org.lolobored.dao.memos.MemosAttachment;
import org.lolobored.dao.memos.MemosNote;
import org.lolobored.dao.rest.Response;
import org.lolobored.services.MemosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MemosServiceImpl implements MemosService {

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public void importNotes(String memosUrl, String memosToken, Map<BigInteger, BearNote> notesById, String bearFolder) throws IOException {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(memosToken);

        for (BearNote bearNote : notesById.values()) {
            MemosNote memosNote= new MemosNote();
            memosNote.setContent(bearNote.getText());
            memosNote.setCreateTime(bearNote.getCreationDate());
            memosNote.setUpdateTime(bearNote.getUpdateDate());

            HttpEntity<MemosNote> request= new HttpEntity<>(memosNote, headers);
            ResponseEntity<Response> response = restTemplate.exchange(memosUrl + "/api/v1/memos", HttpMethod.POST, request, Response.class);
            String memoId= StringUtils.remove(response.getBody().getName(), "memos/");
            List<MemosAttachment> memosAttachments= new ArrayList<>();
            if (!bearNote.getBearAttachments().isEmpty()) {
                Map<String,String> attachmentsToReplace= new HashMap<>();
                for (BearAttachment bearAttachment : bearNote.getBearAttachments()) {
                    MemosAttachment memosAttachment = buildMemosAttachment(bearFolder, bearAttachment, "memos/" + memoId);
                    HttpEntity<MemosAttachment> requestAttachment = new HttpEntity<>(memosAttachment, headers);
                    response = restTemplate.exchange(memosUrl + "/api/v1/attachments", HttpMethod.POST, requestAttachment, Response.class);
                    attachmentsToReplace.put(memosAttachment.getFilename(), "file/"+response.getBody().getName()+"/"+memosAttachment.getFilename());
                    memosAttachment.setName(response.getBody().getName());
                    memosAttachments.add(memosAttachment);
                }

                for (Map.Entry<String, String> entrySet : attachmentsToReplace.entrySet()) {
                    memosNote.setContent(Strings.CS.replace(memosNote.getContent(),"("+entrySet.getKey()+")", "("+entrySet.getValue()+")"));
                }
                memosNote.setAttachments(memosAttachments);
                request= new HttpEntity<>(memosNote, headers);
                restTemplate.exchange(memosUrl + "/api/v1/memos/"+memoId, HttpMethod.PATCH, request, String.class, memoId);
            }
        }

    }
    
    private MemosAttachment buildMemosAttachment(String bearFolder, BearAttachment bearAttachment, String memo) throws IOException {
        String attachmentImageFolder = "/Application Data/Local Files/Note Images";
        String attachmentFileFolder = "/Application Data/Local Files/Note Files";
        MemosAttachment memosAttachment= new MemosAttachment();
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
        memosAttachment.setType(Files.probeContentType(attachmentFile.toPath()));
        memosAttachment.setFilename(bearAttachment.getFileName());
        // read the file as bytes
        memosAttachment.setContent(FileUtils.readFileToByteArray(attachmentFile));
        memosAttachment.setMemo(memo);
        return memosAttachment;
    }
}
