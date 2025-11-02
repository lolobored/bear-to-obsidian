package org.lolobored.dao.memos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.lolobored.dao.bear.BearAttachment;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MemosAttachment extends BearAttachment {
    private String memo;
}
