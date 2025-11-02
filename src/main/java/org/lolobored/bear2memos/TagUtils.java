package org.lolobored.bear2memos;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class TagUtils {
    public static String cleanTag(String tag){
        tag= StringUtils.replace(tag, " ", "-");
        tag= StringUtils.replace(tag, ")", "");
        tag= StringUtils.replace(tag, ":", "");
        return tag;
    }

    public static String cleanTitle(String title){
        title= StringUtils.replace(title, "/", "-");
        return title;
    }

    public static Set<String> extractTags(String text){
        Set<String> tagList= new HashSet<>();

        // remove the code blocks as we will not take care of any tag in there
        text=text.replaceAll("```[^`]+```","");
        String[] lines = StringUtils.split(text, "\n");
        for (String line : lines) {
            // manage single tags starting the line
            line= " "+line;
            // manage also tag in a single line as header
            line= line + " ";
            // manage hashes in line code (removing the code in between)
            line= line.replaceAll("`[^`]+`","");
            String[] tags = StringUtils.split(line, "#");
            for (int i=1; i< tags.length; i++) {
                if (tags[i].startsWith(" ")) continue;
                // easy case
                if (tags[i].endsWith(" ") || i==tags.length-1){
                    tagList.add(cleanTag(StringUtils.substringBefore(tags[i], " ")));
                }
                else{
                    tagList.add(cleanTag(tags[i]));
                }
            }
        }
        return tagList;
    }

    public static String cleanText(String text){
        text= StringUtils.replaceChars(text, "’", "'");
        text= StringUtils.replaceChars(text, "—", "-");
        // remove first line
        text= StringUtils.substringAfter(text, "\n");
        return text;
    }

    public static String replaceImages(String text, Map<String, String> listOfFiles){
        while (text.contains("![](")){
           String before= StringUtils.substringBefore(text, "![](");
           String after= StringUtils.substringAfter(text, "![](");
           String imageName= StringUtils.substringBefore(after, ")");
           // replace space
           imageName=StringUtils.replace(imageName, "%20"," ");
           after= StringUtils.substringAfter(after, ")");

           text= before+"![["+listOfFiles.get(imageName)+"]]"+after;
       }
       return text;
    }

    public static String replacePdf(String text, Map<String, String> listOfFiles){
        StringBuilder appender= new StringBuilder();
        String after=text;
        while (text.contains(".pdf]")){
            String before= StringUtils.substringBefore(text, ".pdf]");
            after= StringUtils.substringAfter(text, ".pdf]");
            String pdfName= StringUtils.substringAfterLast(before, "[") + ".pdf";
            before= StringUtils.substringBeforeLast(before, "[");
            after= StringUtils.substringAfter(after, "-->");
            // replace space
            pdfName=StringUtils.replace(pdfName, "%20"," ");
            appender.append(before).append("![[files/").append(listOfFiles.get(pdfName)).append("]]");
            // replace text
            text= after;
        }
        appender.append(after);
        return appender.toString().trim();
    }

    public static void createChecksum(String fileUrl, String checksumUrl) throws IOException {
        FileUtils.writeStringToFile(new File(checksumUrl), getChecksum(fileUrl),Charset.defaultCharset());
    }

    public static String getChecksum(String fileUrl) throws IOException {
        Checksum crc32 = new CRC32();
        byte[] bytes = FileUtils.readFileToString(new File(fileUrl), Charset.defaultCharset()).getBytes();
        crc32.update(bytes, 0, bytes.length);
        return String.valueOf(crc32.getValue());
    }
}
