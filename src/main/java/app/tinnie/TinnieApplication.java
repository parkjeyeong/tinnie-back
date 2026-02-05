package app.tinnie;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"app.tinnie"})
public class TinnieApplication {

  public static void main(String[] args) {
    SpringApplication.run(TinnieApplication.class, args);
  }

}
