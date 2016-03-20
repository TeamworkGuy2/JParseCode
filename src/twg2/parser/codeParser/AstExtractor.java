package twg2.parser.codeParser;

import java.util.List;
import java.util.Map.Entry;

import twg2.ast.interm.annotation.AnnotationSig;
import twg2.ast.interm.block.BlockAst;
import twg2.ast.interm.classes.ClassAst;
import twg2.ast.interm.field.FieldSig;
import twg2.ast.interm.method.MethodSig;
import twg2.ast.interm.type.TypeSig;
import twg2.parser.baseAst.AstParser;
import twg2.parser.baseAst.CompoundBlock;
import twg2.parser.documentParser.DocumentFragmentText;
import twg2.treeLike.simpleTree.SimpleTree;

/**
 * @param <T_BLOCK> the type of {@link CompoundBlock} that this extract can extract
 * @author TeamworkGuy2
 * @since 2016-1-6
 */
public interface AstExtractor<T_BLOCK extends CompoundBlock> {

	public AstParser<List<List<String>>> createImportStatementParser();

	public AstParser<TypeSig.Simple> createTypeParser();

	public AstParser<List<AnnotationSig>> createAnnotationParser(BlockAst<T_BLOCK> block);

	public AstParser<List<String>> createCommentParser(BlockAst<T_BLOCK> block);

	public AstParser<List<FieldSig>> createFieldParser(BlockAst<T_BLOCK> block, AstParser<List<AnnotationSig>> annotationParser, AstParser<List<String>> commentParser);

	public AstParser<List<MethodSig.SimpleImpl>> createMethodParser(BlockAst<T_BLOCK> block, AstParser<List<AnnotationSig>> annotationParser, AstParser<List<String>> commentParser);


	public List<BlockAst<T_BLOCK>> extractBlocks(List<String> nameScope, SimpleTree<DocumentFragmentText<CodeFragmentType>> astTree, BlockAst<T_BLOCK> parentScope);


	public List<Entry<SimpleTree<DocumentFragmentText<CodeFragmentType>>, ClassAst.SimpleImpl<T_BLOCK>>> extractClassFieldsAndMethodSignatures(SimpleTree<DocumentFragmentText<CodeFragmentType>> astTree);

}
