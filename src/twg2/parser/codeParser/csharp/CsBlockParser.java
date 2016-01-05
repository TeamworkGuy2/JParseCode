package twg2.parser.codeParser.csharp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import lombok.val;
import twg2.collections.tuple.Tuples;
import twg2.parser.baseAst.AccessModifierEnum;
import twg2.parser.baseAst.csharp.CsAstUtil;
import twg2.parser.baseAst.tools.AstFragType;
import twg2.parser.codeParser.CodeFragmentType;
import twg2.parser.codeParser.CodeLanguageOptions;
import twg2.parser.codeParser.tools.TokenListIterable;
import twg2.parser.condition.AstParserCondition;
import twg2.parser.documentParser.DocumentFragmentText;
import twg2.parser.intermAst.block.IntermBlock;
import twg2.parser.intermAst.classes.IntermClass;
import twg2.parser.intermAst.classes.IntermClassSig;
import twg2.parser.intermAst.field.IntermFieldSig;
import twg2.parser.intermAst.method.IntermMethodSig;
import twg2.streams.EnhancedListBuilderIterator;
import twg2.text.stringUtils.StringJoin;
import twg2.treeLike.simpleTree.SimpleTree;

/**
 * @author TeamworkGuy2
 * @since 2015-12-5
 */
public class CsBlockParser {
	private static final CodeLanguageOptions<CodeLanguageOptions.CSharp, CsAstUtil> lang = CodeLanguageOptions.C_SHARP;
	private IntermBlock<IntermClassSig.SimpleImpl, CsBlock> parentScope;
	List<IntermBlock<IntermClassSig.SimpleImpl, CsBlock>> blocks = new ArrayList<>();


	public CsBlockParser(IntermBlock<IntermClassSig.SimpleImpl, CsBlock> parentScope) {
		this.parentScope = parentScope;
	}


	/** This method recursively loops over all nodes, adding/removing scope names on a name stack as named blocks of code are parsed (such as namespaces, or classes)
	 * @param nameScope the current name scope of the code (list of scope names that the current {@code blockTree} is inside of)
	 * @param blockTree the current sub-tree being parsed
	 * @param depth the current blockTree's depth within the tree (0=root node, 1=child of root, etc.)
	 * @param parentNode the current blockTree's parent node or null if the parent is null (only possible if blockTree is a child of a tree with a null root or blockTree is the root and has no parent)
	 */
	public void extractBlocksFromTree(List<String> nameScope, SimpleTree<DocumentFragmentText<CodeFragmentType>> blockTree,
			int depth, SimpleTree<DocumentFragmentText<CodeFragmentType>> parentNode) {
		val children = blockTree.getChildren();

		val childIterable = new TokenListIterable(children);
		val childIter = childIterable.iterator();
		for(val child : childIterable) {
			val token = child.getData();
			int addBlockCount = 0;

			// if this token is an opening block, then this is probably a valid block declaration
			if(AstFragType.isBlock(token, "{")) {
				if(childIter.hasPrevious()) {
					// read the identifier
					int mark = childIter.mark();
					// since the current token is the opening '{', step back to the class signature
					childIter.previous();
					val nameCompoundRes = readClassIdentifierAndExtends(childIter);
					val prevNode = childIter.hasPrevious() ? childIter.previous() : null;

					// if a block keyword ("class", "interface", etc.) and an identifier were found, then this is probably a valid block declaration
					if(nameCompoundRes != null && nameCompoundRes.getKey() != null && prevNode != null && lang.getAstUtil().getChecker().isBlockKeyword(prevNode.getData())) {
						addBlockCount = 1;
						val blockTypeStr = prevNode.getData().getText();
						CsBlock blockType = CsBlock.tryFromKeyword(CsKeyword.tryToKeyword(blockTypeStr));
						val accessModifiers = readAccessModifier(childIter);
						val accessStr = accessModifiers != null ? StringJoin.join(accessModifiers, " ") : null;
						AccessModifierEnum access = lang.getAstUtil().getAccessModifierParser().defaultAccessModifier(accessStr, blockType, parentScope != null ? parentScope.getBlockType() : null);

						nameScope.add(nameCompoundRes.getKey());

						blocks.add(new IntermBlock<>(new IntermClassSig.SimpleImpl(access, new ArrayList<>(nameScope), blockTypeStr, nameCompoundRes.getValue()), child, blockType));
					}

					childIter.reset(mark);
				}
			}

			extractBlocksFromTree(nameScope, child, depth + 1, blockTree);

			while(addBlockCount > 0) {
				nameScope.remove(nameScope.size() - 1);
				addBlockCount--;
			}
		}
	}


