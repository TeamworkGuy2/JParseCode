package twg2.parser.codeParser;

import lombok.AllArgsConstructor;
import lombok.Getter;
import twg2.parser.baseAst.CompoundBlock;
import twg2.parser.documentParser.DocumentFragmentText;
import twg2.parser.intermAst.classes.IntermClass;
import twg2.parser.intermAst.classes.IntermClassSig;
import twg2.parser.output.JsonWritableSig;
import twg2.treeLike.simpleTree.SimpleTree;

/**
 * @author TeamworkGuy2
 * @since 2016-1-9
 */
@AllArgsConstructor
public class CodeFileParsed<T_ID, T_CLASS extends IntermClass<? extends IntermClassSig, ? extends JsonWritableSig, ? extends CompoundBlock>> {
	@Getter T_ID id;
	@Getter T_CLASS parsedClass;
	@Getter SimpleTree<DocumentFragmentText<CodeFragmentType>> astTree;




	public static class Simple<T_ID, T_BLOCK extends CompoundBlock> extends CodeFileParsed<T_ID, IntermClass.SimpleImpl<T_BLOCK>> {

		public Simple(T_ID id, IntermClass.SimpleImpl<T_BLOCK> parsedClass, SimpleTree<DocumentFragmentText<CodeFragmentType>> astTree) {
			super(id, parsedClass, astTree);
		}

	}




	public static class Resolved<T_ID, T_BLOCK extends CompoundBlock> extends CodeFileParsed<T_ID, IntermClass.ResolvedImpl<T_BLOCK>> {

		public Resolved(T_ID id, IntermClass.ResolvedImpl<T_BLOCK> parsedClass, SimpleTree<DocumentFragmentText<CodeFragmentType>> astTree) {
			super(id, parsedClass, astTree);
		}

	}

}
