package twg2.ast.interm.method;

import java.io.IOException;
import java.util.List;

import lombok.Getter;
import lombok.val;
import twg2.annotations.Immutable;
import twg2.ast.interm.annotation.AnnotationSig;
import twg2.ast.interm.type.TypeSig;
import twg2.io.json.stringify.JsonStringify;
import twg2.parser.codeParser.AccessModifier;
import twg2.parser.codeParser.tools.NameUtil;
import twg2.parser.output.JsonWritableSig;
import twg2.parser.output.WriteSettings;
import twg2.text.stringEscape.StringEscapeJson;

/**
 * @author TeamworkGuy2
 * @since 2015-11-24
 */
public interface MethodSig {


	/**
	 * @author TeamworkGuy2
	 * @since 2016-1-3
	 * @param <T_PARAM> the type of {@link ParameterSig} parameters of this method
	 */
	// TODO also support simple/resolved return type and annotations
	@Immutable
	public static class Impl<T_PARAM extends JsonWritableSig, T_RET extends JsonWritableSig> implements JsonWritableSig {
		private final @Getter String name;
		private final @Getter List<String> fullName;
		private final @Getter List<T_PARAM> paramSigs;
		private final @Getter T_RET returnType;
		private final @Getter List<AccessModifier> accessModifiers;
		private final @Getter List<AnnotationSig> annotations;
		private final @Getter List<String> comments;


		public Impl(String name, List<String> fullName, List<? extends T_PARAM> paramSigs,
				T_RET returnType, List<? extends AccessModifier> accessModifiers, List<? extends AnnotationSig> annotations, List<String> comments) {
			@SuppressWarnings("unchecked")
			val paramSigsCast = (List<T_PARAM>)paramSigs;
			@SuppressWarnings("unchecked")
			val accessModifiersCast = (List<AccessModifier>)accessModifiers;
			@SuppressWarnings("unchecked")
			val annotationsCast = (List<AnnotationSig>)annotations;

			this.name = name;
			this.fullName = fullName;
			this.paramSigs = paramSigsCast;
			this.returnType = returnType;
			this.accessModifiers = accessModifiersCast;
			this.annotations = annotationsCast;
			this.comments = comments;
		}


		@Override
		public void toJson(Appendable dst, WriteSettings st) throws IOException {
			dst.append("{ ");
			dst.append("\"name\": \"" + (st.fullMethodName ? NameUtil.joinFqName(fullName) : fullName.get(fullName.size() - 1)) + "\", ");

			dst.append("\"parameters\": [");
			JsonStringify.joinConsume(paramSigs, ", ", dst, (param) -> param.toJson(dst, st));
			dst.append("], ");

			dst.append("\"accessModifiers\": [");
			JsonStringify.join(accessModifiers, ", ", dst, (acs) -> '"' + acs.toSrc() + '"');
			dst.append("], ");

			dst.append("\"annotations\": [");
			JsonStringify.joinConsume(annotations, ", ", dst, (ann) -> ann.toJson(dst, st));
			dst.append("], ");

			dst.append("\"returnType\": ");
			returnType.toJson(dst, st);
			dst.append(", ");

			dst.append("\"comments\": [");
			JsonStringify.joinConsume(comments, ", ", dst, (str) -> { dst.append('"'); StringEscapeJson.toJsonString(str, 0, str.length(), dst); dst.append('"'); });
			dst.append("]");

			dst.append(" }");
		}


		@Override
		public String toString() {
			return returnType + " " + NameUtil.joinFqName(fullName) + paramSigs;
		}

	}




	@Immutable
	public static class SimpleImpl extends Impl<ParameterSig, TypeSig.TypeSigSimple> {

		public SimpleImpl(String name, List<String> fullName, List<? extends ParameterSig> paramSigs,
				TypeSig.TypeSigSimple returnType, List<? extends AccessModifier> accessModifiers, List<? extends AnnotationSig> annotations, List<String> comments) {
			super(name, fullName, paramSigs, returnType, accessModifiers, annotations, comments);
		}

	}




	@Immutable
	public static class ResolvedImpl extends Impl<ParameterSigResolved, TypeSig.TypeSigResolved> {

		public ResolvedImpl(String name, List<String> fullName, List<? extends ParameterSigResolved> paramSigs,
				TypeSig.TypeSigResolved returnType, List<? extends AccessModifier> accessModifiers, List<? extends AnnotationSig> annotations, List<String> comments) {
			super(name, fullName, paramSigs, returnType, accessModifiers, annotations, comments);
		}

	}

}
