package ch.bbw.pr.tresorbackend.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * PasswordEncryptService
 *   used to hash password and verify match
 * @author Peter Rutschmann
 */
@Service
public class PasswordEncryptService {

   private final BCryptPasswordEncoder encoder;

   public PasswordEncryptService() {
      this.encoder = new BCryptPasswordEncoder();
   }

   public String hashPassword(String password) {
      return encoder.encode(password);
   }

   /**
    * Checks a plaintext password against a bcrypt hash from the db.
    */
   public boolean matches(String rawPassword, String hashedPassword) {
      return encoder.matches(rawPassword, hashedPassword);
   }
}
