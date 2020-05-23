package twg2.parser.tokenizers;

import java.util.Arrays;
import java.util.function.Supplier;

import twg2.collections.primitiveCollections.CharArrayList;
import twg2.parser.condition.text.CharParserMatchable;
import twg2.text.tokenizer.CharConditionPipe;
import twg2.text.tokenizer.CharConditions;
import twg2.text.tokenizer.Inclusion;
import twg2.text.tokenizer.StringConditions;

/** Static methods for creating generic type tokenizers that support nesting (i.e. for tokenizing '{@code HashMap<Entry<String, Integer>, List<String>>}').
 * @author TeamworkGuy2
 * @since 2015-11-28
 */
public class GenericTypeTokenizer {

	private GenericTypeTokenizer() { throw new AssertionError("cannot instantiate static class GenericTypeTokenizer"); }


	/** Create a tokenizer for nested generic types up to a specific nesting depth.
	 * @param maxGenericTypeDepth the nesting depth of generic type statements that the returned tokenizer can support.
	 * i.e. '{@code A<B>}' would require depth=1, '{@code A<B<C>>}' would require depth=2
	 * @param singleIdentifierParserConstructor supplies the tokenizer which can tokenize individual text tokens before and inside a generic type. i.e. in '{@code A<B>}' the text tokens are 'A' and 'B'.
	 */
	public static CharParserMatchable createGenericTypeTokenizer(int maxGenericTypeDepth, Supplier<CharParserMatchable> singleIdentifierParserConstructor) {
		return _createGenericTypeTokenizer(maxGenericTypeDepth, singleIdentifierParserConstructor);
	}


	private static CharParserMatchable _createGenericTypeTokenizer(int recursionDepth, Supplier<CharParserMatchable> singleIdentifierParserConstructor) {
		// the condition that parses identifiers nested inside the generic type definition
		var nestedGenericTypeIdentifierCond = recursionDepth > 1 ? _createGenericTypeTokenizer(recursionDepth - 1, singleIdentifierParserConstructor) : singleIdentifierParserConstructor.get();

		var typeIdentifierParser = Arrays.asList(singleIdentifierParserConstructor.get());
		// TODO only matches generic types in the format '<a, b>', allow whitespace between '<'/'>' and after ','
		var genericParamsParser = CharConditionPipe.createPipeAllRequired("generic type signature",
			new CharConditions.Literal("<", CharArrayList.of('<'), Inclusion.INCLUDE),
			CharConditionPipe.createPipeRepeatableSeparator("generic type params",
				Arrays.asList(nestedGenericTypeIdentifierCond),
				Arrays.asList(new StringConditions.Literal("separator", new String[] { ", " }, Inclusion.INCLUDE))
			),
			new CharConditions.Literal(">", CharArrayList.of('>'), Inclusion.INCLUDE)
		);
		var arrayDimensionsParser = CharConditionPipe.createPipeRepeatableSeparator("array dimensions '[]'...", Arrays.asList(new StringConditions.Literal("array dimension '[]'", new String[] { "[]" }, Inclusion.INCLUDE)), null);

		return CharConditionPipe.createPipeOptionalSuffix("type parser", typeIdentifierParser, Arrays.asList(genericParamsParser, arrayDimensionsParser));
	}

}
