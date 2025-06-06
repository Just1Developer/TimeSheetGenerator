/* Licensed under MIT 2023-2024. */
package checker;

import data.TimeSheet;

import java.util.Collection;

/**
 * A checker is able to check the validity of an {@link TimeSheet}.
 */
public interface IChecker {

	/**
	 * Checks the validity of a {@link TimeSheet}.
	 * 
	 * @return If the {@link TimeSheet} is valid {@link CheckerReturn}.Valid is
	 *         returned. Invalid otherwise.
	 * @throws CheckerException if an error occurs while checking the
	 *                          {@link TimeSheet}.
	 */
	CheckerReturn check() throws CheckerException;

	/**
	 * Returns a {@link Collection} of {@link CheckerError} elements that occurred
	 * while checking a {@link TimeSheet}.
	 * 
	 * @return A {@link Collection} of {@link CheckerError CheckerErrors}.
	 */
	Collection<CheckerError> getErrors();

}
