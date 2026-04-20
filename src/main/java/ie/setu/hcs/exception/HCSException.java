package ie.setu.hcs.exception;

public class HCSException extends Exception {
    public HCSException(String message) {
        super(message);
    }

    public HCSException(String message, Throwable cause) {
        super(message, cause);
    }
}
