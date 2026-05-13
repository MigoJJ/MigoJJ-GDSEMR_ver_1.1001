package com.emr.gds.features.thyroid;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class ThyroidLabsPane extends VBox {

    private final ThyroidEntry entry;
    
    private final TextField txtTsh = new TextField();
    private final TextField txtFreeT4 = new TextField();
    private final TextField txtFreeT3 = new TextField();
    private final TextField txtTotalT3 = new TextField();
    private final TextField txtTpoAb = new TextField();
    private final TextField txtTg = new TextField();
    private final TextField txtTgAb = new TextField();
    private final TextField txtTrab = new TextField();
    private final TextField txtCalcitonin = new TextField();
    private final TextField txtReverseT3 = new TextField();
    private final DatePicker dpLastLabDate = new DatePicker();

    public ThyroidLabsPane(ThyroidEntry entry) {
        this.entry = entry;
        initControls();
        buildLayout();
        setSpacing(10);
        setPadding(new Insets(10));
    }

    private void initControls() {
        txtTsh.setPromptText("TSH (0.4-4.0 uIU/mL)");
        txtFreeT4.setPromptText("fT4 (0.8-1.8 ng/dL)");
        txtFreeT3.setPromptText("fT3 (2.3-4.2 pg/mL)");
        txtTotalT3.setPromptText("T3 (80-200 ng/dL)");
        txtTpoAb.setPromptText("TPOAb (≤34.0 IU/mL)");
        txtTg.setPromptText("Tg (3.50-77.00 ng/mL)");
        txtTgAb.setPromptText("TgAb (≤115.0 IU/mL)");
        txtTrab.setPromptText("TRAb (<1.75 IU/L)");
        txtCalcitonin.setPromptText("Calcitonin (M:≤18.2, F:≤11.5 pg/mL)");
        txtReverseT3.setPromptText("revT3 (8-25 ng/dL)");
        dpLastLabDate.setPromptText("Date");
    }

    private void buildLayout() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);

        grid.addRow(0, new Label("TSH (uIU/mL)"), txtTsh, new Label("fT4 (ng/dL)"), txtFreeT4);
        grid.addRow(1, new Label("fT3 (pg/mL)"), txtFreeT3, new Label("T3 (ng/dL)"), txtTotalT3);
        grid.addRow(2, new Label("TPOAb (IU/mL)"), txtTpoAb, new Label("Tg (ng/mL)"), txtTg, new Label("TgAb (IU/mL)"), txtTgAb);
        grid.addRow(3, new Label("TRAb (IU/L)"), txtTrab, new Label("Calcitonin (pg/mL)"), txtCalcitonin, new Label("revT3 (ng/dL)"), txtReverseT3);
        grid.addRow(4, new Label("Date"), dpLastLabDate);

        getChildren().add(grid);
    }

    public void loadFromEntry() {
        if (entry.getTsh() != null) txtTsh.setText(String.valueOf(entry.getTsh()));
        if (entry.getFreeT4() != null) txtFreeT4.setText(String.valueOf(entry.getFreeT4()));
        if (entry.getFreeT3() != null) txtFreeT3.setText(String.valueOf(entry.getFreeT3()));
        if (entry.getTotalT3() != null) txtTotalT3.setText(String.valueOf(entry.getTotalT3()));
        if (entry.getTpoAb() != null) txtTpoAb.setText(String.valueOf(entry.getTpoAb()));
        if (entry.getTg() != null) txtTg.setText(String.valueOf(entry.getTg()));
        if (entry.getTgAb() != null) txtTgAb.setText(String.valueOf(entry.getTgAb()));
        if (entry.getTrab() != null) txtTrab.setText(String.valueOf(entry.getTrab()));
        if (entry.getCalcitonin() != null) txtCalcitonin.setText(String.valueOf(entry.getCalcitonin()));
        if (entry.getReverseT3() != null) txtReverseT3.setText(String.valueOf(entry.getReverseT3()));
        dpLastLabDate.setValue(entry.getLastLabDate());
    }

    public void saveToEntry() {
        entry.setTsh(parseNullableDouble(txtTsh));
        entry.setFreeT4(parseNullableDouble(txtFreeT4));
        entry.setFreeT3(parseNullableDouble(txtFreeT3));
        entry.setTotalT3(parseNullableDouble(txtTotalT3));
        entry.setTpoAb(parseNullableDouble(txtTpoAb));
        entry.setTg(parseNullableDouble(txtTg));
        entry.setTgAb(parseNullableDouble(txtTgAb));
        entry.setTrab(parseNullableDouble(txtTrab));
        entry.setCalcitonin(parseNullableDouble(txtCalcitonin));
        entry.setReverseT3(parseNullableDouble(txtReverseT3));
        entry.setLastLabDate(dpLastLabDate.getValue());
    }

    private Double parseNullableDouble(TextField field) {
        String value = field.getText();
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
