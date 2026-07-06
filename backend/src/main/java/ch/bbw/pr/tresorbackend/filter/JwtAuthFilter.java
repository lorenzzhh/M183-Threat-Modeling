package ch.bbw.pr.tresorbackend.filter;

import ch.bbw.pr.tresorbackend.service.SessionService;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * AuthFilter
 * Runs in front of every request. If a valid session token is present,
 * the authenticated userId is attached to the request as an attribute
 * ("authUserId"), which controllers use to check ownership - instead of
 * trusting a userId/email the client sends in the request body.
 *
 * @author Peter Rutschmann
 */
@Component
@Order(1)
@AllArgsConstructor
public class JwtAuthFilter implements Filter {

   public static final String AUTH_USER_ID_ATTRIBUTE = "authUserId";
   public static final String AUTH_USER_ROLE_ATTRIBUTE = "authUserRole";

   // Endpoints reachable without being logged in yet.
   private static final List<String> PUBLIC_PATHS = List.of(
         "/api/users/login",
         "/api/users/byemail"
   );

   private final SessionService sessionService;

   @Override
   public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
         throws IOException, ServletException {
      HttpServletRequest request = (HttpServletRequest) servletRequest;
      HttpServletResponse response = (HttpServletResponse) servletResponse;

      if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
         chain.doFilter(request, response); // let CORS preflight through
         return;
      }

      String path = request.getRequestURI();
      boolean isRegistration = path.equals("/api/users") && "POST".equalsIgnoreCase(request.getMethod());
      if (PUBLIC_PATHS.contains(path) || isRegistration) {
         chain.doFilter(request, response);
         return;
      }

      String authHeader = request.getHeader("Authorization");
      String token = (authHeader != null && authHeader.startsWith("Bearer "))
            ? authHeader.substring("Bearer ".length())
            : null;

      Long userId = sessionService.getUserId(token);
      if (userId == null) {
         response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid session token");
         return;
      }

      request.setAttribute(AUTH_USER_ID_ATTRIBUTE, userId);
      request.setAttribute(AUTH_USER_ROLE_ATTRIBUTE, sessionService.getRole(token));
      chain.doFilter(request, response);
   }
}