package org.lolobored.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.lolobored.dao.bear.BearAttachment;
import org.lolobored.dao.bear.BearAttachmentSQL;
import org.lolobored.dao.bear.BearNote;
import org.lolobored.dao.memos.MemosAttachment;
import org.lolobored.dao.memos.MemosNote;
import org.lolobored.dao.memos.workspace.MemosSettings;
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
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MemosServiceImpl implements MemosService {

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public MemosNote createNote(String memosUrl, String memosToken, BearNote bearNote) throws IOException {
        MemosNote memosNote = createOrUpdateMemosNote(memosUrl, memosToken, bearNote, null);
        return memosNote;
    }



    @Override
    public MemosNote updateNote(String memosUrl, String memosToken, String memosId, BearNote bearNote) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(memosToken);

        HttpEntity request= new HttpEntity<>(headers);
        // get the initial note
        MemosNote currentMemosNote = restTemplate.exchange(memosUrl + "/api/v1/memos/" + memosId, HttpMethod.GET, request, MemosNote.class).getBody();

        // delete all attachments
        for (MemosAttachment attachment : currentMemosNote.getAttachments()) {
            restTemplate.exchange(memosUrl + "/api/v1/"+attachment.getName(), HttpMethod.DELETE, request, String.class);
        }
        MemosNote memosNote = createOrUpdateMemosNote(memosUrl, memosToken, bearNote, memosId);
        return memosNote;
    }

    @Override
    public void deleteNote(String memosUrl, String memosToken, String memosId) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(memosToken);

        HttpEntity request= new HttpEntity<>(headers);
        // get the initial note
        MemosNote currentMemosNote = restTemplate.exchange(memosUrl + "/api/v1/memos/" + memosId, HttpMethod.GET, request, MemosNote.class).getBody();

        // delete all attachments
        for (MemosAttachment attachment : currentMemosNote.getAttachments()) {
            restTemplate.exchange(memosUrl + "/api/v1/"+attachment.getName(), HttpMethod.DELETE, request, String.class);
        }
        // delete note
        restTemplate.exchange(memosUrl + "/api/v1/memos/" + memosId, HttpMethod.DELETE, request, String.class);
    }

    @Override
    public void setMemosSettings(String memosUrl, String memosToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(memosToken);

        MemosSettings settings = new MemosSettings();
        settings.setName("workspace/settings/MEMO_RELATED");
        HttpEntity<MemosSettings> request= new HttpEntity<>(settings, headers);

        ResponseEntity<String> response = restTemplate.exchange(memosUrl + "/api/v1/workspace/settings/MEMO_RELATED", HttpMethod.PATCH, request, String.class, "workspace/settings/MEMO_RELATED");
    }


    private MemosNote createOrUpdateMemosNote(String memosUrl, String memosToken, BearNote bearNote, String memosId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(memosToken);

        MemosNote memosNote= new MemosNote();
        memosNote.setContent(bearNote.getText());
        memosNote.setCreateTime(bearNote.getCreationDate());
        memosNote.setUpdateTime(bearNote.getUpdateDate());

        HttpEntity<MemosNote> request= new HttpEntity<>(memosNote, headers);
        ResponseEntity<Response> response;
        // if memosId is null then we need to create it first
        if (memosId == null) {
            response = restTemplate.exchange(memosUrl + "/api/v1/memos", HttpMethod.POST, request, Response.class);
            memosId = StringUtils.remove(response.getBody().getName(), "memos/");
        }
        memosNote.setName(memosId);
        List<MemosAttachment> memosAttachments= new ArrayList<>();
        if (!bearNote.getBearAttachments().isEmpty()) {
            Map<String,String> attachmentsToReplace= new HashMap<>();
            for (BearAttachment bearAttachment : bearNote.getBearAttachments()) {
                MemosAttachment memosAttachment= new MemosAttachment();
                memosAttachment.setContent(bearAttachment.getContent());
                memosAttachment.setFilename(bearAttachment.getFilename());
                memosAttachment.setType(bearAttachment.getType());
                memosAttachment.setName(bearAttachment.getName());
                memosAttachment.setMemo("memos/" + memosId);
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

        }
        request= new HttpEntity<>(memosNote, headers);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        restTemplate.exchange(memosUrl + "/api/v1/memos/"+memosId, HttpMethod.PATCH, request, String.class, memosId);
        return memosNote;
    }


}
