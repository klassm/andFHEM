package li.klass.fhem.exception;

public class HostConnectionException extends RuntimeException {
    public HostConnectionException() {
    }

    public HostConnectionException(String detailMessage) {
        super(detailMessage);
    }

    public HostConnectionException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public HostConnectionException(Throwable throwable) {
        super(throwable);
    }
}
