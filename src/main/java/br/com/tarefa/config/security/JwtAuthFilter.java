package br.com.tarefa.config.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import br.com.tarefa.exceptions.RevokeTokenException;
import br.com.tarefa.services.security.CustomUserDetailsService;
import br.com.tarefa.services.security.JwtService;

@Configuration
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthFilter(JwtService jwtService, CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if ( this.isLoginRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token =  this.extractToken(request.getHeader("Authorization"));
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            this.validateTokenRevoke(token);
            String username = this.jwtService.extractUsername(token);
            this.authenticateUserIfNecessary(request, username, token);

        } catch (RevokeTokenException e) {
            this.handleErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, e.getLocalizedMessage());
            return;
        } catch (UsernameNotFoundException e) {
            this.handleErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, e.getLocalizedMessage());
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isLoginRequest(HttpServletRequest request) {
        return request.getServletPath().contains("/login");
    }

    private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7);
    }

    private void validateTokenRevoke(String token) throws RevokeTokenException {
    	 this.jwtService.validTokenRevoke(token);
    }

    private void authenticateUserIfNecessary(HttpServletRequest request, String username, String token) {
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails =  this.userDetailsService.loadUserByUsername(username);
            if (this.jwtService.isTokenValid(token, userDetails)) {
            	this.setAuthentication(request, userDetails);
            }
        }
    }

    private void setAuthentication(HttpServletRequest request, UserDetails userDetails) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    private void handleErrorResponse(HttpServletResponse response, int statusCode, String message) throws IOException {
        response.sendError(statusCode, message);
        logger.error(message);
    }

}
