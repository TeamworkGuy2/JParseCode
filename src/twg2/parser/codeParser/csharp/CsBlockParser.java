package twg2.parser.codeParser.csharp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import lombok.val;
import twg2.collections.tuple.Tuples;
import twg2.parser.baseAst.AccessModifierEnum;
import twg2.parser.baseAst.AstParser;
import twg2.parser.baseAst.tools.AstFragType;
import twg2.parser.baseAst.tools.NameUtil;
import twg2.parser.codeParser.AstExtractor;
import twg2.parser.codeParser.BaseBlockParser;
import twg2.parser.codeParser.BaseDataTypeExtractor;
import twg2.parser.codeParser.BaseFieldExtractor;
import twg2.parser.codeParser.BaseMethodExtractor;
import twg2.parser.codeParser.CodeFragmentType;
import twg2.parser.codeParser.CodeLanguageOptions;
import twg2.parser.codeParser.tools.TokenListIterable;
import twg2.parser.documentParser.DocumentFragmentText;
import twg2.parser.intermAst.annotation.AnnotationSig;
import twg2.parser.intermAst.block.IntermBlock;
import twg2.parser.intermAst.classes.IntermClass;
import twg2.parser.intermAst.classes.IntermClassSig;
import twg2.parser.intermAst.field.IntermFieldSig;
import twg2.parser.intermAst.method.IntermMethodSig;
import twg2.parser.intermAst.type.TypeSig;
import twg2.parser.intermAst.type.TypeSig.Simple;
import twg2.streams.EnhancedListBuilderIterator;
import twg2.text.stringUtils.StringJoin;
import twg2.treeLike.simpleTree.SimpleTree;

/**
 * @author TeamworkGuy2
 * @since 2015-12-5
 */
public class CsBlockParser implements AstExtractor<CsBlock> {

	// TODO parsers only parse some fields and interface methods

	@Override
	public AstParser<List<List<String>>> createImportStatementParser() {
		return new CsUsingStatementExtractor();
	}


	@Override
	public AstParser<Simple> createTypeParser() {
		return new BaseDataTypeExtractor(CodeLanguageOptions.C_SHARP, true);
	}


	@Override
	public AstParser<List<AnnotationSig>> createAnnotationParser(IntermBlock<CsBlock> block) {
		return new CsAnnotationExtractor();
	}


	@Override
	public AstParser<List<IntermFieldSig>> createFieldParser(IntermBlock<CsBlock> block, AstParser<List<AnnotationSig>> annotationParser) {
		val typeParser = new BaseDataTypeExtractor(CodeLanguageOptions.C_SHARP, false);
		return new BaseFieldExtractor("C#", CsKeyword.check, block, typeParser, annotationParser);
	}


	@Override
	public AstParser<List<IntermMethodSig.SimpleImpl>> createMethodParser(IntermBlock<CsBlock> block, AstParser<List<AnnotationSig>> annotationParser) {
		val typeParser = new BaseDataTypeExtractor(CodeLanguageOptions.C_SHARP, true);
		return new BaseMethodExtractor("C#", CsKeyword.check, block, typeParser, annotationParser);
	}


	@Override
	public List<Entry<SimpleTree<DocumentFragmentText<CodeFragmentType>>, IntermClass.SimpleImpl<CsBlock>>> extractClassFieldsAndMethodSignatures(SimpleTree<DocumentFragmentText<CodeFragmentType>> astTree) {
		val blocks = BaseBlockParser.extractBlockFieldsAndInterfaceMethods(this, astTree);
		return blocks;
	}


	@Override
	public List<IntermBlock<CsBlock>> extractBlocks(List<String> nameScope, SimpleTree<DocumentFragmentText<CodeFragmentType>> astTree, IntermBlock<CsBlock> parentScope) {
		List<IntermBlock<CsBlock>> blocks = new ArrayList<>();
		_extractBlocksFromTree(nameScope, astTree, 0, null, parentScope, blocks);
		return blocks;
	}


