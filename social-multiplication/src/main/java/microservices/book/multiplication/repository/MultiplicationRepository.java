package microservices.book.multiplication.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import microservices.book.multiplication.domain.Multiplication;

public interface MultiplicationRepository extends JpaRepository<Multiplication, Long> {

}
