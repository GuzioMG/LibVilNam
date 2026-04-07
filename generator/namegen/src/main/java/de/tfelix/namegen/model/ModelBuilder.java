package de.tfelix.namegen.model;

import com.ibm.icu.util.ULocale;
import org.slf4j.Logger;

import java.util.Random;

/**
 * Use this class to train a model based on a simple text file containing a list
 * of new-line-terminated names. It will initialize a serialized file which can
 * be loaded by the name generator later and used to generate the names.
 * <p>
 * The file to be analyzed must contain a newline terminated list of names.
 * </p>
 *
 * @author Thomas Felix
 * @author Guzio
 */
public class ModelBuilder<R extends Random> {
    private final TrainableModel<R> trainableModel;
    private final ULocale locale;

    /**
     * Ctor.
     * The prior value is added to each probability in order to smooth out the
     * distribution and make less likely symbols occur more often which might be
     * the case for small training samples.
     *
     * @param maxOrder    Maximum order of the Markov model. 3 is a good default value.
     * @param prior       The higher the prior value is, the more random the trainableModel will
     *                    be. Must be higher if there is not enough training data.
     *                    Usually a value between 0.01 and 0.05 is a good start.
     * @param katzBackoff If the probability for choosing a new terminal for the name is
     *                    under this threshold we will fall back to the lower order
     *                    trainableModel of the Markov chain. 0.05 is a reasonable default value.
     * @param locale      The locale to use for generating letters that were not seen in
     *                    the training set.
     */
    public ModelBuilder(int maxOrder, float prior, float katzBackoff, ULocale locale, Logger logger) {
        if (maxOrder < 1 || maxOrder > 10) {
            throw new IllegalArgumentException("Order must be between 1 and 10.");
        }

        if (prior < 0) {
            throw new IllegalArgumentException("Prior value must be bigger than 0.");
        }

        if (katzBackoff < 0) {
            throw new IllegalArgumentException("KatzBackoff must be bigger then 0.");
        }

        this.trainableModel = new MarkovModel<>(maxOrder, prior, locale, logger);
        this.locale = locale;
    }

    /**
     * Adds the specified word(s) to this chain's training data. Can be called multiple times to add many words.
     * 
     * @param spaceSeparated A string containing either one word to train on, or multiple words, each space-separated.
     * @return self
     */
    public ModelBuilder<R> from(String spaceSeparated){
        var data = spaceSeparated.split(" ");
        for (var entry : data) trainableModel.update(entry.trim().toLowerCase(locale.toLocale()));
        return this;
    }

    public RuntimeModel<R> build() {
        return this.trainableModel.build();
    }
}