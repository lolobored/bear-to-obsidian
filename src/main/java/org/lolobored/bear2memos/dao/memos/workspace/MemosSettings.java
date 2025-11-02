package org.lolobored.bear2memos.dao.memos.workspace;

import lombok.Data;

import java.math.BigInteger;

@Data
public class MemosSettings {
    private String name;
    private MemoRelatedSetting memoRelatedSetting= new MemoRelatedSetting();

    @Data
    private class MemoRelatedSetting {
        private BigInteger contentLengthLimit= new BigInteger("2147483647");
        private boolean enableDoubleClickEdit= true;
    }
}
