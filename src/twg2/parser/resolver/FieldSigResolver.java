package twg2.parser.resolver;

import java.util.Collection;
import java.util.List;

import twg2.ast.interm.classes.ClassAst;
import twg2.ast.interm.classes.ClassSig;
import twg2.ast.interm.field.FieldDef;
import twg2.ast.interm.field.FieldDefResolved;
import twg2.parser.codeParser.BlockType;
import twg2.parser.project.ProjectClassSet;
import twg2.parser.workflow.CodeFileParsed;

/**
 * @author TeamworkGuy2
 * @since 2016-3-19
 */
public class FieldSigResolver {

	/** Resolves simple name fields from {@link FieldDef} into fully qualifying names and creates a new {@link ClassSig} with all other fields the same
	 */
	public static <T_FIELD extends FieldDef, T_BLOCK extends BlockType> FieldDefResolved resolveFrom(T_FIELD intermField, ClassAst.SimpleImpl<? extends BlockType> namespaceClass,
			ProjectClassSet<ClassAst.SimpleImpl<T_BLOCK>, CodeFileParsed.Intermediate<T_BLOCK>> projFiles, Collection<List<String>> missingNamespacesDst) {
		// TODO also resolve annotations

		var resolvedFieldType = TypeSigResolver.resolveFrom(intermField.getFieldType(), namespaceClass, projFiles, missingNamespacesDst);
		return new FieldDefResolved(intermField.getName(), intermField.getFullName(), resolvedFieldType, intermField.getAccessModifiers(),
				intermField.getAnnotations(), intermField.getComments(), intermField.getInitializer());
	}

}
