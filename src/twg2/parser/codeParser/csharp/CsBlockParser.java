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
import twg2.ast.interm.method.MethodSigSimple;
import twg2.ast.interm.type.TypeSig;
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
		return new CommentBlockExtractor(lang.displayName());
	}


	@Override
	public AstParser<List<FieldDef>> createFieldParser(BlockAst<CsBlock> block, AstParser<List<AnnotationSig>> annotationParser, AstParser<List<String>> commentParser) {
		var lang = CodeLanguageOptions.C_SHARP;
		var typeParser = new TypeExtractor(lang, false);
		return new FieldExtractor(lang.displayName(), CsKeyword.check, CsOperator.check, block, typeParser, annotationParser, commentParser, lang.getAstUtil());
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
		var commentExtractor = createCommentParser(parentScope);
		extractBlocksFromTree(nameScope, astTree, 0, null, parentScope, annotationExtractor, commentExtractor, blocks);
		return blocks;
	}


	/** This method recursively loops over all nodes, adding/removing scope names on a name stack as named blocks of code are parsed (such as namespaces, or classes)
	 * @param nameScope the current name scope of the code (list of scope names that the current {@code blockTree} is inside of)
	 * @param blockTree the current sub-tree being parsed
	 * @param depth the current blockTree's depth within the tree (0=root node, 1=child of root, etc.)
	 * @param parentNode the current blockTree's parent node or null if the parent is null (only possible if blockTree is a child of a tree with a null root or blockTree is the root and has no parent)
	 */
	public static void extractBlocksFromTree(List<String> nameScope, SimpleTree<CodeToken> blockTree, int depth, SimpleTree<CodeToken> parentNode, BlockAst<CsBlock> parentScope,
			AstParser<List<AnnotationSig>> annotationExtractor, AstParser<List<String>> commentExtractor, List<BlockAst<CsBlock>> blocks) {
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
			boolean commentAccepted = commentExtractor.acceptNext(child) || annotAccepted; // keep comments if annotation was accepted so that comments before an annotation carry over if there is a block following the annotation(s)

			int addBlockCount = 0;
			BlockAst<CsBlock> newestBlock = null;

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
						var blockType = lang.getBlockUtil().tryToBlock(keywordUtil.tryToKeyword(blockTypeStr));
						var accessModifiers = AccessModifierExtractor.readAccessModifiers(keywordUtil, childIter);
						var access = lang.getAstUtil().getAccessModifierParser().defaultAccessModifier(accessModifiers, blockType, parentScope != null ? parentScope.blockType : null);

						nameScope.add(nameCompoundRes.getKey());

						var blockSig = TypeExtractor.extractGenericTypes(NameUtil.joinFqName(nameScope), keywordUtil);
						var blockTypes = blockSig.isGeneric() ? blockSig.getParams() : Collections.<TypeSig.TypeSigSimple>emptyList();
						var blockFqName = NameUtil.splitFqName(blockSig.getTypeName());
						var annotations = new ArrayList<>(annotationExtractor.getParserResult());
						var comments = new ArrayList<>(commentExtractor.getParserResult());

						newestBlock = new BlockAst<>(new ClassSigSimple(blockFqName, blockTypes, access, annotations, comments, blockTypeStr, nameCompoundRes.getValue()), child, blockType);
						blocks.add(newestBlock);
					}

					childIter.reset(mark);
				}
			}

			// a valid block must have 2 or more children: a 'name' and a '{...}' block
			// create a separate annotation extractor when extracting blocks within an annotation (not sure if this could ever happen)
			if(child.size() > 1) {
				var childAnnotExtractor = annotAccepted ? annotationExtractor.copy() : annotationExtractor.recycle();
				var childCommentExtractor = commentAccepted ? commentExtractor.copy() : commentExtractor.recycle();
				extractBlocksFromTree(nameScope, child, depth + 1, blockTree, newestBlock != null ? newestBlock : parentScope, childAnnotExtractor, childCommentExtractor, blocks);
				if(!annotAccepted) { annotationExtractor.recycle(); }
				if(!commentAccepted) { commentExtractor.recycle(); }
			}

			while(addBlockCount > 0) {
				nameScope.remove(nameScope.size() - 1);
				addBlockCount--;
			}
		}
	}


	/** Reads backward from a '{' block through a simple class signature ({@code ClassName [: ClassName, ClassName, ...]}).
	 * Returns the iterator where {@code next()} would return the class name element.
	 * @return {@code <className, extendImplementNames[]>}
	 */
	private static Entry<String, List<String>> readClassIdentifierAndExtends(ListIterator<SimpleTree<CodeToken>> iter) {
		var keywordUtil = CodeLanguageOptions.C_SHARP.getKeywordUtil();
		// class signatures are read backward from the opening '{'
		int prevCount = 0;
		var names = new ArrayList<String>();
		boolean lastNodeWasInheritanceKeyword = false;
		Entry<String, List<String>> nameCompoundRes = null;

		// get the node and begin checking backward
		if(iter.hasPrevious()) { prevCount++; }
		SimpleTree<CodeToken> prevNode = iter.hasPrevious() ? iter.previous() : null;

		while(prevNode != null && (AstFragType.isIdentifierOrKeyword(prevNode.getData()) || keywordUtil.isInheritanceKeyword(prevNode.getData().getText())) && !keywordUtil.blockModifiers().is(prevNode.getData())) {
			// found an object initializer in the form 'new [Abc] {', not a class/interface definition so return nothing
			if(names.size() < 2 && CsKeyword.NEW.toSrc().equals(prevNode.getData().getText())) {
				break;
			}

			if(!keywordUtil.isInheritanceKeyword(prevNode.getData().getText())) {
				names.add(prevNode.getData().getText());
				lastNodeWasInheritanceKeyword = false;
			}
			else {
				lastNodeWasInheritanceKeyword = true;
			}

			if(iter.hasPrevious()) { prevCount++; }
			prevNode = iter.hasPrevious() ? iter.previous() : null;
		}

		// syntax check, ensure there's a class name identifier between the block modifier and inheritance keyword ':'
		if(lastNodeWasInheritanceKeyword) {
			throw new IllegalStateException("found class inheritance keyword ':' with no class name preceeding it, found '" + String.valueOf(prevNode) + "'");
		}

		// move iterator forward since the while loop reads past the class name to the first block modifier
		if(prevCount > 0) {
			iter.next();
		}

		// if a likely valid class block modifier has been reached, then the identifiers just read are the class/interface names and the class name
		if(prevNode != null && keywordUtil.blockModifiers().is(prevNode.getData())) {
			// TODO this is valid for the code: 'method<T>() where T : class { ... }'
			if(names.size() == 0) {
				return null;
				//throw new IllegalStateException("found block with no name");
			}

			String className = names.remove(names.size() - 1);
			if(names.size() > 1) { Collections.reverse(names); }
			var extendImplementNames = names;
			nameCompoundRes = Tuples.of(className, extendImplementNames);
		}

		return nameCompoundRes;
	}

}
