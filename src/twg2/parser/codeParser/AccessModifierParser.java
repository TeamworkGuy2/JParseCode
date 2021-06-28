package twg2.parser.codeParser;

import java.util.List;

import twg2.parser.language.CodeLanguage;

/**
 * @author TeamworkGuy2
 * @since 2015-12-5
 */
public interface AccessModifierParser<T_ACCESS_MODIFIER extends Keyword, T_BLOCK extends BlockType> {

	/**
	 * @return the {@link CodeLanguage} this parser supports parsing
	 */
	public CodeLanguage getLanguage();

	/** Determine the access modifier for a {@link BlockType} based on any available access modifiers and the language's default behavior.
	 * Example, in C# the default access modifier for a class nested inside another class is 'PRIVATE'.
	 * @param accessModifiers the access modifier string tokens for the {@code currentBlock}, can be null if the block has no access modifiers.
	 * The string is converted to an access modifier first via {@link #tryParseFromSrc(String)}
	 * @param currentBlock the type of block the {@code access} is applied to
	 * @param parentBlock the type of block of the parent scope the {@code currentBlock} is nested inside
	 * @return the calculated {@code T_ACCESS_MODIFIER} for this currentBlock based on the {@code access} modifier or lack thereof
	 */
	public T_ACCESS_MODIFIER defaultAccessModifier(List<String> accessModifiers, T_BLOCK currentBlock, T_BLOCK parentBlock);

	/** Determine the access modifier for a {@link BlockType} based on any available access modifiers and the language's default behavior.
	 * Example, in C# the default access modifier for a class nested inside another class is 'PRIVATE'.
	 * @param access access modifier for the {@code currentBlock}, can be null if the block has no access modifiers
	 * @param currentBlock the type of block the {@code access} is applied to
	 * @param parentBlock the type of block of the parent scope the {@code currentBlock} is nested inside
	 * @return the calculated {@code T_ACCESS_MODIFIER} for this currentBlock based on the {@code access} modifier or lack thereof
	 */
	public T_ACCESS_MODIFIER defaultAccessModifier(T_ACCESS_MODIFIER access, T_BLOCK currentBlock, T_BLOCK parentBlock);

	/** Check if a list of strings matches a known access modifier {@link Keyword}.
	 * @param accessModifiers the string tokens to check
	 * @return the {@code T_ACCESS_MODIFIER} these strings represents or null if no match is found
	 */
	public T_ACCESS_MODIFIER tryParseFromSrc(List<String> accessModifiers);

}
