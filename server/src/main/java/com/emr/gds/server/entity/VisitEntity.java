package com.emr.gds.server.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "visits")
public class VisitEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private LocalDateTime occurredAt;

    @Column(nullable = false)
    private String reason;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private PatientEntity patient;

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public PatientEntity getPatient() { return patient; }
    public void setPatient(PatientEntity patient) { this.patient = patient; }
}
