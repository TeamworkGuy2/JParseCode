package twg2.ast.interm.method;

import java.io.IOException;
import java.util.List;

import twg2.annotations.Immutable;
import twg2.ast.interm.annotation.AnnotationSig;
import twg2.io.json.stringify.JsonStringify;
import twg2.parser.codeParser.Keyword;
import twg2.parser.output.JsonWritableSig;
import twg2.parser.output.WriteSettings;

/** Represents a method parameter
 * @author TeamworkGuy2
 * @since 2016-1-3
 */
@Immutable
public class ParameterSig implements JsonWritableSig {
	public final String name;
	public final String typeSimpleName;
	public final List<Keyword> parameterModifiers;
	public final List<AnnotationSig> annotations;
	public final boolean optional;
	public final String defaultValue;


	public ParameterSig(String name, String typeSimpleName, List<? extends Keyword> parameterModifiers, List<? extends AnnotationSig> annotations, boolean optional, String defaultValue) {
		@SuppressWarnings("unchecked")
		var paramModifiersCast = (List<Keyword>)parameterModifiers;
		@SuppressWarnings("unchecked")
		var annotationsCast = (List<AnnotationSig>)annotations;

		this.name = name;
		this.typeSimpleName = typeSimpleName;
		this.parameterModifiers = paramModifiersCast;
		this.annotations = annotationsCast;
		this.optional = optional;
		this.defaultValue = defaultValue;
	}


	@Override
	public void toJson(Appendable dst, WriteSettings st) throws IOException {
		var json = JsonStringify.inst;

		dst.append("{ ");

		json.toProp("type", typeSimpleName, dst);

		json.comma(dst).toProp("name", name, dst);

		json.comma(dst).propName("parameterModifiers", dst)
			.toStringArray(parameterModifiers, dst, (acs) -> acs.toSrc());

		if(annotations != null && annotations.size() > 0) {
			json.comma(dst).propName("annotations", dst)
				.toArrayConsume(annotations, dst, (ann) -> ann.toJson(dst, st));
		}

		if(optional) {
			json.comma(dst).toProp("optional", optional, dst);
		}

		if(defaultValue != null) {
			json.comma(dst).toProp("defaultValue", defaultValue, dst);
		}

		dst.append(" }");
	}


	@Override
	public String toString() {
		return typeSimpleName + " " + (optional ? "?" : "") + name + (defaultValue != null ? " = " + defaultValue : "");
	}

}
