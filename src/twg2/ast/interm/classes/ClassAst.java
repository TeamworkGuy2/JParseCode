package twg2.ast.interm.classes;

import java.io.IOException;
import java.util.List;

import lombok.Getter;
import lombok.val;
import twg2.annotations.Immutable;
import twg2.ast.interm.field.FieldDef;
import twg2.ast.interm.field.FieldDefResolved;
import twg2.ast.interm.field.FieldSig;
import twg2.ast.interm.field.FieldSigResolved;
import twg2.ast.interm.method.MethodSigResolved;
import twg2.ast.interm.method.MethodSigSimple;
import twg2.ast.interm.method.ParameterSig;
import twg2.ast.interm.method.ParameterSigResolved;
import twg2.io.json.stringify.JsonStringify;
import twg2.parser.codeParser.BlockType;
import twg2.parser.codeParser.tools.NameUtil;
import twg2.parser.output.JsonWritableSig;
import twg2.parser.output.WriteSettings;
import twg2.text.stringUtils.StringJoin;

/**
 * @author TeamworkGuy2
 * @since 2015-12-4
 */
public interface ClassAst<T_SIG extends ClassSig, T_BLOCK extends BlockType> extends JsonWritableSig {

	public T_SIG getSignature();

	public T_BLOCK getBlockType();




	/**
	 * @author TeamworkGuy2
	 * @since 2015-12-4
	 */
	@Immutable
	public static class Impl<T_SIG extends ClassSig, T_ENUM extends JsonWritableSig, T_FIELD extends JsonWritableSig, T_METHOD extends JsonWritableSig, T_PARAM extends JsonWritableSig, T_BLOCK extends BlockType>
			implements ClassAst<T_SIG, T_BLOCK> {
		private final @Getter T_SIG signature;
		private final @Getter List<List<String>> usingStatements;
		private final @Getter List<T_ENUM> enumMembers;
		private final @Getter List<T_FIELD> fields;
		private final @Getter List<T_METHOD> methods;
		private final @Getter T_BLOCK blockType;


		public Impl(T_SIG signature, List<List<String>> usingStatements, List<? extends T_FIELD> fields, List<? extends T_METHOD> methods, List<? extends T_ENUM> enums, T_BLOCK blockType) {
			@SuppressWarnings("unchecked")
			val fieldsCast = (List<T_FIELD>)fields;
			@SuppressWarnings("unchecked")
			val methodsCast = (List<T_METHOD>)methods;
			@SuppressWarnings("unchecked")
			val enumsCast = (List<T_ENUM>)enums;

			this.signature = signature;
			this.usingStatements = usingStatements;
			this.enumMembers = enumsCast;
			this.fields = fieldsCast;
			this.methods = methodsCast;
			this.blockType = blockType;
		}


		@Override
		public void toJson(Appendable dst, WriteSettings st) throws IOException {
			val json = JsonStringify.inst;

			dst.append("{\n");

			json.indent('\t', dst).propName("classSignature", dst);
			signature.toJson(dst, st);

			json.comma("\n\t", dst).toProp("blockType", blockType.toString(), dst);

			json.comma("\n\t", dst).propName("using", dst)
				.toStringArray(usingStatements, dst, (us) -> NameUtil.joinFqName(us));

			if(enumMembers != null && blockType.isEnum()) {
				json.comma("\n\t", dst).propName("enumMembers", dst).append('[', dst);
				if(enumMembers.size() > 0) {
					dst.append("\n\t\t");
					json.joinConsume(enumMembers, ",\n\t\t", dst, (f) -> f.toJson(dst, st));
					dst.append("\n\t");
				}
				dst.append("]");
			}

			json.comma("\n\t", dst).propName("fields", dst).append('[', dst);
			if(fields.size() > 0) {
				dst.append("\n\t\t");
				json.joinConsume(fields, ",\n\t\t", dst, (f) -> f.toJson(dst, st));
				dst.append("\n\t");
			}
			dst.append("]");

			json.comma("\n\t", dst).propName("methods", dst).append('[', dst);
			if(methods.size() > 0) {
				dst.append("\n\t\t");
				json.joinConsume(methods, ",\n\t\t", dst, (m) -> m.toJson(dst, st));
				dst.append("\n\t");
			}
			dst.append("]");

			dst.append("\n}");
		}


		@Override
		public String toString() {
			return blockType + " " + signature.toString() + " { " + StringJoin.join(fields, "; ") + " " + StringJoin.join(methods, "; ") + " }";
		}

	}




	/**
	 * @author TeamworkGuy2
	 * @since 2016-1-2
	 */
	@Immutable
	public static class SimpleImpl<T_BLOCK extends BlockType> extends Impl<ClassSigSimple, FieldDef, FieldSig, MethodSigSimple, ParameterSig, T_BLOCK> {

		public SimpleImpl(ClassSigSimple signature, List<List<String>> usingStatements, List<? extends FieldSig> fields,
				List<? extends MethodSigSimple> methods, List<? extends FieldDef> enums, T_BLOCK blockType) {
			super(signature, usingStatements, fields, methods, enums, blockType);
		}
		
	}




	/**
	 * @author TeamworkGuy2
	 * @since 2015-12-4
	 */
	@Immutable
	public static class ResolvedImpl<T_BLOCK extends BlockType> extends Impl<ClassSigResolved, FieldDefResolved, FieldSigResolved, MethodSigResolved, ParameterSigResolved, T_BLOCK> {

		public ResolvedImpl(ClassSigResolved signature, List<List<String>> usingStatements, List<? extends FieldSigResolved> fields,
				List<? extends MethodSigResolved> methods, List<? extends FieldDefResolved> enums, T_BLOCK blockType) {
			super(signature, usingStatements, fields, methods, enums, blockType);
		}
		
	}

}
