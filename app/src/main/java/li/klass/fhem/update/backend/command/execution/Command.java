/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 * server.
 *
 * Copyright (c) 2011, Matthias Klass or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLIC LICENSE, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU GENERAL PUBLIC LICENSE
 * for more details.
 *
 * You should have received a copy of the GNU GENERAL PUBLIC LICENSE
 * along with this distribution; if not, write to:
 *   Free Software Foundation, Inc.
 *   51 Franklin Street, Fifth Floor
 *   Boston, MA  02110-1301  USA
 */

package li.klass.fhem.update.backend.command.execution;

import com.google.common.base.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public class Command {
    final String command;
    final Optional<String> connectionId;

    public Command(String command, Optional<String> connectionId) {
        this.command = command.replaceAll(" {2}", " ");
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

        if (command != null ? !command.equals(command1.command) : command1.command != null) {
            return false;
        } else if (connectionId != null) {
            return connectionId.equals(command1.connectionId);
        } else {
            return command1.connectionId == null;
        }

    }

    @Override
    public int hashCode() {
        int result = command != null ? command.hashCode() : 0;
        result = 31 * result + (connectionId != null ? connectionId.hashCode() : 0);
        return result;
    }
}
