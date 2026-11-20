package pierro.lmsPlatform;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
		"spring.datasource.url=jdbc:h2:mem:context_testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"APP_SIGNER_KEY=1234567890123456789012345678901234567890123456789012345678901234",
		"APP_ENCRYPTION_KEY=KLassBZ05fVMjeRxaLWagLUfy651Om99RewUXpvwsdk=",
		"hashids.salt=test-salt-context-loads"
})
class LmsPlatformApplicationTests {

	@Test
	void contextLoads() {
	}
}