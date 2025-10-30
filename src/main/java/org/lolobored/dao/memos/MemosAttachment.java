package org.lolobored.dao.memos;

import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Strings;
import org.lolobored.dao.bear.BearAttachment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

@Data
public class MemosAttachment {
    private String name;
    private String filename;
    private String type;
    private byte[] content;
    private String memo;
}
