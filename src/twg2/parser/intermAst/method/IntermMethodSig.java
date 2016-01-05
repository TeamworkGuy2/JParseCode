package twg2.parser.intermAst.method;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lombok.Getter;
import lombok.val;
import twg2.annotations.Immutable;
import twg2.parser.baseAst.tools.NameUtil;
import twg2.parser.codeParser.csharp.CsDataTypeExtractor;
import twg2.parser.intermAst.annotation.AnnotationSig;
import twg2.parser.intermAst.classes.IntermClassSig;
import twg2.parser.intermAst.project.ProjectClassSet;
import twg2.parser.intermAst.type.TypeSig;
import twg2.parser.output.JsonWritableSig;
import twg2.parser.output.JsonWrite;
import twg2.parser.output.WriteSettings;

/**
 * @author TeamworkGuy2
 * @since 2015-11-24
 */
public enum IntermMethodSig {
	;


	/** Resolves simple name fields from {@link IntermMethodSig.SimpleImpl} into fully qualifying names and creates a new {@link IntermClassSig} with all other fields the same
	 */
	public static <T_METHOD extends IntermMethodSig.SimpleImpl> IntermMethodSig.ResolvedImpl resolveFrom(T_METHOD intermMethod,
			List<List<String>> namespaces, ProjectClassSet<?, ?> projFiles, Collection<List<String>> missingNamespacesDst) {
		// TODO also resolve annotations

		List<ResolvedParameterSig> resolvedParamSigs = new ArrayList<>();

		// resolve each parameter's type
		val paramSigs = intermMethod.getParamSigs();
		for(val paramSig : paramSigs) {
			TypeSig.Simple genericParamType = CsDataTypeExtractor.extractGenericTypes(paramSig.getTypeSimpleName());
			TypeSig.Resolved resolvedParamType = TypeSig.resolveFrom(genericParamType, namespaces, projFiles, missingNamespacesDst);

			val newParamSig = new ResolvedParameterSig(paramSig.getName(), resolvedParamType, paramSig.isOptional(), paramSig.getDefaultValue());
			resolvedParamSigs.add(newParamSig);
		}

		TypeSig.Resolved resolvedReturnType = TypeSig.resolveFrom(intermMethod.getReturnType(), namespaces, projFiles, missingNamespacesDst);

		return new IntermMethodSig.ResolvedImpl(intermMethod.getName(), intermMethod.getFullyQualifyingName(), resolvedParamSigs, resolvedReturnType, intermMethod.getAnnotations());
	}




	/**
	 * @author TeamworkGuy2
	 * @since 2016-1-3
	 * @param <T_PARAM> the type of {@link IntermParameterSig} parameters of this method
	 */
	// TODO also support simple/resolved return type and annotations
	@Immutable
	public static class Impl<T_PARAM extends JsonWritableSig, T_RET extends JsonWritableSig> implements JsonWritableSig {
		private final @Getter String name;
		private final @Getter List<String> fullyQualifyingName;
		private final @Getter List<T_PARAM> paramSigs;
		private final @Getter T_RET returnType;
		private final @Getter List<AnnotationSig> annotations;


		public Impl(String name, List<String> fullyQualifyingName, List<? extends T_PARAM> paramSigs,
				T_RET returnType, List<? extends AnnotationSig> annotations) {
			@SuppressWarnings("unchecked")
			val paramSigsCast = (List<T_PARAM>)paramSigs;
			@SuppressWarnings("unchecked")
			val annotationsCast = (List<AnnotationSig>)annotations;

			this.name = name;
			this.fullyQualifyingName = fullyQualifyingName;
			this.paramSigs = paramSigsCast;
			this.returnType = returnType;
			this.annotations = annotationsCast;
		}


		@Override
		public void toJson(Appendable dst, WriteSettings st) throws IOException {
			dst.append("{ ");
			dst.append("\"name\": \"" + (st.fullMethodName ? NameUtil.joinFqName(fullyQualifyingName) : fullyQualifyingName.get(fullyQualifyingName.size() - 1)) + "\", ");

			dst.append("\"parameters\": [");
			JsonWrite.joinStrConsumer(paramSigs, ", ", dst, (param) -> param.toJson(dst, st));
			dst.append("], ");

			dst.append("\"annotations\": [");
			JsonWrite.joinStrConsumer(annotations, ", ", dst, (ann) -> ann.toJson(dst, st));
			dst.append("], ");

			dst.append("\"returnType\": ");
			returnType.toJson(dst, st);

			dst.append(" }");
		}


		@Override
		public String toString() {
			return returnType + " " + NameUtil.joinFqName(fullyQualifyingName) + paramSigs;
		}

	}




	@Immutable
	public static class SimpleImpl extends Impl<IntermParameterSig, TypeSig.Simple> {

		public SimpleImpl(String name, List<String> fullyQualifyingName, List<? extends IntermParameterSig> paramSigs,
				TypeSig.Simple returnType, List<AnnotationSig> annotations) {
			super(name, fullyQualifyingName, paramSigs, returnType, annotations);
		}

	}




	@Immutable
	public static class ResolvedImpl extends Impl<ResolvedParameterSig, TypeSig.Resolved> {

		public ResolvedImpl(String name, List<String> fullyQualifyingName, List<? extends ResolvedParameterSig> paramSigs,
				TypeSig.Resolved returnType, List<AnnotationSig> annotations) {
			super(name, fullyQualifyingName, paramSigs, returnType, annotations);
		}

	}

}
