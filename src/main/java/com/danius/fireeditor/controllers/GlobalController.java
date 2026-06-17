package com.danius.fireeditor.controllers;

import com.danius.fireeditor.FireEditor;
import com.danius.fireeditor.savefile.global.Global;
import com.danius.fireeditor.savefile.units.Unit;
import com.danius.fireeditor.savefile.wireless.UnitDu;
import com.danius.fireeditor.util.Portrait;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GlobalController {
    private Global global;
    @FXML
    private ListView<UnitDu> listViewUnit;
    @FXML
    private Label lblUnitCount, labelUnitName;
    @FXML
    private ImageView imgBuild, imgHairColor, imgHair;
    @FXML
    private Spinner<Integer> spinRenown;
    @FXML
    private CheckBox check1, check2, check3;

    public void initialize() {
        FireEditor.globalController = this;
        UI.setSpinnerNumeric(spinRenown, 99999);
        imgBuild.setImage(null);
        imgHair.setImage(null);
        imgHairColor.setImage(null);
        labelUnitName.setText("");
        //Load the save file
        loadFile();
        //Unit List Listeners
        listViewUnit.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(UnitDu unit, boolean empty) {
                super.updateItem(unit, empty);
                if (empty || unit == null) {
                    setText(null);
                } else {
                    setText(unit.toString());
                }
            }
        });
        listViewUnit.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                updateUnits();
                setPortrait();
            }
        });
        spinRenown.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && FireEditor.global != null) {
                spinRenown.increment(0);
                global.glUserBlock.setRenown(Integer.parseInt(newValue));
            }
        });
        setCheckboxFlag(check1, 1);
        setCheckboxFlag(check2, 2);
        setCheckboxFlag(check3, 3);
    }

    public void loadFile() {
        if (FireEditor.global != null && FireEditor.globalController != null) {
            this.global = FireEditor.global;
            listViewUnit.setItems(FXCollections.observableArrayList(global.glUnitBlock.unitList));
            try {
                listViewUnit.getSelectionModel().selectLast();
                listViewUnit.getSelectionModel().selectFirst();
            } catch (Exception e) {
                // Ignore selection errors
            }
            lblUnitCount.setText("Logbook Avatars: " + global.glUnitBlock.unitList.size() + "/99");
            check1.setSelected(global.glUserBlock.hasGlobalFlag(1));
            check2.setSelected(global.glUserBlock.hasGlobalFlag(2));
            check3.setSelected(global.glUserBlock.hasGlobalFlag(3));
            spinRenown.getValueFactory().setValue(global.glUserBlock.getRenown());
            setPortrait();
        }
    }

    public void setCheckboxFlag(CheckBox checkBox, int bit) {
        checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (FireEditor.global != null) {
                global.glUserBlock.setGlobalFlag(bit, checkBox.isSelected());
            }
        });
    }

    public void updateUnits() {
        global.glUnitBlock.unitList = listViewUnit.getItems();
    }

    public void orderUp() {
        int selectedIndex = listViewUnit.getSelectionModel().getSelectedIndex();
        if (selectedIndex > 0) {
            UnitDu selectedItem = listViewUnit.getSelectionModel().getSelectedItem();
            listViewUnit.getItems().remove(selectedIndex);
            listViewUnit.getItems().add(selectedIndex - 1, selectedItem);
            listViewUnit.getSelectionModel().select(selectedIndex - 1);
        }
    }

    public void orderDown() {
        int selectedIndex = listViewUnit.getSelectionModel().getSelectedIndex();
        int itemCount = listViewUnit.getItems().size();

        if (selectedIndex >= 0 && selectedIndex < itemCount - 1) {
            UnitDu selectedItem = listViewUnit.getSelectionModel().getSelectedItem();
            listViewUnit.getItems().remove(selectedIndex);
            listViewUnit.getItems().add(selectedIndex + 1, selectedItem);
            listViewUnit.getSelectionModel().select(selectedIndex + 1);
        }
    }

    public void setPortrait() {
        UnitDu unitDu = listViewUnit.getSelectionModel().getSelectedItem();
        if (unitDu != null) {
            try {
                Unit unit = unitDu.toUnit();
                Image[] portrait = Portrait.setImage(unit);
                imgBuild.setImage(portrait[0]);
                imgHairColor.setImage(portrait[1]);
                imgHair.setImage(portrait[2]);
                labelUnitName.setText(unit.unitName());
            } catch (Exception e) {
                // Global Character Editor not fully implemented - skip portrait rendering
                imgBuild.setImage(null);
                imgHairColor.setImage(null);
                imgHair.setImage(null);
                labelUnitName.setText("Character Editor Not Implemented");
            }
        } else {
            imgBuild.setImage(null);
            imgHairColor.setImage(null);
            imgHair.setImage(null);
            labelUnitName.setText("");
        }
    }

    public void changeRegion() {
        boolean isCurrentWest = global.glUserBlock.avatarMale.isWest;
        String originalRegion = (isCurrentWest) ? "US/Europe" : "Japan";
        String targetRegion = (isCurrentWest) ? "Japan" : "US/Europe";

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        ThemeManager.getInstance().applyToDialogPane(alert.getDialogPane());
        alert.setTitle("Change Region");
        alert.setHeaderText("Current region: " + originalRegion + "\n" +
                "The save file will be changed to " + targetRegion);
        alert.setContentText("Note: The name of the Einherjar Units from the Avatar Logbook will be modified.");
        // Add Confirm and Cancel buttons
        ButtonType confirmButton = new ButtonType("Confirm");
        ButtonType cancelButton = new ButtonType("Cancel");
        alert.getButtonTypes().setAll(confirmButton, cancelButton);

        // Show the dialog and wait for a response
        alert.showAndWait().ifPresent(response -> {
            if (response == confirmButton) {
                FireEditor.global.changeRegion(!isCurrentWest);
                FireEditor.mainController.reloadGlobal(FireEditor.global.getBytes());
            } else if (response == cancelButton) {
                return;
            }
        });
    }

    public void unlockSupports() {
        global.glUserBlock.fullSupportLog();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        ThemeManager.getInstance().applyToDialogPane(alert.getDialogPane());
        alert.setHeaderText(null);
        alert.setContentText("All the Support Log and Unit Gallery entries have been unlocked!");
        alert.showAndWait();
    }

    public void saveFile() {
        if (global == null) return;
        
        // Update the unit list before saving
        updateUnits();
        
        // Create file chooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(MainController.path));
        
        // Show save dialog
        File file = fileChooser.showSaveDialog(null);
        if (file == null) return;
        
        try {
            // Create backup
            createBackup();
            
            // Get the compressed bytes
            byte[] data = global.getBytesComp();
            
            // Save to file
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(data);
            }
            
            // Update the backup file reference
            MainController.backupFile = file;
            MainController.path = file.getParent();
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            ThemeManager.getInstance().applyToDialogPane(alert.getDialogPane());
            alert.setHeaderText(null);
            alert.setContentText("File saved successfully!");
            alert.showAndWait();
            
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            ThemeManager.getInstance().applyToDialogPane(alert.getDialogPane());
            alert.setHeaderText(null);
            alert.setContentText("Error saving file: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    private void createBackup() throws IOException {
        if (MainController.backupFile == null) return;
        
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
        String originalFilename = MainController.backupFile.getName();
        
        // Create the backup file path with the timestamp and original filename
        String backupFilePath = bakFolder.getAbsolutePath() + File.separator + timestamp + "_" + originalFilename;
        
        // Read content from the backupFile and write to the backup file
        try (FileInputStream fis = new FileInputStream(MainController.backupFile);
             FileOutputStream fos = new FileOutputStream(backupFilePath)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }
    }

}



