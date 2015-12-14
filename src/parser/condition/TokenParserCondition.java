package parser.condition;

/**
 * @author TeamworkGuy2
 * @since Dec 12, 2015
 * @param <T_INPUT> the type of input tokens parsed by this parser
 * @param <T_RESULT> the type of result object that parsed data is store in
 */
public interface TokenParserCondition<T_INPUT, T_RESULT> extends ParserCondition {

	/**
	 * @param token the token to parse
	 * @return true if the char was accepted, false if not.
	 * If false is returned, this {@code CharParserCondition} enters a failed state and
	 * will not return true for any further inputs
	 */
	public boolean acceptNext(T_INPUT token);


	/**
	 * @return a destination object containing the parsed data
	 */
	public T_RESULT getParserResult();


	@Override
	public TokenParserCondition<T_INPUT, T_RESULT> copy();


	@Override
	public default TokenParserCondition<T_INPUT, T_RESULT> recycle() {
		throw new UnsupportedOperationException("TokenParserCondition recycling not supported");
	}


	@Override
	public default TokenParserCondition<T_INPUT, T_RESULT> copyOrReuse() {
		TokenParserCondition<T_INPUT, T_RESULT> filter = null;
		if(this.canRecycle()) {
			filter = this.recycle();
		}
		else {
			filter = this.copy();
		}
		return filter;
	}

}
