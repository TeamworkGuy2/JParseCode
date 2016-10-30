package twg2.parser.codeParser.extractors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import lombok.val;
import twg2.ast.interm.classes.ClassAst;
import twg2.ast.interm.field.FieldDef;
import twg2.ast.interm.field.FieldSig;
import twg2.ast.interm.method.MethodSig;
import twg2.parser.codeParser.AstExtractor;
import twg2.parser.codeParser.BlockType;
import twg2.parser.fragment.CodeToken;
import twg2.parser.stateMachine.AstParser;
import twg2.treeLike.simpleTree.SimpleTree;
import twg2.tuple.Tuples;

/** Base static methods for helping {@link AstExtractor} implementations
 * @author TeamworkGuy2
 * @since 2016-1-14
 */
public class BlockExtractor {

	/** Parses a simple AST tree using an {@link AstExtractor}
	 * @param extractor provides parsers and extract methods to consume the astTree
	 * @param astTree the tree of basic {@link CodeToken} tokens
	 * @return a list of entries with simple AST tree blocks as keys and classes ({@link ClassAst} instances) as values containing the annotations, comments, fields, and methods found inside the AST tree
	 */
	// TODO this only parses some fields and interface methods
	public static <_T_BLOCK extends BlockType> List<Entry<SimpleTree<CodeToken>, ClassAst.SimpleImpl<_T_BLOCK>>> extractBlockFieldsAndInterfaceMethods(
			AstExtractor<_T_BLOCK> extractor, SimpleTree<CodeToken> astTree) {

		val nameScope = new ArrayList<String>();

		val blocks = extractor.extractBlocks(nameScope, astTree, null);

		val resBlocks = new ArrayList<Entry<SimpleTree<CodeToken>, ClassAst.SimpleImpl<_T_BLOCK>>>();

		val usingStatementExtractor = extractor.createImportStatementParser();

		runParsers(astTree, usingStatementExtractor);

		val usingStatements = new ArrayList<>(usingStatementExtractor.getParserResult());

		for(val block : blocks) {
			val blockTree = block.getBlockTree();
			val blockType = block.getBlockType();

			usingStatementExtractor.recycle();
			runParsers(blockTree, usingStatementExtractor);

			val tmpUsingStatements = usingStatementExtractor.getParserResult();
			usingStatements.addAll(tmpUsingStatements);

			val annotationExtractor = extractor.createAnnotationParser(block);
			val commentExtractor = extractor.createCommentParser(block);
			val fieldExtractor = extractor.createFieldParser(block, annotationExtractor, commentExtractor);
			val methodExtractor = extractor.createMethodParser(block, annotationExtractor, commentExtractor);
			AstParser<List<FieldDef>> enumMemberExtractor = null;

			if(blockType.isEnum()) {
				enumMemberExtractor = extractor.createEnumParser(block, commentExtractor);
				runParsers(blockTree, annotationExtractor, commentExtractor, fieldExtractor, methodExtractor, enumMemberExtractor);
			}
			else {
				runParsers(blockTree, annotationExtractor, commentExtractor, fieldExtractor, methodExtractor);
			}

			List<FieldSig> fields = null;
			List<FieldDef> enumMembers = null;
			List<MethodSig.SimpleImpl> intfMethods = null;

			if(blockType.isEnum()) {
				enumMembers = enumMemberExtractor.getParserResult();
			}
			if(blockType.canContainFields()) {
				fields = fieldExtractor.getParserResult();
			}
			if(blockType.canContainMethods()) {
				intfMethods = methodExtractor.getParserResult();
			}

			if(blockType.canContainFields() && blockType.canContainMethods()) {
				resBlocks.add(Tuples.of(blockTree, new ClassAst.SimpleImpl<>(block.getDeclaration(), usingStatements, fields, intfMethods, enumMembers, blockType)));
			}
		}

		return resBlocks;
	}


	@SafeVarargs
	public static void runParsers(SimpleTree<CodeToken> tree, AstParser<?>... parsers) {
		val children = tree.getChildren();
		val parserCount = parsers.length;

		for(int i = 0, size = children.size(); i < size; i++) {
			val child = children.get(i);

			// loop over each parser and allow it to consume the block
			for(int ii = 0; ii < parserCount; ii++) {
				val parser = parsers[ii];
				parser.acceptNext(child);

				val complete = parser.isComplete();
				val failed = parser.isFailed();
				if(complete || failed) {
					//val newParser = parser.copyOrReuse();
					//parsers.set(ii, newParser);
				}
			}
		}
	}

}
