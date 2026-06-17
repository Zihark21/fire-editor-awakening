package com.danius.fireeditor.controllers;

import com.danius.fireeditor.FireEditor;
import com.danius.fireeditor.savefile.Chapter13;
import com.danius.fireeditor.savefile.Constants;
import com.danius.fireeditor.savefile.SaveFile;
import com.danius.fireeditor.savefile.global.Global;
import com.danius.fireeditor.savefile.units.Unit;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.stage.FileChooser;

import java.io.*;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainController {

    public static String path = System.getProperty("user.dir");
    public static File backupFile;

    @FXML
    private Tab tabConvoy, tabChapter, tabUnit, tabCheats, tabGlobal;
    @FXML
    private TabPane tabPane;

    public void initialize() {
        FireEditor.mainController = this;
        FireEditor.unitController.disableElements(true);
        FireEditor.unitController.comboUnitGroup.setDisable(true);
        tabConvoy.setDisable(true);
        tabChapter.setDisable(true);
        tabCheats.setDisable(true);
        tabGlobal.setDisable(true);

        if (FireEditor.chapterFile != null) {
            FireEditor.unitController.disableElements(false);
            FireEditor.unitController.comboUnitGroup.setDisable(false);

            tabConvoy.setDisable(false);
            tabChapter.setDisable(false);
            tabCheats.setDisable(false);
        } else if (FireEditor.global != null) {
            tabGlobal.setDisable(false);
            tabPane.getSelectionModel().select(tabGlobal);
            tabUnit.setDisable(true);
        }
    }

    public void openFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(path));

        File file = fileChooser.showOpenDialog(null);
        if (file == null) return;

        loadFile(file);
    }

    @FXML
    public void toggleDarkMode() {
        ThemeManager.getInstance().toggleDarkMode();
    }

    @FXML
    private void handleDragDropped(DragEvent event) {
        if (event.getDragboard().hasFiles()) {
            for (java.io.File file : event.getDragboard().getFiles()) {
                loadFile(file);
            }
            event.setDropCompleted(true);
        } else {
            event.setDropCompleted(false);
        }
        event.consume();
    }

    @FXML
    private void handleDragOver(DragEvent event) {
        if (event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.ANY);
        }
        event.consume();
    }

    private void loadFile(File file) {
        try {
            path = file.getParent(); // Update the path
            backupFile = file;      // Update the backupFile

            byte[] fileBytes = Files.readAllBytes(file.toPath());
            boolean isChapter = SaveFile.isChapter(fileBytes);
            //Chapter Save File
            if (isChapter) {
                reloadChapter(fileBytes);
                tabPane.getSelectionModel().select(tabUnit);
            }
            //Global Save File
            else {
                reloadGlobal(fileBytes);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error loading file: " + e.getMessage());
        }
    }


    public void reloadChapter(byte[] fileBytes) {
        try {
            FireEditor.chapterFile = new Chapter13(fileBytes);
            FireEditor.unitController.loadUnitBlock();
            FireEditor.unitController.comboUnitGroup.setDisable(false);
            tabUnit.setDisable(false);
            tabConvoy.setDisable(false);
            tabChapter.setDisable(false);
            tabCheats.setDisable(false);
            tabGlobal.setDisable(true);
            FireEditor.convoyController.loadBlocks();
            FireEditor.userController.loadBlocks();
            FireEditor.global = null;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error loading chapter file: " + e.getMessage());
        }
    }

    public void reloadGlobal(byte[] fileBytes) {
        try {
            tabGlobal.setDisable(false);
            FireEditor.global = new Global(fileBytes);
            FireEditor.globalController.loadFile();
            tabPane.getSelectionModel().select(tabGlobal);
            FireEditor.unitController.comboUnitGroup.setDisable(true);
            tabConvoy.setDisable(true);
            tabChapter.setDisable(true);
            tabCheats.setDisable(true);
            tabUnit.setDisable(true);
            FireEditor.chapterFile = null;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error loading global file: " + e.getMessage());
        }
    }

    public void createAndExportBackup() throws IOException {
        // Create the "bak" folder if it doesn't exist
        String userDir = System.getProperty("user.dir");
        File bakFolder = new File(userDir, "bak");
        if (!bakFolder.exists()) {
            boolean created = bakFolder.mkdir();
            if (!created) {
                System.err.println("Failed to create 'bak' directory.");
                return;
            }
        }
        // Generate a timestamp for the filename
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = dateFormat.format(new Date());
        // Get the original filename
        String originalFilename = backupFile.getName();
        // Create the backup file path with the timestamp and original filename
        String backupFilePath = bakFolder.getAbsolutePath() + File.separator + timestamp + "_" + originalFilename;
        // Read content from the backupFile and write to the backup file
        try (FileInputStream fis = new FileInputStream(backupFile);
             FileOutputStream fos = new FileOutputStream(backupFilePath)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }
    }

    public void exportDecomp() throws IOException {
        if (FireEditor.chapterFile == null && FireEditor.global == null) return;
        //The data is compiled
        byte[] data = chapterBytes(true);
        //File chooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(path));
        // Show save dialog
        File file = fileChooser.showSaveDialog(null);
        if (file == null) return;
        createAndExportBackup();
        // Save byte array to the selected file
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(data);
        }
    }

    public void exportComp() throws IOException {
        if (FireEditor.chapterFile == null) return;
        //The data is compiled
        byte[] data = chapterBytes(false);
        //File chooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(path));
        // Show save dialog
        File file = fileChooser.showSaveDialog(null);
        createAndExportBackup();
        if (file == null) return;
        // Save byte array to the selected file
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(data);
        }
    }

    public void importUnit() {
        if (FireEditor.chapterFile == null) return;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(path));

        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(null);
        if (selectedFiles == null || selectedFiles.isEmpty()) return;

        path = selectedFiles.get(0).getParent();
        int totalSize = FireEditor.unitController.listViewUnit.getItems().size();
        boolean isWest = FireEditor.unitController.unitBlock.isWest;
        for (File file : selectedFiles) {
            try {
                byte[] data = Files.readAllBytes(file.toPath());
                Unit unit = new Unit(data);
                if (unit.rawLog != null) unit.rawLog.changeRegion(isWest); //Converts the unit to the current region
                if (totalSize < Constants.UNIT_LIMIT) {
                    FireEditor.unitController.addUnit(unit);
                }
                totalSize++;
            } catch (Exception e) {
                throw new RuntimeException("INVALID UNIT FILE!");
            }
        }
    }

    public void exportUnit() {
        if (FireEditor.chapterFile == null) return;
        Unit selectedUnit = FireEditor.unitController.listViewUnit.getSelectionModel().getSelectedItem();
        if (selectedUnit == null) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(path));
        File file = fileChooser.showSaveDialog(null);
        if (file == null) return;

        path = file.getParent();
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(selectedUnit.getUnitBytes());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error exporting unit: " + e.getMessage());
        }
    }

    public byte[] chapterBytes(boolean decomp) {
        if (FireEditor.chapterFile == null) return new byte[0];
        if (decomp) return FireEditor.chapterFile.getBytes();
        else return FireEditor.chapterFile.getBytesComp();
    }

    public static FXMLLoader getWindowUser(String fxml) throws IOException {
        return new FXMLLoader(MainController.class.getResource(Constants.RES_FXML + "user/" + fxml + ".fxml"));
    }

    public static FXMLLoader getWindowUnit(String fxml) throws IOException {
        return new FXMLLoader(MainController.class.getResource(Constants.RES_FXML + "unit/" + fxml + ".fxml"));
    }
}


