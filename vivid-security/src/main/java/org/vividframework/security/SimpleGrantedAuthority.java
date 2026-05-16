package org.vividframework.security;

/**
 * Simple granted authority implementation
 * @author sketch
 */
public class SimpleGrantedAuthority implements GrantedAuthority {

    private final String authority;

    public SimpleGrantedAuthority(String authority) {
        this.authority = authority;
    }

    @Override
    public String getAuthority() {
        return authority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleGrantedAuthority that = (SimpleGrantedAuthority) o;
        return authority.equals(that.authority);
    }

    @Override
    public int hashCode() {
        return authority.hashCode();
    }

    @Override
    public String toString() {
        return authority;
    }
}
