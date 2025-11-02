package org.lolobored.bear2memos.dao.memos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

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

