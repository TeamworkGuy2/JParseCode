package twg2.parser.resolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import lombok.val;
import twg2.ast.interm.classes.ClassAst;
import twg2.ast.interm.classes.ClassSig;
import twg2.ast.interm.type.TypeSig;
import twg2.collections.builder.ListBuilder;
import twg2.parser.baseAst.CompoundBlock;
import twg2.parser.project.ProjectClassSet;

/**
 * @author TeamworkGuy2
 * @since 2016-3-19
 */
public class TypeSigResolver {

	/** Resolves simple name fields from {@link twg2.ast.interm.type.TypeSig.Simple} into fully qualifying names and creates a new {@link ClassSig} with all other fields the same
	 */
	public static TypeSig.Resolved resolveFrom(TypeSig.Simple intermSig, ClassAst.SimpleImpl<? extends CompoundBlock> namespaceClass,
			ProjectClassSet.Simple<?, ? extends CompoundBlock> projFiles, Collection<List<String>> missingNamespacesDst) {
		// TODO also resolve annotations

		List<TypeSig.Resolved> childSigs = Collections.emptyList();
		if(intermSig.isGeneric()) {
			childSigs = new ArrayList<>();
			for(val childSig : intermSig.getParams()) {
				TypeSig.Resolved resolvedChildSig = resolveFrom(childSig, namespaceClass, projFiles, missingNamespacesDst);
				childSigs.add(resolvedChildSig);
			}
		}

		List<String> resolvedType = projFiles.resolveSimpleName(intermSig.getTypeName(), namespaceClass, missingNamespacesDst);

		if(resolvedType == null) {
			resolvedType = ListBuilder.mutable(intermSig.getTypeName());
		}

		if(childSigs.size() > 0) {
			return new TypeSig.ResolvedGenericImpl(resolvedType, childSigs, intermSig.getArrayDimensions(), intermSig.isNullable(), intermSig.isPrimitive());
		}
		else {
			return new TypeSig.ResolvedBaseImpl(resolvedType, intermSig.getArrayDimensions(), intermSig.isNullable(), intermSig.isPrimitive());
		}
	}

}
