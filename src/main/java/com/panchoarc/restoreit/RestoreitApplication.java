package com.panchoarc.restoreit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.shell.command.annotation.CommandScan;

@SpringBootApplication
@CommandScan
public class RestoreitApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestoreitApplication.class, args);
    }

}
