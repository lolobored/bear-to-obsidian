package org.lolobored.dao;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Note {
    private String title;
    private String text;
    private Map<String, String> listOfFiles= new HashMap<>();
}
