package config;


import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.security.PrivateKey;

@Configuration
@EnableWebSecurity

public class SecurityConfig  extends WebSecurityConfigurationAdapter {
@Value("${eureka.username}")
    private String username;
    @Value("${eureka.password}")
    private String password;


     @Override
    public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception{
         authenticationManagerBuilder.inMemoryAuthentication()
                 .withUser(username).password(password)
                 .authorities("USER");

    }

//

@Override
public void configure(HttpSecurity httpSecurity) throws Exception{

         httpSecurity.csrf().disable()
                 .authorizeRequests().anyRequest()
                 .authenticated()
                 .and()
                 .httpBasic();



}




}
