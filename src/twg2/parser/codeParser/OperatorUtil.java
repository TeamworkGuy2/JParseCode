package twg2.parser.codeParser;

import twg2.parser.codeParser.tools.CodeTokenEnumSubSet;

/**
 * @author TeamworkGuy2
 * @since 2016-4-9
 */
public interface OperatorUtil<T_OP extends Operator> {

	public CodeTokenEnumSubSet<T_OP> arithmeticOperators();

	public CodeTokenEnumSubSet<T_OP> assignmentOperators();

	public CodeTokenEnumSubSet<T_OP> bitwiseOperators();

	public CodeTokenEnumSubSet<T_OP> concatOperators();

	public CodeTokenEnumSubSet<T_OP> conditionalOperators();

	public CodeTokenEnumSubSet<T_OP> equalityOperators();

	public CodeTokenEnumSubSet<T_OP> typeCheckOperators();

	public CodeTokenEnumSubSet<T_OP> unaryOperators();

}
