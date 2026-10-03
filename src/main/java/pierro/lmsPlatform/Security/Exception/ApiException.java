package pierro.lmsPlatform.Security.Exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException {
    private final int status;

    public ApiException(int status, String message) {
        super(message);
        this.status = status;
    }
    public ApiException(HttpStatus status, String message) {
        this(status.value(), message);
    }
}
