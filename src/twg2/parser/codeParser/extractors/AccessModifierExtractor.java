package twg2.parser.codeParser.extractors;

import java.util.ArrayList;
import java.util.List;

import twg2.parser.baseAst.AccessModifier;
import twg2.parser.codeParser.KeywordUtil;
import twg2.parser.documentParser.CodeFragment;
import twg2.streams.EnhancedListBuilderIterator;
import twg2.treeLike.simpleTree.SimpleTree;

/**
 * @author TeamworkGuy2
 * @since 2016-2-19
 */
public class AccessModifierExtractor {

	public static AccessModifier readAccessModifier(KeywordUtil<? extends AccessModifier> keyword, SimpleTree<CodeFragment> node) {
		if(node == null) { return null; }
		return keyword.classModifiers().parse(node.getData());
	}


	/** Read backward through any available access modifiers (i.e. 'abstract', 'public', 'static', ...).
	 * Returns the iterator where {@code next()} would return the first access modifier element.
	 * @return access modifiers read backward from the iterator's current {@code previous()} value
	 */
	public static List<String> readAccessModifierFromIter(KeywordUtil<? extends AccessModifier> keyword, EnhancedListBuilderIterator<SimpleTree<CodeFragment>> iter) {
		int prevCount = 0;
		List<String> accessModifiers = new ArrayList<>();
		SimpleTree<CodeFragment> child = iter.hasPrevious() ? iter.previous() : null;

		while(child != null && keyword.classModifiers().is(child.getData())) {
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
