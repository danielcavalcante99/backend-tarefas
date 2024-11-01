package br.com.tarefa.config.openapi;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.In;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;

@Configuration
public class OpenApiConfig {
	
	@Bean
	OpenAPI OpenAPI() {
		return new OpenAPI()
					.info(this.info())
					.schemaRequirement("bearerAuth", this.securitySchema());
	}
	
	private Info info() {
		return new Info().version("1")
				.title("Documentation API")
				.description("API RESTful que permitirá aos usuários gerenciar uma lista de tarefas")
				.contact(new Contact().name("Developer Daniel Cavalcante").email("daniel16henrrique@gmail.com"));
	}

	private SecurityScheme securitySchema() {
		SecurityScheme securityScheme = new SecurityScheme();
		return securityScheme
				.name("bearerAuth")
				.description("JWT auth description")
				.scheme("bearer")
				.type(Type.HTTP)
				.bearerFormat("JWT")
				.in(In.HEADER);
	}

}