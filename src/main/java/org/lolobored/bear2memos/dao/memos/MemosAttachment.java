package org.lolobored.bear2memos.dao.memos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.lolobored.bear2memos.dao.bear.BearAttachment;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MemosAttachment extends BearAttachment {
    private String memo;
}
