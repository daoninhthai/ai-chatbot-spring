package com.daoninhthai.aichatbot.service.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Function;

/**
 * Calculator tool that evaluates simple mathematical expressions.
 * Supports basic arithmetic operations: +, -, *, /
 */
@Slf4j
@Component
public class CalculatorTool implements Function<String, String> {

    @Override
    public String apply(String expression) {
        log.debug("Evaluating expression: {}", expression);
        try {
            String sanitized = expression.replaceAll("[^0-9+\\-*/().\\s]", "").trim();
            double result = evaluateExpression(sanitized);

            if (result == Math.floor(result) && !Double.isInfinite(result)) {
                return String.valueOf((long) result);
            }
            return String.format("%.6f", result);
        } catch (Exception e) {
            log.error("Failed to evaluate expression: {}", expression, e);
            return "Error: Unable to evaluate expression '" + expression + "' - " + e.getMessage();
        }
    }

    private double evaluateExpression(String expr) {
        expr = expr.trim();

        // Handle parentheses
        while (expr.contains("(")) {
            int closeIdx = expr.indexOf(')');
            int openIdx = expr.lastIndexOf('(', closeIdx);
            String inner = expr.substring(openIdx + 1, closeIdx);
            double innerResult = evaluateExpression(inner);
            expr = expr.substring(0, openIdx) + innerResult + expr.substring(closeIdx + 1);
        }

        // Handle addition and subtraction (lowest precedence)
        int addIdx = findLastOperator(expr, '+', '-');
        if (addIdx > 0) {
            double left = evaluateExpression(expr.substring(0, addIdx));
            double right = evaluateExpression(expr.substring(addIdx + 1));
            return expr.charAt(addIdx) == '+' ? left + right : left - right;
        }

        // Handle multiplication and division
        int mulIdx = findLastOperator(expr, '*', '/');
        if (mulIdx > 0) {
            double left = evaluateExpression(expr.substring(0, mulIdx));
            double right = evaluateExpression(expr.substring(mulIdx + 1));
            if (expr.charAt(mulIdx) == '/' && right == 0) {
                throw new ArithmeticException("Division by zero");
            }
            return expr.charAt(mulIdx) == '*' ? left * right : left / right;
        }

        return Double.parseDouble(expr.trim());
    }

    private int findLastOperator(String expr, char op1, char op2) {
        int depth = 0;
        for (int i = expr.length() - 1; i > 0; i--) {
            char c = expr.charAt(i);
            if (c == ')') depth++;
            else if (c == '(') depth--;
            else if (depth == 0 && (c == op1 || c == op2)) {
                return i;
            }
        }
        return -1;
    }
}
