package twg2.ast.interm.method;

import java.io.IOException;
import java.util.List;

import twg2.annotations.Immutable;
import twg2.ast.interm.annotation.AnnotationSig;
import twg2.io.json.stringify.JsonStringify;
import twg2.parser.codeParser.Keyword;
import twg2.parser.codeParser.tools.NameUtil;
import twg2.parser.output.JsonWritableSig;
import twg2.parser.output.WriteSettings;

/**
 * @author TeamworkGuy2
 * @since 2015-11-24
 * @param <T_PARAM> the type of {@link ParameterSig} parameters of this method
 * @param <T_TYPE> the type of {@link TypeSig} returned by this method and accepted as generic type parameters
 */
@Immutable
public class MethodSig<T_PARAM extends JsonWritableSig, T_TYPE extends JsonWritableSig> implements JsonWritableSig {
	public final String name;
	public final List<String> fullName;
	public final List<T_PARAM> paramSigs;
	public final T_TYPE returnType;
	public final List<Keyword> accessModifiers;
	public final List<T_TYPE> typeParameters;
	public final List<AnnotationSig> annotations;
	public final List<String> comments;


	public MethodSig(String name, List<String> fullName, List<? extends T_PARAM> paramSigs,
			T_TYPE returnType, List<? extends Keyword> accessModifiers, List<? extends T_TYPE> typeParameters, List<? extends AnnotationSig> annotations, List<String> comments) {
		@SuppressWarnings("unchecked")
		var paramSigsCast = (List<T_PARAM>)paramSigs;
		@SuppressWarnings("unchecked")
		var accessModifiersCast = (List<Keyword>)accessModifiers;
		@SuppressWarnings("unchecked")
		var typeParamsCast = (List<T_TYPE>)typeParameters;
		@SuppressWarnings("unchecked")
		var annotationsCast = (List<AnnotationSig>)annotations;

		this.name = name;
		this.fullName = fullName;
		this.paramSigs = paramSigsCast;
		this.returnType = returnType;
		this.accessModifiers = accessModifiersCast;
		this.typeParameters = typeParamsCast;
		this.annotations = annotationsCast;
		this.comments = comments;
	}


	@Override
	public void toJson(Appendable dst, WriteSettings st) throws IOException {
		var json = JsonStringify.inst;

		dst.append("{ ");
		json.toProp("name", (st.fullMethodName ? NameUtil.joinFqName(fullName) : fullName.get(fullName.size() - 1)), dst);

		json.comma(dst).propName("parameters", dst)
			.toArrayConsume(paramSigs, dst, (param) -> param.toJson(dst, st));

		json.comma(dst).propName("accessModifiers", dst)
			.toStringArray(accessModifiers, dst, (acs) -> acs.toSrc());

		if(typeParameters != null && typeParameters.size() > 0) {
			json.comma(dst).propName("typeParameters", dst)
				.toArrayConsume(typeParameters, dst, (type) -> type.toJson(dst, st));
		}

		if(annotations != null && annotations.size() > 0) {
			json.comma(dst).propName("annotations", dst)
				.toArrayConsume(annotations, dst, (ann) -> ann.toJson(dst, st));
		}

		json.comma(dst).propName("returnType", dst);
		returnType.toJson(dst, st);

		json.comma(dst).propName("comments", dst)
			.toStringArray(comments, dst);

		dst.append(" }");
	}


	@Override
	public String toString() {
		return returnType + " " + NameUtil.joinFqName(fullName) + paramSigs;
	}

}
