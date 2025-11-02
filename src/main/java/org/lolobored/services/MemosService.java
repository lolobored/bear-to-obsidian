package org.lolobored.services;

import org.lolobored.dao.bear.BearNote;
import org.lolobored.dao.memos.MemosNote;

import java.io.IOException;

public interface MemosService {
    //void importNotes(String memosUrl, String memosToken, Map<BigInteger, BearNote> notesById, String bearFolder) throws IOException;
    MemosNote createNote(String memosUrl, String memosToken, BearNote bearNote) throws IOException;
    MemosNote updateNote(String memosUrl, String memosToken, String memoId, BearNote bearNote) throws IOException;
    void deleteNote(String memosUrl, String memosToken, String memoId) throws IOException;
    void setMemosSettings(String memosUrl, String memosToken);
}
