package twg2.parser.codeParser.csharp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;

import twg2.ast.interm.annotation.AnnotationSig;
import twg2.ast.interm.block.BlockAst;
import twg2.ast.interm.classes.ClassAst;
import twg2.ast.interm.classes.ClassSigSimple;
import twg2.ast.interm.field.FieldDef;
import twg2.ast.interm.field.FieldSig;
import twg2.ast.interm.method.MethodSigSimple;
import twg2.ast.interm.type.TypeSig;
import twg2.collections.dataStructures.BaseList;
import twg2.parser.codeParser.AstExtractor;
import twg2.parser.codeParser.extractors.AccessModifierExtractor;
import twg2.parser.codeParser.extractors.BlockExtractor;
import twg2.parser.codeParser.extractors.CommentBlockExtractor;
import twg2.parser.codeParser.extractors.FieldExtractor;
import twg2.parser.codeParser.extractors.MethodExtractor;
import twg2.parser.codeParser.extractors.TypeExtractor;
import twg2.parser.codeParser.tools.EnhancedListIterator;
import twg2.parser.codeParser.tools.NameUtil;
import twg2.parser.fragment.AstFragType;
import twg2.parser.fragment.CodeToken;
import twg2.parser.language.CodeLanguageOptions;
import twg2.parser.stateMachine.AstParser;
import twg2.text.stringUtils.StringJoin;
import twg2.treeLike.simpleTree.SimpleTree;
import twg2.tuple.Tuples;

/**
 * @author TeamworkGuy2
 * @since 2015-12-5
 */
public class CsBlockParser implements AstExtractor<CsBlock> {
	public static int blockLoopCount = 0;
	public static int treeCount = 0;

	// TODO parsers only parse some fields and interface methods

	@Override
	public AstParser<List<List<String>>> createImportStatementParser() {
		return new CsUsingStatementExtractor();
	}


	@Override
	public AstParser<TypeSig.TypeSigSimple> createTypeParser() {
		var lang = CodeLanguageOptions.C_SHARP;
		return new TypeExtractor(lang, true);
	}


	@Override
	public AstParser<List<FieldDef>> createEnumParser(BlockAst<CsBlock> block, AstParser<List<String>> commentParser) {
		return new CsEnumMemberExtractor(CsKeyword.check, block, commentParser);
	}


	@Override
	public AstParser<List<AnnotationSig>> createAnnotationParser(BlockAst<CsBlock> block) {
		return new CsAnnotationExtractor();
	}


	@Override
	public AstParser<List<String>> createCommentParser(BlockAst<CsBlock> block) {
		var lang = CodeLanguageOptions.C_SHARP;
		return new CommentBlockExtractor(lang.displayName(), block);
	}


	@Override
	public AstParser<List<FieldSig>> createFieldParser(BlockAst<CsBlock> block, AstParser<List<AnnotationSig>> annotationParser, AstParser<List<String>> commentParser) {
		var lang = CodeLanguageOptions.C_SHARP;
		var typeParser = new TypeExtractor(lang, false);
		return new FieldExtractor(lang.displayName(), CsKeyword.check, block, typeParser, annotationParser, commentParser, lang.getAstUtil());
	}


	@Override
	public AstParser<List<MethodSigSimple>> createMethodParser(BlockAst<CsBlock> block, AstParser<List<AnnotationSig>> annotationParser, AstParser<List<String>> commentParser) {
		var lang = CodeLanguageOptions.C_SHARP;
		var typeParser = new TypeExtractor(lang, true);
		return new MethodExtractor(lang.displayName(), CsKeyword.check, lang.getOperatorUtil(), block, typeParser, annotationParser, commentParser);
	}


	@Override
	public List<Entry<SimpleTree<CodeToken>, ClassAst.SimpleImpl<CsBlock>>> extractClassFieldsAndMethodSignatures(SimpleTree<CodeToken> astTree) {
		return BlockExtractor.extractBlockFieldsAndInterfaceMethods(this, astTree);
	}


	@Override
	public List<BlockAst<CsBlock>> extractBlocks(List<String> nameScope, SimpleTree<CodeToken> astTree, BlockAst<CsBlock> parentScope) {
		List<BlockAst<CsBlock>> blocks = new ArrayList<>();
		var annotationExtractor = new CsAnnotationExtractor();
		_extractBlocksFromTree(nameScope, astTree, 0, null, parentScope, annotationExtractor, blocks);
		return blocks;
	}


