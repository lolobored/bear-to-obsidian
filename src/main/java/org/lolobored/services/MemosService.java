package org.lolobored.services;

import org.lolobored.dao.bear.BearNote;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Map;

public interface MemosService {
    public void importNotes(String memosUrl, String memosToken, Map<BigInteger, BearNote> notesById, String bearFolder) throws IOException;
}
