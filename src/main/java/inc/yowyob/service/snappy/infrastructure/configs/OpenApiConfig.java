package inc.yowyob.service.snappy.infrastructure.configs;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("Snappy Service API")
            .description("API for chat, user management, and chatbot functionality with file upload support")
            .version("1.0"))
        .components(
            new Components()
                .addSecuritySchemes(
                    "bearerAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"))
                // Add schema for file uploads
                .addSchemas("FileUpload", 
                    new Schema<>()
                        .type("string")
                        .format("binary")
                        .description("File upload"))
                .addSchemas("MultipleFileUpload",
                    new Schema<>()
                        .type("array")
                        .items(new Schema<>().type("string").format("binary"))
                        .description("Multiple file uploads")))
        .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
  }
}
