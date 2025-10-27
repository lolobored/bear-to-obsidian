package org.lolobored.dao.bear;

import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class BearNote {
    private BigInteger id;
    private String title;
    private String text;
    private LocalDateTime creationDate;
    private LocalDateTime updateDate;
    private List<BearAttachment> bearAttachments = new ArrayList<>();

    public void addAttachment(BearAttachment bearAttachment){
        bearAttachments.add(bearAttachment);
    }
}
