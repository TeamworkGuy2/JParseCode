package twg2.parser.codeParser.tools;

import java.util.function.Function;
import java.util.function.Predicate;

import twg2.parser.fragment.CodeFragment;
import twg2.parser.fragment.CodeFragmentType;

/**
 * @author TeamworkGuy2
 * @since 2016-4-12
 */
public class CodeFragmentEnumSubSet<E> extends EnumSubSet<E> {
	private CodeFragmentType expectedType;


	public CodeFragmentEnumSubSet(CodeFragmentType expectedType, EnumSubSet<E> enumSubSet) {
		this(expectedType, enumSubSet.enumNames.clone(), enumSubSet.enumValues.clone());
	}


	public CodeFragmentEnumSubSet(CodeFragmentType expectedType, String[] enumNames, E[] enumValues) {
		super(enumNames, enumValues);
		this.expectedType = expectedType;
	}


	public CodeFragmentEnumSubSet(CodeFragmentType expectedType, Iterable<E> enums, Predicate<E> filter, Function<E, String> getName) {
		super(enums, filter, getName);
		this.expectedType = expectedType;
	}


	public boolean is(CodeFragment node) {
		if(node != null && node.getFragmentType() == this.expectedType) {
			return super.find(node.getText()) != null;
		}
		return false;
	}


	public E parse(CodeFragment node) {
		if(node != null && node.getFragmentType() == this.expectedType) {
			return super.find(node.getText());
		}
		return null;
	}

}
