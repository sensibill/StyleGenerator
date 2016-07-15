package com.getsensibill.stylecreator;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Arrays;

public class Config {
    public static final java.lang.String BASE_THEME = "BASE_THEME";
    public static final java.lang.String WIDGET_THEME = "WIDGET_THEME";
    public static final java.lang.String BASE_CLASS_PREFIX = "BASE_CLASS_PREFIX";
    public static final java.lang.String WIDGET_CLASS_PREFIX = "WIDGET_CLASS_PREFIX";

    private static PropertiesConfiguration conf;

    public static PropertiesConfiguration getConf() {
        if (conf == null) {

            try {

                File file = Paths.get(System.getProperty("user.home"), ".config.properties").normalize().toFile();
                if (!file.exists()) {
                    file.createNewFile();
                    FileUtils.writeLines(file, Arrays.asList(new String[]{
                            BASE_THEME + " = " + "<absolute path of base theme here>",
                            WIDGET_THEME + " = " + "<absolute path of widget theme here>",
                            "BASE_CLASS_PREFIX=Base.Widget.Sensibill.",
                            "WIDGET_CLASS_PREFIX=Widget.Sensibill."}));
                }
                conf = new PropertiesConfiguration(file);
                return conf;
            } catch (ConfigurationException e) {
                throw new RuntimeException("Configuration file couldn't be retrieved.");
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("error in creating configuration file.");

            }
        } else {
            return conf;
        }
    }


}