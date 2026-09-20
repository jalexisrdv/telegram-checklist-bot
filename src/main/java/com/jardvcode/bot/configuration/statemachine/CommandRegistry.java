package com.jardvcode.bot.configuration.statemachine;

import com.jardvcode.bot.checklist.domain.BotCommand;
import com.jardvcode.bot.checklist.state.AssignmentOverviewState;
import com.jardvcode.bot.checklist.state.SelectAssignmentState;
import com.jardvcode.bot.checklist.state.SelectSectionState;
import com.jardvcode.bot.shared.domain.exception.BotException;
import com.jardvcode.bot.shared.domain.state.State;
import com.jardvcode.bot.user.state.WelcomeState;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Set;

@Component
public final class CommandRegistry {

    private final HashMap<String, Command> commands;

    public CommandRegistry() {
        this.commands = load();
    }

    public boolean canExecute(String command, Set<String> permissions) {
        if(!commands.containsKey(command)) {
            return false;
        }

        if(!permissions.contains(commands.get(command).permission)) {
            throw new BotException("No tienes permisos para ejecutar comando " + command);
        }

        return true;
    }

    public Class<? extends State> find(String command) {
        return commands.get(command).initialState();
    }

    private HashMap<String, Command> load() {
        HashMap<String, Command> commands = new HashMap<>();

        commands.put(BotCommand.GUIDE.value(), Command.create("CHECKLIST_ASSIGNMENTS_BOT", WelcomeState.class));
        commands.put(BotCommand.OVERVIEW.value(), Command.create("CHECKLIST_ASSIGNMENTS_BOT", AssignmentOverviewState.class));
        commands.put(BotCommand.ASSIGNMENTS.value(), Command.create("CHECKLIST_ASSIGNMENTS_BOT", SelectAssignmentState.class));
        commands.put(BotCommand.SECTIONS.value(), Command.create("CHECKLIST_ASSIGNMENTS_BOT", SelectSectionState.class));

        return commands;
    }

    private record Command(String permission, Class<? extends State> initialState) {
        public static Command create(String permission, Class<? extends State> state) {
            return new Command(permission, state);
        }
    }

}
