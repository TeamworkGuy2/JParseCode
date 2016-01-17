package twg2.parser.codeParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import lombok.val;
import twg2.collections.tuple.Tuples;
import twg2.parser.baseAst.AstParser;
import twg2.parser.baseAst.CompoundBlock;
import twg2.parser.documentParser.DocumentFragmentText;
import twg2.parser.intermAst.annotation.AnnotationSig;
import twg2.parser.intermAst.block.IntermBlock;
import twg2.parser.intermAst.classes.IntermClass;
import twg2.parser.intermAst.field.IntermFieldSig;
import twg2.parser.intermAst.method.IntermMethodSig;
import twg2.treeLike.simpleTree.SimpleTree;

/** Base static methods for helping {@link AstExtractor} implementations
 * @author TeamworkGuy2
 * @since 2016-1-14
 */
public class BaseBlockParser {

	/** Parses a simple AST tree using an {@link AstExtractor}
	 * @param extractor provides parsers and extract method to consume the astTree
	 * @param astTree the tree of basic {@link DocumentFragmentText}{@code <}{@link CodeFragmentType}{@code >} tokens
	 * @return a list of entries with simple AST tree blocks as keys and {@link IntermClass} as values
	 */
	// TODO this only parses some fields and interface methods
	public static <_T_BLOCK extends CompoundBlock> List<Entry<SimpleTree<DocumentFragmentText<CodeFragmentType>>, IntermClass.SimpleImpl<_T_BLOCK>>> extractBlockFieldsAndInterfaceMethods(
			AstExtractor<_T_BLOCK> extractor, SimpleTree<DocumentFragmentText<CodeFragmentType>> astTree) {

		val nameScope = new ArrayList<String>();

		List<IntermBlock<_T_BLOCK>> blockDeclarations = extractor.extractBlocks(nameScope, astTree, null);

		List<Entry<SimpleTree<DocumentFragmentText<CodeFragmentType>>, IntermClass.SimpleImpl<_T_BLOCK>>> resBlocks = new ArrayList<>();

		AstParser<List<List<String>>> usingStatementExtractor = extractor.createImportStatementParser();

		runParsers(astTree, usingStatementExtractor);

		List<List<String>> usingStatements = new ArrayList<>(usingStatementExtractor.getParserResult());

		for(val block : blockDeclarations) {
			usingStatementExtractor.recycle();
			runParsers(block.getBlockTree(), usingStatementExtractor);

			List<List<String>> tmpUsingStatements = usingStatementExtractor.getParserResult();
			usingStatements.addAll(tmpUsingStatements);

			List<IntermFieldSig> fields = null;
			List<IntermMethodSig.SimpleImpl> intfMethods = null;

			AstParser<List<AnnotationSig>> annotationExtractor = extractor.createAnnotationParser(block);
			AstParser<List<IntermFieldSig>> fieldExtractor = extractor.createFieldParser(block, annotationExtractor);
			AstParser<List<IntermMethodSig.SimpleImpl>> methodExtractor = extractor.createMethodParser(block, annotationExtractor);

			runParsers(block.getBlockTree(), annotationExtractor, fieldExtractor, methodExtractor);

			if(block.getBlockType().canContainFields()) {
				fields = fieldExtractor.getParserResult();
			}
			if(block.getBlockType().canContainMethods()) {
				intfMethods = methodExtractor.getParserResult();
			}

			if(block.getBlockType().canContainFields() && block.getBlockType().canContainMethods()) {
				resBlocks.add(Tuples.of(block.getBlockTree(), new IntermClass.SimpleImpl<>(block.getDeclaration(), usingStatements, fields, intfMethods, block.getBlockType())));
			}
		}

		return resBlocks;
	}


	@SafeVarargs
	public static void runParsers(SimpleTree<DocumentFragmentText<CodeFragmentType>> tree, AstParser<?>... parsers) {
		val children = tree.getChildren();
		val parserCount = parsers.length;

		for(int i = 0, size = children.size(); i < size; i++) {
			val child = children.get(i);

			// loop over each parser and allow it to consume the token
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


	//public static List<IntermFieldSig> extractDataModelFields(SimpleTree<DocumentFragmentText<CodeFragmentType>> tokenTree, IntermBlock<? extends CompoundBlock> parentBlock) {
	//	CsFieldExtractor extractor = new CsFieldExtractor(parentBlock);
	//	AstUtil.forChildrenOnly(0, tokenTree, extractor::extractDataModelFieldsIndexedTreeConsumer);
	//	return extractor.fields;
	//}

}
