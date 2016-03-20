package twg2.parser.resolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lombok.val;
import twg2.ast.interm.classes.ClassAst;
import twg2.ast.interm.classes.ClassSig;
import twg2.ast.interm.method.MethodSig;
import twg2.ast.interm.method.ParameterSigResolved;
import twg2.ast.interm.type.TypeSig;
import twg2.parser.baseAst.CompoundBlock;
import twg2.parser.codeParser.BaseDataTypeExtractor;
import twg2.parser.codeParser.KeywordUtil;
import twg2.parser.project.ProjectClassSet;

/**
 * @author TeamworkGuy2
 * @since 2016-3-19
 */
public class MethodSigResolver {

	/** Resolves simple name fields from {@link twg2.ast.interm.method.MethodSig.SimpleImpl} into fully qualifying names and creates a new {@link ClassSig} with all other fields the same
	 */
	public static <T_METHOD extends MethodSig.SimpleImpl> MethodSig.ResolvedImpl resolveFrom(KeywordUtil keywordUtil, T_METHOD intermMethod,
			ClassAst.SimpleImpl<? extends CompoundBlock> namespaceClass, ProjectClassSet.Simple<?, ? extends CompoundBlock> projFiles, Collection<List<String>> missingNamespacesDst) {
		// TODO also resolve annotations

		List<ParameterSigResolved> resolvedParamSigs = new ArrayList<>();

		// resolve each parameter's type
		val paramSigs = intermMethod.getParamSigs();
		for(val paramSig : paramSigs) {
			TypeSig.Simple genericParamType = BaseDataTypeExtractor.extractGenericTypes(paramSig.getTypeSimpleName(), keywordUtil);
			TypeSig.Resolved resolvedParamType = TypeSigResolver.resolveFrom(genericParamType, namespaceClass, projFiles, missingNamespacesDst);

			val newParamSig = new ParameterSigResolved(paramSig.getName(), resolvedParamType, paramSig.isOptional(), paramSig.getDefaultValue());
			resolvedParamSigs.add(newParamSig);
		}

		TypeSig.Resolved resolvedReturnType = TypeSigResolver.resolveFrom(intermMethod.getReturnType(), namespaceClass, projFiles, missingNamespacesDst);

		return new MethodSig.ResolvedImpl(intermMethod.getName(), intermMethod.getFullName(), resolvedParamSigs, resolvedReturnType, intermMethod.getAccessModifiers(), intermMethod.getAnnotations(), intermMethod.getComments());
	}

}
