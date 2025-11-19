package com.hdsoft.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  /*  @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests(authorizeRequests ->
                authorizeRequests
                    .anyRequest().authenticated()
            )
            .saml2Login(withDefaults())
            .csrf().disable(); // Configure CSRF according to your needs
    }

    @Bean
    public InMemorySaml2ServiceProviderRegistrationRepository saml2ServiceProviderRegistrationRepository() {
        Saml2ServiceProviderRegistration registration = Saml2ServiceProviderRegistration.withEntityId("your-application-entity-id")
            .singleLogoutServiceLocation("https://your-app.example.com/saml2/sso/logout")
            .assertionConsumerServiceLocation("https://your-app.example.com/saml2/acs")
            .signingX509Credentials(Collections.singletonList(yourSigningCredential()))
            .build();
        
        return new InMemorySaml2ServiceProviderRegistrationRepository(registration);
    }

    @Bean
    public Saml2AuthenticationProvider saml2AuthenticationProvider() {
        return new Saml2AuthenticationProvider();
    } */
}

