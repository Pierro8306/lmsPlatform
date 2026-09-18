package pierro.lmsPlatform.DTO.Response;

import lombok.Data;

@Data
public class ResponseObject {
    private String status;
    private String message;
    private Object data;

}