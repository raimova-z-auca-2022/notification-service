package kg.notifications.telegram.repository;

import kg.notifications.telegram.model.RegistrationRequest;

import java.util.Optional;

public interface RegistrationRequestRepository {

    void save(RegistrationRequest request);

    Optional<RegistrationRequest> findByToken(String token);

    void deleteByToken(String token);
}
