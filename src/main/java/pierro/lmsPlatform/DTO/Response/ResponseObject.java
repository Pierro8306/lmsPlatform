package pierro.lmsPlatform.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseObject {
    private String status;
    private String message;
    private Object data;

    public static ResponseEntity<ResponseObject> ok(String message, Object data) {
        return ResponseEntity.ok(new ResponseObject("SUCCESS", message, data));
    }

    public static ResponseEntity<ResponseObject> ok(String message) {
        return ok(message, null);
    }
}
