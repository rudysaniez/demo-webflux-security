package webflux.security.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.security.reactive.EndpointRequest;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@EnableScheduling
@EnableWebFlux
@EnableWebFluxSecurity
@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

    @Bean
    public SecurityWebFilterChain securityWebFilterChainFree(ServerHttpSecurity http) {
    	
        http.
        	oauth2Client().
        	and().
        	authorizeExchange().
        	matchers(EndpointRequest.to(Management.INFO, Management.HEALTH)).permitAll().
        	pathMatchers(Api.SECURITY_SUPPLIER_PATH).authenticated().
        	pathMatchers(Api.SECURITY_SUPPLIER_INCLUDED_PATH).authenticated().
        	anyExchange().authenticated().
        	and().
        	oauth2ResourceServer().opaqueToken();
        
        return http.build();
    }
    
    @Bean
	public ReactiveOAuth2AuthorizedClientManager authorizedClientManager(ReactiveClientRegistrationRepository clientRegistrationRepository,
			ReactiveOAuth2AuthorizedClientService authorizedClientService) {
	
		ReactiveOAuth2AuthorizedClientProvider authorizedClientProvider = ReactiveOAuth2AuthorizedClientProviderBuilder.
				builder().
				clientCredentials().
				build();
		
		AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager authorizedClientManager = new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(
		            clientRegistrationRepository, authorizedClientService);
		
	    authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);
		    
		return authorizedClientManager;
	}
	
	@Bean
	public WebClient webClient(ReactiveOAuth2AuthorizedClientManager authorizedClientManager) {
		
		ServerOAuth2AuthorizedClientExchangeFilterFunction oauth = new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
		oauth.setDefaultClientRegistrationId("oidcServices");
		
		return WebClient.
				builder().
				filter(oauth).
				build();
	}
	
	@Autowired WebClient webClient;
	
	@Profile("scheduleEnable")
	@Scheduled(fixedRate = 15000)
	public void logResourceServiceResponse() {

	    webClient.get()
	      .uri("http://localhost:8080/suppliers")
	      .retrieve()
	      .bodyToMono(String.class)
	      .map(string 
	        -> "Retrieved using Client Credentials Grant Type (webflux-security) : " + string)
	      .subscribe(log::info);
	}
}
