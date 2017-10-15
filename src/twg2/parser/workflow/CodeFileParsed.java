package twg2.parser.workflow;

import twg2.annotations.Immutable;
import twg2.ast.interm.classes.ClassAst;
import twg2.ast.interm.classes.ClassSig;
import twg2.parser.codeParser.BlockType;
import twg2.parser.fragment.CodeToken;
import twg2.treeLike.simpleTree.SimpleTree;

/**
 * @author TeamworkGuy2
 * @since 2016-1-9
 */
@Immutable
public class CodeFileParsed<T_ID, T_CLASS extends ClassAst<? extends ClassSig, ? extends BlockType>> {
	public final T_ID id;
	public final T_CLASS parsedClass;
	public final SimpleTree<CodeToken> astTree;


	public CodeFileParsed(T_ID id, T_CLASS parsedClass, SimpleTree<CodeToken> astTree) {
		this.id = id;
		this.parsedClass = parsedClass;
		this.astTree = astTree;
	}



	@Immutable
	public static class Simple<T_BLOCK extends BlockType> extends CodeFileParsed<String, ClassAst.SimpleImpl<T_BLOCK>> {

		public Simple(String id, ClassAst.SimpleImpl<T_BLOCK> parsedClass, SimpleTree<CodeToken> astTree) {
			super(id, parsedClass, astTree);
		}

	}




	@Immutable
	public static class Intermediate<T_BLOCK extends BlockType> extends CodeFileParsed<CodeFileSrc, ClassAst.SimpleImpl<T_BLOCK>> {

		public Intermediate(CodeFileSrc id, ClassAst.SimpleImpl<T_BLOCK> parsedClass, SimpleTree<CodeToken> astTree) {
			super(id, parsedClass, astTree);
		}

	}




	@Immutable
	public static class Resolved<T_BLOCK extends BlockType> extends CodeFileParsed<CodeFileSrc, ClassAst.ResolvedImpl<T_BLOCK>> {

		public Resolved(CodeFileSrc id, ClassAst.ResolvedImpl<T_BLOCK> parsedClass, SimpleTree<CodeToken> astTree) {
			super(id, parsedClass, astTree);
		}

	}

}
