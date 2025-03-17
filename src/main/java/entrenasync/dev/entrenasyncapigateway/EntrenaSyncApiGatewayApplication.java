package entrenasync.dev.entrenasyncapigateway;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EntrenaSyncApiGatewayApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(EntrenaSyncApiGatewayApplication.class, args);

    }
    @Override
    public void run(String... args) throws Exception {
        System.out.println("🟢 Servidor escuchando");
    }
}
