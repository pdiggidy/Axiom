package com.axiom.common.transport;

/**
 * Rolling throughput counter for a belt or logistics segment.
 * Counts items successfully handed off and provides a recent-rate snapshot.
 *
 * <p>Rate is expressed as items-per-window-tick so the metrics UI can normalise
 * to items/second by multiplying by 20/windowTicks.</p>
 */
public final class BeltThroughputTracker {

    private final int windowTicks;
    private long windowStart;
    private int currentCount;
    private int lastWindowCount;

    public BeltThroughputTracker(int windowTicks) {
        this.windowTicks = windowTicks;
    }

    /**
     * Record that {@code count} items were successfully passed to the output this tick.
     * {@code gameTick} should be {@code world.getGameTime()}.
     */
    public void recordHandoff(int count, long gameTick) {
        if (gameTick - this.windowStart >= this.windowTicks) {
            this.lastWindowCount = this.currentCount;
            this.currentCount = 0;
            this.windowStart = gameTick;
        }
        this.currentCount += count;
    }

    /**
     * Items successfully handed off in the most recently completed window.
     * Use this for display; it is stable between resets.
     */
    public int getLastWindowCount() {
        return this.lastWindowCount;
    }

    /** Items counted in the current (in-progress) window. */
    public int getCurrentCount() {
        return this.currentCount;
    }

    public int getWindowTicks() {
        return this.windowTicks;
    }

    public void reset() {
        this.currentCount = 0;
        this.lastWindowCount = 0;
        this.windowStart = 0;
    }
}
