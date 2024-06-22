package de.seism0saurus.wisdom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WisdomBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(WisdomBotApplication.class, args);
    }
}
