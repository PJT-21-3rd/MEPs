package org.meps.config;

import lombok.RequiredArgsConstructor;
import org.meps.common.auth.AuthInterceptor;
import org.meps.common.auth.LoginUserArgumentResolver;
import org.meps.user.jwt.JwtProvider;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.*;

import java.util.List;


@Configuration
@EnableWebMvc
@ComponentScan(
        basePackages = "org.meps",
        useDefaultFilters = false,
        includeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ANNOTATION,
                        classes = Controller.class
                ),
                @ComponentScan.Filter(
                        type = FilterType.ANNOTATION,
                        classes = ControllerAdvice.class
                )
        }
)

@RequiredArgsConstructor
public class ServletConfig implements WebMvcConfigurer {

    // 루트 컨텍스트(RootConfig 스캔)의 빈 — 자식 서블릿 컨텍스트에서 부모 빈 주입
    private final JwtProvider jwtProvider;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new LoginUserArgumentResolver(jwtProvider));
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor(jwtProvider))
                .addPathPatterns(
                        "/api/member/**",
                        "/api/buildings/*/safety-report/detailed",
                        "/api/loans",
                        "/api/insurances"
                );
    }

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

    }

    @Override
    public Validator getValidator() {
        return new LocalValidatorFactoryBean();
    }
}
