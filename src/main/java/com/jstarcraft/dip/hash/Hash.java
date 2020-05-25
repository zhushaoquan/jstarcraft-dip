package com.jstarcraft.dip.hash;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigInteger;

import com.github.kilianB.Require;
import com.github.kilianB.StringUtil;
import com.github.kilianB.hashAlgorithms.HashingAlgorithm;
import com.jstarcraft.dip.color.ColorPixel;

/**
 * Hashes are bit encoded encoded values (0101011101) created from images using
 * a hashing algorithm. Hashes enable a quick approximate similarity comparison
 * between images while only storing a fraction of the original data.
 * 
 * <p>
 * They are created from images down scaling information and enabling quick
 * comparison between instances produced by the same algorithm. Every bit in the
 * hash usually represents a section of the image containing certain information
 * (hue, brightness, color, frequencies or gradients)
 * 
 * @author Kilian
 * @since 1.0.0
 * @since 3.0.0 Serializable
 */
public class Hash implements Serializable {

	private static final long serialVersionUID = 3045682506632674223L;

	/**
	 * Unique identifier of the algorithm and settings used to create the hash
	 */
	protected int algorithmId;

	/**
	 * Hash value representation
	 * 
	 * Hashes are constructed by left shifting BigIntegers with either Zero or One
	 * depending on the condition found in the image. Preceding 0's will be
	 * truncated therefore it is the algorithms responsibility to add a 1 padding
	 * bit at the beginning new BigInteger("011011) new BigInteger("000101) 1xxxxx
	 * 
	 */
	protected BigInteger hashValue;

	/**
	 * How many bits does this hash represent. Necessary due to suffix 0 bits
	 * beginning dropped.
	 */
	protected int hashLength;

	/**
	 * Creates a Hash object with the specified hashValue and algorithmId. To allow
	 * save comparison of different hashes they have to be generated by the same
	 * algorithm.
	 * 
	 * @param hashValue   The hash value describing the image
	 * @param hashLength  the actual bit resolution of the hash. The bigInteger
	 *                    truncates leading zero bits resulting in a loss of length
	 *                    information.
	 * @param algorithmId Unique identifier of the algorithm used to create this
	 *                    hash
	 */
	public Hash(BigInteger hashValue, int hashLength, int algorithmId) {
		this.hashValue = hashValue;
		this.algorithmId = algorithmId;
		this.hashLength = hashLength;
	}

	/**
	 * Calculate the hamming distance of 2 hash values. The distance of two hashes
	 * is the difference of the individual bits found in the hash.
	 * <p>
	 * The hamming distance falls within [0-bitResolution]. Lower values indicate
	 * closer similarity while identical images must return a score of 0. On the
	 * flip side score of 0 does not mean images have to be identical!
	 * <p>
	 * 
	 * A longer hash (higher bitResolution) will increase the average hamming
	 * distance returned. While this method allows for the most accurate fine tuning
	 * of the distance {@link #normalizedHammingDistance(Hash)} is hash length
	 * independent.
	 * <p>
	 *
	 * Please be aware that only hashes produced by the same algorithm with the same
	 * settings will return meaningful result and should be compared. This method
	 * will check if the hashes are compatible if no additional check is required
	 * see {@link #hammingDistanceFast(Hash)}
	 * 
	 * @param h The hash to calculate the distance to
	 * @return similarity value ranging between [0 - hash length]
	 */
	public int hammingDistance(Hash h) {
		if (this.algorithmId != h.algorithmId) {
			throw new IllegalArgumentException("Can't compare two hash values created by different algorithms");
		}
		return hammingDistanceFast(h);
	}

