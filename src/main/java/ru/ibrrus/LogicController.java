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
    private static String ITEM_FILE_NAME = "Items_";
    private static String RECIPE_FILE_NAME = "Recipes_";
    static String CHARSET;
    static String languageCode;
    static Alert alert;

    protected static void showAlert(AlertType alertType, String message) throws IOException{
        alert = new Alert(alertType);
        ((Stage)alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image(App.class.getResource("img/logo.png").toString()));
        alert.setTitle(null);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        throw new IOException(message);
    }


    protected static void generate(List<File> scriptsList, Boolean itemsCheck, Boolean recipesCheck,
                                   List<String> itemsOldStr, List<String> recipesOldStr, String languageCode)
            throws IOException {
        if (scriptsList.isEmpty()) {
            showAlert(AlertType.ERROR, "Script list is empty.");
        }
        TreeSet<String> items_elements = new TreeSet<String>();
        TreeSet<String> recipes_elements = new TreeSet<String>();
        TreeMap<String, String> old_translate = new TreeMap<>();
        LogicController.languageCode = languageCode;
        switch (languageCode) {
            case "RU":            case "PT":            case "PTBR":            case "NL":
            case "HU":            case "PL":            case "ES":              case "EE":
            case "DE":            case "FR":            case "CS":              case "TR":
            case "IT":            case "CH":            case "DA":              case "AR":
            case "NO":
                CHARSET = "windows-1251";
                break;
            case "KO":
                CHARSET = "UTF-16";
                break;
            case "EN":            case "TH":            case "CN":
                CHARSET = "UTF-8";
            case "JP": // TODO Don't work.
            default:
                CHARSET = "windows-1251";
                break;
        }

        if (itemsCheck) {
            getElements("Items", items_elements, scriptsList);
            if (!itemsOldStr.isEmpty()) {
                takeOldTranslate(old_translate, itemsOldStr, ITEM_SEARCH_NAME);
            }
            writeFiles(ITEM_FILE_NAME + languageCode, ITEM_SEARCH_NAME, items_elements, !itemsOldStr.isEmpty(),
                    old_translate);
        }

        if (recipesCheck) {
            getElements("Recipes", recipes_elements, scriptsList);
            if (!recipesOldStr.isEmpty()) {
                takeOldTranslate(old_translate, recipesOldStr, RECIPE_SEARCH_NAME);
            }
            writeFiles(RECIPE_FILE_NAME + languageCode, RECIPE_SEARCH_NAME, recipes_elements, !recipesOldStr.isEmpty(), old_translate);
        }
        showAlert(AlertType.INFORMATION,"Done!\nItems: " + items_elements.size() + ". " +
                                                "Recipes: " + recipes_elements.size() + ".\n" +
                                                "Save in: " + System.getProperty("user.dir") );
    }

    private static void getElements (String typeElement,
                          TreeSet elements,
                          List<File> scriptsList) throws IOException{
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
                        showAlert(AlertType.ERROR, "Something goes wrong with open script files (Items)!");
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
                        showAlert(AlertType.ERROR, "Something goes wrong with open script files (Recipes)!");
                    }
                }
                break;
        }
    }

    private static void takeOldTranslate(TreeMap old_translate, List<String> оldFiles, String SEARCH_NAME)
                                        throws IOException{
        old_translate.clear();
        for (String oldFile : оldFiles) {
            try (Stream<String> fileStream = Files.lines(Paths.get(oldFile), Charset.forName(CHARSET))) {
                fileStream.filter(s -> s.trim().startsWith(SEARCH_NAME)).forEach(s -> {
                    try{
                        s = s.trim();
                        String key = s.substring(SEARCH_NAME.length(), s.indexOf('=') - 1).trim().toLowerCase();
                        if (key.startsWith("_")) key = key.substring(1);
                        String value = s.substring(s.indexOf('=') + 1, s.lastIndexOf(',')).trim();
                        old_translate.put(key, value);
                    } catch (Exception mie){
                    }
                });
            } catch (Exception ea) {
                showAlert(AlertType.ERROR, "Something goes wrong with old translations! " +
                        "                           Did you enter the correct language code?");
            }
        }
    }

    private static void writeFiles (String FILE_NAME, String SEARCH_NAME, TreeSet <String> elements,
                                    Boolean itemOldCheck, TreeMap<String, String> old_translate) throws IOException{
        TreeSet <String> notTranslation = new TreeSet <String>();
        try (FileWriter new_translate
                     = new FileWriter("new_" + FILE_NAME + ".txt", Charset.forName(CHARSET))){
            if (FILE_NAME.startsWith("Item")) {
                new_translate.write("Items_" + languageCode + " = {\n");
            } else {
                new_translate.write("Recipe_" + languageCode + " = {\n");
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
            showAlert(AlertType.ERROR, "Something goes wrong with write new files!");
        }
    }
}