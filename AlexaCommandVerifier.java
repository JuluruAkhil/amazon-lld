import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Main class to run the Alexa Command Verification System.
 * This single file contains all the necessary classes for the LLD.
 */
public class AlexaCommandVerifier {

    public static void main(String[] args) {
        // 1. Setup the validator with all the required rules.
        // The Strategy pattern allows us to inject any combination of rules.
        CommandValidator validator = new CommandValidator(
                new WakeWordRule(),
                new SecondWordIsVerbRule(),
                new NoConsecutiveWordsRule(),
                new MaxWordFrequencyRule());

        System.out.println("--- Alexa Custom Command Verifier ---");

        // --- Test Cases ---

        // Case 1: Valid Command
        System.out.println("\n--- Testing Valid Command ---");
        CommandValidationRequest validRequest = new CommandValidationRequest("Alexa play music for me play");
        System.out.println("Command: \"" + validRequest.getCommand() + "\"");
        System.out.println(validator.validate(validRequest.getCommand()));

        // Case 2: Invalid Wake Word
        System.out.println("\n--- Testing Invalid Wake Word ---");
        CommandValidationRequest invalidWakeWord = new CommandValidationRequest("Hey play music");
        System.out.println("Command: \"" + invalidWakeWord.getCommand() + "\"");
        System.out.println(validator.validate(invalidWakeWord.getCommand()));

        // Case 3: Invalid Verb
        System.out.println("\n--- Testing Invalid Verb ---");
        CommandValidationRequest invalidVerb = new CommandValidationRequest("Alexa read a book");
        System.out.println("Command: \"" + invalidVerb.getCommand() + "\"");
        System.out.println(validator.validate(invalidVerb.getCommand()));

        // Case 4: Consecutive words
        System.out.println("\n--- Testing Consecutive Words ---");
        CommandValidationRequest consecutiveWords = new CommandValidationRequest("Alexa play play music");
        System.out.println("Command: \"" + consecutiveWords.getCommand() + "\"");
        System.out.println(validator.validate(consecutiveWords.getCommand()));

        // Case 5: Word appears too many times
        System.out.println("\n--- Testing Max Word Frequency ---");
        CommandValidationRequest highFrequency = new CommandValidationRequest("Alexa tell a joke tell me a joke tell");
        System.out.println("Command: \"" + highFrequency.getCommand() + "\"");
        System.out.println(validator.validate(highFrequency.getCommand()));

        // Case 6: Multiple violations
        System.out.println("\n--- Testing Multiple Violations ---");
        CommandValidationRequest multipleFails = new CommandValidationRequest(
                "Amazon tell tell me a joke about a tell");
        System.out.println("Command: \"" + multipleFails.getCommand() + "\"");
        System.out.println(validator.validate(multipleFails.getCommand()));

        // Case 7: Command too short
        System.out.println("\n--- Testing Short Command ---");
        CommandValidationRequest shortCommand = new CommandValidationRequest("Alexa");
        System.out.println("Command: \"" + shortCommand.getCommand() + "\"");
        System.out.println(validator.validate(shortCommand.getCommand()));
    }
}

// =================================================================================
// //
// CORE SERVICE AND DATA CLASSES
// =================================================================================
// //

/**
 * The CommandValidator orchestrates the validation process.
 * It takes a list of rules and applies them sequentially to a command.
 */
class CommandValidator {
    private final List<IValidationRule> rules;

    public CommandValidator(IValidationRule... rules) {
        this.rules = List.of(rules);
    }

    /**
     * Validates a command against all configured rules.
     * 
     * @param request The request object containing the command string.
     * @return A ValidationResult object with the outcome.
     */
    public ValidationResult validate(String sentence) {
        // Pre-process the command string into a lower-cased array of words.
        String[] words = sentence.trim().toLowerCase().split("\\s+");

        ValidationResult result = new ValidationResult();

        // Use a stream to apply each rule and collect any violations.
        List<String> violations = rules.stream()
                .map(rule -> rule.validate(words)) // Apply rule
                .filter(Optional::isPresent) // Keep only failed rules
                .map(Optional::get) // Get the error message
                .collect(Collectors.toList());

        violations.forEach(result::addViolation);

        return result;
    }
}