	/**
	 * Calculate the hamming distance of 2 hash values. The distance of two hashes
	 * is the difference of the individual bits found in the hash.
	 * <p>
	 * The hamming distance falls within [0-bitResolution]. Lower values indicate
	 * closer similarity while identical images must return a score of 0. On the
	 * flip side score of 0 does not mean images have to be identical!
	 * <p>
	 * 
	 * A longer hash (higher bitResolution) will increase the average hamming
	 * distance returned. While this method allows for the most accurate fine tuning
	 * of the distance {@link #normalizedHammingDistance(Hash)} is hash length
	 * independent.
	 * <p>
	 *
	 * Please be aware that only hashes produced by the same algorithm with the same
	 * settings will return meaningful result and should be compared. This method
	 * will <b>NOT</b> check if the hashes are compatible.
	 * 
	 * @param h The hash to calculate the distance to
	 * @return similarity value ranging between [0 - hash length]
	 * @see #hammingDistance(Hash)
	 */
	public int hammingDistanceFast(Hash h) {
		return this.hashValue.xor(h.getHashValue()).bitCount();
	}

	/**
	 * Calculate the hamming distance of 2 hash values. The distance of two hashes
	 * is the difference of the individual bits found in the hash.
	 * <p>
	 * The hamming distance falls within [0-bitResolution]. Lower values indicate
	 * closer similarity while identical images must return a score of 0. On the
	 * flip side score of 0 does not mean images have to be identical!
	 * <p>
	 * 
	 * A longer hash (higher bitResolution) will increase the average hamming
	 * distance returned. While this method allows for the most accurate fine tuning
	 * of the distance {@link #normalizedHammingDistance(Hash)} is hash length
	 * independent.
	 * <p>
	 *
	 * Please be aware that only hashes produced by the same algorithm with the same
	 * settings will return meaningful result and should be compared. This method
	 * will <b>NOT</b> check if the hashes are compatible.
	 * 
	 * @param bInt A big integer representing a hash
	 * @return similarity value ranging between [0 - hash length]
	 * @see #hammingDistance(Hash)
	 */
	public int hammingDistanceFast(BigInteger bInt) {
		return this.hashValue.xor(bInt).bitCount();
	}

	/**
	 * Calculate the hamming distance of 2 hash values. The distance of two hashes
	 * is the difference of the individual bits found in the hash.
	 * <p>
	 * The normalized hamming distance falls within [0-1]. Lower values indicate
	 * closer similarity while identical images must return a score of 0. On the
	 * flip side score of 0 does not mean images have to be identical!
	 * <p>
	 * 
	 * See {@link #hammingDistance(Hash)} for a non normalized version
	 * 
	 * Please be aware that only hashes produced by the same algorithm with the same
	 * settings will return meaningful result and should be compared. This method
	 * will check if the hashes are compatible if no additional check is required
	 * see {@link #normalizedHammingDistanceFast(Hash)}
	 * 
	 * @param h The hash to calculate the distance to
	 * @return similarity value ranging between [0 - 1]
	 */
	public double normalizedHammingDistance(Hash h) {
		if (this.algorithmId != h.algorithmId) {
			throw new IllegalArgumentException("Can't compare two hash values created by different algorithms");
		}
		// We expect both integers to contain the same bit key lengths!
		// -1 due to the preceding padding bit
		return normalizedHammingDistanceFast(h);
	}

	/**
	 * Calculate the hamming distance of 2 hash values. The distance of two hashes
	 * is the difference of the individual bits found in the hash.
	 * <p>
	 * The normalized hamming distance falls within [0-1]. Lower values indicate
	 * closer similarity while identical images must return a score of 0. On the
	 * flip side score of 0 does not mean images have to be identical!
	 * <p>
	 * 
	 * See {@link #hammingDistance(Hash)} for a non normalized version
	 *
	 * Please be aware that only hashes produced by the same algorithm with the same
	 * settings will return meaningful result and should be compared. This method
	 * will <b>NOT</b> check if the hashes are compatible.
	 * 
	 * @param h The hash to calculate the distance to
	 * @return similarity value ranging between [0 - 1]
	 * @see #hammingDistance(Hash)
	 */
	public double normalizedHammingDistanceFast(Hash h) {
		// We expect both integers to contain the same bit key lengths!
		return hammingDistanceFast(h) / (double) hashLength;
	}

