package twg2.parser.resolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import twg2.ast.interm.classes.ClassAst;
import twg2.ast.interm.classes.ClassSig;
import twg2.ast.interm.method.ParameterSigResolved;
import twg2.ast.interm.type.TypeSig.TypeSigResolved;
import twg2.ast.interm.method.MethodSigResolved;
import twg2.ast.interm.method.MethodSigSimple;
import twg2.parser.codeParser.Keyword;
import twg2.parser.codeParser.BlockType;
import twg2.parser.codeParser.KeywordUtil;
import twg2.parser.codeParser.extractors.TypeExtractor;
import twg2.parser.project.ProjectClassSet;
import twg2.parser.workflow.CodeFileParsed;

/**
 * @author TeamworkGuy2
 * @since 2016-3-19
 */
public class MethodSigResolver {

	/** Resolves simple name fields from {@link twg2.ast.interm.method.MethodSigSimple} into fully qualifying names and creates a new {@link ClassSig} with all other fields the same
	 */
	public static <T_METHOD extends MethodSigSimple, T_BLOCK extends BlockType> MethodSigResolved resolveFrom(KeywordUtil<? extends Keyword> keywordUtil, T_METHOD intermMethod,
			ClassAst.SimpleImpl<? extends BlockType> namespaceClass, ProjectClassSet<ClassAst.SimpleImpl<T_BLOCK>, CodeFileParsed.Intermediate<T_BLOCK>> projFiles, Collection<List<String>> missingNamespacesDst) {
		// TODO also resolve annotations

		var resolvedParamSigs = new ArrayList<ParameterSigResolved>();

		// resolve each parameter's type
		for(var paramSig : intermMethod.paramSigs) {
			var genericParamType = TypeExtractor.extractGenericTypes(paramSig.typeSimpleName, keywordUtil);
			var resolvedParamType = TypeSigResolver.resolveFrom(genericParamType, namespaceClass, projFiles, missingNamespacesDst);

			var newParamSig = new ParameterSigResolved(paramSig.name, resolvedParamType, paramSig.parameterModifiers, paramSig.annotations, paramSig.optional, paramSig.defaultValue);
			resolvedParamSigs.add(newParamSig);
		}

		// resolve each generic type parameter
		var resolvedTypeParams = new ArrayList<TypeSigResolved>();
		for(var typeParam : intermMethod.typeParameters) {
			var newTypeParam = TypeSigResolver.resolveFrom(typeParam, namespaceClass, projFiles, missingNamespacesDst);
			resolvedTypeParams.add(newTypeParam);
		}

		var resolvedReturnType = TypeSigResolver.resolveFrom(intermMethod.returnType, namespaceClass, projFiles, missingNamespacesDst);

		return new MethodSigResolved(intermMethod.name, intermMethod.fullName, resolvedParamSigs, resolvedReturnType, intermMethod.accessModifiers, resolvedTypeParams, intermMethod.annotations, intermMethod.comments);
	}

}
