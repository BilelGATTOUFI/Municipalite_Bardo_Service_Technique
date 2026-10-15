package com.MuniBardo.technique.entity;

public enum ApplicationStatus {
    PENDING("في الانتظار"),
    MISSING_DOCUMENTS("ناقص وثائق"),
    AWAITING_INSPECTION("في انتظار المعاينة"),
    APPROVED("موافق عليه"),
    REJECTED("مرفوض"),
    ARCHIVED("مؤرشف");

    private final String displayName;

    ApplicationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}