package twg2.parser.resolver;

import java.util.Collection;
import java.util.List;

import twg2.ast.interm.classes.ClassAst;
import twg2.ast.interm.classes.ClassSig;
import twg2.ast.interm.field.FieldSig;
import twg2.ast.interm.field.FieldSigResolved;
import twg2.ast.interm.type.TypeSig;
import twg2.parser.baseAst.CompoundBlock;
import twg2.parser.project.ProjectClassSet;

/**
 * @author TeamworkGuy2
 * @since 2016-3-19
 */
public class FieldSigResolver {

	/** Resolves simple name fields from {@link FieldSig} into fully qualifying names and creates a new {@link ClassSig} with all other fields the same
	 */
	public static <T_FIELD extends FieldSig> FieldSigResolved resolveFrom(T_FIELD intermField, ClassAst.SimpleImpl<? extends CompoundBlock> namespaceClass,
			ProjectClassSet.Simple<?, ? extends CompoundBlock> projFiles, Collection<List<String>> missingNamespacesDst) {
		// TODO also resolve annotations

		TypeSig.Resolved resolvedFieldType = TypeSigResolver.resolveFrom(intermField.getFieldType(), namespaceClass, projFiles, missingNamespacesDst);

		return new FieldSigResolved(intermField.getName(), intermField.getFullName(), resolvedFieldType, intermField.getAccessModifiers(), intermField.getAnnotations(), intermField.getComments());
	}

}
