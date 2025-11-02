package org.lolobored.bear2memos.dao.bear;

import lombok.Data;

@Data
public class BearAttachment {
    private String name;
    private String filename;
    private String type;
    private byte[] content;
}
