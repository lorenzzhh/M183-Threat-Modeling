package ch.bbw.pr.tresorbackend.controller;

import ch.bbw.pr.tresorbackend.filter.JwtAuthFilter;
import ch.bbw.pr.tresorbackend.model.Secret;
import ch.bbw.pr.tresorbackend.model.NewSecret;
import ch.bbw.pr.tresorbackend.model.EncryptCredentials;
import ch.bbw.pr.tresorbackend.model.User;
import ch.bbw.pr.tresorbackend.service.SecretService;
import ch.bbw.pr.tresorbackend.service.UserService;
import ch.bbw.pr.tresorbackend.util.EncryptUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * SecretController
 *
 * IMPORTANT: every endpoint here now derives "who is asking" from the
 * verified session token (see JwtAuthFilter / SessionService), never
 * from a userId/email that the client puts in the request body. That's
 * what actually fixes the missing access control on other users'
 * secrets - a client can still claim to be anyone in a request body,
 * but it can no longer prove it without a valid token for that user.
 *
 * @author Peter Rutschmann
 */
@RestController
@AllArgsConstructor
@RequestMapping("api/secrets")
public class SecretController {

   private SecretService secretService;
   private UserService userService;

   // create secret REST API
   @CrossOrigin(origins = "${CROSS_ORIGIN}")
   @PostMapping
   public ResponseEntity<String> createSecret2(@Valid @RequestBody NewSecret newSecret, BindingResult bindingResult,
                                                HttpServletRequest request) {
      //input validation
      if (bindingResult.hasErrors()) {
         List<String> errors = bindingResult.getFieldErrors().stream()
               .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
               .collect(Collectors.toList());
         System.out.println("SecretController.createSecret " + errors);

         JsonArray arr = new JsonArray();
         errors.forEach(arr::add);
         JsonObject obj = new JsonObject();
         obj.add("message", arr);
         String json = new Gson().toJson(obj);

         System.out.println("SecretController.createSecret, validation fails: " + json);
         return ResponseEntity.badRequest().body(json);
      }
      System.out.println("SecretController.createSecret, input validation passed");

      User user = userService.findByEmail(newSecret.getEmail());
      if (user == null) return ResponseEntity.notFound().build();

      // A user may only create secrets for themselves - the email in the
      // body must belong to the very account the JWT authenticates.
      if (!currentUserId(request).equals(user.getId())) {
         return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Cannot create a secret for another user");
      }

      //transfer secret and encrypt content
      Secret secret = new Secret(
            null,
            user.getId(),
            new EncryptUtil(newSecret.getEncryptPassword()).encrypt(newSecret.getContent().toString())
      );
      //save secret in db
      secretService.createSecret(secret);
      System.out.println("SecretController.createSecret, secret saved in db");
      JsonObject obj = new JsonObject();
      obj.addProperty("answer", "Secret saved");
      String json = new Gson().toJson(obj);
      System.out.println("SecretController.createSecret " + json);
      return ResponseEntity.accepted().body(json);
   }

   // Build Get Secrets by userId REST API
   // The userId in the request body is ignored for access-control purposes;
   // only the authenticated user's own secrets are ever returned.
   @CrossOrigin(origins = "${CROSS_ORIGIN}")
   @PostMapping("/byuserid")
   public ResponseEntity<List<Secret>> getSecretsByUserId(@RequestBody EncryptCredentials credentials,
                                                           HttpServletRequest request) {
      System.out.println("SecretController.getSecretsByUserId " + credentials);

      Long authUserId = currentUserId(request);
      List<Secret> secrets = secretService.getSecretsByUserId(authUserId);
      if (secrets.isEmpty()) {
         System.out.println("SecretController.getSecretsByUserId secret isEmpty");
         return ResponseEntity.notFound().build();
      }
      //Decrypt content
      for(Secret secret: secrets) {
         try {
            secret.setContent(new EncryptUtil(credentials.getEncryptPassword()).decrypt(secret.getContent()));
         } catch (EncryptionOperationNotPossibleException e) {
            System.out.println("SecretController.getSecretsByUserId " + e + " " + secret);
            secret.setContent("not encryptable. Wrong password?");
         }
      }

      System.out.println("SecretController.getSecretsByUserId " + secrets);
      return ResponseEntity.ok(secrets);
   }

   // Build Get Secrets by email REST API
   // The email in the request body is ignored for access-control purposes;
   // only the authenticated user's own secrets are ever returned.
   @CrossOrigin(origins = "${CROSS_ORIGIN}")
   @PostMapping("/byemail")
   public ResponseEntity<List<Secret>> getSecretsByEmail(@RequestBody EncryptCredentials credentials,
                                                          HttpServletRequest request) {
      System.out.println("SecretController.getSecretsByEmail " + credentials);

      Long authUserId = currentUserId(request);
      List<Secret> secrets = secretService.getSecretsByUserId(authUserId);
      if (secrets.isEmpty()) {
         System.out.println("SecretController.getSecretsByEmail secret isEmpty");
         return ResponseEntity.notFound().build();
      }
      //Decrypt content
      for(Secret secret: secrets) {
         try {
            secret.setContent(new EncryptUtil(credentials.getEncryptPassword()).decrypt(secret.getContent()));
         } catch (EncryptionOperationNotPossibleException e) {
            System.out.println("SecretController.getSecretsByEmail " + e + " " + secret);
            secret.setContent("not encryptable. Wrong password?");
         }
      }

      System.out.println("SecretController.getSecretsByEmail " + secrets);
      return ResponseEntity.ok(secrets);
   }

