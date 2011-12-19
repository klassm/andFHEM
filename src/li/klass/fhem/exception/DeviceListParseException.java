package li.klass.fhem.exception;

public class DeviceListParseException extends RuntimeException {

    public DeviceListParseException() {
    }

    public DeviceListParseException(String detailMessage) {
        super(detailMessage);
    }

    public DeviceListParseException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public DeviceListParseException(Throwable throwable) {
        super(throwable);
    }
}
