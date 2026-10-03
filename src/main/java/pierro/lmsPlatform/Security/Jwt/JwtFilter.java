package pierro.lmsPlatform.Security.Jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import pierro.lmsPlatform.Repository.Auth.UserRepository;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            jwtService.parse(header.substring(7).trim(), JwtService.TokenType.ACCESS).ifPresent(info ->
                    // Role + phiên bản lấy từ DB: user bị xoá => hết quyền; đổi role có hiệu lực ngay;
                    // đăng xuất / đổi mật khẩu (tăng tokenVersion) => token cũ bị từ chối ngay.
                    userRepository.findAuthInfoById(info.userId())
                            .filter(auth -> auth.getTokenVersion() == info.version())
                            .ifPresent(auth -> SecurityContextHolder.getContext().setAuthentication(
                                    new UsernamePasswordAuthenticationToken(info.userId(), null,
                                            List.of(new SimpleGrantedAuthority("ROLE_" + auth.getRoleName()))))));
        }
        filterChain.doFilter(request, response);
    }
}
