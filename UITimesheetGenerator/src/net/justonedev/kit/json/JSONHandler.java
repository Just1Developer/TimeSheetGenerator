package net.justonedev.kit.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class JSONHandler {

    public static Global globalSettings;

    private static String configDir;
    private static final String configFile = "global.json";


    public static void initialize() {
        String OS = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
        if (OS.contains("win")) {
            configDir = System.getenv("APPDATA");
        } else if (OS.contains("mac")) {
            configDir = System.getProperty("user.home") + "/Library/Application Support";
        } else if (OS.contains("nux") || OS.contains("nix")) {
            configDir = System.getProperty("user.home") + "/.config";
        } else {
            // Default to user home directory
            configDir = System.getProperty("user.home");
        }

        // Create a subdirectory for your application
        configDir += "/TimeSheetGenerator";

        createDefaultGlobalSettings();
        loadGlobal();
    }

    public static void loadGlobal() {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            /*
            // Read month.json
            Month month = objectMapper.readValue(new File("month.json"), Month.class);
            System.out.println("Year: " + month.getYear() + ", Month: " + month.getMonth());

            // Access entries
            for (Month.Entry entry : month.getEntries()) {
                System.out.println("Action: " + entry.getAction() + ", Day: " + entry.getDay());
            }
            */

            // Read global.json
            globalSettings = objectMapper.readValue(getConfigFile(), Global.class);
            System.out.println("Loaded Global Settings.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveGlobal(Global globalSettings) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            objectMapper.writeValue(getConfigFile(), globalSettings);
            System.out.println("Saved global settings.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveMonth(Global globalSettings) {

    }

    private static File getConfigFile() {
        return new File(configDir, configFile);
    }

    private static boolean globalConfigExists() {
        return getConfigFile().exists();
    }

    public static void createDefaultGlobalSettings() {
        if (globalConfigExists()) return;
        try {
            File f = getConfigFile();
            new File(configDir).mkdirs();
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Global global = new Global();
        global.setName("Max Mustermann");
        global.setStaffId(1234567);
        global.setDepartment("Fakultät für Informatik");
        global.setWorkingTime("40:00");
        global.setWage(13.25);
        global.setWorkingArea("ub");
        saveGlobal(global);
        System.out.println("Created Default Global Settings.");
    }

}