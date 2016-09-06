package twg2.parser.stateMachine;

/**
 * @author TeamworkGuy2
 * @since 2016-09-04
 * @param <T_STATE> the state transition type/enum used by this parser to track its state
 * @param <T_RESULT> the type of result object that parsed data is store in
 */
public abstract class AstParserReusableBase<T_STATE, T_RESULT> implements AstParser<T_RESULT> {
	protected String name;
	protected T_STATE state;
	protected final T_STATE completedState;
	protected final T_STATE failedState;


	public AstParserReusableBase(String name, T_STATE completed, T_STATE failed) {
		this.name = name;
		this.completedState = completed;
		this.failedState = failed;
	}


	@Override
	public String name() {
		return name;
	}


	@Override
	public boolean isComplete() {
		return state == completedState;
	}


	@Override
	public boolean isFailed() {
		return state == failedState;
	}


	@Override
	public final boolean canRecycle() {
		return true;
	}

}
