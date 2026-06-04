package com.emr.gds.features.thyroid.view;

import com.emr.gds.features.thyroid.model.ThyroidEntry;
import com.emr.gds.features.thyroid.service.ThyroidRiskCalculator;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

public class ThyroidOverviewPane extends ScrollPane {

    private final ThyroidEntry entry;
    private final ComboBox<ThyroidEntry.VisitType> cmbVisitType = new ComboBox<>();
    private final TextField txtWeight = new TextField();
    private final Label lblLt4Est = new Label("Est. LT4: -");
    
    private final CheckBox chkHypo = new CheckBox("Hypothyroidism");
    private final CheckBox chkHyper = new CheckBox("Hyperthyroidism");
    private final CheckBox chkNodule = new CheckBox("Thyroid nodule");
    private final CheckBox chkCancer = new CheckBox("Thyroid cancer");
    private final CheckBox chkThyroiditis = new CheckBox("Thyroiditis");
    private final CheckBox chkGoiter = new CheckBox("Goiter");

    private final ComboBox<ThyroidEntry.HypoEtiology> cmbHypoEtiology = new ComboBox<>();
    private final ComboBox<ThyroidEntry.HyperEtiology> cmbHyperEtiology = new ComboBox<>();
    private final CheckBox chkHypoOvert = new CheckBox("Overt hypo");
    private final CheckBox chkHyperActive = new CheckBox("Active hyper");

    private final Map<String, List<CheckBox>> conditionGroupMap = new LinkedHashMap<>();
    private static final Map<String, String[]> CONDITION_GROUPS = buildConditionGroups();

    public ThyroidOverviewPane(ThyroidEntry entry) {
        this.entry = entry;
        initControls();
        setContent(buildLayout());
        setFitToWidth(true);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        setPrefViewportHeight(420);
    }

    private void initControls() {
        cmbVisitType.getItems().addAll(ThyroidEntry.VisitType.values());
        cmbVisitType.setPromptText("Visit type...");
        txtWeight.setPromptText("Weight (kg)");
        txtWeight.setPrefWidth(80);

        cmbHypoEtiology.getItems().addAll(ThyroidEntry.HypoEtiology.values());
        cmbHypoEtiology.setPromptText("Hypo etiology...");
        cmbHyperEtiology.getItems().addAll(ThyroidEntry.HyperEtiology.values());
        cmbHyperEtiology.setPromptText("Hyper etiology...");

        txtWeight.textProperty().addListener((obs, old, newValue) -> updateLt4Estimate());
    }

