package net.justonedev.kit;

import net.justonedev.kit.json.JSONHandler;

import java.io.File;

public class TexCompiler {

    public static void compileToTex(File monthFile, File texFile) {

        File globalFile = JSONHandler.getConfigFile();

        main.Main.main(new String[]{"-f", monthFile.getAbsolutePath(), globalFile.getAbsolutePath(), texFile.getAbsolutePath()});
    }

}
