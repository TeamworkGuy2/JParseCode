package twg2.parser.fragment;

import twg2.parser.textFragment.TextFragmentRef;

/**
 * @author TeamworkGuy2
 * @since 2016-4-12
 */
public class CodeToken extends TextFragmentRefToken<CodeTokenType> {

	public CodeToken(CodeTokenType type, TextFragmentRef textFrag, String text) {
		super(type, textFrag, text);
	}

}
