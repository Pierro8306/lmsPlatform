package pierro.lmsPlatform.Controller.Common;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pierro.lmsPlatform.DTO.Request.LoginRequest;
import pierro.lmsPlatform.DTO.Response.LoginResponse;
import pierro.lmsPlatform.DTO.Response.ResponseObject;
import pierro.lmsPlatform.Security.Auth.AuthenticationService;


@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping(path = "/api/auth")
public class AuthController {
    AuthenticationService authenticationService;
    @PostMapping("/login")
    public ResponseEntity<ResponseObject> login(@RequestBody LoginRequest loginRequest) {

        LoginResponse auth = authenticationService.authenticate(loginRequest);

        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ResponseObject("failed", "Invalid username or password", "")
            );
        }

        return ResponseEntity.status(HttpStatus.OK).body(
                new ResponseObject("ok", "Login successfully", auth)
        );
    }
}
