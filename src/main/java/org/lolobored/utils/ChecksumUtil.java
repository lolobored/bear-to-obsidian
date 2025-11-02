package org.lolobored.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.codec.digest.DigestUtils;
import org.lolobored.dao.bear.BearNote;
import org.lolobored.dao.memos.MemosNote;

public class ChecksumUtil {

    public static String bearChecksum(BearNote bearNote) throws JsonProcessingException {
        ObjectMapper objMapper = new ObjectMapper();
        objMapper.registerModule(new JavaTimeModule());
        return DigestUtils.sha512Hex(objMapper.writeValueAsString(bearNote));
    }

    public static String memosChecksum(MemosNote memosNote) throws JsonProcessingException {
        ObjectMapper objMapper = new ObjectMapper();
        objMapper.registerModule(new JavaTimeModule());
        return DigestUtils.sha512Hex(objMapper.writeValueAsString(memosNote));
    }

}
