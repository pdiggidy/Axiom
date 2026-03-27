package com.axiom.common.tile.base;

/**
 * Implemented by machines that can surface a human-readable status to the UI layer.
 * Keys are translation-key suffixes; screens prepend the mod's lang prefix.
 * Returns null when the machine is running normally so screens can omit the warning.
 */
public interface IMachineStatus {

    boolean isActive();

    /**
     * Returns a translation key suffix describing why this machine is not active,
     * or {@code null} when the machine is operating normally.
     * Example: {@code "status.no_input"} → screen resolves full key.
     */
    String getStatusKey();
}
