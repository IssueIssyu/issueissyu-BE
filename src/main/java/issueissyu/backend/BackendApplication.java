package issueissyu.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;

//todo- 로그인 구현완료시 exclude 구문 삭제할것
@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}
