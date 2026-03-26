package de.tfelix.namegen.model;

import java.util.Random;

/**
 * A model containing the descriptive information in order to create new names
 * from it. The model must be serializable so it can be written to a file.
 * 
 * @author Thomas Felix
 * @author Guzio
 *
 */
public interface TrainableModel<R extends Random> {

	/**
	 * Updates the model with new information which is contained in the given
	 * string.
	 * 
	 * @param line A new name to be included into the model's description.
	 */
	void update(String line);

    /**
     * Returns a modified TrainableModel instance that has been optimized for runtime rather than training. This should be called
     * before any subsequent calls to generate().
     * @return A Learner instance
     */
	RuntimeModel<R> build();
}