package twg2.parser.codeParser.tools;

import java.util.function.Function;
import java.util.function.Predicate;

import twg2.parser.fragment.CodeToken;
import twg2.parser.fragment.CodeTokenType;

/**
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


	public boolean is(CodeToken node) {
		if(node != null && node.getTokenType() == this.expectedType) {
			return super.find(node.getText()) != null;
		}
		return false;
	}


	public E parse(CodeToken node) {
		if(node != null && node.getTokenType() == this.expectedType) {
			return super.find(node.getText());
		}
		return null;
	}

}
