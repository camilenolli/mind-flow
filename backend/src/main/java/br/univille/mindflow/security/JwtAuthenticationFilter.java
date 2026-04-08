package br.univille.mindflow.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro que extrai o JWT do header "Authorization: Bearer ..." em cada
 * requisição, valida e popula o SecurityContext com o UserPrincipal.
 *
 * Roda uma única vez por request (OncePerRequestFilter).
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    private final JwtService jwt;
    private final UserDetailsServiceImpl users;

    public JwtAuthenticationFilter(JwtService jwt, UserDetailsServiceImpl users) {
        this.jwt = jwt;
        this.users = users;
    }

    /**
     * Endpoints públicos onde o filtro JWT não deve nem tentar processar
     * o header Authorization. Garante que mesmo se o cliente mandar um token
     * stale/inválido, esses endpoints respondem normalmente.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/api/auth/login")
            || path.equals("/api/auth/register")
            || path.equals("/api/health");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader(HEADER);
        if (header == null || !header.startsWith(PREFIX)) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(PREFIX.length()).trim();
        try {
            String email = jwt.extractEmail(token);
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails ud = users.loadUserByUsername(email);
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(ud, null, ud.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (JwtException | IllegalArgumentException e) {
            // Token inválido/malformado/expirado — limpa contexto e prossegue.
            // Endpoints públicos (login/register/health) ainda respondem normalmente.
            log.debug("[JwtFilter] Token inválido: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        } catch (UsernameNotFoundException e) {
            // Token tem assinatura válida MAS o usuário do subject não existe mais
            // (ex: cold start do Render zerou o H2 in-memory). Esse caso é comum
            // no free tier — não pode escalar para 401 em endpoints públicos.
            log.debug("[JwtFilter] Usuário do token não existe mais: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        } catch (Exception e) {
            // Defensivo: qualquer outra exception durante autenticação não pode
            // quebrar o pipeline; loga e prossegue sem auth.
            log.warn("[JwtFilter] Erro inesperado processando token: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        chain.doFilter(request, response);
    }
}
