package twg2.parser.intermAst.field;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import twg2.annotations.Immutable;
import twg2.io.write.JsonWrite;
import twg2.parser.baseAst.AccessModifier;
import twg2.parser.baseAst.CompoundBlock;
import twg2.parser.baseAst.tools.NameUtil;
import twg2.parser.intermAst.annotation.AnnotationSig;
import twg2.parser.intermAst.classes.IntermClass;
import twg2.parser.intermAst.classes.IntermClassSig;
import twg2.parser.intermAst.project.ProjectClassSet;
import twg2.parser.intermAst.type.TypeSig;
import twg2.parser.output.JsonWritableSig;
import twg2.parser.output.WriteSettings;

/**
 * @author TeamworkGuy2
 * @since 2015-12-4
 */
@Immutable
@AllArgsConstructor
public class IntermFieldSig implements JsonWritableSig {
	private final @Getter String name;
	private final @Getter List<String> fullName;
	private final @Getter TypeSig.Simple fieldType;
	private final @Getter List<AccessModifier> accessModifiers;
	private final @Getter List<AnnotationSig> annotations;


	@Override
	public void toJson(Appendable dst, WriteSettings st) throws IOException {
		dst.append("{ ");
		dst.append("\"name\": \"" + (st.fullFieldName ? NameUtil.joinFqName(fullName) : fullName.get(fullName.size() - 1)) + "\", ");

		dst.append("\"type\": ");
		fieldType.toJson(dst, st);
		dst.append(", ");

		dst.append("\"accessModifiers\": [");
		JsonWrite.joinStr(accessModifiers, ", ", dst, (acs) -> '"' + acs.toSrc() + '"');
		dst.append("], ");

		dst.append("\"annotations\": [");
		JsonWrite.joinStrConsume(annotations, ", ", dst, (ann) -> ann.toJson(dst, st));
		dst.append("]");

		dst.append(" }");
	}


	@Override
	public String toString() {
		return fieldType + " " + NameUtil.joinFqName(fullName);
	}


	/** Resolves simple name fields from {@link IntermFieldSig} into fully qualifying names and creates a new {@link IntermClassSig} with all other fields the same
	 */
	public static <T_FIELD extends IntermFieldSig> ResolvedFieldSig resolveFrom(T_FIELD intermField, IntermClass.SimpleImpl<? extends CompoundBlock> namespaceClass,
			ProjectClassSet.Simple<?, ? extends CompoundBlock> projFiles, Collection<List<String>> missingNamespacesDst) {
		// TODO also resolve annotations

		TypeSig.Resolved resolvedFieldType = TypeSig.resolveFrom(intermField.getFieldType(), namespaceClass, projFiles, missingNamespacesDst);

		return new ResolvedFieldSig(intermField.getName(), intermField.getFullName(), resolvedFieldType, intermField.getAccessModifiers(), intermField.getAnnotations());
	}

}
