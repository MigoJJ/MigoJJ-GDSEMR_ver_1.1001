package com.emr.gds.features.bone.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DexaReport {
    private Long id;
    private String patientName;
    private String patientId;
    private LocalDate birthDate;
    private Integer age;
    private Sex sex;
    private Double heightCm;
    private Double weightKg;
    private LocalDate examDate;
    private LocalDate analysisDate;
    private List<DexaMeasurement> spineMeasurements = new ArrayList<>();
    private List<DexaMeasurement> femurMeasurements = new ArrayList<>();
    private String sourceImagePath;
    private DexaDiagnosis diagnosis;
    private String diagnosisRationale;
    private LocalDate createdAt;

    public DexaReport() {}

    public List<DexaMeasurement> allMeasurements() {
        List<DexaMeasurement> all = new ArrayList<>(spineMeasurements);
        all.addAll(femurMeasurements);
        return all;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public Sex getSex() { return sex; }
    public void setSex(Sex sex) { this.sex = sex; }

    public Double getHeightCm() { return heightCm; }
    public void setHeightCm(Double heightCm) { this.heightCm = heightCm; }

    public Double getWeightKg() { return weightKg; }
    public void setWeightKg(Double weightKg) { this.weightKg = weightKg; }

    public LocalDate getExamDate() { return examDate; }
    public void setExamDate(LocalDate examDate) { this.examDate = examDate; }

    public LocalDate getAnalysisDate() { return analysisDate; }
    public void setAnalysisDate(LocalDate analysisDate) { this.analysisDate = analysisDate; }

    public List<DexaMeasurement> getSpineMeasurements() { return spineMeasurements; }
    public void setSpineMeasurements(List<DexaMeasurement> spineMeasurements) {
        this.spineMeasurements = spineMeasurements != null ? spineMeasurements : new ArrayList<>();
    }

    public List<DexaMeasurement> getFemurMeasurements() { return femurMeasurements; }
    public void setFemurMeasurements(List<DexaMeasurement> femurMeasurements) {
        this.femurMeasurements = femurMeasurements != null ? femurMeasurements : new ArrayList<>();
    }

    public String getSourceImagePath() { return sourceImagePath; }
    public void setSourceImagePath(String sourceImagePath) { this.sourceImagePath = sourceImagePath; }

    public DexaDiagnosis getDiagnosis() { return diagnosis; }
    public void setDiagnosis(DexaDiagnosis diagnosis) { this.diagnosis = diagnosis; }

    public String getDiagnosisRationale() { return diagnosisRationale; }
    public void setDiagnosisRationale(String diagnosisRationale) { this.diagnosisRationale = diagnosisRationale; }

    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }
}
