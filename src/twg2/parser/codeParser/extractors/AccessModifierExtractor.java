package twg2.parser.codeParser.extractors;

import java.util.ArrayList;
import java.util.List;

import twg2.parser.codeParser.Keyword;
import twg2.parser.codeParser.KeywordUtil;
import twg2.parser.fragment.CodeToken;
import twg2.streams.EnhancedListBuilderIterator;
import twg2.treeLike.simpleTree.SimpleTree;

/**
 * @author TeamworkGuy2
 * @since 2016-2-19
 */
public class AccessModifierExtractor {

	public static Keyword parseAccessModifier(KeywordUtil<? extends Keyword> keywordUtil, SimpleTree<CodeToken> node) {
		if(node == null) { return null; }
		return keywordUtil.classModifiers().parse(node.getData());
	}


	/** Read backward through any available access modifiers (i.e. 'abstract', 'public', 'static', ...).
	 * Returns the iterator where {@code next()} would return the first access modifier element.
	 * @return access modifiers read backward from the iterator's current {@code previous()} value
	 */
	public static List<String> readAccessModifiers(KeywordUtil<? extends Keyword> keywordUtil, EnhancedListBuilderIterator<SimpleTree<CodeToken>> iter) {
		int prevCount = 0;
		var accessModifiers = new ArrayList<String>();
		SimpleTree<CodeToken> child = iter.hasPrevious() ? iter.previous() : null;

		while(child != null && keywordUtil.classModifiers().is(child.getData())) {
			accessModifiers.add(0, child.getData().getText());
			child = iter.hasPrevious() ? iter.previous() : null;
			if(iter.hasPrevious()) { prevCount++; }
		}

		// move to next since the while loop doesn't use the last value
		if(prevCount > 0) {
			iter.next();
		}

		return accessModifiers;
	}

}
