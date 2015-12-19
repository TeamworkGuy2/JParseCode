package twg2.parser.codeParser.csharp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.val;
import twg2.parser.baseAst.AccessModifierEnum;
import twg2.parser.baseAst.csharp.CsAstUtil;
import twg2.parser.baseAst.csharp.CsBlock;
import twg2.parser.baseAst.util.AstFragType;
import twg2.parser.baseAst.util.AstUtil;
import twg2.parser.codeParser.CodeFragmentType;
import twg2.parser.codeParser.CodeLanguageOptions;
import twg2.parser.condition.AstParserCondition;
import twg2.parser.documentParser.DocumentFragmentText;
import twg2.parser.intermAst.block.IntermBlock;
import twg2.parser.intermAst.classes.IntermClass;
import twg2.parser.intermAst.classes.IntermClassSig;
import twg2.parser.intermAst.field.IntermFieldSig;
import twg2.parser.intermAst.method.IntermMethodSig;
import twg2.treeLike.simpleTree.SimpleTree;

/**
 * @author TeamworkGuy2
 * @since 2015-12-5
 */
public class CsBlockExtractor {
	private static final CodeLanguageOptions<CodeLanguageOptions.CSharp, CsAstUtil> lang = CodeLanguageOptions.C_SHARP;
	private IntermBlock<CsBlock> parentScope;
	List<IntermBlock<CsBlock>> blocks = new ArrayList<>();


	public CsBlockExtractor(IntermBlock<CsBlock> parentScope) {
		this.parentScope = parentScope;
	}


	public void extractDataModelFieldsIndexedSubTreeConsumer_old(SimpleTree<DocumentFragmentText<CodeFragmentType>> tokenNode, DocumentFragmentText<CodeFragmentType> token,
			int idx, int siblingCount, int depth, List<SimpleTree<DocumentFragmentText<CodeFragmentType>>> siblings,
			SimpleTree<DocumentFragmentText<CodeFragmentType>> parentNode, DocumentFragmentText<CodeFragmentType> parent) {

		if(AstFragType.isBlockKeyword(token)) {
			SimpleTree<DocumentFragmentText<CodeFragmentType>> nextNode = AstUtil.getSibling(siblings, idx, 1);

			// read a compound name (only needed for namespaces)
			int i = idx + 1;
			List<String> nameCompound = new ArrayList<>();
			while(i < siblingCount && nextNode != null && nextNode.getData().getFragmentType() == CodeFragmentType.IDENTIFIER && !AstFragType.isBlock(nextNode.getData(), "{")) {
				nameCompound.add(nextNode.getData().getText());
				nextNode = siblings.get(i + 1);
				i++;
			}

			// if the next token is an opening block, then this is probably a valid block declaration
			if(AstFragType.isBlock(nextNode.getData(), "{")) {
				val prevNode = AstUtil.getSibling(siblings, idx, -1);
				CsBlock blockType = CsBlock.tryFromKeyword(CsKeyword.tryToKeyword(token.getText()));
				val accessStr = prevNode != null ? prevNode.getData().getText() : null;
				AccessModifierEnum access = lang.getAstUtil().getAccessModifierParser().defaultAccessModifier(accessStr, blockType, parentScope != null ? parentScope.getBlockType() : null);
				blocks.add(new IntermBlock<>(new IntermClassSig(access, nameCompound, token.getText()), nextNode, blockType));
			}
		}
	}


	/** This method recursively loops over all nodes, adding/removing scope names on a name stack as named blocks of code are parsed (such as namespaces, or classes)
	 * @param nameScope the current name scope of the code (list of scope names that the current {@code blockTree} is inside of)
	 * @param blockTree the current sub-tree being parsed
	 * @param idx the index of the current blockTree among it's siblings
	 * @param siblingCount total children of the current blockTree's parent, including this blockTree
	 * @param depth the current blockTree's depth within the tree (0=root node, 1=child of root, etc.)
	 * @param parentNode the current blockTree's parent node or null if the parent is null (only possible if blockTree is a child of a tree with a null root or blockTree is the root and has no parent)
	 */
	public void extractDataModelFieldsFromTree(List<String> nameScope, SimpleTree<DocumentFragmentText<CodeFragmentType>> blockTree,
			int idx, int siblingCount, int depth, SimpleTree<DocumentFragmentText<CodeFragmentType>> parentNode) {
		val children = blockTree.getChildren();

		for(int ii = 0, sizeI = children != null ? children.size() : 0; ii < sizeI; ii++) {
			val child = children.get(ii);

			if(child == null) {
				continue;
			}

			val token = child.getData();
			int addBlockCount = 0;

			// if this token is an opening block, then this is probably a valid block declaration
			if(AstFragType.isBlock(token, "{")) {
				SimpleTree<DocumentFragmentText<CodeFragmentType>> prevNode = AstUtil.getSibling(children, ii, -1);

				// read the identifier
				int i = ii - 1;
				String nameCompound = null;
				if(i > 0 && prevNode != null && prevNode.getData().getFragmentType() == CodeFragmentType.IDENTIFIER && !AstFragType.isBlockKeyword(prevNode.getData())) {
					nameCompound = prevNode.getData().getText();
					prevNode = children.get(i - 1);
					i--;
				}

				if(nameCompound != null && prevNode != null && AstFragType.isBlockKeyword(prevNode.getData())) {
					addBlockCount = 1;
					val blockTypeStr = prevNode.getData().getText();
					CsBlock blockType = CsBlock.tryFromKeyword(CsKeyword.tryToKeyword(blockTypeStr));
					val accessModifierNode = AstUtil.getSibling(children, ii, -1);
					val accessStr = accessModifierNode != null ? accessModifierNode.getData().getText() : null;
					AccessModifierEnum access = lang.getAstUtil().getAccessModifierParser().defaultAccessModifier(accessStr, blockType, parentScope != null ? parentScope.getBlockType() : null);

					nameScope.add(nameCompound);

					blocks.add(new IntermBlock<>(new IntermClassSig(access, new ArrayList<>(nameScope), blockTypeStr), child, blockType));
				}
			}

			extractDataModelFieldsFromTree(nameScope, child, ii, sizeI, depth + 1, blockTree);

			while(addBlockCount > 0) {
				nameScope.remove(nameScope.size() - 1);
				addBlockCount--;
			}
		}
	}


