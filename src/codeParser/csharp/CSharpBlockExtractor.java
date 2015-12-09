package codeParser.csharp;

import intermAst.block.IntermBlock;
import intermAst.classes.IntermClassSig;
import intermAst.classes.IntermClassWithFieldsMethods;
import intermAst.field.IntermFieldSig;
import intermAst.method.IntermMethodSig;

import java.util.ArrayList;
import java.util.List;

import lombok.val;
import twg2.treeLike.simpleTree.SimpleTree;
import baseAst.AccessModifierEnum;
import baseAst.csharp.CSharpAstUtil;
import baseAst.csharp.CSharpBlock;
import baseAst.util.AstUtil;
import codeParser.CodeFragmentType;
import codeParser.CodeLanguageOptions;
import documentParser.DocumentFragmentText;

/**
 * @author TeamworkGuy2
 * @since 2015-12-5
 */
public class CSharpBlockExtractor {
	private static final CodeLanguageOptions<CodeLanguageOptions.CSharp, CSharpAstUtil> lang = CodeLanguageOptions.C_SHARP;
	private IntermBlock<CSharpBlock> parentScope;
	List<IntermBlock<CSharpBlock>> blocks = new ArrayList<>();


	public CSharpBlockExtractor(IntermBlock<CSharpBlock> parentScope) {
		this.parentScope = parentScope;
	}


	public void extractDataModelFieldsIndexedSubTreeConsumer_old(SimpleTree<DocumentFragmentText<CodeFragmentType>> tokenNode, DocumentFragmentText<CodeFragmentType> token,
			int idx, int siblingCount, int depth, List<SimpleTree<DocumentFragmentText<CodeFragmentType>>> siblings,
			SimpleTree<DocumentFragmentText<CodeFragmentType>> parentNode, DocumentFragmentText<CodeFragmentType> parent) {

		if(AstUtil.isKeyword(token, CSharpKeyword.CLASS, CSharpKeyword.INTERFACE, CSharpKeyword.NAMESPACE)) {
			SimpleTree<DocumentFragmentText<CodeFragmentType>> nextNode = AstUtil.getSibling(siblings, idx, 1);

			// read a compound name (only needed for namespaces)
			int i = idx + 1;
			List<String> nameCompound = new ArrayList<>();
			while(i < siblingCount && nextNode != null && nextNode.getData().getFragmentType() == CodeFragmentType.IDENTIFIER && !AstUtil.isBlock(nextNode.getData(), "{")) {
				nameCompound.add(nextNode.getData().getText());
				nextNode = siblings.get(i + 1);
				i++;
			}

			// if the next token is an opening block, then this is probably a valid block declaration
			if(AstUtil.isBlock(nextNode.getData(), "{")) {
				val prevNode = AstUtil.getSibling(siblings, idx, -1);
				CSharpBlock blockType = CSharpBlock.tryFromKeyword(CSharpKeyword.tryToKeyword(token.getText()));
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
			if(AstUtil.isBlock(token, "{")) {
				SimpleTree<DocumentFragmentText<CodeFragmentType>> prevNode = AstUtil.getSibling(children, ii, -1);

				// TODO probably not needed since we have a compound name parser (2015-12-9), -- read a compound name backward (only needed for namespaces)
				int i = ii - 1;
				List<String> nameCompound = new ArrayList<>();
				while(i > 0 && prevNode != null && prevNode.getData().getFragmentType() == CodeFragmentType.IDENTIFIER && !AstUtil.isKeyword(prevNode.getData(), CSharpKeyword.CLASS, CSharpKeyword.INTERFACE, CSharpKeyword.NAMESPACE)) {
					nameCompound.add(0, prevNode.getData().getText());
					prevNode = children.get(i - 1);
					i--;
				}

				if(prevNode != null && AstUtil.isKeyword(prevNode.getData(), CSharpKeyword.CLASS, CSharpKeyword.INTERFACE, CSharpKeyword.NAMESPACE)) {
					addBlockCount = nameCompound.size();
					val blockTypeStr = prevNode.getData().getText();
					CSharpBlock blockType = CSharpBlock.tryFromKeyword(CSharpKeyword.tryToKeyword(blockTypeStr));
					val accessModifierNode = AstUtil.getSibling(children, ii, -1);
					val accessStr = accessModifierNode != null ? accessModifierNode.getData().getText() : null;
					AccessModifierEnum access = lang.getAstUtil().getAccessModifierParser().defaultAccessModifier(accessStr, blockType, parentScope != null ? parentScope.getBlockType() : null);

					nameScope.addAll(nameCompound);

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
	public static List<IntermClassWithFieldsMethods<CSharpBlock>> extractBlockFieldsAndInterfaceMethods(SimpleTree<DocumentFragmentText<CodeFragmentType>> tokenTree,
			boolean parseUsingStatements, boolean parseFields, boolean parseMethods) {
		List<IntermBlock<CSharpBlock>> blockDeclarations = CSharpBlockExtractor.extractBlocks(tokenTree, null);
		List<IntermClassWithFieldsMethods<CSharpBlock>> resBlocks = new ArrayList<>();

		List<List<String>> usingStatements = null;
		if(parseUsingStatements) {
			usingStatements = CSharpUsingStatementExtractor.extractUsingStatements(tokenTree);
		}

		for(val block : blockDeclarations) {
			List<IntermFieldSig> fields = null;
			List<IntermMethodSig> intfMethods = null;

			if(parseFields && block.getBlockType().canContainFields()) {
				fields = CSharpDataModelFieldExtractor.extractDataModelFields(block.getBlockTree(), block);
			}
			if(parseMethods && block.getBlockType().canContainMethods()) {
				intfMethods = CSharpInterfaceMethodExtractor.extractInterfaceMethods(block.getBlockTree(), block);
			}

			if(block.getBlockType() != CSharpBlock.NAMESPACE) {
				resBlocks.add(new IntermClassWithFieldsMethods<>(block.getDeclaration(), usingStatements, fields, intfMethods, block.getBlockTree(), block.getBlockType()));
			}
		}

		return resBlocks;
	}


	public static List<IntermBlock<CSharpBlock>> extractBlocks(SimpleTree<DocumentFragmentText<CodeFragmentType>> tokenTree, IntermBlock<CSharpBlock> parentScope) {
		CSharpBlockExtractor extractor = new CSharpBlockExtractor(parentScope);
		//AstUtil.forEach(parsedFile.getDoc(), extractor::extractDataModelFieldsIndexedSubTreeConsumer);
		List<String> nameScope = new ArrayList<>();
		extractor.extractDataModelFieldsFromTree(nameScope, tokenTree, 0, 0, 0, null);
		return extractor.blocks;
	}

}
