package com.dongkuk.weighing.auth.security;

import com.dongkuk.weighing.auth.service.JwtTokenProvider;
import com.dongkuk.weighing.user.domain.UserRole;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 인증 필터
 *
 * 모든 HTTP 요청에 대해 한 번씩 실행되는 인증 필터이다.
 * Authorization 헤더에서 Bearer 토큰을 추출하고, 유효성 검증 후
 * SecurityContext에 인증 정보를 설정한다.
 * 또한 Redis 블랙리스트를 확인하여 로그아웃된 토큰의 사용을 방지한다.
 *
 * @author 시스템
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /** Authorization 헤더명 */
    private static final String AUTHORIZATION_HEADER = "Authorization";

    /** Bearer 토큰 접두사 */
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate;

    /**
     * 요청마다 JWT 토큰을 검증하고 인증 정보를 설정한다.
     * 1. Authorization 헤더에서 Bearer 토큰 추출
     * 2. 토큰 유효성 검증
     * 3. Redis 블랙리스트 확인 (로그아웃 여부)
     * 4. Claims에서 사용자 정보 추출하여 SecurityContext에 설정
     *
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @param filterChain 필터 체인
     * @throws ServletException 서블릿 예외
     * @throws IOException IO 예외
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);

        if (token != null && jwtTokenProvider.validateToken(token)) {
            String jti = jwtTokenProvider.extractJti(token);

            // 블랙리스트에 등록되지 않은 유효한 토큰만 인증 처리
            if (!isBlacklisted(jti)) {
                // 토큰 Claims에서 사용자 정보 추출
                Claims claims = jwtTokenProvider.extractClaims(token);

                Long userId = Long.parseLong(claims.getSubject());
                String loginId = claims.get("login_id", String.class);
                UserRole role = UserRole.valueOf(claims.get("role", String.class));
                Long companyId = claims.get("company_id", Long.class);

                // UserPrincipal 생성 및 SecurityContext에 인증 정보 설정
                UserPrincipal principal = new UserPrincipal(userId, loginId, role, companyId);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * HTTP 요청 헤더에서 Bearer 토큰을 추출한다.
     *
     * @param request HTTP 요청
     * @return JWT 토큰 문자열 (없으면 null)
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    /**
     * Redis에서 토큰의 JTI가 블랙리스트에 등록되어 있는지 확인한다.
     * 로그아웃된 토큰은 블랙리스트에 등록되어 있다.
     *
     * @param jti JWT 토큰 고유 식별자
     * @return 블랙리스트에 있으면 true
     */
    private boolean isBlacklisted(String jti) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:" + jti));
    }
}
