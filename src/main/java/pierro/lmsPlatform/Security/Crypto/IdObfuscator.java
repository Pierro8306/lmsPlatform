package pierro.lmsPlatform.Security.Crypto;

import org.hashids.Hashids;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pierro.lmsPlatform.Security.Exception.BadRequestException;

@Component
public class IdObfuscator {

    private static final int DEFAULT_MIN_LENGTH = 8;
    private final Hashids hashids;

    public IdObfuscator(@Value("${hashids.salt}") String salt) {
        this.hashids = new Hashids(salt, DEFAULT_MIN_LENGTH);
    }

    public String encode(Long id) {
        if (id == null) return null;
        return hashids.encode(id);
    }

    public Long decode(String hash) {
        if (hash == null || hash.isBlank()) {
            throw new BadRequestException("ID không hợp lệ");
        }
        long[] result;
        try {
            result = hashids.decode(hash.trim());
        } catch (RuntimeException e) {
            throw new BadRequestException("ID không hợp lệ");
        }
        if (result.length != 1) {
            throw new BadRequestException("ID không hợp lệ");
        }
        return result[0];
    }
}
