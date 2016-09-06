package twg2.parser.stateMachine;

import lombok.Getter;

/** The possible values that a token consumer can return
 * @author TeamworkGuy2
 * @since 2016-2-19
 */
public enum Consume {
	/** The consumer is still valid and the token was consumed */
	ACCEPTED(true),
	/** The consumer is still valid, but the token was not consumed, and can be passed back into the consumer or into the next consumer in a chain */
	ACCEPTED_CONTINUE(true),
	/** The consumer is no longer valid, the token was not consumed */
	REJECTED(false);


	private final @Getter boolean accept;

	Consume(boolean accept) {
		this.accept = accept;
	}

}
