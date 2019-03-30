package twg2.parser.codeParser.csharp;

import lombok.Getter;
import lombok.experimental.Accessors;
import twg2.arrays.ArrayUtil;
import twg2.collections.primitiveCollections.IntArrayList;
import twg2.collections.primitiveCollections.IntListReadOnly;
import twg2.parser.codeParser.Operator;
import twg2.parser.codeParser.OperatorUtil;
import twg2.parser.codeParser.tools.CodeTokenEnumSubSet;
import twg2.parser.codeParser.tools.EnumSplitter;
import twg2.parser.fragment.CodeTokenType;

/** C# operators enum (i.e. '+', '==', '&&')
 * @author TeamworkGuy2
 * @since 2016-4-9
 */
public enum CsOperator implements Operator {
	// NOTE: these must be in alphabetical order for Inst array binary searches to work
	ADD("+", Flag.ARITHMETIC | Flag.CONCAT | Flag.UNARY, list(1, 2)),
	AND_ASSIGNMENT("&=", Flag.ASSIGNMENT, list(2)),
	AS("as", Flag.TYPE_CHECK, list(2)),
	ASSIGNMENT("=", Flag.ASSIGNMENT, list(2)),
	BITWISE_COMPLEMENT("~", Flag.BITWISE, list(1)),
	BITWISE_OR("|", Flag.BITWISE, list(2)),
	BITWISE_AND("&", Flag.BITWISE, list(2)),
	BITWISE_XOR("^", Flag.BITWISE, list(2)),
	// NOTE: this implementation of ternary operators is a little weird
	CONDITIONAL_A("?", Flag.CONDITIONAL, list(2)),
	CONDITIONAL_B(":", Flag.CONDITIONAL, list(2)),
	DECREMENT("--", Flag.UNARY, list(1)),
	DECREMENT_ASSIGNMENT("-=", Flag.ASSIGNMENT, list(2)),
	DIVISION("/", Flag.ARITHMETIC, list(2)),
	DIVISION_ASSIGNMENT("/=", Flag.ASSIGNMENT, list(2)),
	EQUALITY("==", Flag.EQUALITY, list(2)),
	GREATER_THAN(">", Flag.EQUALITY, list(2)),
	GREATER_THAN_OR_EQUAL(">=", Flag.EQUALITY, list(2)),
	INCREMENT("++", Flag.UNARY, list(1)),
	INCREMENT_ASSIGNMENT("+=", Flag.ASSIGNMENT, list(2)),
	IS("is", Flag.TYPE_CHECK, list(2)),
	LEFT_SHIFT_ASSIGNMENT("<<=", Flag.ASSIGNMENT, list(2)),
	LESS_THAN("<", Flag.EQUALITY, list(2)),
	LESS_THAN_OR_EQUAL("<=", Flag.EQUALITY, list(2)),
	LOGICAL_AND("&&", Flag.CONDITIONAL, list(2)),
	LOGICAL_NEGATION("!", Flag.UNARY, list(1)),
	LOGICAL_OR("||", Flag.CONDITIONAL, list(2)),
	MODULUS("%", Flag.ARITHMETIC, list(2)),
	MODULUS_ASSIGNMENT("%=", Flag.ASSIGNMENT, list(2)),
	MULTIPLICATION("*", Flag.ARITHMETIC, list(2)),
	MULTIPLICATION_ASSIGNMENT("*=", Flag.ASSIGNMENT, list(2)),
	NOT_EQUAL("!=", Flag.EQUALITY, list(2)),
	NULL_COALESCING("??", Flag.CONDITIONAL, list(2)),
	OR_ASSIGNMENT("|=", Flag.ASSIGNMENT, list(2)),
	RIGHT_SHIFT_ASSIGNMENT(">>=", Flag.ASSIGNMENT, list(2)),
	SHIFT_LEFT("<<", Flag.BITWISE, list(2)),
	SHIFT_RIGHT(">>", Flag.BITWISE, list(2)),
	SUBTRACT("-", Flag.ARITHMETIC | Flag.UNARY, list(1, 2)),
	XOR_ASSIGNMENT("^=", Flag.ASSIGNMENT, list(2));


	public static final Inst check = new CsOperator.Inst();

	public final String srcSymbol;
	private final IntArrayList operandCounts;
	public final boolean isArithmetic;
	public final boolean isAssignment;
	public final boolean isBitwise;
	public final boolean isConcat;
	public final boolean isConditional;
	public final boolean isEquality;
	public final boolean isTypeCheck;
	public final boolean isUnary;


