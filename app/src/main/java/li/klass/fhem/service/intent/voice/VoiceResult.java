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

package li.klass.fhem.service.intent.voice;

public abstract class VoiceResult {

    public enum ErrorType {
        MORE_THAN_ONE_DEVICE_MATCHES,
        NO_DEVICE_MATCHED
    }

    public static class Error extends VoiceResult {

        public final ErrorType errorType;

        public Error(ErrorType errorType) {
            this.errorType = errorType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Error error = (Error) o;

            return errorType == error.errorType;

        }

        @Override
        public int hashCode() {
            return errorType != null ? errorType.hashCode() : 0;
        }

        @Override
        public String toString() {
            return "Error{" +
                    "errorType=" + errorType +
                    '}';
        }
    }

    public static class Success extends VoiceResult {
        public final String deviceName;
        public final String targetState;

        public Success(String deviceName, String targetState) {
            this.deviceName = deviceName;
            this.targetState = targetState;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Success success = (Success) o;

            if (deviceName != null ? !deviceName.equals(success.deviceName) : success.deviceName != null)
                return false;
            return !(targetState != null ? !targetState.equals(success.targetState) : success.targetState != null);

        }

        @Override
        public int hashCode() {
            int result = deviceName != null ? deviceName.hashCode() : 0;
            result = 31 * result + (targetState != null ? targetState.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "Success{" +
                    "deviceName='" + deviceName + '\'' +
                    ", targetState='" + targetState + '\'' +
                    '}';
        }
    }
}
