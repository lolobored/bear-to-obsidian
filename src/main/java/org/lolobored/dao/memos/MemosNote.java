package org.lolobored.dao.memos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MemosNote {
    private String name;
    private String content;
    private ZonedDateTime createTime;
    private ZonedDateTime updateTime;
    private List<MemosAttachment> attachments= new ArrayList<>();

    public void addAttachment(MemosAttachment memosAttachment){
        attachments.add(memosAttachment);
    }
}

