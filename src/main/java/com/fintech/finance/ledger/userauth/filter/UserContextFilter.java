package com.fintech.finance.ledger.userauth.filter;

import com.fintech.finance.ledger.common.tenant.UserContext;
import com.fintech.finance.ledger.common.tenant.UserContextData;
import com.fintech.finance.ledger.entity.User;
import com.fintech.finance.ledger.service.UserProvisioningService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Profile("!test")
public class UserContextFilter extends OncePerRequestFilter {

    private final UserProvisioningService provisioningService;

    public UserContextFilter(UserProvisioningService provisioningService) {
        this.provisioningService = provisioningService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            User user = provisioningService.provisionUser(jwt);
            var userContextData = new UserContextData(user.getId(), user.getTenantId(), user.getAuthProviderId());
            UserContext.setUserContextData(userContextData);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            UserContext.clear();
        }
    }
}

