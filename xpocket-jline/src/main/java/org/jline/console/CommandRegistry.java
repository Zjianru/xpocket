/*
 * Copyright (c) 2002-2020, the original author or authors.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 *
 * https://opensource.org/licenses/BSD-3-Clause
 */
package org.jline.console;

import org.jline.reader.impl.completer.SystemCompleter;
import org.jline.terminal.Terminal;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.*;

/**
 * Store command information, compile tab completers and execute registered commands.
 *
 * @author <a href="mailto:matti.rintanikkola@gmail.com">Matti Rinta-Nikkola</a>
 */
public interface CommandRegistry {

    /**
     * Aggregate SystemCompleters of commandRegisteries
     * @param commandRegistries command registeries which completers is to be aggregated
     * @return uncompiled SystemCompleter
     */
    static SystemCompleter aggregateCompleters(CommandRegistry ... commandRegistries) {
        SystemCompleter out = new SystemCompleter();
        for (CommandRegistry r: commandRegistries) {
            out.add(r.compileCompleters());
        }
        return out;
    }

    /**
     * Aggregate and compile SystemCompleters of commandRegisteries
     * @param commandRegistries command registeries which completers is to be aggregated and compile
     * @return compiled SystemCompleter
     */
    static SystemCompleter compileCompleters(CommandRegistry ... commandRegistries) {
        SystemCompleter out = aggregateCompleters(commandRegistries);
        out.compile();
        return out;
    }

    /**
     * Returns the name of this registry.
     * @return the name of the registry
     */
    default String name() {
        return this.getClass().getSimpleName();
    }
    /**
     * Returns the command names known by this registry.
     * @return the set of known command names, excluding aliases
     */
    Set<String> commandNames();

    /**
     * Returns a map of alias-to-command names known by this registry.
     * @return a map with alias keys and command name values
     */
    Map<String, String> commandAliases();

    /**
     * Returns a short info about command known by this registry.
     * @param command the command name
     * @return a short info about command
     */
    List<String> commandInfo(String command);

    /**
     * Returns whether a command with the specified name is known to this registry.
     * @param command the command name to test
     * @return true if the specified command is registered
     */
    boolean hasCommand(String command);

    /**
     * Returns a {@code SystemCompleter} that can provide detailed completion
     * information for all registered commands.
     * @return a SystemCompleter that can provide command completion for all registered commands
     */
    SystemCompleter compileCompleters();

    /**
     * Returns a command description for use in the JLine Widgets framework.
     * Default method must be overridden to return sub command descriptions.
     * @param args command line arguments
     * @return command description for JLine TailTipWidgets to be displayed
     *         in the terminal status bar.
     */
    default CmdDesc commandDescription(List<String> args) {
        return !args.isEmpty() ? commandDescription(args.get(0)) : commandDescription("");
    }

    /**
     * Returns a command description for use in the JLine Widgets framework.
     * @param command the name of the command whose description to return
     * @return command description for JLine TailTipWidgets to be displayed
     *         in the terminal status bar.
     */
    CmdDesc commandDescription(String command);

    /**
     * Execute a command that have only string parameters and options. Implementation of the method is required
     * when aggregating command registries using SystemRegistry.
     * @param session the data of the current command session
     * @param command the name of the command
     * @param args arguments of the command
     * @return result of the command execution
     * @throws Exception in case of error
     */
    default Object execute(CommandSession session, String command, String[] args) throws Exception {
        throw new IllegalArgumentException("CommandRegistry method execute(String command, String[] args) is not implemented!");
    }

    /**
     * Execute a command. If command has other than string parameters a custom implementation is required.
     * This method will be called only when we have ConsoleEngine in SystemRegistry.
     * @param session the data of the current command session
     * @param command the name of the command
     * @param args arguments of the command
     * @return result of the command execution
     * @throws Exception in case of error
     */
    default Object invoke(CommandSession session, String command, Object... args) throws Exception {
        String[] _args = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            if (!(args[i] instanceof String)) {
                throw new IllegalArgumentException();
            }
            _args[i] = args[i].toString();
        }
        return execute(session, command, _args);
    }

    public static class CommandSession {
        private final Terminal terminal;
        private final InputStream in;
        private final PrintStream out;
        private final PrintStream err;

        public CommandSession() {
            this.in = System.in;
            this.out = System.out;
            this.err = System.err;
            this.terminal = null;
        }

        public CommandSession(Terminal terminal) {
            this(terminal, terminal.input(), new PrintStream(terminal.output()), new PrintStream(terminal.output()));
        }

        public CommandSession(Terminal terminal, InputStream in, PrintStream out, PrintStream err) {
            this.terminal = terminal;
            this.in = in;
            this.out = out;
            this.err = err;
        }

        public Terminal terminal() {
            return terminal;
        }

        public InputStream in() {
            return in;
        }

        public PrintStream out() {
            return out;
        }

        public PrintStream err() {
            return err;
        }

    }

}
