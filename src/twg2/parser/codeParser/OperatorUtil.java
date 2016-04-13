package twg2.parser.codeParser;

import twg2.parser.baseAst.Operator;
import twg2.parser.codeParser.tools.CodeFragmentEnumSubSet;

/**
 * @author TeamworkGuy2
 * @since 2016-4-9
 */
public interface OperatorUtil<T_OP extends Operator> {

	public CodeFragmentEnumSubSet<T_OP> arithmeticOperators();

	public CodeFragmentEnumSubSet<T_OP> assignmentOperators();

	public CodeFragmentEnumSubSet<T_OP> bitwiseOperators();

	public CodeFragmentEnumSubSet<T_OP> concatOperators();

	public CodeFragmentEnumSubSet<T_OP> conditionalOperators();

	public CodeFragmentEnumSubSet<T_OP> equalityOperators();

	public CodeFragmentEnumSubSet<T_OP> typeCheckOperators();

	public CodeFragmentEnumSubSet<T_OP> unaryOperators();

}
