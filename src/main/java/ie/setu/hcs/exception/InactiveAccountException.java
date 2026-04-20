package ie.setu.hcs.exception;

public class InactiveAccountException extends AuthenticationException {
    public InactiveAccountException(String message) {
        super(message);
    }
}