	/**
	 * Check if the bit at the given position is set.
	 * 
	 * @param position of the bit. An index of 0 points to the lowest (rightmost
	 *                 bit)
	 * @return true if the bit is set (1) or false if it's not set (0)
	 * @throws IllegalArgumentException if the supplied index is outside the hash
	 *                                  bound
	 * @since 2.0.0
	 */
	public boolean getBit(int position) {
		Require.inRange(position, 0, this.getBitResolution() - 1, "Bit out of bounds");
		return getBitUnsafe(position);
	}

	/**
	 * Check if the bit at the given position of the hash is set. This method does
	 * not check the bounds of the supplied argument.
	 * 
	 * @param position of the bit. An index of 0 points to the lowest (rightmost
	 *                 bit)
	 * @return true if the bit is set (1). False if it's not set (0) ot the index is
	 *         bigger than the hash length.
	 * @throws ArithmeticException if position is negative
	 * @since 2.0.0
	 */
	public boolean getBitUnsafe(int position) {
		return hashValue.testBit(position);
	}

	/**
	 * Return the algorithm identifier specifying by which algorithm and setting
	 * this hash was created. The id shall remain constant.
	 * 
	 * @return The algorithm id
	 */
	public int getAlgorithmId() {
		return algorithmId;
	}

	/**
	 * @return the base BigInteger holding the hash value
	 */
	public BigInteger getHashValue() {
		return hashValue;
	}

	/**
	 * Creates a visual representation of the hash mapping the hash values to the
	 * section of the rescaled image used to generate the hash assuming default bit
	 * encoding.
	 * 
	 * <p>
	 * Some hash algorithms may chose to construct their hashes in a non default
	 * manner (e.g. {@link com.github.kilianB.hashAlgorithms.DifferenceHash}). In this case
	 * {@link #toImage(int, HashingAlgorithm)} may help to resolve the issue;
	 * 
	 * @param blockSize scaling factor of each pixel in the has. each bit of the
	 *                  hash will be represented to blockSize*blockSize pixels
	 * 
	 * @return A black and white image representing the individual bits of the hash
	 */
	public BufferedImage toImage(int blockSize) {
		Color[] colorArr = new Color[] { Color.WHITE, Color.BLACK };
		int[] colorIndex = new int[hashLength];

		for (int i = 0; i < hashLength; i++) {
			colorIndex[i] = hashValue.testBit(i) ? 1 : 0;
		}
		return toImage(colorIndex, colorArr, blockSize);
	}

	/**
	 * Creates a visual representation of the hash mapping the hash values to the
	 * section of the rescaled image used to generate the hash.
	 * 
	 * <p>
	 * Some hash algorithms may chose to construct their hashes in a non default
	 * manner (e.g. {@link com.github.kilianB.hashAlgorithms.DifferenceHash}).
	 * 
	 * @param blockSize scaling factor of each pixel in the has. each bit of the
	 *                  hash will be represented to blockSize*blockSize pixels
	 * @param hasher    HashAlgorithm which created this hash.
	 * @return A black and white image representing the individual bits of the hash
	 * @since 3.0.0
	 */
	public BufferedImage toImage(int blockSize, HashingAlgorithm hasher) {
		return hasher.createAlgorithmSpecificHash(this).toImage(blockSize);
	}

