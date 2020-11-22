package twg2.parser.tokenizers;

import twg2.text.tokenizer.CharParserFactory;
import twg2.text.tokenizer.Inclusion;
import twg2.text.tokenizer.StringParserBuilder;

/**
 * @author TeamworkGuy2
 * @since 2015-11-24
 */
public class CodeBlockTokenizer {

	private CodeBlockTokenizer() { throw new AssertionError("cannot instantiate static class CodeBlockTokenizer"); }


	public static CharParserFactory createBlockTokenizer(char... blockStarts) {
		StringParserBuilder blockParser = new StringParserBuilder("block");

		for(char blockStart : blockStarts) {
			if(blockStart == '{') {
				blockParser.addStartEndMarkers("block { }", '{', '}', Inclusion.INCLUDE);
			}
			else if(blockStart == '(') {
				blockParser.addStartEndMarkers("block ( )", '(', ')', Inclusion.INCLUDE);
			}
			else if(blockStart == '[') {
				blockParser.addStartEndMarkers("block [ ]", '[', ']', Inclusion.INCLUDE);
			}
			else if(blockStart == '<') {
				blockParser.addStartEndMarkers("block < >", '<', '>', Inclusion.INCLUDE);
			}
			else {
				throw new IllegalArgumentException("unknown block type '" + blockStart + "'");
			}
		}

		return blockParser
			.isCompound(true)
			.build();
	}

}
