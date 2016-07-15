package com.getsensibill.stylecreator;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by dipendra on 2016-06-27.
 */
public class StyleGenerator {
    String[] stayArray = new String[]{
            "android:id", "android:layout_width", "android:layout_height", "android:orientation",
            "android:tag", "android:clickable", "android:weightSum", "android:layout_weight",
            "android:onClick", "android:class", "xmlns:android"
    };
    String[] layoutExceptionsArray = new String[]{"android:layout_margin",
            "android:layout_marginLeft", "android:layout_marginRight",
            "android:layout_marginTop", "android:layout_marginBottom"};

    public List<String> getCodeBlocks(String inputText, String styleClassName) {
        List<String> codeBlockList = new ArrayList<>();

        List<String> layoutExceptionList = Arrays.asList(layoutExceptionsArray);
        List<String> ignoreAttributeList = Arrays.asList(stayArray);


        List<String> rawLines = Arrays.asList(inputText.split("\n"));


        List<String> stayList = new ArrayList<>();
        List<String> migrateList = new ArrayList<>();
        String endingChars = assignToAppropriateList(ignoreAttributeList, layoutExceptionList,
                rawLines, stayList, migrateList);

        addStyleAndEndingCharToStaylist(styleClassName, stayList, endingChars);

        return outputResults(styleClassName, stayList, migrateList);

    }

    private List<String> outputResults(String styleName, List<String> stayList, List<String> migrateList) {
        List<String> codeBlocks = new ArrayList<>();

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < stayList.size(); i++) {
            System.out.println(stayList.get(i));
            builder.append(stayList.get(i));
            builder.append("\n");
        }
        codeBlocks.add(builder.toString());


        addStyleToMigrateList(styleName, migrateList);
        builder = new StringBuilder();
        for (int i = 0; i < migrateList.size(); i++) {
            builder.append(migrateList.get(i));
            builder.append("\n");
        }
        codeBlocks.add(builder.toString());


        String baseStyle = "  <style name=\"" + Config.getConf().getString(Config.WIDGET_CLASS_PREFIX) + styleName + "\" parent=\"" + Config.getConf().getString(Config.BASE_CLASS_PREFIX) + styleName + "\"/>\n";
        codeBlocks.add(baseStyle);
        return codeBlocks;
    }

    private static void addStyleToMigrateList(String styleName, List<String> migrateList) {
        migrateList.add(0, "<style name=\"" + Config.getConf().getString(Config.BASE_CLASS_PREFIX) + styleName + "\" parent=\"\">");
        migrateList.add("</style>");
    }

    private static void addStyleAndEndingCharToStaylist(String styleName, List<String> stayList, String endingChars) {
        stayList.add("style=" + "\"@style/" + Config.getConf().getString(Config.WIDGET_CLASS_PREFIX) + styleName + "\"");

        if (endingChars != null) {
            String lastLine = stayList.get(stayList.size() - 1);
            stayList.remove(stayList.size() - 1);
            lastLine = lastLine + endingChars;
            stayList.add(lastLine);
        }
    }

    private static String assignToAppropriateList(List<String> ignoreAttributeList, List<String> layoutExceptions,
                                                  List<String> rawLines, List<String> stayList,
                                                  List<String> migrateList) {

        String endingChars = null;

        for (int i = 0; i < rawLines.size(); i++) {

            String line = rawLines.get(i).trim();
            if (line.length() == 0) {
                continue;
            }


            if (line.startsWith("<")) {
                stayList.add(line);
                continue;
            }

            if (line.endsWith("/>")) {
                endingChars = "/>";
                line = line.replace("/>", StringUtils.EMPTY);

            } else if (line.endsWith(">")) {
                endingChars = ">";
                line = line.replace(">", StringUtils.EMPTY);
            }

            String[] split = line.split("=");
            String key = split[0].trim();

            if (ignoreAttributeList.contains(key)) {
                stayList.add(line);
                continue;
            }
            if (!layoutExceptions.contains(key) && key.startsWith("android:layout_")) {
                stayList.add(line);
                continue;
            }
            if (key.startsWith("tools:")) {
                stayList.add(line);
                continue;
            }

            String requiredTextFormat = "<item name=\"$$1$$\">$$2$$</item>";
            requiredTextFormat = requiredTextFormat.replace("$$1$$", key.replace("\"", StringUtils.EMPTY));

            String value = split[1].trim().replace("\"", StringUtils.EMPTY);
            requiredTextFormat = requiredTextFormat.replace("$$2$$", value.replace("\"", StringUtils.EMPTY));
            migrateList.add(requiredTextFormat);
        }
        return endingChars;
    }


}

