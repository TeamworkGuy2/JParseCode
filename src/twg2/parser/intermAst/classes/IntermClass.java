package twg2.parser.intermAst.classes;

import java.io.IOException;
import java.util.List;

import lombok.Getter;
import lombok.val;
import twg2.annotations.Immutable;
import twg2.parser.baseAst.CompoundBlock;
import twg2.parser.baseAst.tools.NameUtil;
import twg2.parser.intermAst.field.IntermFieldSig;
import twg2.parser.intermAst.field.ResolvedFieldSig;
import twg2.parser.intermAst.method.IntermMethodSig;
import twg2.parser.intermAst.method.IntermParameterSig;
import twg2.parser.intermAst.method.ResolvedParameterSig;
import twg2.parser.output.JsonWritableSig;
import twg2.parser.output.JsonWrite;
import twg2.parser.output.WriteSettings;
import twg2.text.stringUtils.StringJoin;

/**
 * @author TeamworkGuy2
 * @since 2015-12-4
 */
public interface IntermClass<T_SIG extends IntermClassSig, T_METHOD extends JsonWritableSig, T_BLOCK extends CompoundBlock> extends JsonWritableSig {

	public T_SIG getSignature();

	public List<List<String>> getUsingStatements();

	public List<T_METHOD> getMethods();

	public T_BLOCK getBlockType();




	/**
	 * @author TeamworkGuy2
	 * @since 2015-12-4
	 */
	@Immutable
	public static class Impl<T_SIG extends IntermClassSig, T_FIELD extends JsonWritableSig, T_METHOD extends JsonWritableSig, T_PARAM extends JsonWritableSig, T_BLOCK extends CompoundBlock>
			implements IntermClass<T_SIG, T_METHOD, T_BLOCK> {
		private final @Getter T_SIG signature;
		private final @Getter List<List<String>> usingStatements;
		private final @Getter List<T_FIELD> fields;
		private final @Getter List<T_METHOD> methods;
		private final @Getter T_BLOCK blockType;


		public Impl(T_SIG signature, List<List<String>> usingStatements, List<? extends T_FIELD> fields, List<? extends T_METHOD> methods, T_BLOCK blockType) {
			@SuppressWarnings("unchecked")
			val fieldsCast = (List<T_FIELD>)fields;
			@SuppressWarnings("unchecked")
			val methodsCast = (List<T_METHOD>)methods;

			this.signature = signature;
			this.usingStatements = usingStatements;
			this.fields = fieldsCast;
			this.methods = methodsCast;
			this.blockType = blockType;
		}


		@Override
		public void toJson(Appendable dst, WriteSettings st) throws IOException {
			dst.append("{\n");

			dst.append("\"classSignature\": ");
			signature.toJson(dst, st);
			dst.append(",\n");

			dst.append("\"blockType\": \"" + blockType + "\",\n");

			dst.append("\"using\": [");
			JsonWrite.joinStrConsumer(usingStatements, ", ", dst, (us) -> dst.append('"' + NameUtil.joinFqName(us) + '"'));
			dst.append("],\n");

			dst.append("\"fields\": [");
			JsonWrite.joinStrConsumer(fields, ", ", dst, (f) -> f.toJson(dst, st));
			dst.append("],\n");

			dst.append("\"methods\": [");
			JsonWrite.joinStrConsumer(methods, ", ", dst, (m) -> m.toJson(dst, st));
			dst.append("]\n");

			dst.append("}");
		}


		@Override
		public String toString() {
			return blockType + " " + signature.toString() + " { " + StringJoin.Objects.join(fields, "; ") + " " + StringJoin.Objects.join(methods, "; ") + " }";
		}

	}




	/**
	 * @author TeamworkGuy2
	 * @since 2016-1-2
	 */
	@Immutable
	public static class SimpleImpl<T_BLOCK extends CompoundBlock> extends Impl<IntermClassSig.SimpleImpl, IntermFieldSig, IntermMethodSig.SimpleImpl, IntermParameterSig, T_BLOCK> {

		public SimpleImpl(IntermClassSig.SimpleImpl signature, List<List<String>> usingStatements, List<? extends IntermFieldSig> fields,
				List<? extends IntermMethodSig.SimpleImpl> methods, T_BLOCK blockType) {
			super(signature, usingStatements, fields, methods, blockType);
		}
		
	}




	/**
	 * @author TeamworkGuy2
	 * @since 2015-12-4
	 */
	@Immutable
	public static class ResolvedImpl<T_BLOCK extends CompoundBlock> extends Impl<IntermClassSig.ResolvedImpl, ResolvedFieldSig, IntermMethodSig.ResolvedImpl, ResolvedParameterSig, T_BLOCK> {

		public ResolvedImpl(IntermClassSig.ResolvedImpl signature, List<List<String>> usingStatements, List<? extends ResolvedFieldSig> fields,
				List<? extends IntermMethodSig.ResolvedImpl> methods, T_BLOCK blockType) {
			super(signature, usingStatements, fields, methods, blockType);
		}
		
	}

}
