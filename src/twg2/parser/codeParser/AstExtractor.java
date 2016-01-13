package twg2.parser.codeParser;

import java.util.List;
import java.util.Map.Entry;

import twg2.parser.baseAst.CompoundBlock;
import twg2.parser.documentParser.DocumentFragmentText;
import twg2.parser.intermAst.classes.IntermClass;
import twg2.treeLike.simpleTree.SimpleTree;

/**
 * @param <T_BLOCK> the type of {@link CompoundBlock} that this extract can extract
 * @author TeamworkGuy2
 * @since 2016-1-6
 */
public interface AstExtractor<T_BLOCK extends CompoundBlock> {

	public List<Entry<SimpleTree<DocumentFragmentText<CodeFragmentType>>, IntermClass.SimpleImpl<T_BLOCK>>> extractClassFieldsAndMethodSignatures(SimpleTree<DocumentFragmentText<CodeFragmentType>> astTree);

}
