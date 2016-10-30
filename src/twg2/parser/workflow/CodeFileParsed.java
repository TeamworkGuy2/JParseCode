package twg2.parser.workflow;

import lombok.AllArgsConstructor;
import lombok.Getter;
import twg2.ast.interm.classes.ClassAst;
import twg2.ast.interm.classes.ClassSig;
import twg2.parser.codeParser.BlockType;
import twg2.parser.fragment.CodeToken;
import twg2.parser.output.JsonWritableSig;
import twg2.treeLike.simpleTree.SimpleTree;

/**
 * @author TeamworkGuy2
 * @since 2016-1-9
 */
@AllArgsConstructor
public class CodeFileParsed<T_ID, T_CLASS extends ClassAst<? extends ClassSig, ? extends JsonWritableSig, ? extends BlockType>> {
	@Getter T_ID id;
	@Getter T_CLASS parsedClass;
	@Getter SimpleTree<CodeToken> astTree;




	public static class Simple<T_ID, T_BLOCK extends BlockType> extends CodeFileParsed<T_ID, ClassAst.SimpleImpl<T_BLOCK>> {

		public Simple(T_ID id, ClassAst.SimpleImpl<T_BLOCK> parsedClass, SimpleTree<CodeToken> astTree) {
			super(id, parsedClass, astTree);
		}

	}




	public static class Resolved<T_ID, T_BLOCK extends BlockType> extends CodeFileParsed<T_ID, ClassAst.ResolvedImpl<T_BLOCK>> {

		public Resolved(T_ID id, ClassAst.ResolvedImpl<T_BLOCK> parsedClass, SimpleTree<CodeToken> astTree) {
			super(id, parsedClass, astTree);
		}

	}

}
