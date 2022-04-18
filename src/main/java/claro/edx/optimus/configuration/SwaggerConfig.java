package claro.edx.optimus.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

	@Bean
	public Docket productApi() {
		return new Docket(DocumentationType.SWAGGER_2).select()
				.apis(RequestHandlerSelectors.basePackage("claro.edx.optimus.api"))
				.paths(PathSelectors.regex("/api.*")).build().apiInfo(apiEndPointsInfo());
	}
	
	private ApiInfo apiEndPointsInfo() {
		return new ApiInfoBuilder().title("Optimus - PaymentData File Management - REST API").description(
				"Microservicio B ( Scheduler) de gestion de archivos de pago. Que identifiquen status de directorio y analisis de archivos de pago linea a linea.")
				.contact(new Contact("INFO", "www.gizlocorp.com", "ronald.barrera@gizlocorp.com"))
				.version("1.0.0").build();
	}

}