   // Removed: "get all secrets" returned every user's secrets to any
   // caller with no ownership check whatsoever - the most severe of the
   // access-control gaps. There's no legitimate use case for a regular
   // user to fetch everyone's secrets, so the endpoint was deleted
   // rather than patched. Reintroduce only behind a real admin role
   // check if it's genuinely needed.

   // Build Update Secrete REST API
   // http://localhost:8080/api/secrets/1
   @CrossOrigin(origins = "${CROSS_ORIGIN}")
   @PutMapping("{id}")
   public ResponseEntity<String> updateSecret(
         @PathVariable("id") Long secretId,
         @Valid @RequestBody NewSecret newSecret,
         BindingResult bindingResult,
         HttpServletRequest request) {
      //input validation
      if (bindingResult.hasErrors()) {
         List<String> errors = bindingResult.getFieldErrors().stream()
               .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
               .collect(Collectors.toList());
         System.out.println("SecretController.createSecret " + errors);

         JsonArray arr = new JsonArray();
         errors.forEach(arr::add);
         JsonObject obj = new JsonObject();
         obj.add("message", arr);
         String json = new Gson().toJson(obj);

         System.out.println("SecretController.updateSecret, validation fails: " + json);
         return ResponseEntity.badRequest().body(json);
      }

      //get Secret with id
      Secret dbSecrete = secretService.getSecretById(secretId);
      if(dbSecrete == null){
         System.out.println("SecretController.updateSecret, secret not found in db");
         JsonObject obj = new JsonObject();
         obj.addProperty("answer", "Secret not found in db");
         String json = new Gson().toJson(obj);
         System.out.println("SecretController.updateSecret failed:" + json);
         return ResponseEntity.badRequest().body(json);
      }

      // Ownership check against the AUTHENTICATED user, not just against
      // whatever email happens to be in the request body.
      Long authUserId = currentUserId(request);
      if (!dbSecrete.getUserId().equals(authUserId)) {
         System.out.println("SecretController.updateSecret, not owner of secret");
         JsonObject obj = new JsonObject();
         obj.addProperty("answer", "Secret does not belong to you");
         String json = new Gson().toJson(obj);
         return ResponseEntity.status(HttpStatus.FORBIDDEN).body(json);
      }

      User user = userService.findByEmail(newSecret.getEmail());
      if (user == null || !user.getId().equals(authUserId)) return ResponseEntity.notFound().build();

      //check if Secret can be decrypted with password
      try {
         new EncryptUtil(newSecret.getEncryptPassword()).decrypt(dbSecrete.getContent());
      } catch (EncryptionOperationNotPossibleException e) {
         System.out.println("SecretController.updateSecret, invalid password");
         JsonObject obj = new JsonObject();
         obj.addProperty("answer", "Password not correct.");
         String json = new Gson().toJson(obj);
         System.out.println("SecretController.updateSecret failed:" + json);
         return ResponseEntity.badRequest().body(json);
      }
      //modify Secret in db.
      Secret secret = new Secret(
            secretId,
            user.getId(),
            new EncryptUtil(newSecret.getEncryptPassword()).encrypt(newSecret.getContent().toString())
      );
      secretService.updateSecret(secret);
      System.out.println("SecretController.updateSecret, secret updated in db");
      JsonObject obj = new JsonObject();
      obj.addProperty("answer", "Secret updated");
      String json = new Gson().toJson(obj);
      System.out.println("SecretController.updateSecret " + json);
      return ResponseEntity.accepted().body(json);
   }

   // Build Delete Secret REST API
   // Only the owner of a secret may delete it.
   @CrossOrigin(origins = "${CROSS_ORIGIN}")
   @DeleteMapping("{id}")
   public ResponseEntity<String> deleteSecret(@PathVariable("id") Long secretId, HttpServletRequest request) {
      Secret secret = secretService.getSecretById(secretId);
      if (secret == null) {
         return ResponseEntity.notFound().build();
      }
      if (!secret.getUserId().equals(currentUserId(request))) {
         System.out.println("SecretController.deleteSecret, not owner of secret " + secretId);
         return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Secret does not belong to you");
      }
      secretService.deleteSecret(secretId);
      System.out.println("SecretController.deleteSecret succesfully: " + secretId);
      return new ResponseEntity<>("Secret successfully deleted!", HttpStatus.OK);
   }

   /**
    * Reads the userId that the auth filter attached to the request after
    * validating the session token.
    */
   private Long currentUserId(HttpServletRequest request) {
      return (Long) request.getAttribute(JwtAuthFilter.AUTH_USER_ID_ATTRIBUTE);
   }
}
