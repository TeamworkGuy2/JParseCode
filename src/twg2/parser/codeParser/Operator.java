package twg2.parser.codeParser;

import twg2.collections.primitiveCollections.IntListReadOnly;

/**
 * @author TeamworkGuy2
 * @since 2016-4-13
 */
public interface Operator {

	public String toSrc();

	public IntListReadOnly operandCount();

}
