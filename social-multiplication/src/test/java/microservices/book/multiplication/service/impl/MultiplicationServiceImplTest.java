package microservices.book.multiplication.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import microservices.book.multiplication.domain.Multiplication;
import microservices.book.multiplication.domain.MultiplicationResultAttempt;
import microservices.book.multiplication.domain.User;
import microservices.book.multiplication.repository.MultiplicationResultAttemptRepository;
import microservices.book.multiplication.repository.UserRepository;
import microservices.book.multiplication.service.RandomGeneratorService;

class MultiplicationServiceImplTest {

	private MultiplicationServiceImpl multiplicationServiceImpl;

	@Mock
	private RandomGeneratorService randomGeneratorService;
	
	@Mock
	private MultiplicationResultAttemptRepository attemptRepository;
	
	@Mock
	private UserRepository userRepository;

	@BeforeEach
	public void setUp() {
		// With this call to initMocks we tell Mockito to process the annotations
		MockitoAnnotations.initMocks(this);
		multiplicationServiceImpl = new MultiplicationServiceImpl(
				randomGeneratorService,
				attemptRepository,
				userRepository);
	}

	@Test
	void createRandomMultiplicationTest() {
		// given (our mocked Random Generator service will return first 50, then 30
		given(randomGeneratorService.generateRandomFactor()).willReturn(50, 30);

		// when
		Multiplication multiplication = multiplicationServiceImpl.createRandomMultiplication();

		// assert
		assertThat(multiplication.getFactorA()).isEqualTo(50);
		assertThat(multiplication.getFactorB()).isEqualTo(30);
	}

	@Test
	void checkCorrectAttemptTest() {
		Multiplication multiplication = new Multiplication(50, 60);
		User user = new User("rafael");
		
		MultiplicationResultAttempt attempt =
				new MultiplicationResultAttempt(user, multiplication, 3000, false);
		MultiplicationResultAttempt verifiedAttempt =
				new MultiplicationResultAttempt(user, multiplication, 3000, true);
		
		given(userRepository.findByAlias("rafael")).willReturn(Optional.empty());
		
		// when
		boolean attemptResult = multiplicationServiceImpl.checkAttempt(attempt);

		// assert
		assertThat(attemptResult).isTrue();
		verify(attemptRepository).save(verifiedAttempt);
	}

	@Test
	void checkWrongAttemptTest() {
		Multiplication multiplication = new Multiplication(50, 60);
		User user = new User("rafael");
		
		MultiplicationResultAttempt attempt =
				new MultiplicationResultAttempt(user, multiplication, 7000, false);
		
		given(userRepository.findByAlias("rafael")).willReturn(Optional.empty());

		// when
		boolean attemptResult = multiplicationServiceImpl.checkAttempt(attempt);

		// assert
		assertThat(attemptResult).isFalse();
		verify(attemptRepository).save(attempt);
	}

}
