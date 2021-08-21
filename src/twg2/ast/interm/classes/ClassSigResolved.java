package twg2.ast.interm.classes;

import java.io.IOException;
import java.util.List;

import lombok.Getter;
import twg2.annotations.Immutable;
import twg2.ast.interm.annotation.AnnotationSig;
import twg2.ast.interm.type.TypeSig;
import twg2.ast.interm.type.TypeSig.TypeSigResolved;
import twg2.io.json.stringify.JsonStringify;
import twg2.parser.codeParser.Keyword;
import twg2.parser.codeParser.tools.NameUtil;
import twg2.parser.output.WriteSettings;

/**
 * @author TeamworkGuy2
 * @since 2015-12-5
 */
@Immutable
public class ClassSigResolved implements ClassSig {
	private final @Getter List<String> fullName;
	/** This type's generic type parameters, if any */
	private final @Getter List<TypeSig.TypeSigResolved> params;
	/** The block's access (i.e. 'public', 'private', etc.) */
	private final @Getter Keyword accessModifier;
	/** The block's annotations */
	private final @Getter List<AnnotationSig> annotations;
	private final @Getter List<String> comments;
	/** The block's type (i.e. 'interface', 'class', 'enum', etc.) */
	private final @Getter String declarationType;
	private final @Getter TypeSig.TypeSigResolved extendClass;
	private final @Getter List<TypeSig.TypeSigResolved> implementInterfaces;


	public ClassSigResolved(List<String> fullName, List<? extends TypeSigResolved> params, Keyword accessModifier, List<? extends AnnotationSig> annotations, List<String> comments,
			String declarationType, TypeSigResolved extendClass, List<? extends TypeSigResolved> implementInterfaces) {
		@SuppressWarnings("unchecked")
		var paramsCast = (List<TypeSigResolved>)params;
		@SuppressWarnings("unchecked")
		var annotationsCast = (List<AnnotationSig>)annotations;
		@SuppressWarnings("unchecked")
		var implementInterfacesCast = (List<TypeSigResolved>)implementInterfaces;

		this.fullName = fullName;
		this.params = paramsCast;
		this.accessModifier = accessModifier;
		this.annotations = annotationsCast;
		this.comments = comments;
		this.declarationType = declarationType;
		this.extendClass = extendClass;
		this.implementInterfaces = implementInterfacesCast;
	}


	@Override
	public String getSimpleName() {
		return fullName.get(fullName.size() - 1);
	}


	public boolean isGeneric() {
		return params.size() > 0;
	}


	@Override
	public void toJson(Appendable dst, WriteSettings st) throws IOException {
		var json = JsonStringify.inst;

		dst.append("{ ");
		json.toProp("access", accessModifier.toSrc(), dst).comma(dst)
			.toProp("name", (st.fullClassName ? NameUtil.joinFqName(fullName) : fullName.get(fullName.size() - 1)), dst).comma(dst)
			.toProp("declarationType", declarationType, dst);

		if(params != null && params.size() > 0) {
			json.comma(dst).append("\"genericParameters\": ", dst);
			json.toArrayConsume(params, dst, (p) -> p.toJson(dst, st));
		}

		if(extendClass != null) {
			json.comma(dst).append("\"extendClassName\": ", dst);
			extendClass.toJson(dst, st);
		}

		if(implementInterfaces.size() > 0) {
			json.comma(dst).append("\"implementClassNames\": ", dst);
			json.toArrayConsume(implementInterfaces, dst, (intfType) -> intfType.toJson(dst, st));
		}

		if(annotations.size() > 0) {
			json.comma(dst).propName("annotations", dst)
				.toArrayConsume(annotations, dst, (a) -> a.toJson(dst, st));
		}

		if(comments.size() > 0) {
			json.comma(dst).propName("comments", dst)
				.toStringArray(comments, dst);
		}

		dst.append(" }");
	}


	@Override
	public String toString() {
		return accessModifier.toSrc() + " " + declarationType + " " + NameUtil.joinFqName(fullName);
	}

}