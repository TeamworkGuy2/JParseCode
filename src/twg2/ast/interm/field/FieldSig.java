package twg2.ast.interm.field;

import java.io.IOException;
import java.util.List;

import lombok.Getter;
import twg2.annotations.Immutable;
import twg2.ast.interm.annotation.AnnotationSig;
import twg2.ast.interm.type.TypeSig;
import twg2.ast.interm.type.TypeSig.TypeSigSimple;
import twg2.io.json.stringify.JsonStringify;
import twg2.parser.codeParser.Keyword;
import twg2.parser.codeParser.tools.NameUtil;
import twg2.parser.output.JsonWritableSig;
import twg2.parser.output.WriteSettings;

/** Represents a class field
 * @author TeamworkGuy2
 * @since 2015-12-4
 */
@Immutable
public class FieldSig implements JsonWritableSig {
	final @Getter String name;
	final @Getter List<String> fullName;
	final @Getter TypeSig.TypeSigSimple fieldType;
	final @Getter List<Keyword> accessModifiers;
	final @Getter List<AnnotationSig> annotations;
	final @Getter List<String> comments;


	public FieldSig(String name, List<String> fullName, TypeSigSimple fieldType, List<? extends Keyword> accessModifiers, List<? extends AnnotationSig> annotations, List<String> comments) {
		@SuppressWarnings("unchecked")
		var accessModifiersCast = (List<Keyword>)accessModifiers;
		@SuppressWarnings("unchecked")
		var annotationsCast = (List<AnnotationSig>)annotations;

		this.name = name;
		this.fullName = fullName;
		this.fieldType = fieldType;
		this.accessModifiers = accessModifiersCast;
		this.annotations = annotationsCast;
		this.comments = comments;
	}


	@Override
	public void toJson(Appendable dst, WriteSettings st) throws IOException {
		var json = JsonStringify.inst;

		dst.append("{ ");
		json.toProp("name", (st.fullFieldName ? NameUtil.joinFqName(fullName) : fullName.get(fullName.size() - 1)), dst);

		json.comma(dst).propName("type", dst);
		fieldType.toJson(dst, st);

		json.comma(dst).propName("accessModifiers", dst)
			.toStringArray(accessModifiers, dst, (acs) -> acs.toSrc());

		if(annotations != null && annotations.size() > 0) {
			json.comma(dst).propName("annotations", dst)
				.toArrayConsume(annotations, dst, (ann) -> ann.toJson(dst, st));
		}

		json.comma(dst).propName("comments", dst)
			.toStringArray(comments, dst);

		dst.append(" }");
	}


	@Override
	public String toString() {
		return fieldType + " " + NameUtil.joinFqName(fullName);
	}

}
