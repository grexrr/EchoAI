package grexrr.echoai.config;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())        // Disable CSRF (FOR NOW !!!!!!)
                .authorizeHttpRequests(auth -> auth  // Configuring Authentication Rules
                                .requestMatchers("/public/**").permitAll()  // Public Paths
                                .anyRequest().authenticated()  // Others require authentication
                )
                .httpBasic(withDefaults());  // Default launching HTTP Basic authentication
    return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
        public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
            UserDetails user = User.builder()
                    .username("user")
                    .password(passwordEncoder.encode("password"))  // 加密密码
                    .roles("USER")
                    .build();
            return new InMemoryUserDetailsManager(user);
        }
}
