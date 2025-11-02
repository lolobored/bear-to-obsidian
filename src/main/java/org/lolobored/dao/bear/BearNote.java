package org.lolobored.dao.bear;

import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class BearNote implements Comparable<BearNote> {
    private BigInteger id;
    private String title;
    private String text;
    private ZonedDateTime creationDate;
    private ZonedDateTime updateDate;
    private List<BearAttachment> bearAttachments = new ArrayList<>();
    private boolean deleted;

    public void addAttachment(BearAttachment bearAttachment){
        bearAttachments.add(bearAttachment);
    }

    @Override
    public int compareTo(BearNote toCompare) {
        return this.getId().compareTo(toCompare.getId());
    }
}