	/** This method recursively loops over all nodes, adding/removing scope names on a name stack as named blocks of code are parsed (such as namespaces, or classes)
	 * @param nameScope the current name scope of the code (list of scope names that the current {@code blockTree} is inside of)
	 * @param blockTree the current sub-tree being parsed
	 * @param depth the current blockTree's depth within the tree (0=root node, 1=child of root, etc.)
	 * @param parentNode the current blockTree's parent node or null if the parent is null (only possible if blockTree is a child of a tree with a null root or blockTree is the root and has no parent)
	 */
	public static void _extractBlocksFromTree(List<String> nameScope, SimpleTree<DocumentFragmentText<CodeFragmentType>> blockTree,
			int depth, SimpleTree<DocumentFragmentText<CodeFragmentType>> parentNode, IntermBlock<CsBlock> parentScope, List<IntermBlock<CsBlock>> blocks) {
		CodeLanguageOptions.CSharp lang = CodeLanguageOptions.C_SHARP;
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
					if(nameCompoundRes != null && nameCompoundRes.getKey() != null && prevNode != null && lang.getKeyword().isBlockKeyword(prevNode.getData())) {
						addBlockCount = 1;
						val blockTypeStr = prevNode.getData().getText();
						CsBlock blockType = CsBlock.tryFromKeyword(CsKeyword.check.tryToKeyword(blockTypeStr));
						val accessModifiers = readAccessModifier(childIter);
						val accessStr = accessModifiers != null ? StringJoin.join(accessModifiers, " ") : null;
						AccessModifierEnum access = lang.getAstUtil().getAccessModifierParser().defaultAccessModifier(accessStr, blockType, parentScope != null ? parentScope.getBlockType() : null);

						nameScope.add(nameCompoundRes.getKey());

						val blockGenericTypesSig = BaseDataTypeExtractor.extractGenericTypes(NameUtil.joinFqName(nameScope));
						val blockGenericTypes = blockGenericTypesSig.isGeneric() ? blockGenericTypesSig.getGenericParams() : Collections.<TypeSig.Simple>emptyList();
						val blockFqName = NameUtil.splitFqName(blockGenericTypesSig.getTypeName());

						blocks.add(new IntermBlock<>(new IntermClassSig.SimpleImpl(access, blockFqName, blockGenericTypes, blockTypeStr, nameCompoundRes.getValue()), child, blockType));
					}

					childIter.reset(mark);
				}
			}

			_extractBlocksFromTree(nameScope, child, depth + 1, blockTree, parentScope, blocks);

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
		CodeLanguageOptions.CSharp lang = CodeLanguageOptions.C_SHARP;
		int prevCount = 0;
		List<String> accessModifiers = new ArrayList<>();
		SimpleTree<DocumentFragmentText<CodeFragmentType>> child = iter.hasPrevious() ? iter.previous() : null;

		while(child != null && lang.getKeyword().isClassModifierKeyword(child.getData())) {
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
		CodeLanguageOptions.CSharp lang = CodeLanguageOptions.C_SHARP;
		// class signatures are read backward from the opening '{'
		int prevCount = 0;
		List<String> names = new ArrayList<>();
		Entry<String, List<String>> nameCompoundRes = null;

		// get the first element and begin checking
		if(iter.hasPrevious()) { prevCount++; }
		SimpleTree<DocumentFragmentText<CodeFragmentType>> prevNode = iter.hasPrevious() ? iter.previous() : null;

		// TODO should read ', ' between each name, currently only works with 1 extend/implement class name
		while(prevNode != null && prevNode.getData().getFragmentType() == CodeFragmentType.IDENTIFIER && !lang.getKeyword().isBlockKeyword(prevNode.getData())) {
			names.add(prevNode.getData().getText());
			prevNode = iter.hasPrevious() ? iter.previous() : null;
			if(iter.hasPrevious()) { prevCount++; }
		}

		// if the class signature extends/implements, then the identifiers just read are the class/interface names, next read the actual class name
		if(prevNode != null && prevNode.getData().getText().trim().equals(":")) {
			prevNode = iter.hasPrevious() ? iter.previous() : null;
			if(iter.hasPrevious()) { prevCount++; }
			if(prevNode != null && prevNode.getData().getFragmentType() == CodeFragmentType.IDENTIFIER && !lang.getKeyword().isBlockKeyword(prevNode.getData())) {
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

}
