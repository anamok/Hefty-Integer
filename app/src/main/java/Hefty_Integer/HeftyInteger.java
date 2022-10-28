/**
 * HeftyInteger Implementation for CS1501 Project 5
 * @author	Anastasia Mokhon
 */
package Hefty_Integer;

import java.util.Random;

public class HeftyInteger {
	private final byte[] ZERO = {(byte) 0};
	private final byte[] ONE = {(byte) 1};
	private byte[] val;

	/**
	 * Construct the HeftyInteger from a given byte array
	 * @param b the byte array that this HeftyInteger should represent
	 */
	public HeftyInteger(byte[] b) {
		val = b;
	}

	/**
	 * Return this HeftyInteger's val
	 * @return val
	 */
	public byte[] getVal() {
		return val;
	}

	/**
	 * Return the number of bytes in val
	 * @return length of the val byte array
	 */
	public int length() {
		return val.length;
	}

	/**
	 * Add a new byte as the most significant in this
	 * @param extension the byte to place as most significant
	 */
	public void extend(byte extension) {
		byte[] newv = new byte[val.length + 1];
		newv[0] = extension;
		for (int i = 0; i < val.length; i++) {
			newv[i + 1] = val[i];
		}
		val = newv;
	}

	/**
	 * If this is negative, most significant bit will be 1 meaning most
	 * significant byte will be a negative signed number
	 * @return true if this is negative, false if positive
	 */
	public boolean isNegative() {
		return (val[0] < 0);
	}

	/**
	 * Computes the sum of this and other
	 * @param other the other HeftyInteger to sum with this
	 */
	public HeftyInteger add(HeftyInteger other) {
		byte[] a, b;
		// If operands are of different sizes, put larger first ...
		if (val.length < other.length()) {
			a = other.getVal();
			b = val;
		}
		else {
			a = val;
			b = other.getVal();
		}

		// ... and normalize size for convenience
		if (b.length < a.length) {
			int diff = a.length - b.length;

			byte pad = (byte) 0;
			if (b[0] < 0) {
				pad = (byte) 0xFF;
			}

			byte[] newb = new byte[a.length];
			for (int i = 0; i < diff; i++) {
				newb[i] = pad;
			}

			for (int i = 0; i < b.length; i++) {
				newb[i + diff] = b[i];
			}

			b = newb;
		}

		// Actually compute the add
		int carry = 0;
		byte[] res = new byte[a.length];
		for (int i = a.length - 1; i >= 0; i--) {
			// Be sure to bitmask so that cast of negative bytes does not
			//  introduce spurious 1 bits into result of cast
			carry = ((int) a[i] & 0xFF) + ((int) b[i] & 0xFF) + carry;

			// Assign to next byte
			res[i] = (byte) (carry & 0xFF);

			// Carry remainder over to next byte (always want to shift in 0s)
			carry = carry >>> 8;
		}

		HeftyInteger res_li = new HeftyInteger(res);

		// If both operands are positive, magnitude could increase as a result
		//  of addition
		if (!this.isNegative() && !other.isNegative()) {
			// If we have either a leftover carry value or we used the last
			//  bit in the most significant byte, we need to extend the result
			if (res_li.isNegative()) {
				res_li.extend((byte) carry);
			}
		}
		// Magnitude could also increase if both operands are negative
		else if (this.isNegative() && other.isNegative()) {
			if (!res_li.isNegative()) {
				res_li.extend((byte) 0xFF);
			}
		}

		// Note that result will always be the same size as biggest input
		//  (e.g., -127 + 128 will use 2 bytes to store the result value 1)
		return res_li;
	}

