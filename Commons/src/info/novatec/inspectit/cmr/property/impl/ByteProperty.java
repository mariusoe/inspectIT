package info.novatec.inspectit.cmr.property.impl;

import info.novatec.inspectit.cmr.property.SingleProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Property holding byte count values. This property parses {@link String} values that represent the
 * byte count and vice versa.
 * 
 * @author Ivan Senic
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "byte-property")
public class ByteProperty extends SingleProperty<Long> {

	/**
	 * Used value in {@link String}.
	 */
	@XmlAttribute(name = "used-value")
	private String usedValue;

	/**
	 * Default value in {@link String}.
	 */
	@XmlAttribute(name = "default-value", required = true)
	private String defaultValue;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Long getDefaultValue() {
		return fromString(defaultValue);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void setDefaultValue(Long defaultValue) {
		this.defaultValue = toString(defaultValue.longValue());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Long getUsedValue() {
		if (null != usedValue) {
			return fromString(usedValue);
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void setUsedValue(Long usedValue) {
		this.usedValue = toString(usedValue.longValue());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Long parseLiteral(String literal) {
		return fromString(literal);
	}

	/**
	 * Returns the bytes number from human readable string.
	 * 
	 * @param str
	 *            Human readable string.
	 * @return Bytes number.
	 */
	private Long fromString(String str) {
		if (null == str) {
			return null;
		}

		int exp = 0;
		int length = str.length();
		Character c = str.charAt(length - 2);
		switch (c) {
		case 'E':
			exp++;
		case 'P':
			exp++;
		case 'T':
			exp++;
		case 'G':
			exp++;
		case 'M':
			exp++;
		case 'K':
			exp++;
		default:
			break;
		}

		String number = (exp == 0) ? str.substring(0, length - 1) : str.substring(0, length - 2);
		try {
			return Long.valueOf((long) (Long.parseLong(number) * Math.pow(1024, exp)));
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * Returns the human readable bytes number.
	 * 
	 * @param bytes
	 *            Bytes to transform.
	 * @return Human readable string.
	 */
	private String toString(long bytes) {
		int unit = 1024;
		if (bytes < unit) {
			return bytes + " B";
		} else {
			int exp = (int) (Math.log(bytes) / Math.log(unit));
			String pre = String.valueOf("KMGTPE".charAt(exp - 1));
			return String.format("%.1f%sB", bytes / Math.pow(unit, exp), pre);
		}
	}

}
