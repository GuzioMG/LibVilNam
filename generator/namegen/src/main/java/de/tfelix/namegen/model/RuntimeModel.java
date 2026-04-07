package de.tfelix.namegen.model;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Random;
import java.util.function.Function;

/**
 * The model contains all the data to encode a simple markov graph in order to
 * generate names.
 *
 * @author Thomas Felix
 * @author Guzio
 *
 */
public class RuntimeModel<R extends Random> implements Function<R, String> {
    private final int order;
    private final String prefix;
    private final Map<String, Transition> transitions;
    private final Transition delimiterTransition;

    public RuntimeModel(int order, Map<String, Transition> transitions, Transition delimiterTransition) {
        this.order = order;
        this.transitions = transitions;
        this.prefix = SymbolManager.getStartSymbol(order);
        this.delimiterTransition = delimiterTransition;
    }

    /**
     * The leading text is transformed so it is usable. It gets appended or
     * shortened so that a categorical exists.
     *
     * @param context The context string.
     * @return A prepared context string.
     */
    private String backoff(@NotNull String context) {

        // bring the context to the length of the order.
        if (context.length() > order) {
            context = context.substring(context.length() - order);
        } else if (context.length() < order) {
            context = SymbolManager.getStartSymbol(order - context.length()) + context;
        }

        // Remove length until we find a categorical.
        while (!transitions.containsKey(context) && !context.isEmpty()) {
            context = context.substring(1);
        }

        return context;
    }

    /**
     * Get a Transition for this context
     */
    private Transition getTransition(String context) {
        if (!transitions.containsKey(context)) {
            return delimiterTransition;
        }
        return transitions.get(context);
    }

    /**
     * Generates a new random char from the model depending on the prior context
     * and the random number.
     *
     * @param context The leading text.
     * @param rand    Random number between 0 and 1.
     * @return A new char.
     */
    private char sample(String context, float rand) throws RuntimeException {
        // Check if we need to backoff the context.
        context = backoff(context);
        return getTransition(context).pick(rand);
    }

    /**
     * Generate a random name from the model which was previously generated.
     *
     * @param rand An instance of a random number generator.
     * @return A generated name from the model.
     * @throws RuntimeException if the model hasn't yet been built
     */
    @Override
    public String apply(R rand) throws RuntimeException {
        StringBuilder sequence = new StringBuilder();
        sequence.append(prefix);

        sequence.append(sample(sequence.toString(), rand.nextFloat()));

        while (sequence.charAt(sequence.length() - 1) != SymbolManager.getEndSymbol()) {
            sequence.append(sample(sequence.toString(), rand.nextFloat()));
        }

        // Remove end symbol.
        sequence.delete(0, prefix.length());
        sequence.deleteCharAt(sequence.length() - 1);

        return sequence.toString();
    }
}