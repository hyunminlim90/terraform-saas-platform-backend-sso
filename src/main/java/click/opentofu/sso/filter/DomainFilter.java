package click.opentofu.sso.filter;

import java.io.IOException;

import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class DomainFilter implements Filter {

    private final String ALLOWED_HOST = "java-sso.opentofu.click";
    
    @Override
    public void doFilter(
        ServletRequest request,
        ServletResponse response,
        FilterChain chain
    ) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String host = httpRequest.getHeader("Host");
        if (ALLOWED_HOST.equals(host)) {
            chain.doFilter(request, response);
        } else {
            throw new RuntimeException("Invalid host");
        }
    }
}
