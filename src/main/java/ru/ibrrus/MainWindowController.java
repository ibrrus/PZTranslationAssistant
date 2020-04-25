package ru.ibrrus;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;

public class MainWindowController {

    public static String defaultDirectoryStr;

    @FXML
    private ListView textArea;
    @FXML
    private Button addFilesBtn;
    @FXML
    private Button addFolderBtn;
    @FXML
    private Button deleteBtn;

    @FXML
    private void addFilesCmd() throws IOException {
        File defaultDirectory = new File(defaultDirectoryStr);
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select files ...");
        fileChooser.setInitialDirectory(defaultDirectory);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        try {
            List <File> files = fileChooser.showOpenMultipleDialog(App.guiStage);
            textArea.getItems().addAll(FXCollections.observableList(files));
            defaultDirectoryStr = files.get(0).getParentFile().toString();
        } catch (RuntimeException re){
        }
    }

    @FXML
    private void addFolderCmd() throws IOException {
        File defaultDirectory = new File(defaultDirectoryStr);
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select folder ...");
        directoryChooser.setInitialDirectory(defaultDirectory);
        try {
            File directory = directoryChooser.showDialog(App.guiStage);
            textArea.getItems().addAll(directory.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".txt");
                }
            }));
            defaultDirectoryStr = directory.getParentFile().toString();
        } catch (RuntimeException re){
        }
    }

    @FXML
    private void clearTextArea() throws IOException {
        textArea.getItems().clear();
    }

    @FXML
    private CheckBox itemsCb;
    @FXML
    private Label itemsOldLabel;
    @FXML
    private ListView itemsOldPath;
    @FXML
    private Button itemsOldBrowse;

    @FXML
    private void itemsCbClick() throws IOException {
        if (itemsCb.isSelected()) {
            itemsOldLabel.setDisable(false);
            itemsOldPath.setDisable(false);
            itemsOldBrowse.setDisable(false);
        } else {
            itemsOldLabel.setDisable(true);
            itemsOldPath.setDisable(true);
            itemsOldBrowse.setDisable(true);
        }
    }
    @FXML
    private Button itemsOldClear;
    @FXML
    private void itemsOldBrowseCmd() throws IOException {
        File defaultDirectory = new File(defaultDirectoryStr);
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(defaultDirectory);
        fileChooser.setTitle("Select file ...");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        try {
            File t = fileChooser.showOpenDialog(App.guiStage);
            itemsOldPath.getItems().add(t.getPath());
            itemsOldClear.setDisable(false);
            defaultDirectoryStr = t.getParentFile().toString();
        } catch (RuntimeException re){
        }
    }

    @FXML
    private void itemsOldClearCmd() throws IOException{
        itemsOldPath.getItems().clear();
        itemsOldClear.setDisable(true);
    }

    @FXML
    private CheckBox recipesCb;
    @FXML
    private Label recipesOldLabel;
    @FXML
    private ListView recipesOldPath;
    @FXML
    private Button recipesOldBrowse;

    @FXML
    private void recipesCbClick() throws IOException {
        if (recipesCb.isSelected()) {
            recipesOldLabel.setDisable(false);
            recipesOldPath.setDisable(false);
            recipesOldBrowse.setDisable(false);
        } else {
            recipesOldLabel.setDisable(true);
            recipesOldPath.setDisable(true);
            recipesOldBrowse.setDisable(true);
        }
    }
    @FXML
    private Button recipesOldClear;
    @FXML
    private void recipesOldBrowseCmd() throws IOException {
        File defaultDirectory = new File(defaultDirectoryStr);
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(defaultDirectory);
        fileChooser.setTitle("Select file ...");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        try {
            File t = fileChooser.showOpenDialog(App.guiStage);
            recipesOldPath.getItems().add(t.getPath());
            defaultDirectoryStr = t.getParentFile().toString();
            recipesOldClear.setDisable(false);
        } catch (RuntimeException re){
        }
    }

    @FXML
    private void recipesOldClearCmd() throws IOException{
        recipesOldPath.getItems().clear();
        recipesOldClear.setDisable(true);
    }

    @FXML
    private Button generateBtn;
    @FXML
    private void generateCheck() throws IOException {
        if (itemsCb.isSelected() || recipesCb.isSelected()) {
            generateBtn.setDisable(false);
        } else {
            generateBtn.setDisable(true);
        }
    }

    @FXML
    private TextField languageCodeTF;

    @FXML
    private void textFieldTyped(){
        languageCodeTF.setTextFormatter(new TextFormatter<>((change) -> {
            change.setText(change.getText().toUpperCase());
            return change;
        }));
    }

    @FXML
    private void generateBtnCmd() throws IOException {
        String languageCode = languageCodeTF.getText();
        if (languageCode.trim().isEmpty()) languageCode = "XX";
        List <File> scriptsList = textArea.getItems();
        Boolean itemsCheck = itemsCb.isSelected();
        Boolean recipesCheck = recipesCb.isSelected();
        List <String> itemsOldStr = itemsOldPath.getItems();
        List <String> recipesOldStr = recipesOldPath.getItems();
        try{
            LogicController.generate(scriptsList, itemsCheck, recipesCheck,itemsOldStr, recipesOldStr, languageCode);
        } catch (IOException ex){
        }
    }
}