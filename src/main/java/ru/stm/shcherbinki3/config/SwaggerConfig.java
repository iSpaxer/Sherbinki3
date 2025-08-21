package ru.stm.shcherbinki3.config;

import ru.stm.shcherbinki3.dto.LoginRequest;
import ru.stm.shcherbinki3.util.ApplicationDataComponent;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.*;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;

@Configuration
public class SwaggerConfig {


    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Your API").version("1.0.0"))
                .components(new Components()
                                    // JWT
                                    .addSecuritySchemes("JWT", new SecurityScheme()
                                            .type(SecurityScheme.Type.HTTP)
                                            .scheme("bearer")
                                            .bearerFormat("JWT")));
    }

    @Bean
    public GroupedOpenApi adminGroup(ApplicationDataComponent dataComponent) {
        GroupedOpenApi.Builder builder = GroupedOpenApi.builder();
        builder.group("Common panel");
        builder.addOpenApiCustomizer(openApi -> {
            ModelConverters.getInstance().read(LoginRequest.class)
                    .forEach(openApi.getComponents()::addSchemas);

            openApi
                    .path(dataComponent.glueEndpoint("/jwt/login"), new PathItem()
                            .post(new Operation()
                                          .summary("Log in to your account with your username (email) and password.")
                                          .security(Collections.emptyList())
                                          .addTagsItem("Account API")
                                          .requestBody(new RequestBody()
                                                               .content(new Content().addMediaType(
                                                                                org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                                                                                new MediaType().schema(new Schema<LoginRequest>()
                                                                                                               .$ref("#/components/schemas/LoginRequest"))
                                                                        )
                                                               )
                                          )
                                          .responses(new ApiResponses()
                                                             .addApiResponse("201", new ApiResponse()
                                                                     .description("Successful login!")
                                                                     .content(new Content().addMediaType(
                                                                             org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                                                                             new MediaType().schema(
                                                                                     new Schema<String>().example(
                                                                                             "Login successful")))))
                                                             .addApiResponse("400", new ApiResponse()
                                                                     .description("Bad request"))
                                                             .addApiResponse("500", new ApiResponse()
                                                                     .description("INNER SERVER ERROR"))
                                          )
                            )
                    )
                    .path(dataComponent.glueEndpoint("/jwt/logout"), new PathItem()
                            .post(new Operation()
                                          .summary("Logout from account")
                                          .addTagsItem("Account API")
                                          .security(List.of(new SecurityRequirement().addList("JWT")))
                                          .responses(new ApiResponses()
                                                             .addApiResponse("200", new ApiResponse()
                                                                     .description("Successful logout!")
                                                                     .content(new Content().addMediaType(
                                                                             org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                                                                             new MediaType().schema(
                                                                                     new Schema<String>().example(
                                                                                             "Logout successful")))))
                                                             .addApiResponse("400", new ApiResponse()
                                                                     .description("Bad request"))
                                                             .addApiResponse("403", new ApiResponse()
                                                                     .description("Forbidden"))
                                                             .addApiResponse("500", new ApiResponse()
                                                                     .description("INNER SERVER ERROR"))
                                          )
                            )
                    )
                    .path(dataComponent.glueEndpoint("/jwt/refresh"), new PathItem()
                            .post(new Operation()
                                          .summary("Get new access and refresh tocken.")
                                          .addTagsItem("Account API")
                                          .requestBody(new RequestBody()
                                                               .content(new Content().addMediaType(
                                                                                org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                                                                                new MediaType().schema(
                                                                                        new Schema().addProperty("refresh",
                                                                                                                 new Schema<String>().description(
                                                                                                                         "Refresh token")))
                                                                        )
                                                               )
                                          )
                                          .responses(new ApiResponses()
                                                             .addApiResponse("200", new ApiResponse()
                                                                     .description("Successful login!")
                                                                     .content(new Content().addMediaType(
                                                                             org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                                                                             new MediaType().schema(
                                                                                     new Schema<String>().example(
                                                                                             "Login successful")))))
                                                             .addApiResponse("400", new ApiResponse()
                                                                     .description("Bad request"))
                                                             .addApiResponse("500", new ApiResponse()
                                                                     .description("INNER SERVER ERROR"))
                                          )
                            )
                    );
        });
        builder.packagesToScan("ru.stm.shcherbinki3.controller");
        builder.addOpenApiCustomizer(openApi -> openApi.info(new Info().title("Admins API").version("1.0.0")));
        return builder
                .build();
    }


}

