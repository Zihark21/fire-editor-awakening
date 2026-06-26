package com.danius.fireeditor.controllers.unit;

import com.danius.fireeditor.controllers.UI;
import com.danius.fireeditor.savefile.units.Unit;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;

import java.util.Arrays;
import java.util.List;

public class WeaponRankController {

    private Unit unit;

    @FXML
    private Spinner<Integer> spinSword, spinLance, spinAxe, spinBow, spinTome, spinStave;
    @FXML
    private Label lblSword, lblLance, lblAxe, lblBow, lblTome, lblStave;

    private List<Spinner<Integer>> spinners;
    private List<Label> rankLabels;

    public void initialize() {
        int maxExp = 90;
        UI.setSpinnerNumeric(spinSword, maxExp);
        UI.setSpinnerNumeric(spinLance, maxExp);
        UI.setSpinnerNumeric(spinAxe, maxExp);
        UI.setSpinnerNumeric(spinBow, maxExp);
        UI.setSpinnerNumeric(spinTome, maxExp);
        UI.setSpinnerNumeric(spinStave, maxExp);

        spinners = Arrays.asList(spinSword, spinLance, spinAxe, spinBow, spinTome, spinStave);
        rankLabels = Arrays.asList(lblSword, lblLance, lblAxe, lblBow, lblTome, lblStave);

        for (int i = 0; i < spinners.size(); i++) {
            int slot = i;
            spinners.get(i).valueProperty().addListener((observable, oldValue, newValue) -> {
                if (unit != null) {
                    unit.rawBlock2.setWeaponExp(newValue, slot);
                    rankLabels.get(slot).setText(weaponLevel(newValue));
                }
            });
        }
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
        int[] weaponExp = unit.rawBlock2.getWeaponExp();
        for (int i = 0; i < spinners.size(); i++) {
            spinners.get(i).getValueFactory().setValue(weaponExp[i]);
            rankLabels.get(i).setText(weaponLevel(weaponExp[i]));
        }
    }

    public void maxAll() {
        if (unit == null) return;
        for (int i = 0; i < spinners.size(); i++) {
            spinners.get(i).getValueFactory().setValue(90);
            unit.rawBlock2.setWeaponExp(90, i);
            rankLabels.get(i).setText(weaponLevel(90));
        }
    }

    private String weaponLevel(int value) {
        if (value >= 0 && value <= 14) return "E-Rank";
        else if (value >= 15 && value <= 34) return "D-Rank";
        else if (value >= 35 && value <= 59) return "C-Rank";
        else if (value >= 60 && value <= 89) return "B-Rank";
        else return "A-Rank";
    }
}
