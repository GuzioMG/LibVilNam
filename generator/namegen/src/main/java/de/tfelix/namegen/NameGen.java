package de.tfelix.namegen;

import java.util.Random;
import de.tfelix.namegen.model.RuntimeModel;

/**
 * Name generator main class. This class has to be used as main entry point for
 * the name generation operation.
 * 
 * @author Thomas Felix
 * @author Guzio
 *
 */
public record NameGen<R extends Random>(R random, RuntimeModel generator) {
	/**
	 * Returns a new name, based on the learned model file.
	 * 
	 * @return A new random name.
	 */
	public String getName() throws RuntimeException {
		return generator.apply(random);
	}
}