    private GridPane buildLayout() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));

        int row = 0;
        grid.add(new Label("Visit Type:"), 0, row);
        grid.add(cmbVisitType, 1, row);
        grid.add(new Label("Weight (kg):"), 2, row);
        grid.add(txtWeight, 3, row);
        grid.add(lblLt4Est, 4, row);
        row++;

        VBox hypoBox = new VBox(6,
                new Label("Hypothyroidism"),
                new HBox(10, chkHypo, cmbHypoEtiology, chkHypoOvert)
        );

        VBox hyperBox = new VBox(6,
                new Label("Hyperthyroidism"),
                new HBox(10, chkHyper, cmbHyperEtiology, chkHyperActive)
        );

        grid.add(new Label("Categories:"), 0, row);
        VBox catBox = new VBox(12,
                hypoBox,
                hyperBox,
                new Separator(),
                new HBox(10, chkNodule, chkCancer, chkThyroiditis, chkGoiter)
        );
        grid.add(catBox, 1, row, 4, 1);
        row++;

        Label conditionsLabel = new Label("Condition checklist:");
        conditionsLabel.setStyle("-fx-font-weight: bold;");
        grid.add(conditionsLabel, 0, row);
        grid.add(buildConditionChecklist(), 1, row, 4, 1);

        return grid;
    }

    private VBox buildConditionChecklist() {
        VBox root = new VBox(8);
        root.setPadding(new Insets(4, 0, 0, 0));

        int groupIndex = 0;
        int groupCount = CONDITION_GROUPS.size();
        for (var entry : CONDITION_GROUPS.entrySet()) {
            Label groupLabel = new Label(entry.getKey());
            groupLabel.setStyle("-fx-font-weight: bold;");
            GridPane grid = new GridPane();
            grid.setHgap(12);
            grid.setVgap(6);

            String[] items = entry.getValue();
            List<CheckBox> groupChecks = new ArrayList<>();
            for (int i = 0; i < items.length; i++) {
                CheckBox cb = new CheckBox(items[i]);
                groupChecks.add(cb);
                int col = i % 2;
                int row = i / 2;
                grid.add(cb, col, row);
            }
            conditionGroupMap.put(entry.getKey(), groupChecks);

            root.getChildren().addAll(groupLabel, grid);
            groupIndex++;
            if (groupIndex < groupCount) {
                root.getChildren().add(new Separator());
            }
        }
        return root;
    }

    private void updateLt4Estimate() {
        try {
            double w = Double.parseDouble(txtWeight.getText());
            double dose = ThyroidRiskCalculator.calculateFullReplacementDose(w);
            lblLt4Est.setText("Est. LT4: " + (int)dose + " mcg");
        } catch (NumberFormatException e) {
            lblLt4Est.setText("Est. LT4: -");
        }
    }

    private static Map<String, String[]> buildConditionGroups() {
        Map<String, String[]> map = new LinkedHashMap<>();
        map.put("Hypothyroidism", new String[]{
                "Hashimoto's thyroiditis (chronic autoimmune)",
                "Iatrogenic (post-surgery, post-RAI)",
                "Drug-induced (lithium, amiodarone, ICIs)",
                "Central (pituitary/hypothalamic) hypothyroidism",
                "Congenital hypothyroidism",
                "Subclinical hypothyroidism"
        });
        map.put("Hyperthyroidism", new String[]{
                "Graves' disease (diffuse toxic goiter)",
                "Toxic multinodular goiter (Plummer's disease)",
                "Toxic adenoma (solitary autonomous nodule)",
                "Thyroiditis-associated thyrotoxicosis",
                " - Subacute (de Quervain's) thyroiditis",
                " - Silent (painless) thyroiditis",
                " - Postpartum thyroiditis",
                "Iodine-induced hyperthyroidism (Jod-Basedow phenomenon)",
                "TSH-secreting pituitary adenoma",
                "hCG-mediated thyrotoxicosis (gestational, trophoblastic tumors)",
                "Factitious thyrotoxicosis (exogenous thyroid hormone)",
                "Subclinical hyperthyroidism"
        });
        map.put("Thyroiditis", new String[]{
                "Acute (suppurative) thyroiditis",
                "Subacute (de Quervain's) thyroiditis",
                "Chronic autoimmune (Hashimoto's) thyroiditis",
                "Silent (painless) thyroiditis",
                "Postpartum thyroiditis",
                "Drug-induced thyroiditis",
                "Riedel's thyroiditis (fibrous thyroiditis)"
        });
        map.put("Goiter", new String[]{
                "Simple (nontoxic) goiter",
                "Endemic goiter (iodine deficiency)",
                "Sporadic goiter",
                "Multinodular goiter (toxic and nontoxic)",
                "Diffuse goiter (Graves' disease, thyroiditis)"
        });
        map.put("Thyroid Nodules", new String[]{
                "Benign thyroid nodules",
                " - Colloid nodules",
                " - Follicular adenoma",
                " - Thyroid cysts",
                "Malignant thyroid nodules (see thyroid cancer)"
        });
        map.put("Thyroid Cancer", new String[]{
                "Differentiated thyroid cancer",
                " - Papillary thyroid carcinoma (most common)",
                " - Follicular thyroid carcinoma",
                " - Hurthle cell carcinoma",
                "Medullary thyroid carcinoma (from C cells)",
                "Anaplastic (undifferentiated) thyroid carcinoma",
                "Primary thyroid lymphoma",
                "Metastatic disease to thyroid"
        });
        map.put("Congenital / Developmental", new String[]{
                "Congenital hypothyroidism",
                "Thyroid dysgenesis (agenesis, ectopic thyroid)",
                "Dyshormonogenesis (defects in thyroid hormone synthesis)",
                "Thyroglossal duct cyst",
                "Lingual thyroid"
        });
        map.put("Sick Euthyroid", new String[]{
                "Nonthyroidal illness syndrome",
                "Low T3 syndrome"
        });
        map.put("Thyroid Hormone Resistance", new String[]{
                "Resistance to thyroid hormone (RTH)",
                "TSH receptor mutations"
        });
        map.put("Pregnancy-Related", new String[]{
                "Gestational thyrotoxicosis (hyperemesis gravidarum-related)",
                "Postpartum thyroiditis",
                "Transient thyrotoxicosis of pregnancy"
        });
        return map;
    }

    // Sync methods
    public void loadFromEntry() {
        cmbVisitType.setValue(entry.getVisitType());
        if (entry.getPatientWeightKg() != null) {
            txtWeight.setText(String.valueOf(entry.getPatientWeightKg()));
        }
        
        List<ThyroidEntry.MainCategory> cats = entry.getCategories();
        chkHypo.setSelected(cats.contains(ThyroidEntry.MainCategory.HYPOTHYROIDISM));
        chkHyper.setSelected(cats.contains(ThyroidEntry.MainCategory.HYPERTHYROIDISM));
        chkNodule.setSelected(cats.contains(ThyroidEntry.MainCategory.NODULE));
        chkCancer.setSelected(cats.contains(ThyroidEntry.MainCategory.CANCER));
        chkThyroiditis.setSelected(cats.contains(ThyroidEntry.MainCategory.THYROIDITIS));
        chkGoiter.setSelected(cats.contains(ThyroidEntry.MainCategory.GOITER));

        cmbHypoEtiology.setValue(entry.getHypoEtiology());
        if (entry.isHypoOvert() != null) chkHypoOvert.setSelected(entry.isHypoOvert());
        cmbHyperEtiology.setValue(entry.getHyperEtiology());
        if (entry.isHyperActive() != null) chkHyperActive.setSelected(entry.isHyperActive());
        
        updateLt4Estimate();
    }

    public void saveToEntry() {
        entry.setVisitType(cmbVisitType.getValue());
        try {
            entry.setPatientWeightKg(Double.parseDouble(txtWeight.getText()));
        } catch (NumberFormatException ignored) {}

        List<ThyroidEntry.MainCategory> cats = new ArrayList<>();
        if (chkHypo.isSelected()) cats.add(ThyroidEntry.MainCategory.HYPOTHYROIDISM);
        if (chkHyper.isSelected()) cats.add(ThyroidEntry.MainCategory.HYPERTHYROIDISM);
        if (chkNodule.isSelected()) cats.add(ThyroidEntry.MainCategory.NODULE);
        if (chkCancer.isSelected()) cats.add(ThyroidEntry.MainCategory.CANCER);
        if (chkThyroiditis.isSelected()) cats.add(ThyroidEntry.MainCategory.THYROIDITIS);
        if (chkGoiter.isSelected()) cats.add(ThyroidEntry.MainCategory.GOITER);
        entry.setCategories(cats);

        entry.setHypoEtiology(cmbHypoEtiology.getValue());
        entry.setHypoOvert(chkHypoOvert.isSelected());
        entry.setHyperEtiology(cmbHyperEtiology.getValue());
        entry.setHyperActive(chkHyperActive.isSelected());
    }

    public Map<String, List<String>> getSelectedConditions() {
        Map<String, List<String>> selected = new LinkedHashMap<>();
        for (var entry : conditionGroupMap.entrySet()) {
            List<String> checked = entry.getValue().stream()
                    .filter(CheckBox::isSelected)
                    .map(CheckBox::getText)
                    .toList();
            if (!checked.isEmpty()) {
                selected.put(entry.getKey(), checked);
            }
        }
        return selected;
    }
}
