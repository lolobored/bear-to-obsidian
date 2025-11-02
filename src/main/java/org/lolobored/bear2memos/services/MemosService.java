package org.lolobored.bear2memos.services;

import org.lolobored.bear2memos.dao.bear.BearNote;
import org.lolobored.bear2memos.dao.memos.MemosNote;

import java.io.IOException;
import java.util.List;

public interface MemosService {
    //void importNotes(String memosUrl, String memosToken, Map<BigInteger, BearNote> notesById, String bearFolder) throws IOException;
    MemosNote createNote(String memosUrl, String memosToken, BearNote bearNote) throws IOException;
    MemosNote updateNote(String memosUrl, String memosToken, String memoId, BearNote bearNote) throws IOException;
    void deleteNote(String memosUrl, String memosToken, String memoId) throws IOException;
    void setMemosSettings(String memosUrl, String memosToken);
    List<MemosNote> listMemoNotes(String memosUrl, String memosToken);
}
