package twg2.ast.interm.field;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.Getter;
import twg2.annotations.Immutable;
import twg2.ast.interm.annotation.AnnotationSig;
import twg2.ast.interm.type.TypeSig;
import twg2.ast.interm.type.TypeSig.TypeSigSimple;
import twg2.io.json.stringify.JsonStringify;
import twg2.parser.codeParser.Keyword;
import twg2.parser.codeParser.extractors.TypeExtractor;
import twg2.parser.codeParser.tools.NameUtil;
import twg2.parser.fragment.CodeToken;
import twg2.parser.output.JsonWritableSig;
import twg2.parser.output.WriteSettings;
import twg2.text.stringEscape.StringEscapeJson;
import twg2.treeLike.simpleTree.SimpleTree;

/** A class field definition, contains the field name, full name, type, access modifiers, annotations, comments, and initializer (if present)
 * @author TeamworkGuy2
 * @since 2016-09-04
 */
@Immutable
public class FieldDef implements JsonWritableSig {
	final @Getter String name;
	final @Getter List<String> fullName;
	final @Getter TypeSig.TypeSigSimple fieldType;
	final @Getter List<Keyword> accessModifiers;
	final @Getter List<AnnotationSig> annotations;
	final @Getter List<String> comments;
	final @Getter List<SimpleTree<CodeToken>> initializer;


	public FieldDef(String name, List<String> fullName, TypeSigSimple fieldType, List<? extends Keyword> accessModifiers,
			List<? extends AnnotationSig> annotations, List<String> comments, List<SimpleTree<CodeToken>> initializer) {
		@SuppressWarnings("unchecked")
		var accessModifiersCast = (List<Keyword>)accessModifiers;
		@SuppressWarnings("unchecked")
		var annotationsCast = (List<AnnotationSig>)annotations;

		this.name = name;
		this.fullName = fullName;
		this.fieldType = fieldType;
		this.accessModifiers = accessModifiersCast;
		this.annotations = annotationsCast;
		this.comments = comments;
		this.initializer = initializer;
	}


	@Override
	public void toJson(Appendable dst, WriteSettings st) throws IOException {
		var json = JsonStringify.inst;

		dst.append("{ ");
		json.toProp("name", (st.fullFieldName ? NameUtil.joinFqName(fullName) : fullName.get(fullName.size() - 1)), dst);

		json.comma(dst).propName("type", dst);
		fieldType.toJson(dst, st);

		json.comma(dst).propName("accessModifiers", dst)
			.toStringArray(accessModifiers, dst, (acs) -> acs.toSrc());

		if(annotations != null && annotations.size() > 0) {
			json.comma(dst).propName("annotations", dst)
				.toArrayConsume(annotations, dst, (ann) -> ann.toJson(dst, st));
		}

		initializerToJson(initializer, true, true, dst, st);

		json.comma(dst).propName("comments", dst)
			.toStringArray(comments, dst);

		dst.append(" }");
	}


	@Override
	public String toString() {
		return fieldType + " " + NameUtil.joinFqName(fullName);
	}


	/** Write a field initializer to a JSON field named 'initializer' if the value is a number, boolean, string, or null literal, else write it to a field named 'initializerExpression'
	 * @throws IOException
	 */
	public static void initializerToJson(List<SimpleTree<CodeToken>> astNodes, boolean preClosingComma, boolean includePropName, Appendable dst, WriteSettings st) throws IOException {
		AtomicBoolean isNumOrBoolOrNull = new AtomicBoolean(false);
		int astNodeCount = astNodes != null ? astNodes.size() : 0;
		if(astNodeCount > 0) {
			var text = TypeExtractor.isSimpleLiteral(astNodes, isNumOrBoolOrNull::set);
			if(text != null) {
				if(preClosingComma) {
					dst.append(", ");
				}
				if(includePropName) {
					dst.append("\"initializer\": ");
				}
				if(isNumOrBoolOrNull.get()) {
					dst.append(text);
				}
				else {
					dst.append('"');
					dst.append(StringEscapeJson.toJsonString(text));
					dst.append('"');
				}
			}
			else {
				if(preClosingComma) {
					dst.append(", ");
				}
				if(includePropName) {
					dst.append("\"initializerExpression\": ");
				}
				dst.append('"');
				CodeToken data;
				for(var astNode : astNodes) {
					if((data = astNode.getData()) != null) {
						dst.append(StringEscapeJson.toJsonString(data.getText()));
					}
				}
				dst.append('"');
			}
		}
	}


	/** Convert a field initializer to a string
	 */
	public static String initializerToString(List<SimpleTree<CodeToken>> astNodes) {
		AtomicBoolean isNumOrBoolOrNull = new AtomicBoolean(false);
		int astNodeCount = astNodes != null ? astNodes.size() : 0;
		if(astNodeCount > 0) {
			var text = TypeExtractor.isSimpleLiteral(astNodes, isNumOrBoolOrNull::set);
			if(text != null) {
				return text;
			}
			else {
				CodeToken data;
				text = "";
				for(var astNode : astNodes) {
					if((data = astNode.getData()) != null) {
						text += data.getText();
					}
				}
				return text;
			}
		}
		return null;
	}

}
