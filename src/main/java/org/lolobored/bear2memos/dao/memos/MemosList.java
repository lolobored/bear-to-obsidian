package org.lolobored.bear2memos.dao.memos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MemosList {
    private List<MemosNote> memos;
    private String nextPageToken;
}
