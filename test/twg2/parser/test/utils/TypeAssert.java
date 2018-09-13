package twg2.parser.test.utils;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;

import twg2.ast.interm.type.TypeSig.TypeSigResolved;
import twg2.ast.interm.type.TypeSig.TypeSigSimple;
import twg2.text.stringUtils.StringSplit;

/**
 * @author TeamworkGuy2
 * @since 2017-06-25
 */
public class TypeAssert {

	/** Compare a manually defined type signature (via nested arrays and strings) with an actual type signature
	 * @param type either the expected name of a non-generic type or an array containing a generic type name and an array of it's type parameters
	 * each of which can be a name or nested array containing a nested generic type definition
	 * @param sig the type signature to compare to
	 */
	public static void assertType(Object type, TypeSigSimple sig) {
		assertSimpleTypes(type, Arrays.asList(sig), 0);
	}


	/** Compare a manually defined type signature (via nested arrays and strings) with an actual type signature
	 * @param type either the expected name of a non-generic type or an array containing a generic type name and an array of it's type parameters
	 * each of which can be a name or nested array containing a nested generic type definition
	 * @param sig the type signature to compare to
	 */
	public static void assertType(Object type, TypeSigResolved sig) {
		assertResolvedTypes(type, Arrays.asList(sig), 0);
	}


	private static void assertSimpleTypes(Object type, List<TypeSigSimple> sigs, int depth) {
		if(type instanceof Object[]) {
			Object[] typeAry = (Object[])type;
			if(typeAry.length == 2 && sigs.size() == 1) {
				assertSimpleType((String)typeAry[0], sigs.get(0));
				assertSimpleTypes(typeAry[1], sigs.get(0).getParams(), depth + 1);
			}
			else {
				Assert.assertEquals(sigs.size(), typeAry.length);
				int i = 0;
				for(Object subType : typeAry) {
					if(subType instanceof String) {
						assertSimpleType((String)subType, sigs.get(i));
					}
					else {
						assertSimpleTypes(subType, sigs.get(i).getParams(), depth);
					}
					i++;
				}
			}
		}
		else if(type instanceof String) {
			assertSimpleType((String)type, sigs.get(0));
		}
		else {
			throw new IllegalArgumentException("Expected type string or array, but was " + (type != null ? type.getClass() : null));
		}
	}


	private static void assertResolvedTypes(Object type, List<TypeSigResolved> sigs, int depth) {
		if(type instanceof Object[]) {
			Object[] typeAry = (Object[])type;
			if(typeAry.length == 2 && sigs.size() == 1) {
				assertResolvedType((String)typeAry[0], sigs.get(0));
				assertResolvedTypes(typeAry[1], sigs.get(0).getParams(), depth + 1);
			}
			else {
				Assert.assertEquals(sigs.size(), typeAry.length);
				int i = 0;
				for(Object subType : typeAry) {
					if(subType instanceof String) {
						assertResolvedType((String)subType, sigs.get(i));
					}
					else {
						assertResolvedTypes(subType, sigs.get(i).getParams(), depth);
					}
					i++;
				}
			}
		}
		else if(type instanceof String) {
			assertResolvedType((String)type, sigs.get(0));
		}
		else {
			throw new IllegalArgumentException("Expected type string or array, but was " + (type != null ? type.getClass() : null));
		}
	}


	/** Assert a type name and it's array dimensions.
	 * For example, if the type is 'int' only the name is compared.
	 * If the type is 'int[][]' both the name and {@link TypeSigSimple#getArrayDimensions()} are checked.
	 */
	private static void assertSimpleType(String type, TypeSigSimple sig) {
		String typeName = type;
		int arrayDimensions = StringSplit.countMatches(typeName, "[]");
		if(arrayDimensions > 0) {
			typeName = typeName.substring(0, typeName.length() - (arrayDimensions * 2));

			Assert.assertEquals(arrayDimensions, sig.getArrayDimensions());
		}

		Assert.assertEquals(typeName, sig.getTypeName());
	}


	/** Assert a type name and it's array dimensions.
	 * For example, if the type is 'int' only the name is compared.
	 * If the type is 'int[][]' both the name and {@link TypeSigSimple#getArrayDimensions()} are checked.
	 */
	private static void assertResolvedType(String type, TypeSigResolved sig) {
		String typeName = type;
		int arrayDimensions = StringSplit.countMatches(typeName, "[]");
		if(arrayDimensions > 0) {
			typeName = typeName.substring(0, typeName.length() - (arrayDimensions * 2));

			Assert.assertEquals(arrayDimensions, sig.getArrayDimensions());
		}

		Assert.assertEquals(typeName, sig.getSimpleName());
	}


	public static Object[] ls(Object... s) {
		return s;
	}

}
