package com.civilizationmod;

/** Pure aggregate rules for translating food shortage into settlement stability. */
public final class StabilityDebt {
	public static final int MIN_STABILITY = 0;
	public static final int MAX_STABILITY = 100;
	public static final int STABILITY_LOSS_ON_SHORTAGE = 5;
	public static final int STABILITY_GAIN_ON_SURPLUS = 1;
	public static final int MAX_DEBT = 1_000_000;

	private StabilityDebt() {
	}

	public static int addShortage(int currentDebt, long shortage) {
		return clampDebt((long) Math.max(0, currentDebt) + Math.max(0L, shortage));
	}

	public static int reduceWithSurplus(int currentDebt, int surplus) {
		return clampDebt((long) Math.max(0, currentDebt) - Math.max(0, surplus));
	}

	public static int applyShortage(int currentStability, int stabilityLoss) {
		return clampStability((long) clampStability(currentStability) - Math.max(0, stabilityLoss));
	}

	public static int recoverWithSurplus(int currentStability, int surplus) {
		return clampStability((long) clampStability(currentStability) + Math.min(STABILITY_GAIN_ON_SURPLUS, Math.max(0, surplus)));
	}

	public static int clampStability(int stability) {
		return clampStability((long) stability);
	}

	private static int clampStability(long stability) {
		return (int) Math.max(MIN_STABILITY, Math.min(MAX_STABILITY, stability));
	}

	private static int clampDebt(long debt) {
		return (int) Math.max(0, Math.min(MAX_DEBT, debt));
	}
}
