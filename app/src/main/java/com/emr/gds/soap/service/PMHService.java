package com.emr.gds.soap.service;

import com.emr.gds.soap.model.PMHEntry;
import com.emr.gds.util.I18N; // Import I18N
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * PMH 데이터 처리 서비스
 * UI와 독립적인 비즈니스 로직 담당
 */
public class PMHService {
    
    private final Map<String, String> abbreviationMap;
    
    public PMHService(Map<String, String> abbreviationMap) {
        this.abbreviationMap = abbreviationMap != null ? abbreviationMap : Collections.emptyMap();
    }

    /**
     * PMH 요약 텍스트 생성
     * @param entries PMH 항목 리스트
     * @param applySaveLogic 저장 시 특수 로직 적용 여부
     * @return 포맷된 요약 텍스트
     */
    public String buildSummary(List<PMHEntry> entries, boolean applySaveLogic) {
        StringBuilder sb = new StringBuilder();
        sb.append(I18N.get("pmh.summary.header")).append("\n"); // Use I18N

        List<String> checkedLines = new ArrayList<>();
        List<String> uncheckedLines = new ArrayList<>();

        // "All denied allergies" 선택 여부 확인
        boolean allDeniedSelected = entries.stream()
                .anyMatch(e -> e.getCategory().contains("All denied allergies") && e.isSelected());

        for (PMHEntry entry : entries) {
            // 특수 로직: "All denied allergies" 처리
            if (applySaveLogic && entry.getCategory().equals("All denied allergies...") && entry.isSelected()) {
                String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                checkedLines.add(String.format(I18N.get("pmh.summary.all_denied_allergies_text"), date)); // Use I18N
                continue;
            }

            // "All denied" 선택 시 개별 알레르기 항목 숨김
            if (allDeniedSelected && 
                entry.getCategory().contains("Allergy") && 
                !entry.getCategory().equals("All denied allergies...")) {
                continue;
            }

            if (entry.shouldIncludeInSummary()) {
                String line = entry.formatForDisplay();
                if (entry.isSelected()) {
                    checkedLines.add(line);
                } else {
                    uncheckedLines.add(line);
                }
            }
        }

        if (checkedLines.isEmpty() && uncheckedLines.isEmpty()) {
            return I18N.get("pmh.summary.no_items_selected"); // Use I18N
        }

        // 선택된 항목 먼저 추가
        checkedLines.forEach(line -> sb.append(line).append("\n"));

        // 간격 추가
        if (!checkedLines.isEmpty() && !uncheckedLines.isEmpty()) {
            sb.append("\n");
        }

        // 선택되지 않은 항목을 2열로 배치
        appendTwoColumnLayout(sb, uncheckedLines);

        return sb.toString();
    }

    /**
     * 기본 점(.) 적용
     */
    public void applyDefaultDots(List<PMHEntry> entries) {
        entries.stream()
                .filter(PMHEntry::isDefaultDotTarget)
                .forEach(PMHEntry::applyDefaultDot);
    }

    /**
     * 약어 확장
     */
    public Optional<String> expandAbbreviation(String abbreviation) {
        if (abbreviation == null || !abbreviation.startsWith(":")) {
            return Optional.empty();
        }
        String key = abbreviation.substring(1);
        return Optional.ofNullable(abbreviationMap.get(key));
    }

    /**
     * 카테고리별 필터링
     */
    public List<PMHEntry> filterByType(List<PMHEntry> entries, PMHEntry.CategoryType type) {
        return entries.stream()
                .filter(e -> e.getType() == type)
                .collect(Collectors.toList());
    }

    /**
     * 선택된 항목만 반환
     */
    public List<PMHEntry> getSelectedEntries(List<PMHEntry> entries) {
        return entries.stream()
                .filter(PMHEntry::isSelected)
                .collect(Collectors.toList());
    }

    /**
     * 데이터 검증
     */
    public ValidationResult validate(List<PMHEntry> entries) {
        List<String> warnings = new ArrayList<>();
        
        // 알레르기 항목 중복 체크
        long allergyCount = entries.stream()
                .filter(e -> e.getType() == PMHEntry.CategoryType.ALLERGY && e.isSelected())
                .count();
        
        boolean allDenied = entries.stream()
                .anyMatch(e -> e.getCategory().contains("All denied") && e.isSelected());
        
        if (allDenied && allergyCount > 1) {
            warnings.add(I18N.get("pmh.validation.allergy_conflict")); // Use I18N
        }

        // 빈 메모 체크
        long emptyNotes = entries.stream()
                .filter(PMHEntry::isSelected)
                .filter(e -> e.getNotes().trim().isEmpty())
                .count();
        
        if (emptyNotes > 0) {
            warnings.add(String.format(I18N.get("pmh.validation.empty_notes"), emptyNotes)); // Use I18N
        }

        return new ValidationResult(warnings.isEmpty(), warnings);
    }

    // === Private Helper Methods ===

    private void appendTwoColumnLayout(StringBuilder sb, List<String> lines) {
        if (lines.isEmpty()) return;

        // 왼쪽 열의 최대 길이 계산
        int maxLeft = 0;
        for (int i = 0; i < lines.size(); i += 2) {
            maxLeft = Math.max(maxLeft, lines.get(i).length());
        }
        int columnWidth = Math.min(Math.max(maxLeft + 2, 34), 60);

        // 2열로 배치
        for (int i = 0; i < lines.size(); i += 2) {
            String left = lines.get(i);
            String right = (i + 1 < lines.size()) ? lines.get(i + 1) : null;

            if (right == null) {
                sb.append(left).append("\n");
            } else {
                sb.append(padRight(left, columnWidth)).append(right).append("\n");
            }
        }
    }

    private String padRight(String text, int width) {
        if (text.length() >= width) {
            return text + " ";
        }
        return String.format("%-" + width + "s", text);
    }

    // === Inner Classes ===

    public static class ValidationResult {
        private final boolean valid;
        private final List<String> warnings;

        public ValidationResult(boolean valid, List<String> warnings) {
            this.valid = valid;
            this.warnings = warnings;
        }

        public boolean isValid() { return valid; }
        public List<String> getWarnings() { return warnings; }
        public String getWarningMessage() {
            return String.join("\n", warnings);
        }
    }
}