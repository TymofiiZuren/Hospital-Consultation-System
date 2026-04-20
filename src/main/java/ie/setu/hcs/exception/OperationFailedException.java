package ie.setu.hcs.exception;

public class OperationFailedException extends HCSException {
    public OperationFailedException(String message) {
        super(message);
    }

    public OperationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
