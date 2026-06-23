package com.danius.fireeditor.controllers;

import com.danius.fireeditor.FireEditor;
import com.danius.fireeditor.controllers.unit.LogController;
import com.danius.fireeditor.controllers.unit.SkillController;
import com.danius.fireeditor.data.ClassDb;
import com.danius.fireeditor.savefile.Constants;
import com.danius.fireeditor.savefile.global.Global;
import com.danius.fireeditor.savefile.units.Stats;
import com.danius.fireeditor.savefile.units.Unit;
import com.danius.fireeditor.savefile.wireless.UnitDu;
import com.danius.fireeditor.util.Portrait;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
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

    @FXML
    private Spinner<Integer> spinLevel;
    @FXML
    private ComboBox<String> comboClass;
    @FXML
    private ColorPicker colorHair;
    @FXML
    private TextField txtGrowthHp, txtGrowthStr, txtGrowthMag, txtGrowthSkl, txtGrowthSpd,
            txtGrowthLck, txtGrowthDef, txtGrowthRes, txtGrowthMove;
    @FXML
    private TextField txtStatHp, txtStatStr, txtStatMag, txtStatSkl, txtStatSpd,
            txtStatLck, txtStatDef, txtStatRes, txtStatMove;
    @FXML
    private CheckBox checkLimit;
    @FXML
    private Button btnMaxStats, btnOpenSkills, btnOpenAvatar, btnDuplicate, btnRemove;

    private boolean updatingFields = false;

    public void initialize() {
        FireEditor.globalController = this;
        UI.setSpinnerNumeric(spinRenown, 99999);
        setupElements();
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
                if (oldValue != null) {
                    updateUnitFromFields(oldValue);
                }
                updateUnits();
                setFields(newValue);
                setPortrait();
            } else {
                disableElements(true);
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
        setupListeners();
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
            if (listViewUnit.getSelectionModel().getSelectedItem() != null) {
                setFields(listViewUnit.getSelectionModel().getSelectedItem());
                setPortrait();
            } else {
                disableElements(true);
            }
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

    // -------------------------------------------------------------------------
    // Unit Editor
    // -------------------------------------------------------------------------

    private void setupElements() {
        UI.setSpinnerNumeric(spinLevel, 30);

        UI.setSignedNumericTextField(txtGrowthHp, 255);
        UI.setSignedNumericTextField(txtGrowthStr, 255);
        UI.setSignedNumericTextField(txtGrowthMag, 255);
        UI.setSignedNumericTextField(txtGrowthSkl, 255);
        UI.setSignedNumericTextField(txtGrowthSpd, 255);
        UI.setSignedNumericTextField(txtGrowthLck, 255);
        UI.setSignedNumericTextField(txtGrowthDef, 255);
        UI.setSignedNumericTextField(txtGrowthRes, 255);
        UI.setNumericTextField(txtGrowthMove, 255);

        ObservableList<String> classes = FXCollections.observableArrayList(ClassDb.getClassNames());
        comboClass.setItems(classes);
    }

    private void setupListeners() {
        //Limit break checkbox refreshes the displayed stats
        checkLimit.setOnAction(event -> {
            UnitDu selected = listViewUnit.getSelectionModel().getSelectedItem();
            if (selected != null) {
                setFieldsStats(selected);
            }
        });
        //Hair color updates the portrait
        colorHair.valueProperty().addListener((observable, oldColor, newColor) -> {
            UnitDu selected = listViewUnit.getSelectionModel().getSelectedItem();
            if (selected != null && newColor != null) {
                selected.setHairColorFx(newColor);
                setPortrait();
            }
        });
        spinLevel.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && listViewUnit.getSelectionModel().getSelectedItem() != null) {
                spinLevel.increment(0);
                listViewUnit.getSelectionModel().getSelectedItem().setLevel(spinLevel.getValue());
            }
        });
        //Class changes portrait and stats
        comboClass.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && listViewUnit.getSelectionModel().getSelectedItem() != null) {
                UnitDu selected = listViewUnit.getSelectionModel().getSelectedItem();
                selected.setUnitClass(comboClass.getSelectionModel().getSelectedIndex());
                refreshData(selected);
            }
        });
        textStatsListeners(txtGrowthHp, 0);
        textStatsListeners(txtGrowthStr, 1);
        textStatsListeners(txtGrowthMag, 2);
        textStatsListeners(txtGrowthSkl, 3);
        textStatsListeners(txtGrowthSpd, 4);
        textStatsListeners(txtGrowthLck, 5);
        textStatsListeners(txtGrowthDef, 6);
        textStatsListeners(txtGrowthRes, 7);
        textStatsListeners(txtGrowthMove, -1);
    }

    private void textStatsListeners(TextField textField, int slot) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (updatingFields) return;
            if (newValue == null || newValue.isEmpty() || newValue.equals("-")) return;
            if (listViewUnit.getSelectionModel().getSelectedItem() != null) {
                UnitDu selected = listViewUnit.getSelectionModel().getSelectedItem();
                int value = Integer.parseInt(newValue);
                if (slot >= 0) selected.setGrowth(UI.toUnsigned(value), slot);
                else selected.setMovement(value);
                setFieldsStats(selected);
            }
        });
    }

    public void setFields(UnitDu unitDu) {
        if (unitDu == null) {
            disableElements(true);
            return;
        }
        updatingFields = true;
        try {
            disableElements(false);
            Unit unit = unitDu.toUnit();
            checkLimit.setSelected(Stats.hasLimitBreaker(unit));
            labelUnitName.setText(unitDu.getName());
            comboClass.getSelectionModel().select(unitDu.getUnitClass());
            spinLevel.getValueFactory().setValue(unitDu.getLevel());
            colorHair.setValue(unitDu.getHairColorFx());
            int[] growth = unitDu.getGrowth();
            txtGrowthHp.setText(String.valueOf(UI.toSigned(growth[0])));
            txtGrowthStr.setText(String.valueOf(UI.toSigned(growth[1])));
            txtGrowthMag.setText(String.valueOf(UI.toSigned(growth[2])));
            txtGrowthSkl.setText(String.valueOf(UI.toSigned(growth[3])));
            txtGrowthSpd.setText(String.valueOf(UI.toSigned(growth[4])));
            txtGrowthLck.setText(String.valueOf(UI.toSigned(growth[5])));
            txtGrowthDef.setText(String.valueOf(UI.toSigned(growth[6])));
            txtGrowthRes.setText(String.valueOf(UI.toSigned(growth[7])));
            txtGrowthMove.setText(String.valueOf(unitDu.getMovement()));
            setFieldsStats(unit);
            setPortrait();
        } finally {
            updatingFields = false;
        }
    }

    public void setFieldsStats(UnitDu unitDu) {
        setFieldsStats(unitDu.toUnit());
    }

    public void setFieldsStats(Unit unit) {
        int[] currentStats = unit.currentStats(checkLimit.isSelected());
        txtStatHp.setText(String.valueOf(currentStats[0]));
        txtStatStr.setText(String.valueOf(currentStats[1]));
        txtStatMag.setText(String.valueOf(currentStats[2]));
        txtStatSkl.setText(String.valueOf(currentStats[3]));
        txtStatSpd.setText(String.valueOf(currentStats[4]));
        txtStatLck.setText(String.valueOf(currentStats[5]));
        txtStatDef.setText(String.valueOf(currentStats[6]));
        txtStatRes.setText(String.valueOf(currentStats[7]));
        txtStatMove.setText(String.valueOf(Stats.getMoveTotal(unit)));
    }

    public void updateUnitFromFields(UnitDu unitDu) {
        if (unitDu == null) return;
        spinLevel.increment(0);
        unitDu.setUnitClass(comboClass.getSelectionModel().getSelectedIndex());
        unitDu.setLevel(spinLevel.getValue());
        unitDu.setHairColorFx(colorHair.getValue());
        unitDu.setGrowth(UI.parseSignedTextField(txtGrowthHp), 0);
        unitDu.setGrowth(UI.parseSignedTextField(txtGrowthStr), 1);
        unitDu.setGrowth(UI.parseSignedTextField(txtGrowthMag), 2);
        unitDu.setGrowth(UI.parseSignedTextField(txtGrowthSkl), 3);
        unitDu.setGrowth(UI.parseSignedTextField(txtGrowthSpd), 4);
        unitDu.setGrowth(UI.parseSignedTextField(txtGrowthLck), 5);
        unitDu.setGrowth(UI.parseSignedTextField(txtGrowthDef), 6);
        unitDu.setGrowth(UI.parseSignedTextField(txtGrowthRes), 7);
        unitDu.setMovement(Integer.parseInt(txtGrowthMove.getText()));
    }

    private void refreshData(UnitDu unitDu) {
        setFieldsStats(unitDu);
        setPortrait();
    }

    public void maxGrowth() {
        UnitDu selected = listViewUnit.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        Unit unit = selected.toUnit();
        unit.maxGrowth();
        int[] growth = unit.rawBlock1.growth();
        selected.setGrowth(growth[0], 0);
        selected.setGrowth(growth[1], 1);
        selected.setGrowth(growth[2], 2);
        selected.setGrowth(growth[3], 3);
        selected.setGrowth(growth[4], 4);
        selected.setGrowth(growth[5], 5);
        selected.setGrowth(growth[6], 6);
        selected.setGrowth(growth[7], 7);
        selected.setMovement(2);
        txtGrowthHp.setText(String.valueOf(UI.toSigned(growth[0])));
        txtGrowthStr.setText(String.valueOf(UI.toSigned(growth[1])));
        txtGrowthMag.setText(String.valueOf(UI.toSigned(growth[2])));
        txtGrowthSkl.setText(String.valueOf(UI.toSigned(growth[3])));
        txtGrowthSpd.setText(String.valueOf(UI.toSigned(growth[4])));
        txtGrowthLck.setText(String.valueOf(UI.toSigned(growth[5])));
        txtGrowthDef.setText(String.valueOf(UI.toSigned(growth[6])));
        txtGrowthRes.setText(String.valueOf(UI.toSigned(growth[7])));
        txtGrowthMove.setText(String.valueOf(2));
        setFieldsStats(selected);
    }

    public void unitDuplicate() {
        ObservableList<UnitDu> unitList = listViewUnit.getItems();
        UnitDu selectedUnit = listViewUnit.getSelectionModel().getSelectedItem();
        if (selectedUnit != null && unitList.size() < Constants.UNIT_LIMIT) {
            updateUnitFromFields(selectedUnit);
            byte[] fullBytes = selectedUnit.bytesFull();
            byte[] header = selectedUnit.getHeader();
            byte[] main;
            UnitDu duplicatedUnit;
            if (header != null && header.length == 5) {
                main = Arrays.copyOfRange(fullBytes, header.length, fullBytes.length);
                duplicatedUnit = new UnitDu(main, null);
                duplicatedUnit.setHeader(Arrays.copyOf(header, header.length));
            } else {
                main = fullBytes;
                duplicatedUnit = new UnitDu(main, null);
            }
            int selectedIndex = unitList.indexOf(selectedUnit);
            unitList.add(selectedIndex + 1, duplicatedUnit);
            listViewUnit.setItems(FXCollections.observableArrayList(unitList));
            listViewUnit.getSelectionModel().select(selectedIndex + 1);
            displayUnitCount();
        }
    }

    public void unitDelete() {
        ObservableList<UnitDu> unitList = listViewUnit.getItems();
        UnitDu selectedUnit = listViewUnit.getSelectionModel().getSelectedItem();
        int index = listViewUnit.getSelectionModel().getSelectedIndex();
        if (selectedUnit != null) {
            unitList.remove(index);
            listViewUnit.setItems(unitList);
            updateUnits();
            displayUnitCount();
            if (unitList.isEmpty()) {
                listViewUnit.getSelectionModel().clearSelection();
            }
        }
    }

    public void displayUnitCount() {
        lblUnitCount.setText("Logbook Avatars: " + listViewUnit.getItems().size() + "/99");
    }

    public void disableElements(boolean disable) {
        comboClass.setDisable(disable);
        spinLevel.setDisable(disable);
        colorHair.setDisable(disable);
        txtGrowthHp.setDisable(disable);
        txtGrowthStr.setDisable(disable);
        txtGrowthMag.setDisable(disable);
        txtGrowthSkl.setDisable(disable);
        txtGrowthSpd.setDisable(disable);
        txtGrowthLck.setDisable(disable);
        txtGrowthDef.setDisable(disable);
        txtGrowthRes.setDisable(disable);
        txtGrowthMove.setDisable(disable);
        btnMaxStats.setDisable(disable);
        btnOpenSkills.setDisable(disable);
        btnOpenAvatar.setDisable(disable);
        btnDuplicate.setDisable(disable);
        btnRemove.setDisable(disable);
        checkLimit.setDisable(disable);
        if (disable) {
            imgBuild.setImage(null);
            imgHair.setImage(null);
            imgHairColor.setImage(null);
            labelUnitName.setText("");
        }
    }

    public void openSkills(ActionEvent event) {
        try {
            UnitDu selectedDu = listViewUnit.getSelectionModel().getSelectedItem();
            if (selectedDu != null) {
                updateUnitFromFields(selectedDu);
                Unit selectedValue = selectedDu.toUnit();
                FXMLLoader fxmlLoader = MainController.getWindowUnit("viewSkills");
                Parent root = fxmlLoader.load();
                SkillController skillController = fxmlLoader.getController();
                skillController.setUnit(selectedValue);
                Stage secondaryStage = new Stage();
                secondaryStage.initModality(Modality.APPLICATION_MODAL);
                secondaryStage.setTitle("Skills");
                secondaryStage.setScene(new Scene(root));
                ThemeManager.getInstance().applyToStage(secondaryStage);
                secondaryStage.showAndWait();
                // Active skills are stored in a separate Unit block, so write them back to the UnitDu
                int[] activeSkills = selectedValue.rawBlock2.getCurrentSkills();
                for (int i = 0; i < activeSkills.length; i++) {
                    selectedDu.setActiveSkills(activeSkills[i], i);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void openLog(ActionEvent event) {
        try {
            UnitDu selectedDu = listViewUnit.getSelectionModel().getSelectedItem();
            if (selectedDu != null) {
                updateUnitFromFields(selectedDu);
                Unit selectedValue = selectedDu.toUnit();
                FXMLLoader fxmlLoader = MainController.getWindowUnit("viewLogbook");
                Parent root = fxmlLoader.load();
                LogController logController = fxmlLoader.getController();
                logController.setUnit(selectedValue, global.glUserBlock.avatarMale.isWest);
                Stage secondaryStage = new Stage();
                secondaryStage.initModality(Modality.APPLICATION_MODAL);
                secondaryStage.setTitle("Avatar Data");
                secondaryStage.setScene(new Scene(root));
                ThemeManager.getInstance().applyToStage(secondaryStage);
                secondaryStage.showAndWait();
                //Reload the logbook-dependent fields in case the avatar name changed
                setFields(selectedDu);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}