	/**
	 * Creates a visual representation of the hash mapping the hash values to the
	 * section of the rescaled image used to generate the hash.
	 * 
	 * @param bitColorIndex array mapping each bit of the hash to a color of the
	 *                      color array
	 * @param colors        array to colorize the pixels
	 * @param blockSize     scaling factor of each pixel in the has. each bit of the
	 *                      hash will be represented to blockSize*blockSize pixels
	 * @return A colorized image representing the individual bits of the hash
	 */
	public BufferedImage toImage(int[] bitColorIndex, Color[] colors, int blockSize) {
		int width = (int) Math.sqrt(hashLength);
		int height = width;

		BufferedImage bi = new BufferedImage(blockSize * width, blockSize * height, BufferedImage.TYPE_3BYTE_BGR);

		ColorPixel fp = ColorPixel.create(bi);

		int i = 0;
		for (int w = 0; w < width * blockSize; w = w + blockSize) {
			for (int h = 0; h < height * blockSize; h = h + blockSize) {
				Color c = colors[bitColorIndex[i++]];
				int red = c.getRed();
				int green = c.getGreen();
				int blue = c.getBlue();

				for (int m = 0; m < blockSize; m++) {
					for (int n = 0; n < blockSize; n++) {
						int x = w + m;
						int y = h + n;
						// bi.setRGB(y, x, bit ? black : white);
						// fp.setAverageGrayscale(x, y, gray);
						fp.setRedScalar(x, y, red);
						fp.setGreenScalar(x, y, green);
						fp.setBlueScalar(x, y, blue);
					}
				}
			}
		}
		return bi;
	}

	/**
	 * @return the hash resolution in bits
	 */
	public int getBitResolution() {
		return hashLength;
	}

	/**
	 * Saves this hash to a file for persistent storage. The hash can later be
	 * recovered by calling {@link #fromFile(File)};
	 * 
	 * @param saveLocation the file to save the hash to
	 * @throws IOException If an error occurs during file access
	 * @since 3.0.0
	 */
	public void toFile(File saveLocation) throws IOException {
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(saveLocation))) {
			oos.writeObject(this);
		}
	}

	/**
	 * Reads a hash from a serialization file and returns it. Only hashes can be
	 * read from file that got saved by the same class instance using
	 * {@link #toFile(File)};
	 * 
	 * @param source The file this hash can be read from.
	 * @return a hash object
	 * @throws IOException            If an error occurs during file read
	 * @throws ClassNotFoundException if the class used to serialize this hash can
	 *                                not be found
	 * @since 3.0.0
	 */
	public static Hash fromFile(File source) throws IOException, ClassNotFoundException {
		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(source))) {
			Object o = ois.readObject();
			// Ugly. This is not oop.
			if (o.getClass() == FuzzyHash.class) {
				return FuzzyHash.fromFile(source);
			}
			return (Hash) o;
		}
	}

	/**
	 * Return the byte representation of the big integer with the leading zero byte
	 * stripped if present. The BigInteger class prepends a sign byte if necessary
	 * to indicate the signum of the number. Since our hashes are always positive we
	 * can get rid of it and reduce the space requirement in our db by 1 byte.
	 * 
	 * <p>
	 * To reconstruct the big integer value we can simply prepend a [0x00] byte even
	 * if it wasn't present in the first place. The constructor
	 * {@link java.math.BigInteger#BigInteger(byte[])} will take care of it.
	 * 
	 * @return the byte representation of the big integer without an artificial sign
	 *         byte.
	 */
	public byte[] toByteArray() {
		byte[] bArray = hashValue.toByteArray();

		if (bArray[0] != 0) {
			return bArray;
		} else {
			byte[] bArrayWithoutSign = new byte[bArray.length - 1];
			System.arraycopy(bArray, 1, bArrayWithoutSign, 0, bArray.length - 1);
			return bArrayWithoutSign;
		}

	}

	public String toString() {
		return "Hash: " + StringUtil.fillStringBeginning("0", hashLength, hashValue.toString(2)) + " [algoId: " + algorithmId + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + algorithmId;
		result = prime * result + ((hashValue == null) ? 0 : hashValue.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Hash other = (Hash) obj;
		if (algorithmId != other.getAlgorithmId())
			return false;
		if (hashValue == null) {
			if (other.hashValue != null)
				return false;
		} else if (!hashValue.equals(other.getHashValue()))
			return false;
		return true;
	}

}
