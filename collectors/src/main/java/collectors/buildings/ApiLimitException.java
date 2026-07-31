package collectors.buildings;

public class ApiLimitException extends RuntimeException {
    public ApiLimitException(String message) {
        super(message);
    }
}