package twg2.parser.codeParser.tools;

import java.util.function.Function;
import java.util.function.Predicate;

import twg2.parser.fragment.CodeToken;
import twg2.parser.fragment.CodeTokenType;

/** An {@link EnumSubSet} for {@link CodeTokenType}
 * @author TeamworkGuy2
 * @since 2016-4-12
 */
public class CodeTokenEnumSubSet<E> extends EnumSubSet<E> {
	private CodeTokenType expectedType;


	public CodeTokenEnumSubSet(CodeTokenType expectedType, EnumSubSet<E> enumSubSet) {
		this(expectedType, enumSubSet.enumNames.clone(), enumSubSet.enumValues.clone());
	}


	public CodeTokenEnumSubSet(CodeTokenType expectedType, String[] enumNames, E[] enumValues) {
		super(enumNames, enumValues);
		this.expectedType = expectedType;
	}


	public CodeTokenEnumSubSet(CodeTokenType expectedType, Iterable<E> enums, Predicate<E> filter, Function<E, String> getName) {
		super(enums, filter, getName);
		this.expectedType = expectedType;
	}


	/** Check whether a {@link CodeToken}'s type matches the expected type of this enum subset and the the CodeToken's text is contained in this enum subset
	 * @param node check the CodeToken's {@link CodeToken#getText()}
	 * @return whether the token text is found in this enum subset
	 */
	public boolean is(CodeToken node) {
		if(node != null && node.getTokenType() == this.expectedType) {
			return super.find(node.getText()) != null;
		}
		return false;
	}


	/** Parse a {@link CodeToken}'s text against this enum subset and return the matching value
	 * @param node parse the CodeToken's {@link CodeToken#getText()}
	 * @return the enum subset value matching the token's text or null if no matching value found for the enum name
	 */
	public E parse(CodeToken node) {
		if(node != null && node.getTokenType() == this.expectedType) {
			return super.find(node.getText());
		}
		return null;
	}

}
