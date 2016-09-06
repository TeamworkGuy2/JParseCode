package twg2.parser.codeParser.java;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import lombok.val;
import twg2.ast.interm.annotation.AnnotationSig;
import twg2.ast.interm.block.BlockAst;
import twg2.ast.interm.classes.ClassAst;
import twg2.ast.interm.classes.ClassSig;
import twg2.ast.interm.field.FieldDef;
import twg2.ast.interm.field.FieldSig;
import twg2.ast.interm.method.MethodSig;
import twg2.ast.interm.type.TypeSig;
import twg2.parser.codeParser.AstExtractor;
import twg2.parser.codeParser.extractors.AccessModifierExtractor;
import twg2.parser.codeParser.extractors.BlockExtractor;
import twg2.parser.codeParser.extractors.CommentBlockExtractor;
import twg2.parser.codeParser.extractors.DataTypeExtractor;
import twg2.parser.codeParser.extractors.FieldExtractor;
import twg2.parser.codeParser.extractors.MethodExtractor;
import twg2.parser.codeParser.tools.NameUtil;
import twg2.parser.codeParser.tools.TokenListIterable;
import twg2.parser.documentParser.CodeFragment;
import twg2.parser.fragment.AstFragType;
import twg2.parser.language.CodeLanguageOptions;
import twg2.parser.stateMachine.AstParser;
import twg2.streams.EnhancedListBuilderIterator;
import twg2.text.stringUtils.StringJoin;
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
		val lang = CodeLanguageOptions.JAVA;
		return new DataTypeExtractor(lang, true);
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
		val lang = CodeLanguageOptions.JAVA;
		return new CommentBlockExtractor(lang.displayName(), block);
	}


	@Override
	public AstParser<List<FieldSig>> createFieldParser(BlockAst<JavaBlock> block, AstParser<List<AnnotationSig>> annotationParser, AstParser<List<String>> commentParser) {
		val lang = CodeLanguageOptions.JAVA;
		val typeParser = new DataTypeExtractor(lang, false);
		return new FieldExtractor(lang.displayName(), JavaKeyword.check, block, typeParser, annotationParser, commentParser, lang.getAstUtil());
	}


	@Override
	public AstParser<List<MethodSig.SimpleImpl>> createMethodParser(BlockAst<JavaBlock> block, AstParser<List<AnnotationSig>> annotationParser, AstParser<List<String>> commentParser) {
		val lang = CodeLanguageOptions.JAVA;
		val typeParser = new DataTypeExtractor(lang, true);
		return new MethodExtractor(lang.displayName(), JavaKeyword.check, block, typeParser, annotationParser, commentParser);
	}


	@Override
	public List<Entry<SimpleTree<CodeFragment>, ClassAst.SimpleImpl<JavaBlock>>> extractClassFieldsAndMethodSignatures(SimpleTree<CodeFragment> astTree) {
		return extractBlockFieldsAndInterfaceMethods(astTree);
	}


	@Override
	public List<BlockAst<JavaBlock>> extractBlocks(List<String> nameScope, SimpleTree<CodeFragment> astTree, BlockAst<JavaBlock> parentScope) {
		List<BlockAst<JavaBlock>> blocks = new ArrayList<>();
		// parse package name and push it into the name scope
		String pkgName = parsePackageDeclaration(astTree);
		if(pkgName == null) {
			pkgName = "(default package)";
		}
		nameScope.add(pkgName);

		_extractBlocksFromTree(nameScope, astTree, 0, null, parentScope, blocks);
		return blocks;
	}


	// TODO this only parses some fields and interface methods
	public List<Entry<SimpleTree<CodeFragment>, ClassAst.SimpleImpl<JavaBlock>>> extractBlockFieldsAndInterfaceMethods(SimpleTree<CodeFragment> tokenTree) {
		// TODO are all Java blocks valid blocks possibly containing fields/methods
		val blocks = BlockExtractor.extractBlockFieldsAndInterfaceMethods(this, tokenTree);
		return blocks;
	}


	/** This method recursively loops over all nodes, adding/removing scope names on a name stack as named blocks of code are parsed (such as namespaces, or classes)
	 * @param nameScope the current name scope of the code (list of scope names that the current {@code blockTree} is inside of)
	 * @param blockTree the current sub-tree being parsed
	 * @param depth the current blockTree's depth within the tree (0=root node, 1=child of root, etc.)
	 * @param parentNode the current blockTree's parent node or null if the parent is null (only possible if blockTree is a child of a tree with a null root or blockTree is the root and has no parent)
	 */
	public static void _extractBlocksFromTree(List<String> nameScope, SimpleTree<CodeFragment> blockTree,
			int depth, SimpleTree<CodeFragment> parentNode, BlockAst<JavaBlock> parentScope, List<BlockAst<JavaBlock>> blocks) {
		val lang = CodeLanguageOptions.JAVA;
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
					if(nameCompoundRes != null && nameCompoundRes.getKey() != null && prevNode != null && lang.getKeywordUtil().blockModifiers().is(prevNode.getData())) {
						addBlockCount = 1;
						val blockTypeStr = prevNode.getData().getText();
						val blockType = lang.getBlockUtil().tryParseKeyword(lang.getKeywordUtil().tryToKeyword(blockTypeStr));
						val accessModifiers = AccessModifierExtractor.readAccessModifiers(lang.getKeywordUtil(), childIter);
						// TODO we can't just join the access modifiers, defaultAccessModifier doesn't parse this way
						val accessStr = accessModifiers != null ? StringJoin.join(accessModifiers, " ") : null;
						val access = lang.getAstUtil().getAccessModifierParser().defaultAccessModifier(accessStr, blockType, parentScope != null ? parentScope.getBlockType() : null);

						nameScope.add(nameCompoundRes.getKey());

						val blockSig = DataTypeExtractor.extractGenericTypes(NameUtil.joinFqName(nameScope), lang.getKeywordUtil());
						val blockTypes = blockSig.isGeneric() ? blockSig.getParams() : Collections.<TypeSig.TypeSigSimple>emptyList();
						val blockFqName = NameUtil.splitFqName(blockSig.getTypeName());

						blocks.add(new BlockAst<>(new ClassSig.SimpleImpl(blockFqName, blockTypes, access, blockTypeStr, nameCompoundRes.getValue()), child, blockType));
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


	private static String parsePackageDeclaration(SimpleTree<CodeFragment> astTree) {
		val lang = CodeLanguageOptions.JAVA;
		List<SimpleTree<CodeFragment>> childs = null;
		if(astTree.hasChildren() && (childs = astTree.getChildren()).size() > 1) {
			if(lang.getAstUtil().isKeyword(childs.get(0).getData(), JavaKeyword.PACKAGE) &&
					AstFragType.isIdentifier(childs.get(1).getData())) {
				return childs.get(1).getData().getText();
			}
			return null;
		}
		return null;
	}


	/** Reads backward from a '{' block through a simple class signature ({@code ClassName [extends ClassName] [implements InterfaceNme]}).
	 * Returns the iterator where {@code next()} would return the class name element.
	 * @return {@code <className, extendImplementNames>}
	 */
	private static Entry<String, List<String>> readClassIdentifierAndExtends(EnhancedListBuilderIterator<SimpleTree<CodeFragment>> iter) {
		val lang = CodeLanguageOptions.JAVA;
		// class signatures are read backward from the opening '{'
		int prevCount = 0;
		val names = new ArrayList<String>();
		Entry<String, List<String>> nameCompoundRes = null;

		// get the first element and begin checking
		if(iter.hasPrevious()) { prevCount++; }
		SimpleTree<CodeFragment> prevNode = iter.hasPrevious() ? iter.previous() : null;

		// TODO should read ', ' between each name, currently only works with 1 extend/implement class name
		while(prevNode != null && AstFragType.isIdentifierOrKeyword(prevNode.getData()) && !lang.getKeywordUtil().blockModifiers().is(prevNode.getData())) {
			names.add(prevNode.getData().getText());
			prevNode = iter.hasPrevious() ? iter.previous() : null;
			if(iter.hasPrevious()) { prevCount++; }
		}

		// if the class signature extends/implements, then the identifiers just read are the class/interface names, next read the actual class name
		if(prevNode != null && lang.getAstUtil().isKeyword(prevNode.getData(), JavaKeyword.EXTENDS, JavaKeyword.IMPLEMENTS)) {
			prevNode = iter.hasPrevious() ? iter.previous() : null;
			if(iter.hasPrevious()) { prevCount++; }
			if(prevNode != null && AstFragType.isIdentifierOrKeyword(prevNode.getData()) && !lang.getKeywordUtil().blockModifiers().is(prevNode.getData())) {
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