	CsOperator(String symbol, IntArrayList operandCounts) {
		this.srcSymbol = symbol;
		this.operandCounts = operandCounts;
		this.isArithmetic = false;
		this.isAssignment = false;
		this.isBitwise = false;
		this.isConcat = false;
		this.isConditional = false;
		this.isEquality = false;
		this.isTypeCheck = false;
		this.isUnary = false;
	}


	CsOperator(String symbol, int typeFlags, IntArrayList operandCounts) {
		this.srcSymbol = symbol;
		this.operandCounts = operandCounts;
		this.isArithmetic = (typeFlags & Flag.ARITHMETIC) == Flag.ARITHMETIC;
		this.isAssignment = (typeFlags & Flag.ASSIGNMENT) == Flag.ASSIGNMENT;
		this.isBitwise = (typeFlags & Flag.BITWISE) == Flag.BITWISE;
		this.isConcat = (typeFlags & Flag.CONCAT) == Flag.CONCAT;
		this.isConditional = (typeFlags & Flag.CONDITIONAL) == Flag.CONDITIONAL;
		this.isEquality = (typeFlags & Flag.EQUALITY) == Flag.EQUALITY;
		this.isTypeCheck = (typeFlags & Flag.TYPE_CHECK) == Flag.TYPE_CHECK;
		this.isUnary = (typeFlags & Flag.UNARY) == Flag.UNARY;
	}


	@Override
	public String toSrc() {
		return srcSymbol;
	}


	@Override
	public IntListReadOnly operandCount() {
		return operandCounts;
	}


	private static final IntArrayList list(int... vals) {
		return IntArrayList.of(vals);
	}




	/**
	 * @author TeamworkGuy2
	 * @since 2016-4-9
	 */
	@Accessors(fluent = true)
	public static class Inst implements OperatorUtil<CsOperator> {
		public final String[] keywords;
		private final CsOperator[] values;
		@Getter private final CodeTokenEnumSubSet<CsOperator> arithmeticOperators;
		@Getter private final CodeTokenEnumSubSet<CsOperator> assignmentOperators;
		@Getter private final CodeTokenEnumSubSet<CsOperator> bitwiseOperators;
		@Getter private final CodeTokenEnumSubSet<CsOperator> concatOperators;
		@Getter private final CodeTokenEnumSubSet<CsOperator> conditionalOperators;
		@Getter private final CodeTokenEnumSubSet<CsOperator> equalityOperators;
		@Getter private final CodeTokenEnumSubSet<CsOperator> typeCheckOperators;
		@Getter private final CodeTokenEnumSubSet<CsOperator> unaryOperators;


		{
			this.values = CsOperator.values();
			var enumData = EnumSplitter.split(this.values, (e) -> e.srcSymbol,
				(e) -> e.isArithmetic,
				(e) -> e.isAssignment,
				(e) -> e.isBitwise,
				(e) -> e.isConcat,
				(e) -> e.isConditional,
				(e) -> e.isEquality,
				(e) -> e.isTypeCheck,
				(e) -> e.isUnary
			);
			this.keywords = enumData.getKey();

			int i = 0;
			var enumSets = ArrayUtil.map(enumData.getValue(), CodeTokenEnumSubSet.class, (es) -> new CodeTokenEnumSubSet<>(CodeTokenType.OPERATOR, es));
			arithmeticOperators = enumSets[i++];
			assignmentOperators = enumSets[i++];
			bitwiseOperators = enumSets[i++];
			concatOperators = enumSets[i++];
			conditionalOperators = enumSets[i++];
			equalityOperators = enumSets[i++];
			typeCheckOperators = enumSets[i++];
			unaryOperators = enumSets[i++];
		}

	}




	/**
	 * @author TeamworkGuy2
	 * @since 2016-4-9
	 */
	static class Flag {
		static final int ARITHMETIC = 1;
		@SuppressWarnings("hiding")
		static final int ASSIGNMENT = 2;
		static final int BITWISE = 4;
		static final int CONCAT = 8;
		static final int CONDITIONAL = 16;
		@SuppressWarnings("hiding")
		static final int EQUALITY = 32;
		static final int TYPE_CHECK = 64;
		static final int UNARY = 128;
	}

}
