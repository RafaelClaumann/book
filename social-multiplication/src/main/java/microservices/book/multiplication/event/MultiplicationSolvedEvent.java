package microservices.book.multiplication.event;

import java.io.Serializable;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Event that models the fact that a
 * {@link microservices.book.multiplication.domain.Multiplication} has been
 * solved in the system. Provides some context information about the
 * multiplication.
 */
@RequiredArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
public class MultiplicationSolvedEvent implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final long multiplicationResultAttemptId;
	private final long userId;
	private final boolean correct;

}
