package com.dating.flairbit.filters;

import com.dating.flairbit.security.JwtUtils;
import com.dating.flairbit.service.auth.UserDetailsServiceImpl;
import com.dating.flairbit.utils.basic.ErrorUtility;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;


@Component
public class AuthTokenFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;

    @Autowired
    public AuthTokenFilter(JwtUtils jwtUtils, UserDetailsServiceImpl userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) {
        try {
            Map<String, String> creds = retrieveTokenAndUsername(request);
            String username = creds.get("username");
            String jwtToken = creds.get("jwtToken");

            if (!Objects.isNull(username) && Objects.isNull(SecurityContextHolder.getContext().getAuthentication())) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                if (this.jwtUtils.validateToken(jwtToken, userDetails)) {
                    if (userDetails.getAuthorities() != null && !userDetails.getAuthorities().isEmpty()) {
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    } else {
                        throw new AccessDeniedException("Access Denied");
                    }
                }
            }

            filterChain.doFilter(request, response);

        } catch (AccessDeniedException ex) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            ErrorUtility.printError("Access Denied: " + ex.getMessage(), response);

        } catch (IllegalArgumentException | ExpiredJwtException | MalformedJwtException ex) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            ErrorUtility.printError("Authentication Failed: " + ex.getMessage(), response);

        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            ErrorUtility.printError("Internal Server Error: " + ex.getMessage(), response);
        }
    }

    private Map<String, String> retrieveTokenAndUsername(HttpServletRequest request) {
        Map<String, String> creds = new HashMap<>();
        String jwtToken = null;
        String username = null;
        String requestTokenHeader = request.getHeader("Authorization");

        if (!StringUtils.isEmpty(requestTokenHeader) && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            username = jwtUtils.getUsernameFromToken(jwtToken);
        }
        creds.put("username", username);
        creds.put("jwtToken", jwtToken);
        return creds;
    }
}
