package microservices.book.multiplication.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import microservices.book.multiplication.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByAlias(final String alias);
}
