/*
 * Copyright 2021 TR Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package picocli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.util.*;

import static java.lang.String.format;

/**
 * Stand-alone tool that generates bash auto-complete scripts for picocli-based command line applications.
 */
public class AutoComplete {
    private AutoComplete() { }

    /**
     * Generates a bash completion script for the specified command class.
     * @param args command line options. Specify at least the {@code commandLineFQCN} mandatory parameter, which is
     *      the fully qualified class name of the annotated {@code @Command} class to generate a completion script for.
     *      Other parameters are optional. Specify {@code -h} to see details on the available options.
     */
    public static void main(String... args) { CommandLine.run(new App(), System.err, args); }

    /**
     * CLI command class for generating completion script.
     */
    @Command(name = "picocli.AutoComplete", sortOptions = false,
            description = "Generates a bash completion script for the specified command class.")
    private static class App implements Runnable {

        @Parameters(arity = "1", description = "Fully qualified class name of the annotated " +
                "@Command class to generate a completion script for.")
        String commandLineFQCN;

        @Option(names = {"-n", "--name"}, description = "Name of the command to create a completion script for. " +
                "When omitted, the annotated class @Command 'name' attribute is used. " +
                "If no @Command 'name' attribute exists, '<CLASS-SIMPLE-NAME>' (in lower-case) is used.")
        String commandName;

        @Option(names = {"-o", "--completionScript"},
                description = "Path of the completion script file to generate. " +
                        "When omitted, a file named '<commandName>_completion' " +
                        "is generated in the current directory.")
        File autoCompleteScript;

        @Option(names = {"-w", "--writeCommandScript"},
                description = "Write a '<commandName>' sample command script to the same directory " +
                        "as the completion script.")
        boolean writeCommandScript;

        @Option(names = {"-f", "--force"}, description = "Overwrite existing script files.")
        boolean overwriteIfExists;

        @Option(names = { "-h", "--help"}, usageHelp = true, description = "Display this help message and quit.")
        boolean usageHelpRequested;

        public void run() {
            try {
                Class<?> cls = Class.forName(commandLineFQCN);
                CommandLine commandLine = new CommandLine(cls.newInstance());

                if (commandName == null) {
                    commandName = new CommandLine.Help(commandLine.getCommand()).commandName;
                    if (CommandLine.Help.DEFAULT_COMMAND_NAME.equals(commandName)) {
                        commandName = cls.getSimpleName().toLowerCase();
                    }
                }
                if (autoCompleteScript == null) {
                    autoCompleteScript = new File(commandName + "_completion");
                }
                File commandScript = null;
                if (writeCommandScript) {
                    commandScript = new File(autoCompleteScript.getAbsoluteFile().getParentFile(), commandName);
                }
                if (commandScript != null && !overwriteIfExists && checkExists(commandScript)) { return; }
                if (!overwriteIfExists && checkExists(autoCompleteScript)) { return; }

                AutoComplete.bash(commandName, autoCompleteScript, commandScript, commandLine);

            } catch (Exception ex) {
                ex.printStackTrace();
                CommandLine.usage(new App(), System.err);
            }
        }

        private boolean checkExists(final File file) {
            if (file.exists()) {
                System.err.println(file.getAbsolutePath() + " exists. Specify -f to overwrite.");
                CommandLine.usage(this, System.err);
                return true;
            }
            return false;
        }
    }

    private static interface Function<T, V> {
        V apply(T t);
    }

