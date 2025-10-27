package org.lolobored.services.impl;

import org.apache.commons.lang3.StringUtils;
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

import java.io.IOException;
import java.math.BigInteger;
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

           /** if (!bearNote.getBearAttachments().isEmpty()){
                for (BearAttachment bearAttachment : bearNote.getBearAttachments()) {
                    MemosAttachment memosAttachment= new MemosAttachment(bearFolder, bearAttachment);
                    memosNote.addAttachment(memosAttachment);
                }

            }**/

            HttpEntity<MemosNote> request= new HttpEntity<>(memosNote, headers);
            ResponseEntity<Response> response = restTemplate.exchange(memosUrl + "/api/v1/memos", HttpMethod.POST, request, Response.class);
            String memoId= StringUtils.remove(response.getBody().getName(), "memos/");
            if (!bearNote.getBearAttachments().isEmpty()) {
                Map<String,String> attachmentsToReplace= new HashMap<>();
                for (BearAttachment bearAttachment : bearNote.getBearAttachments()) {
                    MemosAttachment memosAttachment = new MemosAttachment(bearFolder, bearAttachment, "memos/"+memoId);
                    HttpEntity<MemosAttachment> requestAttachment = new HttpEntity<>(memosAttachment, headers);
                    response = restTemplate.exchange(memosUrl + "/api/v1/attachments", HttpMethod.POST, requestAttachment, Response.class);
                    attachmentsToReplace.put(memosAttachment.getFilename(), "file/"+response.getBody().getName()+"/"+memosAttachment.getFilename());
                }
                for (Map.Entry<String, String> entrySet : attachmentsToReplace.entrySet()) {
                    memosNote.setContent(StringUtils.replaceChars(memosNote.getContent(),"("+entrySet.getKey()+")", "("+entrySet.getValue()+")"));
                }
                request= new HttpEntity<>(memosNote, headers);
                restTemplate.exchange(memosUrl + "/api/v1/memos/"+memoId, HttpMethod.PATCH, request, String.class, memoId);
                int j=0;
            }

            int i=0;
        }

    }
}
