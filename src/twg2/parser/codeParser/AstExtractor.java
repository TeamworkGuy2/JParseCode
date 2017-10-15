package twg2.parser.codeParser;

import java.util.List;
import java.util.Map.Entry;

import twg2.ast.interm.annotation.AnnotationSig;
import twg2.ast.interm.block.BlockAst;
import twg2.ast.interm.classes.ClassAst;
import twg2.ast.interm.field.FieldDef;
import twg2.ast.interm.field.FieldSig;
import twg2.ast.interm.method.MethodSigSimple;
import twg2.ast.interm.type.TypeSig;
import twg2.parser.fragment.CodeToken;
import twg2.parser.stateMachine.AstParser;
import twg2.treeLike.simpleTree.SimpleTree;

/**
 * @param <T_BLOCK> the type of {@link BlockType} that this extract can extract
 * @author TeamworkGuy2
 * @since 2016-1-6
 */
public interface AstExtractor<T_BLOCK extends BlockType> {

	public AstParser<List<List<String>>> createImportStatementParser();

	public AstParser<TypeSig.TypeSigSimple> createTypeParser();

	public AstParser<List<FieldDef>> createEnumParser(BlockAst<T_BLOCK> block, AstParser<List<String>> commentParser);

	public AstParser<List<AnnotationSig>> createAnnotationParser(BlockAst<T_BLOCK> block);

	public AstParser<List<String>> createCommentParser(BlockAst<T_BLOCK> block);

	public AstParser<List<FieldSig>> createFieldParser(BlockAst<T_BLOCK> block, AstParser<List<AnnotationSig>> annotationParser, AstParser<List<String>> commentParser);

	public AstParser<List<MethodSigSimple>> createMethodParser(BlockAst<T_BLOCK> block, AstParser<List<AnnotationSig>> annotationParser, AstParser<List<String>> commentParser);

	/** This method recursively loops over all nodes, adding/removing scope names on a name stack as named blocks of code are parsed (such as namespaces, or classes)
	 * @param nameScope the current name scope of the code (list of scope names that the current {@code blockTree} is inside of)
	 * @param astTree the AST tree containing blocks to extract
	 * @param parentScope the current blockTree's parent node or null if the parent is null (only possible if blockTree is a child of a tree with a null root or astTree is the root and has no parent)
	 */
	public List<BlockAst<T_BLOCK>> extractBlocks(List<String> nameScope, SimpleTree<CodeToken> astTree, BlockAst<T_BLOCK> parentScope);

	/** Parses a simple AST tree
	 * @param astTree the tree of basic {@link CodeToken} tokens
	 * @return a list of entries with simple AST tree blocks as keys and classes ({@link ClassAst} instances) as values containing the annotations, comments, fields, and methods found inside the AST tree
	 */
	public List<Entry<SimpleTree<CodeToken>, ClassAst.SimpleImpl<T_BLOCK>>> extractClassFieldsAndMethodSignatures(SimpleTree<CodeToken> astTree);

}
