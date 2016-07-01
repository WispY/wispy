package com.wispy.wispy.processor;

/**
 * @author WispY
 */
public class Command {
    private String usage;
    private String description;
    private String pattern;
    private int argumentsCount;
    private String argumentsPattern;
    private boolean hardMatch;
    private CommandHandler handler;

    public String getUsage() {
        return usage;
    }

    public String getDescription() {
        return description;
    }

    public String getPattern() {
        return pattern;
    }

    public int getArgumentsCount() {
        return argumentsCount;
    }

    public String getArgumentsPattern() {
        return argumentsPattern;
    }

    public boolean isHardMatch() {
        return hardMatch;
    }

    public CommandHandler getHandler() {
        return handler;
    }

    public static class CommandBuilder {
        private Command command = new Command();

        public static CommandBuilder builder() {
            return new CommandBuilder();
        }

        public CommandBuilder usage(String usage) {
            command.usage = usage;
            return this;
        }

        public CommandBuilder description(String description) {
            command.description = description;
            return this;
        }

        public CommandBuilder pattern(String pattern) {
            command.pattern = pattern;
            return this;
        }

        public CommandBuilder argumentsCount(int argumentsCount) {
            command.argumentsCount = argumentsCount;
            return this;
        }

        public CommandBuilder argumentsPattern(String argumentsPattern) {
            command.argumentsPattern = argumentsPattern;
            return this;
        }

        public CommandBuilder hardMatch(boolean hardMatch) {
            command.hardMatch = hardMatch;
            return this;
        }

        public CommandBuilder handler(CommandHandler handler) {
            command.handler = handler;
            return this;
        }

        public Command build() {
            return command;
        }
    }
}