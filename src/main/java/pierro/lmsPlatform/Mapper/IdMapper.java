package pierro.lmsPlatform.Mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pierro.lmsPlatform.Security.Crypto.IdObfuscator;

@Component
@RequiredArgsConstructor
public class IdMapper {
    private final IdObfuscator idObfuscator;

    public String encode(Long id) {
        return idObfuscator.encode(id);
    }
}
