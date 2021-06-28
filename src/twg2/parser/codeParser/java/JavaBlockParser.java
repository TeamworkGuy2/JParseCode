package twg2.parser.codeParser.java;

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
import twg2.collections.interfaces.ListReadOnly;
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
public class JavaBlockParser implements AstExtractor<JavaBlock> {


	@Override
	public AstParser<List<List<String>>> createImportStatementParser() {
		return new JavaImportStatementExtractor();
	}


	@Override
	public AstParser<TypeSig.TypeSigSimple> createTypeParser() {
		var lang = CodeLanguageOptions.JAVA;
		return new TypeExtractor(lang, true);
	}


	@Override
	public AstParser<List<FieldDef>> createEnumParser(BlockAst<JavaBlock> block, AstParser<List<String>> commentParser) {
		return new JavaEnumMemberExtractor(JavaKeyword.check, block, commentParser);
	}


	@Override
	public AstParser<List<AnnotationSig>> createAnnotationParser(BlockAst<JavaBlock> block) {
		return new JavaAnnotationExtractor();
	}


	@Override
	public AstParser<List<String>> createCommentParser(BlockAst<JavaBlock> block) {
		var lang = CodeLanguageOptions.JAVA;
		return new CommentBlockExtractor(lang.displayName(), block);
	}


	@Override
	public AstParser<List<FieldDef>> createFieldParser(BlockAst<JavaBlock> block, AstParser<List<AnnotationSig>> annotationParser, AstParser<List<String>> commentParser) {
		var lang = CodeLanguageOptions.JAVA;
		var typeParser = new TypeExtractor(lang, false);
		return new FieldExtractor(lang.displayName(), JavaKeyword.check, JavaOperator.check, block, typeParser, annotationParser, commentParser, lang.getAstUtil());
	}


	@Override
	public AstParser<List<MethodSigSimple>> createMethodParser(BlockAst<JavaBlock> block, AstParser<List<AnnotationSig>> annotationParser, AstParser<List<String>> commentParser) {
		var lang = CodeLanguageOptions.JAVA;
		var typeParser = new TypeExtractor(lang, true);
		return new MethodExtractor(lang.displayName(), JavaKeyword.check, lang.getOperatorUtil(), block, typeParser, annotationParser, commentParser);
	}


	// TODO this only parses some fields and interface methods
	@Override
	public List<Entry<SimpleTree<CodeToken>, ClassAst.SimpleImpl<JavaBlock>>> extractClassFieldsAndMethodSignatures(SimpleTree<CodeToken> astTree) {
		// TODO are all Java blocks valid blocks possibly containing fields/methods
		return BlockExtractor.extractBlockFieldsAndInterfaceMethods(this, astTree);
	}


	@Override
	public List<BlockAst<JavaBlock>> extractBlocks(List<String> nameScope, SimpleTree<CodeToken> astTree, BlockAst<JavaBlock> parentScope) {
		List<BlockAst<JavaBlock>> blocks = new ArrayList<>();
		var annotationExtractor = new JavaAnnotationExtractor();
		// parse package name and push it into the name scope
		String pkgName = parsePackageDeclaration(astTree);
		if(pkgName == null) {
			pkgName = "(default package)";
		}
		nameScope.add(pkgName);

		extractBlocksFromTree(nameScope, astTree, 0, null, parentScope, annotationExtractor, blocks);
		return blocks;
	}


