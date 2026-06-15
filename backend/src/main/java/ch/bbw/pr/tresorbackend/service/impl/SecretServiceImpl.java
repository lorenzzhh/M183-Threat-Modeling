package ch.bbw.pr.tresorbackend.service.impl;

import ch.bbw.pr.tresorbackend.model.Secret;
import ch.bbw.pr.tresorbackend.repository.SecretRepository;
import ch.bbw.pr.tresorbackend.service.SafeDbCall;
import ch.bbw.pr.tresorbackend.service.SecretService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * SecretServiceImpl
 * @author Peter Rutschmann
 */
@Service
@AllArgsConstructor
public class SecretServiceImpl implements SecretService {

   private SecretRepository secretRepository;

   @Override
   public Secret createSecret(Secret secret) {
      return SafeDbCall.safeDbCall(()-> secretRepository.save(secret), null);
   }

   @Override
   public Secret getSecretById(Long secretId) {
      Optional<Secret> optionalSecret = SafeDbCall.safeDbCall(() -> secretRepository.findById(secretId)
              , Optional.empty());
      return optionalSecret.get();
   }

   @Override
   public List<Secret> getAllSecrets() {
      return (List<Secret>) SafeDbCall.safeDbCall(() -> secretRepository.findAll(), List.of());
   }

   @Override
   public Secret updateSecret(Secret secret) {
      Optional<Secret> optionalSecret = SafeDbCall.safeDbCall(() -> secretRepository.findById(secret.getId())
              , Optional.empty());
      Secret existingSecret = optionalSecret.get();
      existingSecret.setUserId(secret.getUserId());
      existingSecret.setContent(secret.getContent());
      Secret updatedSecret = SafeDbCall.safeDbCall(() -> secretRepository.save(existingSecret), null);
      return updatedSecret;
   }

   @Override
   public void deleteSecret(Long secretId) {
      SafeDbCall.safeDbCall(() -> secretRepository.deleteById(secretId));
   }

   @Override
   public List<Secret> getSecretsByUserId(Long userId) {
      return SafeDbCall.safeDbCall(() -> secretRepository.findByUserId(userId), null);
   }

}
