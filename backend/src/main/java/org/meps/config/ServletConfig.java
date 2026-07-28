package org.meps.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
@EnableWebMvc
@ComponentScan(
        basePackages = {
                "org.meps.controller",
                //"org.meps.exception"
        }
)

public class ServletConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Authorization")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(
            ResourceHandlerRegistry registry
    ) {

        registry
                .addResourceHandler(
                        "/resources/**"
                )
                .addResourceLocations(
                        "/resources/"
                );

        // registry
        // 	.addResourceHandler(
        // 		"/swagger-ui.html"
        // 	)
        // 	.addResourceLocations(
        // 		"classpath:/META-INF/resources/"
        // 	);
        //
        // registry
        // 	.addResourceHandler(
        // 		"/webjars/**"
        // 	)
        // 	.addResourceLocations(
        // 		"classpath:/META-INF/resources/webjars/"
        // 	);
        //
        // registry
        // 	.addResourceHandler(
        // 		"/favicon.ico"
        // 	)
        // 	.addResourceLocations(
        // 		"/resources/favicon.ico"
        // 	);
    }
}
