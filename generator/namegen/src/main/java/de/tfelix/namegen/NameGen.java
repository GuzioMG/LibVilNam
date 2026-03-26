package de.tfelix.namegen;

import java.util.Random;
import de.tfelix.namegen.model.RuntimeModel;
import org.jetbrains.annotations.NotNull;

/**
 * Name generator main class. This class has to be used as main entry point for
 * the name generation operation.
 * 
 * @author Thomas Felix
 * @author Guzio
 *
 */
public record NameGen<R extends Random>(R random, RuntimeModel<R> generator) {
	/**
	 * Returns a new name, based on the learned model file. Unlike most toString() you encounter in Java, this one will - by definition - give you a different result each time.
	 * 
	 * @return A new random name.
	 */
	@Override
	public @NotNull String toString() throws RuntimeException {
		return generator.apply(random);
	}
}