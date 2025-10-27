package org.lolobored.services;

import org.lolobored.dao.bear.BearNote;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Map;

public interface BearService {
    public Map<BigInteger, BearNote> exportBearNotes(String bearFolder) throws IOException;
}
