package de.tfelix.namegen.model;

import com.ibm.icu.text.UnicodeSet;
import org.slf4j.Logger;

import com.ibm.icu.util.ULocale;
import com.ibm.icu.util.LocaleData;

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;

/**
 * This holds every following character for an input state. It can be picked at
 * random by providing a float between 0 and 1.
 * 
 * @author Thomas Felix
 * @author Guzio
 *
 */
public class Transition implements Serializable {
	private final ULocale locale;
	private final Map<Character, Integer> observedChars;
	private int observations;
	private float priorProbability;
	private final Logger logger;
	private Map<Character, Float> distribution;  // Store the distribution here once the observations are finished.

	/*
	 * Larger alphabets should really have smaller priors, but anyway, to offer some amount of independence between the
	 * prior and the alphabet, we'll scale the selection point by the actual probability distribution (created with the
	 * prior), rather than trying to build a distribution that sums to 1.0. The sum of our assembled probabilities will
	 * be «selectionRange».
	 */

	/**
	 * A Transition represents the flow of «something» to a random character from an available alphabet.
	 *
	 * @param priorProbability: The default chance of being chosen, applied to each character of the alphabet.
	 */
	public Transition(float priorProbability, ULocale alphabetLocale, Logger logger) {
        this.logger = logger;
        if(priorProbability < 0 || priorProbability > 1.0f) {
			throw new IllegalArgumentException("Prior must be ≥ 0.");
		}
		this.locale = alphabetLocale;
		this.priorProbability = priorProbability;
		this.observedChars = new HashMap<>();
	}

	/**
	 * Updates the transition with a new output choice. The
	 * internal counts and observations will be updated.
	 * 
	 * @param c The character that is being defined as a valid output.
	 */
	public void update(char c) {
		if (!observedChars.containsKey(c)) {
			observedChars.put(c, 0);
		}

		observedChars.put(c, observedChars.get(c) + 1);
		observations++;
	}

	/**
	 * Once we've finished learning, prepare the whole alphabet for generating letters.
	 */
	public Transition build() {
		Transition runtimeTransition = new Transition(this.priorProbability, this.locale, logger);
		// Having a tree allows deterministic traversal
		runtimeTransition.distribution = new TreeMap<>();
		float observationRange = 1.0f;
		if(priorProbability >= Math.ulp(1.0)) {
			// Prior is desired; initialize the alphabet.
			UnicodeSet alphabet = LocaleData.getExemplarSet(this.locale, UnicodeSet.IGNORE_SPACE, LocaleData.ES_STANDARD); //I'm not sure what these options do (they apparently weren't a thing back when Felix wrote this), but the description says the IGNORE_SPACE bit is always set, regardless of the value of 'options', so I'm picking the safe option of a no-op.
			/* Observations need to be scaled so that the probability across the alphabet sums to 1.0 */
			observationRange = (1.0f - priorProbability * alphabet.size());
			if (observationRange < 0.0) {
				logger.warn("[Markov:Transition/build] The prior probability was meant to be the chance that any available letter would occur. By specifying a probability of {} with {} letters in the alphabet means that there's no room in the probability distribution to adjust for the letters that are more likely.", priorProbability, alphabet.size());
				// Set a default prior − the show must go on
				priorProbability = 1.0f / (2.0f*alphabet.size());
				observationRange = 0.5f;  // Half of our outputs will be influenced by the observations
			}
            for (var letter : alphabet) runtimeTransition.distribution.put(letter.charAt(0), priorProbability);
		}
		for(Entry<Character, Integer> entry: observedChars.entrySet()) {
			// Some observed characters (such as the ending token, hopefully) might not belong to the alphabet
			float probability = observationRange * entry.getValue() / (float)(this.observations);
			if (runtimeTransition.distribution.containsKey(entry.getKey())) {
				Float prior = runtimeTransition.distribution.get(entry.getKey());
				probability += prior;
			}
			runtimeTransition.distribution.put(entry.getKey(), probability);
		}
		return runtimeTransition;
	}

	/**
	 * Deterministically pick a new character from this transition's probability distribution.
	 * 
	 * @param position A position in the probability distribution ∈ [0, 1.0].
	 * @return A randomly picked character.
	 */
	public char pick(float position) throws RuntimeException {
		if (position < 0 || position > 1.0) {
			throw new IllegalArgumentException(String.format("Probability %g must be between 0 and 1.0", position));
		}
		if(distribution == null) {
			throw new RuntimeException("A transition was called for sampling before it had been built.");
		}
		float cumulation = 0f;
		for (Entry<Character, Float> entry : distribution.entrySet()) {
			// By iterating and cumulating, it's easier to query specific letters and see their probability.
			cumulation += entry.getValue();
			if(cumulation > position) {
				return entry.getKey();
			}
		}
		logger.error("[Markov:Transition/pick] Unable to find a position for {} in Transition ", position);
		return SymbolManager.getEndSymbol();
	}

	@Override
	public String toString() {
		return String.format("Transition: %d sightings ∈ output: [%s]", observations, observedChars.entrySet());
	}
}