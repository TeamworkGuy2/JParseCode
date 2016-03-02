package twg2.parser.intermAst.method;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lombok.Getter;
import lombok.val;
import twg2.annotations.Immutable;
import twg2.io.write.JsonWrite;
import twg2.parser.baseAst.AccessModifier;
import twg2.parser.baseAst.CompoundBlock;
import twg2.parser.baseAst.tools.NameUtil;
import twg2.parser.codeParser.BaseDataTypeExtractor;
import twg2.parser.codeParser.KeywordUtil;
import twg2.parser.intermAst.annotation.AnnotationSig;
import twg2.parser.intermAst.classes.IntermClass;
import twg2.parser.intermAst.classes.IntermClassSig;
import twg2.parser.intermAst.project.ProjectClassSet;
import twg2.parser.intermAst.type.TypeSig;
import twg2.parser.output.JsonWritableSig;
import twg2.parser.output.WriteSettings;

/**
 * @author TeamworkGuy2
 * @since 2015-11-24
 */
public enum IntermMethodSig {
	;


	/** Resolves simple name fields from {@link IntermMethodSig.SimpleImpl} into fully qualifying names and creates a new {@link IntermClassSig} with all other fields the same
	 */
	public static <T_METHOD extends IntermMethodSig.SimpleImpl> IntermMethodSig.ResolvedImpl resolveFrom(KeywordUtil keywordUtil, T_METHOD intermMethod,
			IntermClass.SimpleImpl<? extends CompoundBlock> namespaceClass, ProjectClassSet.Simple<?, ? extends CompoundBlock> projFiles, Collection<List<String>> missingNamespacesDst) {
		// TODO also resolve annotations

		List<ResolvedParameterSig> resolvedParamSigs = new ArrayList<>();

		// resolve each parameter's type
		val paramSigs = intermMethod.getParamSigs();
		for(val paramSig : paramSigs) {
			TypeSig.Simple genericParamType = BaseDataTypeExtractor.extractGenericTypes(paramSig.getTypeSimpleName(), keywordUtil);
			TypeSig.Resolved resolvedParamType = TypeSig.resolveFrom(genericParamType, namespaceClass, projFiles, missingNamespacesDst);

			val newParamSig = new ResolvedParameterSig(paramSig.getName(), resolvedParamType, paramSig.isOptional(), paramSig.getDefaultValue());
			resolvedParamSigs.add(newParamSig);
		}

		TypeSig.Resolved resolvedReturnType = TypeSig.resolveFrom(intermMethod.getReturnType(), namespaceClass, projFiles, missingNamespacesDst);

		return new IntermMethodSig.ResolvedImpl(intermMethod.getName(), intermMethod.getFullName(), resolvedParamSigs, resolvedReturnType, intermMethod.getAccessModifiers(), intermMethod.getAnnotations());
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
		private final @Getter List<String> fullName;
		private final @Getter List<T_PARAM> paramSigs;
		private final @Getter T_RET returnType;
		private final @Getter List<AccessModifier> accessModifiers;
		private final @Getter List<AnnotationSig> annotations;


		public Impl(String name, List<String> fullName, List<? extends T_PARAM> paramSigs,
				T_RET returnType, List<? extends AccessModifier> accessModifiers, List<? extends AnnotationSig> annotations) {
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
		}


		@Override
		public void toJson(Appendable dst, WriteSettings st) throws IOException {
			dst.append("{ ");
			dst.append("\"name\": \"" + (st.fullMethodName ? NameUtil.joinFqName(fullName) : fullName.get(fullName.size() - 1)) + "\", ");

			dst.append("\"parameters\": [");
			JsonWrite.joinStrConsume(paramSigs, ", ", dst, (param) -> param.toJson(dst, st));
			dst.append("], ");

			dst.append("\"accessModifiers\": [");
			JsonWrite.joinStr(accessModifiers, ", ", dst, (acs) -> '"' + acs.toSrc() + '"');
			dst.append("], ");

			dst.append("\"annotations\": [");
			JsonWrite.joinStrConsume(annotations, ", ", dst, (ann) -> ann.toJson(dst, st));
			dst.append("], ");

			dst.append("\"returnType\": ");
			returnType.toJson(dst, st);

			dst.append(" }");
		}


		@Override
		public String toString() {
			return returnType + " " + NameUtil.joinFqName(fullName) + paramSigs;
		}

	}




	@Immutable
	public static class SimpleImpl extends Impl<IntermParameterSig, TypeSig.Simple> {

		public SimpleImpl(String name, List<String> fullName, List<? extends IntermParameterSig> paramSigs,
				TypeSig.Simple returnType, List<? extends AccessModifier> accessModifiers, List<? extends AnnotationSig> annotations) {
			super(name, fullName, paramSigs, returnType, accessModifiers, annotations);
		}

	}




	@Immutable
	public static class ResolvedImpl extends Impl<ResolvedParameterSig, TypeSig.Resolved> {

		public ResolvedImpl(String name, List<String> fullName, List<? extends ResolvedParameterSig> paramSigs,
				TypeSig.Resolved returnType, List<? extends AccessModifier> accessModifiers, List<? extends AnnotationSig> annotations) {
			super(name, fullName, paramSigs, returnType, accessModifiers, annotations);
		}

	}

}
