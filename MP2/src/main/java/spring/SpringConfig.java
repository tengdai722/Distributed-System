package spring;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:mp2AppConfig")
@ComponentScan(basePackages = "edu.illinois.cs425")
public class SpringConfig {
}