    /**
     * Drops all characters that are not valid for bash function and identifier names.
     */
    private static class Bashify implements Function<CharSequence, String> {
        public String apply(CharSequence value) {
            return bashify(value);
        }
    }
    private static String bashify(CharSequence value) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (Character.isLetterOrDigit(c) || c == '_') {
                builder.append(c);
            } else if (Character.isSpaceChar(c)) {
                builder.append('_');
            }
        }
        return builder.toString();
    }
    private static class EnumNameFunction implements Function<Enum<?>, String> {
        public String apply(final Enum<?> anEnum) {
            return anEnum.name();
        }
    }

    private static class NullFunction implements Function<CharSequence, String> {
        public String apply(CharSequence value) { return value.toString(); }
    }

    private static interface Predicate<T> {
        boolean test(T t);
    }
    private static class BooleanFieldFilter implements Predicate<Field> {
        public boolean test(Field f) {
            return f.getType() == Boolean.TYPE || f.getType() == Boolean.class;
        }
    }
    private static class EnumFieldFilter implements Predicate<Field> {
        public boolean test(Field f) {
            return f.getType().isEnum();
        }
    }
    private static <T> Predicate<T> negate(final Predicate<T> original) {
        return new Predicate<T>() {
            public boolean test(T t) {
                return !original.test(t);
            }
        };
    }
    private static <T> List<T> filter(List<T> list, Predicate<T> filter) {
        List<T> result = new ArrayList<T>();
        for (T t : list) { if (filter.test(t)) { result.add(t); } }
        return result;
    }
    /** Package-private for tests; consider this class private. */
    static class CommandDescriptor {
        final String functionName;
        final String commandName;
        CommandDescriptor(String functionName, String commandName) {
            this.functionName = functionName;
            this.commandName = commandName;
        }
        public int hashCode() { return functionName.hashCode() * 37 + commandName.hashCode(); }
        public boolean equals(Object obj) {
            if (!(obj instanceof CommandDescriptor)) { return false; }
            if (obj == this) { return true; }
            CommandDescriptor other = (CommandDescriptor) obj;
            return other.functionName.equals(functionName) && other.commandName.equals(commandName);
        }
    }

    private static final String HEADER = "" +
            "#!/usr/bin/env bash\n" +
            "#\n" +
            "# %1$s Bash Completion\n" +
            "# =======================\n" +
            "#\n" +
            "# Bash completion support for the `%1$s` command,\n" +
            "# generated by [picocli](http://picocli.info/) version %2$s.\n" +
            "#\n" +
            "# Installation\n" +
            "# ------------\n" +
            "#\n" +
            "# 1. Place this file in a `bash-completion.d` folder:\n" +
            "#\n" +
            "#   * /etc/bash-completion.d\n" +
            "#   * /usr/local/etc/bash-completion.d\n" +
            "#   * ~/bash-completion.d\n" +
            "#\n" +
            "# 2. Open a new bash console, and type `%1$s [TAB][TAB]`\n" +
            "#\n" +
            "# Documentation\n" +
            "# -------------\n" +
            "# The script is called by bash whenever [TAB] or [TAB][TAB] is pressed after\n" +
            "# '%1$s (..)'. By reading entered command line parameters,\n" +
            "# it determines possible bash completions and writes them to the COMPREPLY variable.\n" +
            "# Bash then completes the user input if only one entry is listed in the variable or\n" +
            "# shows the options if more than one is listed in COMPREPLY.\n" +
            "#\n" +
            "# References\n" +
            "# ----------\n" +
            "# [1] http://stackoverflow.com/a/12495480/1440785\n" +
            "# [2] http://tiswww.case.edu/php/chet/bash/FAQ\n" +
            "# [3] https://www.gnu.org/software/bash/manual/html_node/The-Shopt-Builtin.html\n" +
            "# [4] https://stackoverflow.com/questions/17042057/bash-check-element-in-array-for-elements-in-another-array/17042655#17042655\n" +
            "# [5] https://www.gnu.org/software/bash/manual/html_node/Programmable-Completion.html#Programmable-Completion\n" +
            "#\n" +
            "\n" +
            "# Enable programmable completion facilities (see [3])\n" +
            "shopt -s progcomp\n" +
            "\n" +
            "# ArrContains takes two arguments, both of which are the name of arrays.\n" +
            "# It creates a temporary hash from lArr1 and then checks if all elements of lArr2\n" +
            "# are in the hashtable.\n" +
            "#\n" +
            "# Returns zero (no error) if all elements of the 2nd array are in the 1st array,\n" +
            "# otherwise returns 1 (error).\n" +
            "#\n" +
            "# Modified from [4]\n" +
            "function ArrContains() {\n" +
            "  local lArr1 lArr2\n" +
            "  declare -A tmp\n" +
            "  eval lArr1=(\"\\\"\\${$1[@]}\\\"\")\n" +
            "  eval lArr2=(\"\\\"\\${$2[@]}\\\"\")\n" +
            "  for i in \"${lArr1[@]}\";{ [ -n \"$i\" ] && ((++tmp[$i]));}\n" +
            "  for i in \"${lArr2[@]}\";{ [ -n \"$i\" ] && [ -z \"${tmp[$i]}\" ] && return 1;}\n" +
            "  return 0\n" +
            "}\n" +
            "\n";

    private static final String FOOTER = "" +
            "\n" +
            "# Define a completion specification (a compspec) for the\n" +
            "# `%1$s`, `%1$s.sh`, and `%1$s.bash` commands.\n" +
            "# Uses the bash `complete` builtin (see [5]) to specify that shell function\n" +
            "# `_complete_%1$s` is responsible for generating possible completions for the\n" +
            "# current word on the command line.\n" +
            "# The `-o default` option means that if the function generated no matches, the\n" +
            "# default Bash completions and the Readline default filename completions are performed.\n" +
            "complete -F _complete_%1$s -o default %1$s %1$s.sh %1$s.bash\n";

    /**
     * Generates source code for an autocompletion bash script for the specified picocli-based application,
     * and writes this script to the specified {@code out} file, and optionally writes an invocation script
     * to the specified {@code command} file.
     * @param scriptName the name of the command to generate a bash autocompletion script for
     * @param commandLine the {@code CommandLine} instance for the command line application
     * @param out the file to write the autocompletion bash script source code to
     * @param command the file to write a helper script to that invokes the command, or {@code null} if no helper script file should be written
     * @throws IOException if a problem occurred writing to the specified files
     */
    public static void bash(String scriptName, File out, File command, CommandLine commandLine) throws IOException {
        String autoCompleteScript = bash(scriptName, commandLine);
        Writer completionWriter = null;
        Writer scriptWriter = null;
        try {
            completionWriter = new FileWriter(out);
            completionWriter.write(autoCompleteScript);

            if (command != null) {
                scriptWriter = new FileWriter(command);
                scriptWriter.write("" +
                        "#!/usr/bin/env bash\n" +
                        "\n" +
                        "LIBS=path/to/libs\n" +
                        "CP=\"${LIBS}/myApp.jar\"\n" +
                        "java -cp \"${CP}\" '" + commandLine.getCommand().getClass().getName() + "' $@");
            }
        } finally {
            if (completionWriter != null) { completionWriter.close(); }
            if (scriptWriter != null)     { scriptWriter.close(); }
        }
    }

    /**
     * Generates and returns the source code for an autocompletion bash script for the specified picocli-based application.
     * @param scriptName the name of the command to generate a bash autocompletion script for
     * @param commandLine the {@code CommandLine} instance for the command line application
     * @return source code for an autocompletion bash script
     */
    public static String bash(String scriptName, CommandLine commandLine) {
        if (scriptName == null)  { throw new NullPointerException("scriptName"); }
        if (commandLine == null) { throw new NullPointerException("commandLine"); }
        String result = "";
        result += format(HEADER, scriptName, CommandLine.VERSION);

        Map<CommandDescriptor, CommandLine> function2command = new LinkedHashMap<CommandDescriptor, CommandLine>();
        result += generateEntryPointFunction(scriptName, commandLine, function2command);

        for (Map.Entry<CommandDescriptor, CommandLine> functionSpec : function2command.entrySet()) {
            CommandDescriptor descriptor = functionSpec.getKey();
            result += generateFunctionForCommand(descriptor.functionName, descriptor.commandName, functionSpec.getValue());
        }
        result += format(FOOTER, scriptName);
        return result;
    }

    private static String generateEntryPointFunction(String scriptName,
                                                     CommandLine commandLine,
                                                     Map<CommandDescriptor, CommandLine> function2command) {
        String HEADER = "" +
                "# Bash completion entry point function.\n" +
                "# _complete_%1$s finds which commands and subcommands have been specified\n" +
                "# on the command line and delegates to the appropriate function\n" +
                "# to generate possible options and subcommands for the last specified subcommand.\n" +
                "function _complete_%1$s() {\n" +
//                "  CMDS1=(%1$s gettingstarted)\n" +
//                "  CMDS2=(%1$s tool)\n" +
//                "  CMDS3=(%1$s tool sub1)\n" +
//                "  CMDS4=(%1$s tool sub2)\n" +
//                "\n" +
//                "  ArrContains COMP_WORDS CMDS4 && { _picocli_basic_tool_sub2; return $?; }\n" +
//                "  ArrContains COMP_WORDS CMDS3 && { _picocli_basic_tool_sub1; return $?; }\n" +
//                "  ArrContains COMP_WORDS CMDS2 && { _picocli_basic_tool; return $?; }\n" +
//                "  ArrContains COMP_WORDS CMDS1 && { _picocli_basic_gettingstarted; return $?; }\n" +
//                "  _picocli_%1$s; return $?;\n" +
//                "}\n" +
//                "\n" +
//                "complete -F _complete_%1$s %1$s\n" +
//                "\n";
                "";
        String FOOTER = "\n" +
                "  # No subcommands were specified; generate completions for the top-level command.\n" +
                "  _picocli_%1$s; return $?;\n" +
                "}\n";

        StringBuilder buff = new StringBuilder(1024);
        buff.append(format(HEADER, scriptName));

        List<String> predecessors = new ArrayList<String>();
        List<String> functionCallsToArrContains = new ArrayList<String>();

        function2command.put(new CommandDescriptor("_picocli_" + scriptName, scriptName), commandLine);
        generateFunctionCallsToArrContains(scriptName, predecessors, commandLine, buff, functionCallsToArrContains, function2command);

        buff.append("\n");
        Collections.reverse(functionCallsToArrContains);
        for (String func : functionCallsToArrContains) {
            buff.append(func);
        }
        buff.append(format(FOOTER, scriptName));
        return buff.toString();
    }

    private static void generateFunctionCallsToArrContains(String scriptName,
                                                           List<String> predecessors,
                                                           CommandLine commandLine,
                                                           StringBuilder buff,
                                                           List<String> functionCalls,
                                                           Map<CommandDescriptor, CommandLine> function2command) {

        // breadth-first: generate command lists and function calls for predecessors + each subcommand
        for (Map.Entry<String, CommandLine> entry : commandLine.getSubcommands().entrySet()) {
            int count = functionCalls.size();
            String functionName = "_picocli_" + scriptName + "_" + concat("_", predecessors, entry.getKey(), new Bashify());
            functionCalls.add(format("  ArrContains COMP_WORDS CMDS%2$d && { %1$s; return $?; }\n", functionName, count));
            buff.append(      format("  CMDS%2$d=(%1$s)\n", concat(" ", predecessors, entry.getKey(), new Bashify()), count));

            // remember the function name and associated subcommand so we can easily generate a function later
            function2command.put(new CommandDescriptor(functionName, entry.getKey()), entry.getValue());
        }

        // then recursively do the same for all nested subcommands
        for (Map.Entry<String, CommandLine> entry : commandLine.getSubcommands().entrySet()) {
            predecessors.add(entry.getKey());
            generateFunctionCallsToArrContains(scriptName, predecessors, entry.getValue(), buff, functionCalls, function2command);
            predecessors.remove(predecessors.size() - 1);
        }
    }
    private static String concat(String infix, String... values) {
        return concat(infix, Arrays.asList(values));
    }
    private static String concat(String infix, List<String> values) {
        return concat(infix, values, null, new NullFunction());
    }
    private static <V, T extends V> String concat(String infix, List<T> values, T lastValue, Function<V, String> normalize) {
        StringBuilder sb = new StringBuilder();
        for (T val : values) {
            if (sb.length() > 0) { sb.append(infix); }
            sb.append(normalize.apply(val));
        }
        if (lastValue == null) { return sb.toString(); }
        if (sb.length() > 0) { sb.append(infix); }
        return sb.append(normalize.apply(lastValue)).toString();
    }

    private static String generateFunctionForCommand(String functionName, String commandName, CommandLine commandLine) {
        String HEADER = "" +
                "\n" +
                "# Generates completions for the options and subcommands of the `%s` %scommand.\n" +
                "function %s() {\n" +
                "  # Get completion data\n" +
                "  CURR_WORD=${COMP_WORDS[COMP_CWORD]}\n" +
                "  PREV_WORD=${COMP_WORDS[COMP_CWORD-1]}\n" +
                "\n" +
                "  COMMANDS=\"%s\"\n" +  // COMMANDS="gettingstarted tool"
                "  FLAG_OPTS=\"%s\"\n" + // FLAG_OPTS="--verbose -V -x --extract -t --list"
                "  ARG_OPTS=\"%s\"\n";   // ARG_OPTS="--host --option --file -f -u --timeUnit"

        String FOOTER = "" +
                "\n" +
                "  COMPREPLY=( $(compgen -W \"${FLAG_OPTS} ${ARG_OPTS} ${COMMANDS}\" -- ${CURR_WORD}) )\n" +
                "}\n";

        // Get the fields annotated with @Option and @Parameters for the specified CommandLine.
        List<Field> optionFields = new ArrayList<Field>();
        List<Field> positionalFields = new ArrayList<Field>();
        extractOptionsAndParameters(commandLine, optionFields, positionalFields);

        // Build a list of "flag" options that take no parameters and "arg" options that do take parameters, and subcommands.
        String flagOptionNames = optionNames(filter(optionFields, new BooleanFieldFilter()));
        List<Field> argOptionFields = filter(optionFields, negate(new BooleanFieldFilter()));
        String argOptionNames = optionNames(argOptionFields);
        String commands = concat(" ", new ArrayList<String>(commandLine.getSubcommands().keySet())).trim();

        // Generate the header: the function declaration, CURR_WORD, PREV_WORD and COMMANDS, FLAG_OPTS and ARG_OPTS.
        StringBuilder buff = new StringBuilder(1024);
        String sub = functionName.equals("_picocli_" + commandName) ? "" : "sub";
        buff.append(format(HEADER, commandName, sub, functionName, commands, flagOptionNames, argOptionNames));

        // Generate completion lists for options with a known set of valid values.
        // Starting with java enums.
        List<Field> enumOptions = filter(optionFields, new EnumFieldFilter());
        for (Field f : enumOptions) {
            buff.append(format("  %s_OPTION_ARGS=\"%s\" # %s values\n",
                    bashify(f.getName()),
                    concat(" ", Arrays.asList((Enum[]) f.getType().getEnumConstants()), null, new EnumNameFunction()).trim(),
                    f.getType().getSimpleName()));
        }
        // TODO generate completion lists for other option types:
        // Charset, Currency, Locale, TimeZone, ByteOrder,
        // javax.crypto.Cipher, javax.crypto.KeyGenerator, javax.crypto.Mac, javax.crypto.SecretKeyFactory
        // java.security.AlgorithmParameterGenerator, java.security.AlgorithmParameters, java.security.KeyFactory, java.security.KeyPairGenerator, java.security.KeyStore, java.security.MessageDigest, java.security.Signature
        // sql.Types?

        // Now generate the "case" switches for the options whose arguments we can generate completions for
        buff.append(generateOptionsSwitch(argOptionFields, enumOptions));

        // Generate the footer: a default COMPREPLY to fall back to, and the function closing brace.
        buff.append(format(FOOTER));
        return buff.toString();
    }

    private static String generateOptionsSwitch(List<Field> argOptionFields, List<Field> enumOptions) {
        StringBuilder buff = new StringBuilder(1024);
        buff.append("\n");
        buff.append("  case ${CURR_WORD} in\n"); // outer case
        String outerCases = generateOptionsCases(argOptionFields, enumOptions, "", "\"\"");
        if (outerCases.length() == 0) {
            return "";
        }
        buff.append(outerCases);
        buff.append("    *)\n");
        buff.append("      case ${PREV_WORD} in\n"); // inner case
        buff.append(generateOptionsCases(argOptionFields, enumOptions, "    ", "$CURR_WORD"));
        buff.append("      esac\n"); // end inner case
        buff.append("  esac\n"); // end outer case
        return buff.toString();
    }

    private static String generateOptionsCases(List<Field> argOptionFields, List<Field> enumOptions, String indent, String currWord) {
        StringBuilder buff = new StringBuilder(1024);
        for (Field f : argOptionFields) {
            Option option = f.getAnnotation(Option.class);
            if (enumOptions.contains(f)) {
                buff.append(format("%s    %s)\n", indent, concat("|", option.names()))); // "    -u|--timeUnit)\n"
                buff.append(format("%s      COMPREPLY=( $( compgen -W \"${%s_OPTION_ARGS}\" -- %s ) )\n", indent, f.getName(), currWord));
                buff.append(format("%s      return $?\n", indent));
                buff.append(format("%s      ;;\n", indent));
            } else if (f.getType().equals(File.class) || "java.nio.file.Path".equals(f.getType().getName())) {
                buff.append(format("%s    %s)\n", indent, concat("|", option.names()))); // "    -f|--file)\n"
                buff.append(format("%s      compopt -o filenames\n", indent));
                buff.append(format("%s      COMPREPLY=( $( compgen -f -- %s ) ) # files\n", indent, currWord));
                buff.append(format("%s      return $?\n", indent));
                buff.append(format("%s      ;;\n", indent));
            } else if (f.getType().equals(InetAddress.class)) {
                buff.append(format("%s    %s)\n", indent, concat("|", option.names()))); // "    -h|--host)\n"
                buff.append(format("%s      compopt -o filenames\n", indent));
                buff.append(format("%s      COMPREPLY=( $( compgen -A hostname -- %s ) )\n", indent, currWord));
                buff.append(format("%s      return $?\n", indent));
                buff.append(format("%s      ;;\n", indent));
            }
        }
        return buff.toString();
    }

    private static String optionNames(List<Field> optionFields) {
        List<String> result = new ArrayList<String>();
        for (Field f : optionFields) {
            Option option = f.getAnnotation(Option.class);
            result.addAll(Arrays.asList(option.names()));
        }
        return concat(" ", result, "", new NullFunction()).trim();
    }

    private static void extractOptionsAndParameters(CommandLine commandLine,
                                                    List<Field> optionFields,
                                                    List<Field> positionalParameterFields) {

        Map<String, Field> optionName2Field = new LinkedHashMap<String, Field>();
        Class<?> cls = commandLine.getCommand().getClass();
        while (cls != null) {
            CommandLine.init(cls, new ArrayList<Field>(), optionName2Field, new HashMap<Character, Field>(), positionalParameterFields);
            cls = cls.getSuperclass();
        }
        for (Field f : optionName2Field.values()) {
            if (!optionFields.contains(f)) { optionFields.add(f); }
        }
    }
}
