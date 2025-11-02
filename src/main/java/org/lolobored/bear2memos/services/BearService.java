package org.lolobored.bear2memos.services;

import org.lolobored.bear2memos.dao.bear.BearNote;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Map;


public interface BearService {
    Map<BigInteger, BearNote> retrieveBearNotes(String bearFolder) throws IOException;
}
