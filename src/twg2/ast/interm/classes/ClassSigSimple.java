package twg2.ast.interm.classes;

import java.io.IOException;
import java.util.List;

import lombok.Getter;
import twg2.annotations.Immutable;
import twg2.ast.interm.annotation.AnnotationSig;
import twg2.ast.interm.type.TypeSig;
import twg2.ast.interm.type.TypeSig.TypeSigSimple;
import twg2.io.json.stringify.JsonStringify;
import twg2.parser.codeParser.Keyword;
import twg2.parser.codeParser.tools.NameUtil;
import twg2.parser.output.WriteSettings;

@Immutable
public class ClassSigSimple implements ClassSig {
	private final @Getter List<String> fullName;
	/** This type's generic type parameters, if any */
	private final @Getter List<TypeSig.TypeSigSimple> params;
	/** The block's access (i.e. 'public', 'private', etc.) */
	private final @Getter Keyword accessModifier;
	/** The block's annotations */
	private final @Getter List<AnnotationSig> annotations;
	/** The block's type (i.e. 'interface', 'class', 'enum', etc.) */
	private final @Getter String declarationType;
	private final @Getter List<String> extendImplementSimpleNames;
	private final @Getter List<String> comments;



	public ClassSigSimple(List<String> fullName, List<? extends TypeSigSimple> params, Keyword accessModifier, List<? extends AnnotationSig> annotations, List<String> comments, String declarationType, List<String> extendImplementSimpleNames) {
		@SuppressWarnings("unchecked")
		var paramsCast = (List<TypeSigSimple>)params;
		@SuppressWarnings("unchecked")
		var annotationsCast = (List<AnnotationSig>)annotations;

		this.fullName = fullName;
		this.params = paramsCast;
		this.accessModifier = accessModifier;
		this.annotations = annotationsCast;
		this.comments = comments;
		this.declarationType = declarationType;
		this.extendImplementSimpleNames = extendImplementSimpleNames;
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

		json.toProp("access", accessModifier.toSrc(), dst);
		json.comma(dst).toProp("name", (st.fullClassName ? NameUtil.joinFqName(fullName) : fullName.get(fullName.size() - 1)), dst);

		if(declarationType != null) {
			json.comma(dst).toProp("declarationType", declarationType, dst);
		}

		if(params != null && params.size() > 0) {
			json.comma(dst).propName("genericParameters", dst)
				.toArrayConsume(params, dst, (p) -> p.toJson(dst, st));
		}

		if(extendImplementSimpleNames.size() > 0) {
			json.comma(dst).propName("extendImplementClassNames", dst)
				.toStringArray(extendImplementSimpleNames, dst);
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