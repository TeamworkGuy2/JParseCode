package twg2.parser.codeParser.parsers;

import java.util.Arrays;
import java.util.function.Supplier;

import lombok.val;
import twg2.collections.primitiveCollections.CharArrayList;
import twg2.parser.Inclusion;
import twg2.parser.text.CharConditionPipe;
import twg2.parser.text.CharConditions;
import twg2.parser.text.CharParserCondition;
import twg2.parser.text.StringConditions;

/**
 * @author TeamworkGuy2
 * @since 2015-11-28
 */
public class GenericTypeParser {

	/**
	 * @param genericTypeDepth the nesting depth of generic type statements that the returned parser can support.
	 * i.e. {@code A<B>} would require depth=1, {@code A<B<C>>} would require depth=2
	 */
	public static CharParserCondition createGenericTypeStatementCondition(int genericTypeDepth, Supplier<CharParserCondition> singleIdentifierParserConstructor) {
		return _createGenericTypeStatementCondition(genericTypeDepth, singleIdentifierParserConstructor);
	}


	public static CharParserCondition _createGenericTypeStatementCondition(int recursionDepth, Supplier<CharParserCondition> singleIdentifierParserConstructor) {
		// the condition that parses identifiers nested inside the generic type definition
		val nestedGenericTypeIdentifierCond = recursionDepth > 1 ? _createGenericTypeStatementCondition(recursionDepth - 1, singleIdentifierParserConstructor) : singleIdentifierParserConstructor.get();

		val requiredParser = Arrays.asList(singleIdentifierParserConstructor.get());
		// TODO only matches generic types in the format '<a, b>', allow whitespace between '<'/'>' and after ','
		val optionalParser = Arrays.asList(CharConditionPipe.createPipeAllRequired("generic type signature", Arrays.asList(
			new CharConditions.CharLiteralFilter("<", CharArrayList.of('<'), Inclusion.INCLUDE),
			CharConditionPipe.createPipeRepeatableSeparator("generic type params",
				Arrays.asList(nestedGenericTypeIdentifierCond),
				Arrays.asList(new StringConditions.StringLiteralFilter("separator", new String[] { ", " }, Inclusion.INCLUDE))
			),
			new CharConditions.CharLiteralFilter(">", CharArrayList.of('>'), Inclusion.INCLUDE)
		)));
		return CharConditionPipe.createPipeOptionalSuffix("type parser", requiredParser, optionalParser);
	}

}
