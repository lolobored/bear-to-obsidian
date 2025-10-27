package org.lolobored.dao.memos;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class MemosNote {
    private String content;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime updateTime;
    private List<MemosAttachment> attachments= new ArrayList<>();

    public void addAttachment(MemosAttachment memosAttachment){
        attachments.add(memosAttachment);
    }
}

