package li.klass.fhem.service;

import com.google.common.base.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public class Command {
    final String command;
    final Optional<String> connectionId;

    public Command(String command, Optional<String> connectionId) {
        this.command = command.replaceAll("  ", " ");
        this.connectionId = checkNotNull(connectionId);
    }

    public Command(String command) {
        this(command, Optional.<String>absent());
    }

    @Override
    public String toString() {
        return "Command{" +
                "command='" + command + '\'' +
                ", connectionId=" + connectionId +
                '}';
    }

    public String getCommand() {
        return command;
    }

    public Optional<String> getConnectionId() {
        return connectionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Command command1 = (Command) o;

        if (command != null ? !command.equals(command1.command) : command1.command != null)
            return false;
        return connectionId != null ? connectionId.equals(command1.connectionId) : command1.connectionId == null;

    }

    @Override
    public int hashCode() {
        int result = command != null ? command.hashCode() : 0;
        result = 31 * result + (connectionId != null ? connectionId.hashCode() : 0);
        return result;
    }
}
