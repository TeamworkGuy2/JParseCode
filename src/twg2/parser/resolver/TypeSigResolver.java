package twg2.parser.resolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import twg2.ast.interm.classes.ClassAst;
import twg2.ast.interm.classes.ClassSig;
import twg2.ast.interm.type.TypeSig;
import twg2.collections.builder.ListBuilder;
import twg2.parser.codeParser.BlockType;
import twg2.parser.project.ProjectClassSet;
import twg2.parser.workflow.CodeFileParsed;

/**
 * @author TeamworkGuy2
 * @since 2016-3-19
 */
public class TypeSigResolver {

	/** Resolves simple name fields from {@link twg2.ast.interm.type.TypeSig.TypeSigSimple} into fully qualifying names and creates a new {@link ClassSig} with all other fields the same
	 */
	public static <T_BLOCK extends BlockType> TypeSig.TypeSigResolved resolveFrom(TypeSig.TypeSigSimple intermSig, ClassAst.SimpleImpl<? extends BlockType> namespaceClass,
			ProjectClassSet<ClassAst.SimpleImpl<T_BLOCK>, CodeFileParsed.Intermediate<T_BLOCK>> projFiles, Collection<List<String>> missingNamespacesDst) {
		// TODO also resolve annotations

		List<TypeSig.TypeSigResolved> childSigs = Collections.emptyList();
		if(intermSig.isGeneric()) {
			childSigs = new ArrayList<>();
			for(var childSig : intermSig.getParams()) {
				var resolvedChildSig = resolveFrom(childSig, namespaceClass, projFiles, missingNamespacesDst);
				childSigs.add(resolvedChildSig);
			}
		}

		var resolvedClass = projFiles.resolveSimpleNameToClass(intermSig.getTypeName(), namespaceClass, missingNamespacesDst);
		List<String> resolvedType = resolvedClass != null ? resolvedClass.getSignature().getFullName() : null;

		if(resolvedType == null) {
			resolvedType = ListBuilder.mutable(intermSig.getTypeName());
		}

		if(childSigs.size() > 0) {
			return new TypeSig.TypeSigResolvedGeneric(resolvedType, childSigs, intermSig.getArrayDimensions(), intermSig.isNullable(), intermSig.isPrimitive());
		}
		else {
			return new TypeSig.TypeSigResolvedBase(resolvedType, intermSig.getArrayDimensions(), intermSig.isNullable(), intermSig.isPrimitive());
		}
	}

}
