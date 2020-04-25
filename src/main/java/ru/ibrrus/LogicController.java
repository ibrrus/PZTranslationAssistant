package ru.ibrrus;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Stream;


public class LogicController {

    private static String ITEM_SEARCH_NAME = "DisplayName";
    private static String RECIPE_SEARCH_NAME = "Recipe";
    private static String ITEM_FILE_NAME = "Items_XX";
    private static String RECIPE_FILE_NAME = "Recipes_XX";

    protected static void generate(List<File> scriptsList, Boolean itemsCheck, Boolean recipesCheck, List<String> itemsOldStr, List<String> recipesOldStr) throws IOException {
        Alert alert;
        if (scriptsList.isEmpty()) {
            alert = new Alert(AlertType.ERROR);
            alert.setTitle(null);
            alert.setContentText("Script list is empty.");
            alert.setHeaderText(null);
            ((Stage)alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image(App.class.getResource("img/logo.png").toString()));
            alert.showAndWait();
            throw new IOException("Script list is empty.");
        }
        TreeSet<String> items_elements = new TreeSet<String>();
        TreeSet<String> recipes_elements = new TreeSet<String>();
        TreeMap<String, String> old_translate = new TreeMap<>();

        if (itemsCheck) {
            getElements("Items", items_elements, scriptsList);
            if (!itemsOldStr.isEmpty()) {
                takeOldTranslate(old_translate, itemsOldStr, ITEM_SEARCH_NAME, "windows-1251");
            }
            writeFiles(ITEM_FILE_NAME, ITEM_SEARCH_NAME, items_elements, !itemsOldStr.isEmpty(), old_translate);
        }

        if (recipesCheck) {
            getElements("Recipes", recipes_elements, scriptsList);
            if (!recipesOldStr.isEmpty()) {
                takeOldTranslate(old_translate, recipesOldStr, RECIPE_SEARCH_NAME, "windows-1251");
            }
            writeFiles(RECIPE_FILE_NAME, RECIPE_SEARCH_NAME, recipes_elements, !recipesOldStr.isEmpty(), old_translate);
        }
        alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(null);
        alert.setContentText("Done!\nItems: " + items_elements.size() + ". Recipes: " + recipes_elements.size() + ".\nSave in: " + System.getProperty("user.dir"));
        alert.setHeaderText(null);
        ((Stage)alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image(App.class.getResource("img/logo.png").toString()));
        alert.showAndWait();
    }

    private static void getElements (String typeElement,
                          TreeSet elements,
                          List<File> scriptsList) {
        switch (typeElement){
            case "Items":
                for (File oneFile : scriptsList) {
                    try (Stream<String> fileStream = Files.lines(oneFile.toPath(), StandardCharsets.UTF_8)) {
                        fileStream.filter(s -> s.trim().startsWith(ITEM_SEARCH_NAME)).forEach(s -> {
                            s = s.substring(s.lastIndexOf('=') + 1, s.lastIndexOf(',')).trim()
                                    .replace(" ", "_")
                                    .replace("-", "_");
                            elements.add(s);
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case "Recipes":
                for (File oneFile : scriptsList) {
                    try (Stream<String> fileStream = Files.lines(oneFile.toPath(), StandardCharsets.UTF_8)) {
                        fileStream.filter(s -> s.trim().startsWith(RECIPE_SEARCH_NAME.toLowerCase())).forEach(s -> {
                            s = s.substring(s.indexOf(RECIPE_SEARCH_NAME.toLowerCase()) + RECIPE_SEARCH_NAME.length()).trim()
                                    .replace(" ", "_");
                            elements.add(s);
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    private static void takeOldTranslate(TreeMap old_translate, List<String> оldFiles, String SEARCH_NAME,
                                         String CHARSET) {
        old_translate.clear();
        for (String oldFile : оldFiles) {
            System.out.println(oldFile.toString());
            try (Stream<String> fileStream = Files.lines(Paths.get(oldFile), Charset.forName(CHARSET))) {
                fileStream.filter(s -> s.trim().startsWith(SEARCH_NAME)).forEach(s -> {
                    try{
                        String key = s.substring(s.indexOf('_') + 1, s.indexOf('=') - 1).trim().toLowerCase();
                        String value = s.substring(s.indexOf('=') + 1, s.lastIndexOf(',')).trim();
                        old_translate.put(key, value);
                    } catch (Exception mie){
                    }
                });
            } catch (Exception e) {
                try (Stream<String> fileStream = Files.lines(Paths.get(oldFile))) {
                    fileStream.filter(s -> s.trim().startsWith(SEARCH_NAME)).forEach(s -> {
                        try{
                            String key = s.substring(s.indexOf('_') + 1, s.indexOf('=') - 1).trim().toLowerCase();
                            String value = s.substring(s.indexOf('=') + 1, s.lastIndexOf(',')).trim();
                            old_translate.put(key, value);
                        } catch (Exception mie){
                        }
                    });
                } catch (IOException ea) {
                    System.out.println("ERROR");
                    //ea.printStackTrace();
                }
            }
        }
    }

    private static void writeFiles (String FILE_NAME, String SEARCH_NAME, TreeSet <String> elements, Boolean itemOldCheck, TreeMap<String, String> old_translate) {
        TreeSet <String> notTranslation = new TreeSet <String>();
        try (FileWriter new_translate
                     = new FileWriter("new_" + FILE_NAME + ".txt", Charset.forName("windows-1251"))){
            if (FILE_NAME.startsWith("Item")) {
                new_translate.write("Items_XX = {\n");
            } else {
                new_translate.write("Recipe_XX = {\n");
            }
            new_translate.write("\t/* Created by PZTranslationAssistant */\n");
            new_translate.write("\t/* Datetime: " + new Date().toString() + " */\n\n");
            for (String item : elements) {
                if (itemOldCheck) {
                    String item_old = old_translate.get(item.toLowerCase());
                    if (item_old == null) {
                        notTranslation.add("\t" + SEARCH_NAME + "_" + item + " = \""+ item.replace("_", " ") + "\",\n");
                    } else {
                        new_translate.write("\t" + SEARCH_NAME + "_" + item + " = " + item_old + ",\n");
                    }
                } else {
                    new_translate.write("\t" + SEARCH_NAME + "_" + item + " = \""+ item.replace("_", " ") + "\",\n");
                }
            }
            if (itemOldCheck) {
                new_translate.write("\n\n\t/* NEED TO TRANSLATE: */\n\n\n");
                for (String str : notTranslation){
                    new_translate.write(str);
                }
            }
            new_translate.write("}");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}