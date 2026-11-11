package pierro.lmsPlatform.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pierro.lmsPlatform.security.crypto.IdObfuscator;

@Component
@RequiredArgsConstructor
public class IdMapper {
    private final IdObfuscator idObfuscator;

    public String encode(Long id) {
        return idObfuscator.encode(id);
    }
}
