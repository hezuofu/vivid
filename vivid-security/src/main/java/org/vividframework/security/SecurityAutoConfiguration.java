package org.vividframework.security;

import org.vividframework.beans.BeanConfigurer;
import org.vividframework.beans.BeanDefinitionRegistry;
import org.vividframework.beans.BeanRegistration;
import org.vividframework.web.filter.Filter;

/**
 * Auto-configuration that registers security components into the web pipeline.
 * Implements BeanConfigurer for programmatic registration without annotation scanning.
 *
 * @author sketch
 */
public class SecurityAutoConfiguration implements BeanConfigurer {

    @Override
    public void configure(BeanDefinitionRegistry registry) {
        // Create and register components
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        InMemoryUserDetailsManager userDetailsService = new InMemoryUserDetailsManager();
        userDetailsService.createUser(UserDetails.create("admin",
                passwordEncoder.encode("admin"), "USER", "ADMIN"));

        ProviderManager authManager = new ProviderManager();
        authManager.addProvider(new UsernamePasswordAuthenticationProvider(
                userDetailsService, passwordEncoder));

        BeanRegistration.forType(registry, PasswordEncoder.class)
                .qualifier("passwordEncoder").instance(passwordEncoder).register();
        BeanRegistration.forType(registry, UserDetailsService.class)
                .qualifier("userDetailsService").instance(userDetailsService).register();
        BeanRegistration.forType(registry, AuthenticationManager.class)
                .qualifier("authenticationManager").instance(authManager).register();
        BeanRegistration.forType(registry, Filter.class)
                .qualifier("authenticationFilter").instance(new AuthenticationFilter(authManager)).register();
    }

    /**
     * Simple authentication provider that validates username/password against UserDetailsService.
     */
    public static class UsernamePasswordAuthenticationProvider implements AuthenticationProvider {

        private final UserDetailsService userDetailsService;
        private final PasswordEncoder passwordEncoder;

        public UsernamePasswordAuthenticationProvider(UserDetailsService userDetailsService,
                                                       PasswordEncoder passwordEncoder) {
            this.userDetailsService = userDetailsService;
            this.passwordEncoder = passwordEncoder;
        }

        @Override
        public boolean supports(Class<?> authentication) {
            return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
        }

        @Override
        public Authentication authenticate(Authentication authentication) {
            UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) authentication;
            String username = token.getPrincipal().toString();
            String password = token.getCredentials().toString();

            UserDetails user = userDetailsService.loadUserByUsername(username);
            if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
                throw new BadCredentialsException("Bad credentials for " + username);
            }

            return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        }
    }
}
