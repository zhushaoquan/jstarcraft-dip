package com.github.kilianB.pcg.lock;

/**
 * A 64 bit State PcgRNG with 32 bit output. PCG-XSH-RR <p>
 * 
 * The pcg family combines a linear congruential generators with a permutation
 * output function resulting in high quality pseudo random numbers. <p>
 * 
 * The original concept was introduced by Melissa O’Neill please refer to <a
 * href="http://www.pcg-random.org/">pcg-random</a> for more information. <p>
 * Opposed to RR this version performs a random shift rather than a random
 * rotation.
 * 
 * The RS instance permutates the output using the following function:
 * 
 * <pre>
 * {@code
 * ((state >>> 22) ^ state) >>> ((state >>> 61) + 22)
 * }
 * </pre>
 * 
 * This implementation is thread safe utilizing read write locks. During non contested
 * periods this version performs considerably slower and only gains the upper hand
 * if the synchronized parts are hammered consistently. 
 * 
 * @author Kilian
 * @see <a href="http://www.pcg-random.org/">www.pcg-random.org</a>
 */
public class PcgRSLocked extends RandomBaseLocked {

	private static final long serialVersionUID = 6405237437417614399L;

	/**
	 * Create a synchronized PcgRS instance seeded with with 2 longs generated by xorshift*. 
	 * The values chosen are very likely not used as seeds in any other non argument constructor
	 * of any of the classes provided in this library. 
	 */
	public PcgRSLocked() {
		super();
	}
	
	/**
	 * Create a random number generator with the given seed and stream number. The
	 * seed defines the current state in which the rng is in and corresponds to
	 * seeds usually found in other RNG implementations. RNGs with different seeds are
	 * able to catch up after they exhaust their period and produce the same
	 * numbers. <p>
	 * 
	 * Different stream numbers alter the increment of the rng and ensure distinct
	 * state sequences <p>
	 * 
	 * Only generators with the same seed AND stream numbers will produce identical
	 * values <p>
	 * 
	 * @param seed
	 *            used to compute the starting state of the RNG
	 * @param streamNumber
	 *            used to compute the increment for the lcg.
	 */
	public PcgRSLocked(long seed, long streamNumber) {
		super(seed, streamNumber);
	}
	
	/**
	 * Copy constructor. <b>Has</b> to be implemented in all inheriting instances.
	 * This will be invoked through reflection! when calling {@link #split()} or
	 * {@link #splitDistinct()} If no special behavior is desired simply pass though
	 * the values.
	 * 
	 * This constructor should usually not be called manually as the seed and
	 * increment will just be set without performing any randomization.
	 * 
	 * @param seed
	 *            of the lcg. The value will be set and not altered.
	 * @param streamNumber
	 *            used in the lcg as increment constant.
	 * @param dummy
	 *            unused. Resolve signature disambiguate
	 */
	@Deprecated
	public PcgRSLocked(long seed, long streamNumber, boolean dummy) {
		super(seed, streamNumber, true);
	}

	@Override
	protected int getInt(long state) {
		return (int) (((state >>> 22) ^ state) >>> ((state >>> 61) + 22));
	}

	
//	protected int rotateRightUnsigned(int value, int rot) {
//		 return (value >>> rot) | (value << ((- rot) & 31));
//	}	

	
}
