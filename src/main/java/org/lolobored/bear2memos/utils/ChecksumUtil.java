package org.lolobored.bear2memos.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.codec.digest.DigestUtils;
import org.lolobored.bear2memos.dao.bear.BearNote;
import org.lolobored.bear2memos.dao.memos.MemosAttachment;
import org.lolobored.bear2memos.dao.memos.MemosNote;

public class ChecksumUtil {

    public static String bearChecksum(BearNote bearNote) throws JsonProcessingException {
        ObjectMapper objMapper = new ObjectMapper();
        objMapper.registerModule(new JavaTimeModule());
        return DigestUtils.sha512Hex(objMapper.writeValueAsString(bearNote));
    }

    public static String memosChecksum(MemosNote memosNote) throws JsonProcessingException {
        ObjectMapper objMapper = new ObjectMapper();
        objMapper.registerModule(new JavaTimeModule());
        // remove content
        for (MemosAttachment attachment : memosNote.getAttachments()) {
            attachment.setContent(null);
        }
        return DigestUtils.sha512Hex(objMapper.writeValueAsString(memosNote));
    }

}
