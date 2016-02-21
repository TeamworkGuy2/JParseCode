package twg2.parser.codeParser;

import java.util.ArrayList;
import java.util.List;

import twg2.parser.baseAst.AccessModifier;
import twg2.parser.documentParser.DocumentFragmentText;
import twg2.streams.EnhancedListBuilderIterator;
import twg2.treeLike.simpleTree.SimpleTree;

/**
 * @author TeamworkGuy2
 * @since 2016-2-19
 */
public class BaseAccessModifierExtractor {

	public static AccessModifier readAccessModifier(KeywordUtil keyword, SimpleTree<DocumentFragmentText<CodeFragmentType>> node) {
		if(node == null) { return null; }
		return keyword.parseClassModifierKeyword(node.getData());
	}


	/** Read backward through any available access modifiers (i.e. 'abstract', 'public', 'static', ...).
	 * Returns the iterator where {@code next()} would return the first access modifier element.
	 * @return access modifiers read backward from the iterator's current {@code previous()} value
	 */
	public static List<String> readAccessModifierFromIter(KeywordUtil keyword, EnhancedListBuilderIterator<SimpleTree<DocumentFragmentText<CodeFragmentType>>> iter) {
		int prevCount = 0;
		List<String> accessModifiers = new ArrayList<>();
		SimpleTree<DocumentFragmentText<CodeFragmentType>> child = iter.hasPrevious() ? iter.previous() : null;

		while(child != null && keyword.isClassModifierKeyword(child.getData())) {
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
