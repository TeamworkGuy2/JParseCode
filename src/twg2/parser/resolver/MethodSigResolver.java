package twg2.parser.resolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import twg2.ast.interm.classes.ClassAst;
import twg2.ast.interm.classes.ClassSig;
import twg2.ast.interm.method.ParameterSigResolved;
import twg2.ast.interm.method.MethodSigResolved;
import twg2.ast.interm.method.MethodSigSimple;
import twg2.parser.codeParser.Keyword;
import twg2.parser.codeParser.BlockType;
import twg2.parser.codeParser.KeywordUtil;
import twg2.parser.codeParser.extractors.DataTypeExtractor;
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

		List<ParameterSigResolved> resolvedParamSigs = new ArrayList<>();

		// resolve each parameter's type
		var paramSigs = intermMethod.paramSigs;
		for(var paramSig : paramSigs) {
			var genericParamType = DataTypeExtractor.extractGenericTypes(paramSig.getTypeSimpleName(), keywordUtil);
			var resolvedParamType = TypeSigResolver.resolveFrom(genericParamType, namespaceClass, projFiles, missingNamespacesDst);

			var newParamSig = new ParameterSigResolved(paramSig.getName(), resolvedParamType, paramSig.getParameterModifiers(), paramSig.getAnnotations(), paramSig.isOptional(), paramSig.getDefaultValue());
			resolvedParamSigs.add(newParamSig);
		}

		var resolvedReturnType = TypeSigResolver.resolveFrom(intermMethod.returnType, namespaceClass, projFiles, missingNamespacesDst);

		return new MethodSigResolved(intermMethod.name, intermMethod.fullName, resolvedParamSigs, resolvedReturnType, intermMethod.accessModifiers, intermMethod.annotations, intermMethod.comments);
	}

}
