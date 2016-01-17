package twg2.parser.codeParser;

import java.util.List;
import java.util.Map.Entry;

import twg2.parser.baseAst.AstParser;
import twg2.parser.baseAst.CompoundBlock;
import twg2.parser.documentParser.DocumentFragmentText;
import twg2.parser.intermAst.annotation.AnnotationSig;
import twg2.parser.intermAst.block.IntermBlock;
import twg2.parser.intermAst.classes.IntermClass;
import twg2.parser.intermAst.field.IntermFieldSig;
import twg2.parser.intermAst.method.IntermMethodSig;
import twg2.parser.intermAst.type.TypeSig;
import twg2.treeLike.simpleTree.SimpleTree;

/**
 * @param <T_BLOCK> the type of {@link CompoundBlock} that this extract can extract
 * @author TeamworkGuy2
 * @since 2016-1-6
 */
public interface AstExtractor<T_BLOCK extends CompoundBlock> {

	public AstParser<List<List<String>>> createImportStatementParser();

	public AstParser<TypeSig.Simple> createTypeParser();

	public AstParser<List<AnnotationSig>> createAnnotationParser(IntermBlock<T_BLOCK> block);

	public AstParser<List<IntermFieldSig>> createFieldParser(IntermBlock<T_BLOCK> block, AstParser<List<AnnotationSig>> annotationParser);

	public AstParser<List<IntermMethodSig.SimpleImpl>> createMethodParser(IntermBlock<T_BLOCK> block, AstParser<List<AnnotationSig>> annotationParser);


	public List<IntermBlock<T_BLOCK>> extractBlocks(List<String> nameScope, SimpleTree<DocumentFragmentText<CodeFragmentType>> astTree, IntermBlock<T_BLOCK> parentScope);


	public List<Entry<SimpleTree<DocumentFragmentText<CodeFragmentType>>, IntermClass.SimpleImpl<T_BLOCK>>> extractClassFieldsAndMethodSignatures(SimpleTree<DocumentFragmentText<CodeFragmentType>> astTree);

}