	/**
	 * Negate val using two's complement representation
	 * @return negation of this
	 */
	public HeftyInteger negate() {
		byte[] neg = new byte[val.length];
		int offset = 0;

		// Check to ensure we can represent negation in same length
		//  (e.g., -128 can be represented in 8 bits using two's
		//  complement, +128 requires 9)
		if (val[0] == (byte) 0x80) { // 0x80 is 10000000
			boolean needs_ex = true;
			for (int i = 1; i < val.length; i++) {
				if (val[i] != (byte) 0) {
					needs_ex = false;
					break;
				}
			}
			// if first byte is 0x80 and all others are 0, must extend
			if (needs_ex) {
				neg = new byte[val.length + 1];
				neg[0] = (byte) 0;
				offset = 1;
			}
		}

		// flip all bits
		for (int i  = 0; i < val.length; i++) {
			neg[i + offset] = (byte) ~val[i];
		}

		HeftyInteger neg_li = new HeftyInteger(neg);

		// add 1 to complete two's complement negation
		return neg_li.add(new HeftyInteger(ONE));
	}

	/**
	 * Implement subtraction as simply negation and addition
	 * @param other HeftyInteger to subtract from this
	 * @return difference of this and other
	 */
	public HeftyInteger subtract(HeftyInteger other) {
		return this.add(other.negate());
	}

