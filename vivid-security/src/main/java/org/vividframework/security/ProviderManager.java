package org.vividframework.security;

import java.util.ArrayList;
import java.util.List;

/**
 * Authentication manager that delegates to a list of providers
 * @author sketch
 */
public class ProviderManager implements AuthenticationManager {

    private final List<AuthenticationProvider> providers;
    private AuthenticationManager parent;

    public ProviderManager(List<AuthenticationProvider> providers) {
        this.providers = new ArrayList<>(providers);
    }

    public ProviderManager(AuthenticationProvider... providers) {
        this.providers = new ArrayList<>();
        for (AuthenticationProvider provider : providers) {
            this.providers.add(provider);
        }
    }

    public ProviderManager() {
        this.providers = new ArrayList<>();
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Class<? extends Authentication> type = authentication.getClass();

        for (AuthenticationProvider provider : providers) {
            if (provider.supports(type)) {
                try {
                    Authentication result = provider.authenticate(authentication);
                    if (result != null) {
                        copyDetails(authentication, result);
                        return result;
                    }
                } catch (AuthenticationException e) {
                    throw e;
                } catch (Exception e) {
                    throw new AuthenticationException("Authentication failed", authentication, e);
                }
            }
        }

        if (parent != null) {
            return parent.authenticate(authentication);
        }

        throw new AuthenticationException("No authentication provider found", authentication);
    }

    private void copyDetails(Authentication source, Authentication target) {
        // Copy details if possible
    }

    @Override
    public AuthenticationManager getParent() {
        return parent;
    }

    @Override
    public void setParent(AuthenticationManager parent) {
        this.parent = parent;
    }

    public void addProvider(AuthenticationProvider provider) {
        providers.add(provider);
    }

    public List<AuthenticationProvider> getProviders() {
        return providers;
    }
}
