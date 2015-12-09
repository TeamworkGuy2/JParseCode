package codeParser.parsers;

import java.util.Arrays;
import java.util.function.Supplier;

import lombok.val;
import parser.condition.CharConditions;
import parser.condition.ConditionPipeFilter;
import parser.condition.ParserCondition;
import parser.condition.StringConditions;

/**
 * @author TeamworkGuy2
 * @since 2015-11-28
 */
public class GenericTypeParser {

	/**
	 * @param genericTypeDepth the nesting depth of generic type statements that the returned parser can support.
	 * i.e. {@code A<B>} would require depth=1, {@code A<B<C>>} would require depth=2
	 */
	public static ParserCondition createGenericTypeStatementCondition(int genericTypeDepth, Supplier<ParserCondition> singleIdentifierParserConstructor) {
		return _createGenericTypeStatementCondition(genericTypeDepth, singleIdentifierParserConstructor);
	}


	public static ParserCondition _createGenericTypeStatementCondition(int recursionDepth, Supplier<ParserCondition> singleIdentifierParserConstructor) {
		// the condition that parses identifiers nested inside the generic type definition
		val nestedGenericTypeIdentifierCond = recursionDepth > 1 ? _createGenericTypeStatementCondition(recursionDepth - 1, singleIdentifierParserConstructor) : singleIdentifierParserConstructor.get();

		val requiredParser = Arrays.asList(singleIdentifierParserConstructor.get());
		// TODO only matches generic types in the format '<a, b>', allow whitespace between '<'/'>' and after ','
		val optionalParser = Arrays.asList(ConditionPipeFilter.createPipeAllRequired(Arrays.asList(
			CharConditions.charLiteralFactory().create('<'),
			ConditionPipeFilter.createPipeRepeatableSeparator(
				Arrays.asList(nestedGenericTypeIdentifierCond),
				Arrays.asList(StringConditions.stringLiteralFactory().create(", "))
			),
			CharConditions.charLiteralFactory().create('>')
		)));
		return ConditionPipeFilter.createPipeOptionalSuffix(requiredParser, optionalParser);
	}

}
