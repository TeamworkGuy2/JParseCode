package twg2.ast.interm.field;

import java.io.IOException;
import java.util.List;

import lombok.Getter;
import lombok.val;
import twg2.annotations.Immutable;
import twg2.ast.interm.annotation.AnnotationSig;
import twg2.ast.interm.type.TypeSig.TypeSigResolved;
import twg2.io.json.stringify.JsonStringify;
import twg2.parser.codeParser.AccessModifier;
import twg2.parser.codeParser.tools.NameUtil;
import twg2.parser.fragment.CodeToken;
import twg2.parser.output.WriteSettings;
import twg2.treeLike.simpleTree.SimpleTree;

/** A {@link FieldSigResolved} with an initializer
 * @author TeamworkGuy2
 * @since 2016-09-04
 */
@Immutable
public class FieldDefResolved extends FieldSigResolved {
	private final @Getter SimpleTree<CodeToken> initializer;


	public FieldDefResolved(String name, List<String> fullName, TypeSigResolved fieldType, List<AccessModifier> accessModifiers,
			List<AnnotationSig> annotations, List<String> comments, SimpleTree<CodeToken> initializer) {
		super(name, fullName, fieldType, accessModifiers, annotations, comments);
		this.initializer = initializer;
	}


	@Override
	public void toJson(Appendable dst, WriteSettings st) throws IOException {
		val json = JsonStringify.inst;

		dst.append("{ ");
		json.toProp("name", (st.fullFieldName ? NameUtil.joinFqName(fullName) : fullName.get(fullName.size() - 1)), dst);

		json.comma(dst).propName("type", dst);
		fieldType.toJson(dst, st);

		json.comma(dst).propName("accessModifiers", dst)
			.toStringArray(accessModifiers, dst, (acs) -> acs.toSrc());

		json.comma(dst).propName("annotations", dst)
			.toArrayConsume(annotations, dst, (ann) -> ann.toJson(dst, st));

		FieldDef.initializerToJson(initializer, true, dst, st);

		json.comma(dst).propName("comments", dst)
			.toStringArray(comments, dst);

		dst.append(" }");
	}


	@Override
	public String toString() {
		return fieldType + " " + NameUtil.joinFqName(fullName);
	}

}