	/** Read backward through any available access modifiers (i.e. 'abstract', 'public', 'static', ...).
	 * Returns the iterator where {@code next()} would return the first access modifier element.
	 * @return access modifiers read backward from the iterator's current {@code previous()} value
	 */
	private static List<String> readAccessModifier(EnhancedListBuilderIterator<SimpleTree<DocumentFragmentText<CodeFragmentType>>> iter) {
		int prevCount = 0;
		List<String> accessModifiers = new ArrayList<>();
		SimpleTree<DocumentFragmentText<CodeFragmentType>> child = iter.hasPrevious() ? iter.previous() : null;
		while(child != null && lang.getAstUtil().getChecker().isClassModifierKeyword(child.getData())) {
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


	/** Reads backward from a '{' block through a simple class signature ({@code ClassName [: ClassName]}).
	 * Returns the iterator where {@code next()} would return the class name element.
	 * @return {@code <className, extendImplementNames>}
	 */
	private static Entry<String, List<String>> readClassIdentifierAndExtends(EnhancedListBuilderIterator<SimpleTree<DocumentFragmentText<CodeFragmentType>>> iter) {
		// class signatures are read backward from the opening '{'
		int prevCount = 0;
		List<String> names = new ArrayList<>();
		Entry<String, List<String>> nameCompoundRes = null;

		// get the first element and begin checking
		if(iter.hasPrevious()) { prevCount++; }
		SimpleTree<DocumentFragmentText<CodeFragmentType>> prevNode = iter.hasPrevious() ? iter.previous() : null;

		// TODO should read ', ' between each name, currently only works with 1 extend/implement class name
		while(prevNode != null && prevNode.getData().getFragmentType() == CodeFragmentType.IDENTIFIER && !lang.getAstUtil().getChecker().isBlockKeyword(prevNode.getData())) {
			names.add(prevNode.getData().getText());
			prevNode = iter.hasPrevious() ? iter.previous() : null;
			if(iter.hasPrevious()) { prevCount++; }
		}

		// if the class signature extends/implements, then the identifiers just read are the class/interface names, next read the actual class name
		if(prevNode != null && prevNode.getData().getText().trim().equals(":")) {
			prevNode = iter.hasPrevious() ? iter.previous() : null;
			if(iter.hasPrevious()) { prevCount++; }
			if(prevNode != null && prevNode.getData().getFragmentType() == CodeFragmentType.IDENTIFIER && !lang.getAstUtil().getChecker().isBlockKeyword(prevNode.getData())) {
				val extendImplementNames = names;
				val className = prevNode.getData().getText();
				nameCompoundRes = Tuples.of(className, extendImplementNames);
			}
			else {
				throw new IllegalStateException("found block with extend/implement names, but no class name " + names);
			}
		}
		// else, we should have only read one name with the loop and it is the class name
		else if(names.size() == 1) {
			val className = names.get(0);
			nameCompoundRes = Tuples.of(className, new ArrayList<>());
			// move iterator forward since the while loop doesn't use the last value (i.e. reads one element past the valid elements it wants to consume)
			if(prevCount > 0) {
				iter.next();
			}
		}

		return nameCompoundRes;
	}


	// TODO this only parses some fields and interface methods
	public static List<IntermClass.SimpleImpl<CsBlock>> extractBlockFieldsAndInterfaceMethods(SimpleTree<DocumentFragmentText<CodeFragmentType>> tokenTree) {
		List<IntermBlock<IntermClassSig.SimpleImpl, CsBlock>> blockDeclarations = CsBlockParser.extractBlocks(tokenTree, null);

		List<IntermClass.SimpleImpl<CsBlock>> resBlocks = new ArrayList<>();

		CsUsingStatementExtractor usingStatementExtractor = new CsUsingStatementExtractor();

		runParsers(tokenTree, new ArrayList<>(Arrays.asList(usingStatementExtractor)));

		List<List<String>> usingStatements = new ArrayList<>(usingStatementExtractor.getParserResult());

		for(val block : blockDeclarations) {
			List<IntermFieldSig> fields = null;
			List<IntermMethodSig.SimpleImpl> intfMethods = null;

			CsAnnotationExtractor annotationExtractor = new CsAnnotationExtractor();
			CsDataModelFieldExtractor fieldExtractor = new CsDataModelFieldExtractor(block, annotationExtractor);
			CsInterfaceMethodExtractor methodExtractor = new CsInterfaceMethodExtractor(block, annotationExtractor);

			val parsers = new ArrayList<>(Arrays.asList(annotationExtractor, fieldExtractor, methodExtractor));

			usingStatementExtractor.recycle();
			runParsers(block.getBlockTree(), new ArrayList<>(Arrays.asList(usingStatementExtractor)));

			List<List<String>> tmpUsingStatements = usingStatementExtractor.getParserResult();
			usingStatements.addAll(tmpUsingStatements);

			runParsers(block.getBlockTree(), (List<AstParserCondition<?>>)(ArrayList)parsers);

			if(block.getBlockType().canContainFields()) {
				fields = fieldExtractor.getParserResult();
			}
			if(block.getBlockType().canContainMethods()) {
				intfMethods = methodExtractor.getParserResult();
			}

			if(block.getBlockType() != CsBlock.NAMESPACE) {
				resBlocks.add(new IntermClass.SimpleImpl<>(block.getDeclaration(), usingStatements, fields, intfMethods, block.getBlockTree(), block.getBlockType()));
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


	public static List<IntermBlock<IntermClassSig.SimpleImpl, CsBlock>> extractBlocks(SimpleTree<DocumentFragmentText<CodeFragmentType>> tokenTree, IntermBlock<IntermClassSig.SimpleImpl, CsBlock> parentScope) {
		CsBlockParser extractor = new CsBlockParser(parentScope);
		//AstUtil.forEach(parsedFile.getDoc(), extractor::extractDataModelFieldsIndexedSubTreeConsumer);
		List<String> nameScope = new ArrayList<>();
		extractor.extractBlocksFromTree(nameScope, tokenTree, 0, null);
		return extractor.blocks;
	}

}
