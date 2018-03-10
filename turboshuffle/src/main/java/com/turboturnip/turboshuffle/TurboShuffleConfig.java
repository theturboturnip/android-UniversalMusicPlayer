package com.turboturnip.turboshuffle;

import java.util.HashMap;
import java.util.Map;

public class TurboShuffleConfig {
	// clumpType = float [0.0, 1.0] (0 = unclumped, 0.5 = no effect, 1 = clumped)
	public float clumpType;
	// clumpSeverity = float [0,] (0 = clumping ignored, inf = clumping used to max. Practical limit = 10)
	public float clumpSeverity;
	// clumpWeight = float [0.0, 1.0] (0 = no effect, 1 = maximal effect)
	public float clumpWeight;
	// clumpLengthSeverity = float [0,] (0 = length ignored when clumped, inf = length used to max when clumped)
	public float clumpLengthSeverity;
	// historySeverity = float [0,] (0 = history ignored, inf = history used to max. Practical limit = 500)
	public float historySeverity;

	public enum ProbabilityMode {
		BySong("Song"),
		ByLength("Length");

		private final String name;

		private static final Map<String, ProbabilityMode> map = new HashMap<>();

		static {
			for (ProbabilityMode en : values()) {
				map.put(en.name, en);
			}
		}

		public static ProbabilityMode valueFor(String name) {
			return map.get(name);
		}

		ProbabilityMode(String s) {
			name = s;
		}

		public boolean equals(String otherName) {
			// (otherName == null) check is not needed because name.equals(null) returns false
			return name.equals(otherName);
		}

		public String toString() {
			return this.name;
		}
	}
	public ProbabilityMode probabilityMode;

	public int maximumMemory;

	public float[] poolWeights;
	public float[] sumToOnePoolWeights;

	public TurboShuffleConfig(float clumpType, float clumpSeverity, float clumpWeight, float clumpLengthSeverity, float historySeverity, ProbabilityMode probabilityMode, int maximumMemory, float[] poolWeights) {
		if (clumpType < 0.0f || clumpType > 1.0f)
			throw new IllegalArgumentException(
					"Tried to create a shuffle config with clumpType " + clumpType + " outside of the range [0, 1]!"
			);
		this.clumpType = clumpType;
		if (clumpSeverity < 0.0f)
			throw new IllegalArgumentException(
					"Tried to create a shuffle config with clumpSeverity " + clumpSeverity + " smaller than 0!"
			);
		this.clumpSeverity = clumpSeverity;
		if (clumpWeight < 0.0f || clumpWeight > 1.0f)
			throw new IllegalArgumentException(
					"Tried to create a shuffle config with clumpWeight " + clumpWeight + " outside of the range [0, 1]!"
			);
		this.clumpWeight = clumpWeight;
		if (clumpLengthSeverity < 0.0f)
			throw new IllegalArgumentException(
					"Tried to create a shuffle config with clumpLengthSeverity " + clumpLengthSeverity + " smaller than 0!"
			);
		this.clumpLengthSeverity = clumpLengthSeverity;
		if (historySeverity < 0.0f)
			throw new IllegalArgumentException(
					"Tried to create a shuffle config with historySeverity " + historySeverity + " smaller than 0!"
			);
		this.historySeverity = historySeverity;

		this.probabilityMode = probabilityMode;

		if (maximumMemory < 0)
			throw new IllegalArgumentException(
					"Tried to create a shuffle config with maximumMemory " + maximumMemory + " smaller than 0!"
			);
		this.maximumMemory = maximumMemory;

		if (poolWeights.length == 0)
			throw new IllegalArgumentException(
					"Tried to create a shuffle config with 0 weights!"
			);

		this.poolWeights = new float[poolWeights.length];
		float totalWeights = 0;
		for (int i = 0; i < poolWeights.length; i++) {
			if (poolWeights[i] <= 0)
				throw new IllegalArgumentException(
						"Illegal pool weight " + poolWeights[i] +
								" for index " + i
				);
			totalWeights += poolWeights[i];
			this.poolWeights[i] = poolWeights[i];
		}

		this.sumToOnePoolWeights = new float[poolWeights.length];
		for (int i = 0; i < poolWeights.length; i++) {
			this.sumToOnePoolWeights[i] = poolWeights[i] / totalWeights;
		}
	}
}