	/** This method recursively loops over all nodes, adding/removing scope names on a name stack as named blocks of code are parsed (such as namespaces, or classes)
	 * @param nameScope the current name scope of the code (list of scope names that the current {@code blockTree} is inside of)
	 * @param blockTree the current sub-tree being parsed
	 * @param depth the current blockTree's depth within the tree (0=root node, 1=child of root, etc.)
	 * @param parentNode the current blockTree's parent node or null if the parent is null (only possible if blockTree is a child of a tree with a null root or blockTree is the root and has no parent)
	 */
	public static void _extractBlocksFromTree(List<String> nameScope, SimpleTree<CodeToken> blockTree,
			int depth, SimpleTree<CodeToken> parentNode, BlockAst<CsBlock> parentScope, CsAnnotationExtractor annotationExtractor, List<BlockAst<CsBlock>> blocks) {
		var lang = CodeLanguageOptions.C_SHARP;
		var keywordUtil = lang.getKeywordUtil();
		var children = blockTree.getChildren();
		treeCount++;

		//var childIter = (BaseList<SimpleTree<CodeToken>>.BaseListIterator)children.listIterator();
		var childIter = new EnhancedListIterator<SimpleTree<CodeToken>>(children); // this appears ~1% faster in total program time, slower using BaseList iterator (2020-11-21)

		while(childIter.hasNext()) {
			var child = childIter.next();
			blockLoopCount++;
			var token = child.getData();

			boolean annotAccepted = annotationExtractor.acceptNext(child);

			int addBlockCount = 0;

			// if this token is an opening block, then this is probably a valid block declaration
			if(AstFragType.isBlock(token, '{')) {
				if(childIter.hasPrevious()) {
					// read the identifier
					int mark = childIter.nextIndex();
					// since the current token is the opening '{', step back to the class signature
					childIter.previous();
					var nameCompoundRes = readClassIdentifierAndExtends(childIter);
					var prevNode = childIter.hasPrevious() ? childIter.previous() : null;

					// if a block keyword ("class", "interface", etc.) and an identifier were found, then this is probably a valid block declaration
					if(nameCompoundRes != null && prevNode != null && keywordUtil.blockModifiers().is(prevNode.getData())) {
						addBlockCount = 1;
						var blockTypeStr = prevNode.getData().getText();
						var blockType = lang.getBlockUtil().tryParseKeyword(keywordUtil.tryToKeyword(blockTypeStr));
						var accessModifiers = AccessModifierExtractor.readAccessModifiers(keywordUtil, childIter);
						// TODO we can't just join the access modifiers, defaultAccessModifier doesn't parse this way
						var accessStr = accessModifiers != null ? StringJoin.join(accessModifiers, " ") : null;
						var access = lang.getAstUtil().getAccessModifierParser().defaultAccessModifier(accessStr, blockType, parentScope != null ? parentScope.blockType : null);

						nameScope.add(nameCompoundRes.getKey());

						var blockSig = TypeExtractor.extractGenericTypes(NameUtil.joinFqName(nameScope), keywordUtil);
						var blockTypes = blockSig.isGeneric() ? blockSig.getParams() : Collections.<TypeSig.TypeSigSimple>emptyList();
						var blockFqName = NameUtil.splitFqName(blockSig.getTypeName());
						var annotations = new ArrayList<>(annotationExtractor.getParserResult());

						blocks.add(new BlockAst<>(new ClassSigSimple(blockFqName, blockTypes, access, annotations, blockTypeStr, nameCompoundRes.getValue()), child, blockType));
					}

					childIter.reset(mark);
				}
			}

			// a valid block must have 2 or more children: a 'name' and a '{...}' block
			// create a separate annotation extractor when extracting blocks within an annotation (not sure if this could ever happen)
			if(child.size() > 1) {
				_extractBlocksFromTree(nameScope, child, depth + 1, blockTree, parentScope, (annotAccepted ? annotationExtractor.copy() : annotationExtractor.recycle()), blocks);
				if(!annotAccepted) { annotationExtractor.recycle(); }
			}

			while(addBlockCount > 0) {
				nameScope.remove(nameScope.size() - 1);
				addBlockCount--;
			}
		}
	}


	/** Reads backward from a '{' block through a simple class signature ({@code ClassName [: ClassName]}).
	 * Returns the iterator where {@code next()} would return the class name element.
	 * @return {@code <className, extendImplementNames>}
	 */
	private static Entry<String, List<String>> readClassIdentifierAndExtends(ListIterator<SimpleTree<CodeToken>> iter) {
		var keywordUtil = CodeLanguageOptions.C_SHARP.getKeywordUtil();
		// class signatures are read backward from the opening '{'
		int prevCount = 0;
		var names = new ArrayList<String>();
		Entry<String, List<String>> nameCompoundRes = null;

		// get the first element and begin checking
		if(iter.hasPrevious()) { prevCount++; }
		SimpleTree<CodeToken> prevNode = iter.hasPrevious() ? iter.previous() : null;

		while(prevNode != null && AstFragType.isIdentifierOrKeyword(prevNode.getData()) && !keywordUtil.blockModifiers().is(prevNode.getData()) && !keywordUtil.isInheritanceKeyword(prevNode.getData().getText())) {
			// found an object initializer in the form 'new [Abc] {', not a class/interface definition so return nothing
			if(names.size() < 2 && CsKeyword.NEW.toSrc().equals(prevNode.getData().getText())) {
				break;
			}
			names.add(prevNode.getData().getText());
			prevNode = iter.hasPrevious() ? iter.previous() : null;
			if(iter.hasPrevious()) { prevCount++; }
		}

		// if the class signature extends/implements, then the identifiers just read are the class/interface names, next read the actual class name
		if(prevNode != null && prevNode.getData().getText().equals(":")) {
			prevNode = iter.hasPrevious() ? iter.previous() : null;
			if(iter.hasPrevious()) { prevCount++; }
			if(prevNode != null && AstFragType.isIdentifierOrKeyword(prevNode.getData()) && !keywordUtil.blockModifiers().is(prevNode.getData())) {
				Collections.reverse(names);
				var extendImplementNames = names;
				String className = prevNode.getData().getText();
				nameCompoundRes = Tuples.of(className, extendImplementNames);
			}
			else {
				throw new IllegalStateException("found block with extend/implement names, but no class name " + names);
			}
		}
		// else, we should have only read one name with the loop and it is the class name
		else if(names.size() == 1) {
			String className = names.get(0);
			nameCompoundRes = Tuples.of(className, Collections.emptyList());
			// move iterator forward since the while loop doesn't use the last value (i.e. reads one element past the valid elements it wants to consume)
			if(prevCount > 0) {
				iter.next();
			}
		}

		return nameCompoundRes;
	}

}
