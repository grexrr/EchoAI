package grexrr.echoai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class EchoaiApplication {
	public static void main(String[] args) {
		SpringApplication.run(EchoaiApplication.class, args);
	}
}

@RestController
class HelloController {
	@GetMapping("/hello")
	public String hello() {
		return "Hello EchoAI!";
	}
} 