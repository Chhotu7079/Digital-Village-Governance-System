package com.dvgs.scheme.exception;

public class ReapplyCooldownException extends RuntimeException {

    private final long remainingDays;

    public ReapplyCooldownException(Long schemeId, long remainingDays) {
        super("Re-apply cooldown active for schemeId=" + schemeId + ". Remaining days: " + remainingDays);
        this.remainingDays = remainingDays;
    }

    public long getRemainingDays() {
        return remainingDays;
    }
}
