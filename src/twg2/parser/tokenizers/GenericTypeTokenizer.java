package twg2.parser.tokenizers;

import java.util.Arrays;
import java.util.function.Supplier;

import lombok.val;
import twg2.collections.primitiveCollections.CharArrayList;
import twg2.parser.Inclusion;
import twg2.parser.condition.text.CharParserMatchable;
import twg2.parser.text.CharConditionPipe;
import twg2.parser.text.CharConditions;
import twg2.parser.text.StringConditions;

/**
 * @author TeamworkGuy2
 * @since 2015-11-28
 */
public class GenericTypeTokenizer {

	/**
	 * @param genericTypeDepth the nesting depth of generic type statements that the returned parser can support.
	 * i.e. {@code A<B>} would require depth=1, {@code A<B<C>>} would require depth=2
	 */
	public static CharParserMatchable createGenericTypeTokenizer(int genericTypeDepth, Supplier<CharParserMatchable> singleIdentifierParserConstructor) {
		return _createGenericTypeTokenizer(genericTypeDepth, singleIdentifierParserConstructor);
	}


	public static CharParserMatchable _createGenericTypeTokenizer(int recursionDepth, Supplier<CharParserMatchable> singleIdentifierParserConstructor) {
		// the condition that parses identifiers nested inside the generic type definition
		val nestedGenericTypeIdentifierCond = recursionDepth > 1 ? _createGenericTypeTokenizer(recursionDepth - 1, singleIdentifierParserConstructor) : singleIdentifierParserConstructor.get();

		val requiredParser = Arrays.asList(singleIdentifierParserConstructor.get());
		// TODO only matches generic types in the format '<a, b>', allow whitespace between '<'/'>' and after ','
		val optionalParser = Arrays.asList(CharConditionPipe.createPipeOptionalSuffixesAny("generic type and array dimensions", Arrays.asList(
			CharConditionPipe.createPipeAllRequired("generic type signature", Arrays.asList(
				new CharConditions.Literal("<", CharArrayList.of('<'), Inclusion.INCLUDE),
				CharConditionPipe.createPipeRepeatableSeparator("generic type params",
					Arrays.asList(nestedGenericTypeIdentifierCond),
					Arrays.asList(new StringConditions.Literal("separator", new String[] { ", " }, Inclusion.INCLUDE))
				),
				new CharConditions.Literal(">", CharArrayList.of('>'), Inclusion.INCLUDE)
			))), Arrays.asList(
				CharConditionPipe.createPipeRepeatableSeparator("array dimensions '[]'...", Arrays.asList(new StringConditions.Literal("array dimension '[]'", new String[] { "[]" }, Inclusion.INCLUDE)), null)
			)
		));

		return CharConditionPipe.createPipeOptionalSuffix("type parser", requiredParser, optionalParser);
	}

}