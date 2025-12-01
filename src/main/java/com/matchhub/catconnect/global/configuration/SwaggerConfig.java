package com.matchhub.catconnect.global.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// 이 클래스가 스프링 설정 클래스임을 스프링부트에게 알리고 Spring Configuration Bean으로 등록
@Configuration
public class SwaggerConfig {

    // Swagger 문서에 대한 기본 정보(제목, 설명, 연락처 등)를 설정하는 용도의 Bean
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                                .title("CatConnect API")
                                .version("1.0.0")
                                .description("CatConnect Application을 위한 API 명세용 문서 \n\n![logo](https://picsum.photos/300/300)")

                                .contact(new Contact()
                                        .name("CatConnect")
                                        .email("catconnect@matchhub.com")
                                        .url("https://catconnect.matchhub.com"))

                        // 아래는 필요시 사용

                        // 서비스 이용 약관 링크
                        // .termsOfService("https://catconnect.matchhub.com/terms")

                        // 라이선스 정보
                        // .license(new License()
                        //        .name("Apache 2.0")
                        //        .url("http://www.apache.org/licenses/LICENSE-2.0.html"))
                );
    }

    // 게시글 관련 API만 별도의 Swagger 문서 그룹으로 구분
    @Bean
    public GroupedOpenApi boardApi() {
        return GroupedOpenApi.builder()
                .group("게시글 관련 API") // Swagger UI에 표시될 탭 이름
                .pathsToMatch("/api/boards/**") // 포함시킬 URL 경로 패턴
                .build();
    }

    // 사용자 관련 API 그룹
    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("사용자 관련 API")
                .pathsToMatch("/api/users/**")
                .build();
    }

    // 댓글 관련 API 그룹
    @Bean
    public GroupedOpenApi commentApi() {
        return GroupedOpenApi.builder()
                .group("댓글 관련 API")
                .pathsToMatch("/api/comments/**")
                .build();
    }

    // 좋아요 관련 API 그룹
    @Bean
    public GroupedOpenApi likeApi() {
        return GroupedOpenApi.builder()
                .group("좋아요 관련 API")
                .pathsToMatch("/api/likes/**")
                .build();
    }
}
