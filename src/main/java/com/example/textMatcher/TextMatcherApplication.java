package com.example.textMatcher;

import com.example.textMatcher.config.FileMatcherProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(FileMatcherProperties.class)
public class TextMatcherApplication {

	public static void main(String[] args) {
		SpringApplication.run(TextMatcherApplication.class, args);
	}

}
