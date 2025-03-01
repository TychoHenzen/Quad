package com.quadexercise.quad.utils;

import com.fasterxml.jackson.core.TreeNode;
import com.quadexercise.quad.exceptions.TriviaParseException;

/**
 * Utility class for validating JSON structures.
 * Provides centralized validation logic for API responses.
 */
@SuppressWarnings("DuplicateStringLiteralInspection")
public enum JsonValidator {
    ;

    /**
     * Validates that a TreeNode contains the specified field.
     *
     * @param node  The TreeNode to validate
     * @param field The field name to check for
     * @throws TriviaParseException if the field is missing
     */
    public static void validateField(TreeNode node, String field) {
        if (node.get(field) == null) {
            throw new TriviaParseException(
                    String.format("Missing required field (%s) in trivia question", field));
        }
    }

    /**
     * Validates that a trivia question node contains all required fields.
     *
     * @param node The node to validate
     * @throws TriviaParseException if any required field is missing
     */
    public static void validateResultNode(TreeNode node) {
        // Check for required fields existence
        validateField(node, "category");
        validateField(node, "type");
        validateField(node, "difficulty");
        validateField(node, "question");
        validateField(node, "correct_answer");
        validateField(node, "incorrect_answers");
    }

    /**
     * Validates that a full trivia API response has the expected structure.
     *
     * @param root The root JsonNode of the API response
     * @throws TriviaParseException if the response structure is invalid
     */
    public static void validateTriviaResponse(TreeNode root) {
        if (root.get("results") == null) {
            throw new TriviaParseException("required field (results) not found in trivia response");
        }

        if (!root.get("results").isArray()) {
            throw new TriviaParseException("field (results) is not an array in trivia response");
        }
    }
}