/**
 * Encapsulates the result of a validation check, including success status
 * and a list of specific violation messages.
 */
class ValidationResult {
    private final List<String> violations = new ArrayList<>();

    public void addViolation(String message) {
        violations.add(message);
    }

    public boolean isValid() {
        return violations.isEmpty();
    }

    public List<String> getViolations() {
        return Collections.unmodifiableList(violations);
    }

    @Override
    public String toString() {
        if (isValid()) {
            return "✅ Command is valid.";
        }
        return "❌ Command is invalid. Reasons: \n- " + String.join("\n- ", violations);
    }
}

// =================================================================================
// //
// VALIDATION RULES (STRATEGY PATTERN)
// =================================================================================
// //

/**
 * The Strategy Interface for all validation rules.
 * A functional interface allows for lambda-based rule creation if needed.
 */
@FunctionalInterface
interface IValidationRule {
    /**
     * Validates an array of words against a specific rule.
     * 
     * @param words The command tokenized into an array of words.
     * @return An Optional containing an error message if the rule is violated,
     *         otherwise an empty Optional.
     */
    Optional<String> validate(String[] words);
}

/**
 * Rule #1: The first word of each command has to be "Alexa".
 */
class WakeWordRule implements IValidationRule {
    private static final String WAKE_WORD = "alexa";

    @Override
    public Optional<String> validate(String[] words) {
        if (words.length == 0 || !words[0].equalsIgnoreCase(WAKE_WORD)) {
            return Optional.of("Rule Violated: The first word must be '" + WAKE_WORD + "'. Found: '"
                    + (words.length > 0 ? words[0] : "nothing") + "'.");
        }
        return Optional.empty();
    }
}

/**
 * Rule #2: The second word has to be a verb.
 */
class SecondWordIsVerbRule implements IValidationRule {
    // In a real system, this would come from a dictionary, a config file, or a more
    // sophisticated NLP service.
    private static final Set<String> VERBS = new HashSet<>(Arrays.asList(
            "play", "set", "call", "tell", "ask", "turn", "start", "stop", "increase", "decrease"));

    @Override
    public Optional<String> validate(String[] words) {
        if (words.length < 2) {
            // This case is implicitly handled by other rules but good to have for clarity.
            return Optional.of("Rule Violated: Command is too short, requires a verb after the wake word.");
        }
        if (!VERBS.contains(words[1].toLowerCase())) {
            return Optional.of("Rule Violated: The second word '" + words[1] + "' is not a recognized verb.");
        }
        return Optional.empty();
    }
}

/**
 * Rule #3: No word should appear twice in a row.
 */
class NoConsecutiveWordsRule implements IValidationRule {
    @Override
    public Optional<String> validate(String[] words) {
        for (int i = 0; i < words.length - 1; i++) {
            if (words[i].equalsIgnoreCase(words[i + 1])) {
                return Optional.of("Rule Violated: The word '" + words[i] + "' appears twice in a row.");
            }
        }
        return Optional.empty();
    }
}

/**
 * Rule #4: No word should appear more than twice in the entire phrase.
 */
class MaxWordFrequencyRule implements IValidationRule {
    private static final int MAX_OCCURRENCES = 2;

    @Override
    public Optional<String> validate(String[] words) {
        // Create a frequency map of words (case-insensitive).
        Map<String, Long> wordCounts = Arrays.stream(words)
                .map(String::toLowerCase)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        // Check if any word exceeds the maximum allowed occurrences.
        for (Map.Entry<String, Long> entry : wordCounts.entrySet()) {
            if (entry.getValue() > MAX_OCCURRENCES) {
                return Optional.of("Rule Violated: The word '" + entry.getKey() + "' appears " + entry.getValue()
                        + " times, but the maximum is " + MAX_OCCURRENCES + ".");
            }
        }
        return Optional.empty();
    }
}