	public boolean isZero() {
		for (byte b : val) {
			if (b != 0) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Compute the product of this and other
	 * @param other HeftyInteger to multiply by this
	 * @return product of this and other
	 */
	
	public  HeftyInteger multiply(HeftyInteger other)
	{	
		if (this.isZero() || other.isZero()) {
			return new HeftyInteger(ZERO);
		}

		// initializing the resulting array and setting every byte to 0
		byte[] result = new byte[this.length() + other.length()];
		for (int k = 0; k < result.length; k++) 
		{
			result[k] = 0;
		}

		// checking if any of the numbers is negative and if the result will be negative
		boolean negResult = false;
		HeftyInteger a = this;
		HeftyInteger b = other;
		if (a.isNegative() && !b.isNegative()) {
			a = a.negate();
			negResult = true;
		}
		else if (!a.isNegative() && b.isNegative()) {
			b = b.negate();
			negResult = true;
		}
		else if (a.isNegative() && b.isNegative()) {
			a = a.negate();
			b = b.negate();
		}

		byte[] x = a.getVal();
		byte[] y = b.getVal();

		byte carry = 0;
		int multBy;
		int sum = 0;

		// initializing counters for the loops
		// outer loop cycles through bytes of x from left to right
		// inner loop cycles through bytes of y from right to left
		int j = y.length - 1;
		int i = 0;
		do 
		{
			multBy = y[j] & 0xFF;
			// if the current multBy is not 0, multiply
			if (multBy != 0x00)
			{
				i = x.length - 1;
				carry = 0x00;
				do
				{
					sum = ((x[i] & 0xFF) * multBy) + (result[i + j + 1] & 0xFF) + (carry & 0xFF);
					result[i + j + 1] = (byte)(sum & 0xFF);
					carry = (byte) ((int) sum >> 8);
					i--;
				} 
				while (i >= 0);

				result[j] = carry;
				j--;
			}
			// if the current multBy is 0, no need to multiply, just set to 0
			else
			{
				result[j] = 0x00;
				j--;
			}
		} while (j >= 0);

		HeftyInteger res = new HeftyInteger(result);
		if (negResult) {
			res = res.negate();
		}

		return res;
	}

	/*
	 * Run the extended Euclidean algorithm on this and other
	 * @param other another HeftyInteger
	 * @return an array structured as follows:
	 *   0:  the GCD of this and other
	 *   1:  a valid x value
	 *   2:  a valid y value
	 * such that this * x + other * y == GCD in index 0
	 */
	 public HeftyInteger[] XGCD(HeftyInteger other) {
	 	HeftyInteger[] result = new HeftyInteger[3];
		// base case, other is 0
		if (other.isZero()) {
			result[0] = new HeftyInteger(this.getVal());
			result[1] = new HeftyInteger(ONE);
			result[2] = new HeftyInteger(ZERO);
			return result;
		}
		// base case, this is 0
		if (this.isZero()) {
			result[0] = new HeftyInteger(other.getVal());
			result[1] = new HeftyInteger(ONE);
			result[2] = new HeftyInteger(ZERO); 
			return result;
		}

		HeftyInteger a = this;
		HeftyInteger b = other;
		if (a.isNegative() && !b.isNegative()) {
			a = a.negate();
		}
		else if (!a.isNegative() && b.isNegative()) {
			b = b.negate();
		}
		else if (a.isNegative() && b.isNegative()) {
			a = a.negate();
			b = b.negate();
		}

		// running the extended Euclidean algorithm
		HeftyInteger[] values = b.XGCD(a.mod(b));
		HeftyInteger d = values[0];
		HeftyInteger x = values[2];
		HeftyInteger div = a.divide(b);
		HeftyInteger y = values[1].subtract(div.multiply(x));
		result[0] = d;
		result[1] = x;
		result[2] = y;
		return result;
	}

	// helper method which computes this mod other
	private HeftyInteger mod(HeftyInteger other) {
		return this.subtract(other.multiply(this.divide(other)));
	}

	// helper method which shifts the value of this to the left by 1
	private HeftyInteger leftShift() {
		byte[] result;
		// checking if the result array needs to be extended
		if ((val[0] & 0xC0) == 0x40) {
			result = new byte[val.length + 1];
		}
		else {
			result = new byte[val.length];
		}

		// shift the array to the left one value at a time
		int prev = 0;
		int mostSigBit;
		for (int i = 1; i <= val.length; i++) {
			mostSigBit = (val[val.length - i] & 0x80) >> 7;
			result[result.length - i] = (byte) (val[val.length - i] << 1);
			result[result.length - i] |= prev;
			prev = mostSigBit;
		}

		return new HeftyInteger(result);
	}

	// helper method which shifts the value of this to the right by 1
	private HeftyInteger rightShift() {
		byte[] result;
		// checking if the result array needs to be shorter
		int counter = 0;
		if (val[0] == 0 && (val[1] & 0x80) == 0x80) {
			result = new byte[val.length - 1];
			counter = 1;
		} 
		else {
			result = new byte[val.length];
		}

		int mostSigBit;
		int prev = 0;
		if(this.isNegative())
			prev = 1;

		// shift the array to the right one value at a time
		for(int i = 0; i < result.length; i++) {
			mostSigBit = val[counter] & 0x01;
			result[i] = (byte) ((val[counter] >> 1) & 0x7F);
			result[i] |= prev << 7;
			prev = mostSigBit;
			counter++;
		}

		return new HeftyInteger(result);
	}

	// helper method which divides this by other
	public HeftyInteger divide(HeftyInteger other) {
		HeftyInteger quotient = new HeftyInteger(ZERO);
		HeftyInteger dividend = this;
		HeftyInteger divisor = other;

		// checking if any of the numbers is negative and if the result will be negative
		boolean negResult = false;
		if (dividend.isNegative() && !divisor.isNegative()) {
			dividend = dividend.negate();
			negResult = true;
		}
		else if (!dividend.isNegative() && divisor.isNegative()) {
			divisor = divisor.negate();
			negResult = true;
		}
		else if (dividend.isNegative() && divisor.isNegative()) {
			dividend = dividend.negate();
			divisor = divisor.negate();
		}

		// grade school division algorithm
		int count = 0;
		while (!dividend.subtract(divisor).isNegative()) {
			divisor = divisor.leftShift();
			count++;
		}

		divisor = divisor.rightShift();
		for (int i = count; i > 0; i--) {
			quotient = quotient.leftShift();
			if (!dividend.subtract(divisor).isNegative()) {
				dividend = dividend.subtract(divisor);
				quotient = quotient.add(new HeftyInteger(ONE));
			}
			divisor = divisor.rightShift();
		}

		if (negResult) {
			return quotient.negate();
		}
		else {
			return quotient;
		}
	}
}