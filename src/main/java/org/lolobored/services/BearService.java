package org.lolobored.services;

import org.lolobored.dao.bear.BearNote;

import java.io.IOException;
import java.util.List;


public interface BearService {
    List<BearNote> retrieveBearNotes(String bearFolder) throws IOException;
}