	// TODO this only parses interface methods
	public static List<IntermClass<CsBlock>> extractBlockFieldsAndInterfaceMethods(SimpleTree<DocumentFragmentText<CodeFragmentType>> tokenTree) {
		List<IntermBlock<CsBlock>> blockDeclarations = CsBlockExtractor.extractBlocks(tokenTree, null);

		List<IntermClass<CsBlock>> resBlocks = new ArrayList<>();

		CsUsingStatementExtractor extractor = new CsUsingStatementExtractor();

		runParsers(tokenTree, new ArrayList<>(Arrays.asList(extractor)));

		List<List<String>> usingStatements = new ArrayList<>(extractor.getParserResult());

		for(val block : blockDeclarations) {
			List<IntermFieldSig> fields = null;
			List<IntermMethodSig> intfMethods = null;

			CsAnnotationParser annotationExtractor = new CsAnnotationParser();
			CsDataModelFieldExtractor fieldExtractor = new CsDataModelFieldExtractor(block, annotationExtractor);
			CsInterfaceMethodExtractor methodExtractor = new CsInterfaceMethodExtractor(block, annotationExtractor);

			val parsers = new ArrayList<>(Arrays.asList(annotationExtractor, fieldExtractor, methodExtractor));

			extractor.recycle();
			runParsers(block.getBlockTree(), new ArrayList<>(Arrays.asList(extractor)));

			List<List<String>> tmpUsingStatements = extractor.getParserResult();
			usingStatements.addAll(tmpUsingStatements);

			runParsers(block.getBlockTree(), (List<AstParserCondition<?>>)(ArrayList)parsers);

			if(block.getBlockType().canContainFields()) {
				fields = fieldExtractor.getParserResult();
			}
			if(block.getBlockType().canContainMethods()) {
				intfMethods = methodExtractor.getParserResult();
			}

			if(block.getBlockType() != CsBlock.NAMESPACE) {
				resBlocks.add(new IntermClass<>(block.getDeclaration(), usingStatements, fields, intfMethods, block.getBlockTree(), block.getBlockType()));
			}
		}

		return resBlocks;
	}


	public static void runParsers(SimpleTree<DocumentFragmentText<CodeFragmentType>> tree, List<AstParserCondition<?>> parsers) {
		val children = tree.getChildren();
		val parserCount = parsers.size();

		for(int i = 0, size = children.size(); i < size; i++) {
			val child = children.get(i);

			// loop over each parser and allow it to consume the token
			for(int ii = 0; ii < parserCount; ii++) {
				val parser = parsers.get(ii);
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
	//	CsDataModelFieldExtractor extractor = new CsDataModelFieldExtractor(parentBlock);
	//	AstUtil.forChildrenOnly(0, tokenTree, extractor::extractDataModelFieldsIndexedTreeConsumer);
	//	return extractor.fields;
	//}


	public static List<IntermBlock<CsBlock>> extractBlocks(SimpleTree<DocumentFragmentText<CodeFragmentType>> tokenTree, IntermBlock<CsBlock> parentScope) {
		CsBlockExtractor extractor = new CsBlockExtractor(parentScope);
		//AstUtil.forEach(parsedFile.getDoc(), extractor::extractDataModelFieldsIndexedSubTreeConsumer);
		List<String> nameScope = new ArrayList<>();
		extractor.extractDataModelFieldsFromTree(nameScope, tokenTree, 0, 0, 0, null);
		return extractor.blocks;
	}

}
