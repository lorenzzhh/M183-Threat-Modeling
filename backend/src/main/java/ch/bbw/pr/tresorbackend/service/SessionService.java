package ch.bbw.pr.tresorbackend.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SessionService
 * Minimal in-memory login session store: on login we hand out a random
 * token and remember which user it belongs to. Every other request must
 * send that token, so the server - not the client - decides which
 * userId a request belongs to. That's the piece that was missing
 * before and caused the access-control problem.
 *
 * Note: in-memory means sessions are lost on restart and this won't
 * work if you ever run more than one backend instance. Good enough to
 * close the access-control gap; swap for a proper store (DB/Redis/JWT)
 * if that becomes a problem.
 *
 * @author Peter Rutschmann
 */
@Service
public class SessionService {

   private final Map<String, Long> tokenToUserId = new ConcurrentHashMap<>();

   public String createSession(Long userId) {
      String token = UUID.randomUUID().toString();
      tokenToUserId.put(token, userId);
      return token;
   }

   /** Returns the userId for a token, or null if the token is unknown/invalid. */
   public Long getUserId(String token) {
      if (token == null) return null;
      return tokenToUserId.get(token);
   }

   public void invalidate(String token) {
      tokenToUserId.remove(token);
   }
}