	/** This method recursively loops over all nodes, adding/removing scope names on a name stack as named blocks of code are parsed (such as namespaces, or classes)
	 * @param nameScope the current name scope of the code (list of scope names that the current {@code blockTree} is inside of)
	 * @param blockTree the current sub-tree being parsed
	 * @param depth the current blockTree's depth within the tree (0=root node, 1=child of root, etc.)
	 * @param parentNode the current blockTree's parent node or null if the parent is null (only possible if blockTree is a child of a tree with a null root or blockTree is the root and has no parent)
	 */
	public static void extractBlocksFromTree(List<String> nameScope, SimpleTree<CodeToken> blockTree,
			int depth, SimpleTree<CodeToken> parentNode, BlockAst<JavaBlock> parentScope, JavaAnnotationExtractor annotationExtractor, List<BlockAst<JavaBlock>> blocks) {
		var lang = CodeLanguageOptions.JAVA;
		var keywordUtil = lang.getKeywordUtil();
		var children = blockTree.getChildren();

		//var childIter = (BaseList<SimpleTree<CodeToken>>.BaseListIterator)children.listIterator();
		var childIter = new EnhancedListIterator<SimpleTree<CodeToken>>(children); // this appears ~1% faster in total program time, slower using BaseList iterator (2020-11-21)

		while(childIter.hasNext()) {
			var child = childIter.next();
			var token = child.getData();

			boolean annotAccepted = annotationExtractor.acceptNext(child);

			int addBlockCount = 0;
			BlockAst<JavaBlock> newestBlock = null;

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

						newestBlock = new BlockAst<>(new ClassSigSimple(blockFqName, blockTypes, access, annotations, blockTypeStr, nameCompoundRes.getValue()), child, blockType);
						blocks.add(newestBlock);
					}

					childIter.reset(mark);
				}
			}

			// a valid block must have 2 or more children: a 'name' and a '{...}' block
			if(child.size() > 1) {
				extractBlocksFromTree(nameScope, child, depth + 1, blockTree, newestBlock != null ? newestBlock : parentScope, (annotAccepted ? annotationExtractor.copy() : annotationExtractor.recycle()), blocks);
				if(!annotAccepted) { annotationExtractor.recycle(); }
			}

			while(addBlockCount > 0) {
				nameScope.remove(nameScope.size() - 1);
				addBlockCount--;
			}
		}
	}


	private static String parsePackageDeclaration(SimpleTree<CodeToken> astTree) {
		var lang = CodeLanguageOptions.JAVA;
		ListReadOnly<SimpleTree<CodeToken>> childs = null;
		if(astTree.hasChildren() && (childs = astTree.getChildren()).size() > 1) {
			if(lang.getAstUtil().isKeyword(childs.get(0).getData(), JavaKeyword.PACKAGE) &&
					AstFragType.isIdentifier(childs.get(1).getData())) {
				return childs.get(1).getData().getText();
			}
			return null;
		}
		return null;
	}


	/** Reads backward from a '{' block through a simple class signature ({@code ClassName [extends ClassName] [implements InterfaceName, InterfaceName, ...]}).
	 * Returns the iterator where {@code next()} would return the class name element.
	 * @return {@code <className, extendImplementNames[]>}
	 */
	private static Entry<String, List<String>> readClassIdentifierAndExtends(ListIterator<SimpleTree<CodeToken>> iter) {
		var keywordUtil = CodeLanguageOptions.JAVA.getKeywordUtil();
		// class signatures are read backward from the opening '{'
		int prevCount = 0;
		var names = new ArrayList<String>();
		boolean lastNodeWasInheritanceKeyword = false;
		Entry<String, List<String>> nameCompoundRes = null;

		// get the node and begin checking backward
		if(iter.hasPrevious()) { prevCount++; }
		SimpleTree<CodeToken> prevNode = iter.hasPrevious() ? iter.previous() : null;

		// read all identifiers until a block modifier is reached, including inheritance keywords (i.e. 'extends' and 'implements')
		// this creates a backward list of 'implements' class names, then 'extends' class names, then the actual class name
		while(prevNode != null && AstFragType.isIdentifierOrKeyword(prevNode.getData()) && !keywordUtil.blockModifiers().is(prevNode.getData())) {
			if(!keywordUtil.isInheritanceKeyword(prevNode.getData().getText())) {
				names.add(prevNode.getData().getText());
				lastNodeWasInheritanceKeyword = false;
			}
			// basic syntax checking to make sure there's an identifier between 'extends'/'implements' keywords
			else {
				if(lastNodeWasInheritanceKeyword) {
					throw new IllegalStateException("found two adjacent 'extends'/'implements' keywords with no intermediate class name");
				}
				lastNodeWasInheritanceKeyword = true;
			}

			if(iter.hasPrevious()) { prevCount++; }
			prevNode = iter.hasPrevious() ? iter.previous() : null;
		}

		// syntax check, ensure there's a class name identifier between the block modifier and 'extends'/'implements' keyword
		if(lastNodeWasInheritanceKeyword) {
			throw new IllegalStateException("found 'extends'/'implements' keyword but no class name");
		}

		// move iterator forward since the while loop reads past the class name to the first block modifier
		if(prevCount > 0) {
			iter.next();
		}

		// if a likely valid class block modifier has been reached, then the identifiers just read are the class/interface names and the class name
		if(prevNode != null && keywordUtil.blockModifiers().is(prevNode.getData())) {
			if(names.size() == 0) {
				throw new IllegalStateException("found block with no name");
			}
			String className = names.remove(names.size() - 1);
			if(names.size() > 1) { Collections.reverse(names); }
			var extendImplementNames = names;
			nameCompoundRes = Tuples.of(className, extendImplementNames);
		}

		return nameCompoundRes;
	}

}
