package microservices.book.multiplication.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.eq;
import static org.mockito.ArgumentMatchers.any;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import microservices.book.multiplication.domain.Multiplication;
import microservices.book.multiplication.domain.MultiplicationResultAttempt;
import microservices.book.multiplication.domain.User;
import microservices.book.multiplication.event.EventDispatcher;
import microservices.book.multiplication.event.MultiplicationSolvedEvent;
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
	
	@Mock
	private EventDispatcher eventDispatcher;

	@BeforeEach
	public void setUp() {
		// With this call to initMocks we tell Mockito to process the annotations
		MockitoAnnotations.initMocks(this);
		multiplicationServiceImpl = new MultiplicationServiceImpl(
				randomGeneratorService,
				attemptRepository,
				userRepository,
				eventDispatcher);
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
		
		MultiplicationSolvedEvent event =
				new MultiplicationSolvedEvent(attempt.getId(), attempt.getUser().getId(), true);
		
		given(userRepository.findByAlias("rafael")).willReturn(Optional.empty());
		
		// when
		boolean attemptResult = multiplicationServiceImpl.checkAttempt(attempt);

		// assert
		assertThat(attemptResult).isTrue();
		verify(attemptRepository).save(verifiedAttempt);
		verify(eventDispatcher).send(eq(event));
	}

	@Test
	void checkWrongAttemptTest() {
		Multiplication multiplication = new Multiplication(50, 60);
		User user = new User("rafael");
		
		MultiplicationResultAttempt attempt =
				new MultiplicationResultAttempt(user, multiplication, 7000, false);
		
		MultiplicationSolvedEvent event =
				new MultiplicationSolvedEvent(attempt.getId(), attempt.getUser().getId(), false);
		
		given(userRepository.findByAlias("rafael")).willReturn(Optional.empty());

		// when
		boolean attemptResult = multiplicationServiceImpl.checkAttempt(attempt);

		// assert
		assertThat(attemptResult).isFalse();
		verify(attemptRepository).save(attempt);
		verify(eventDispatcher).send(eq(event));
	}
	
	@Test
	void retrieveStatsTest() {
		Multiplication multiplication = new Multiplication(50, 60);
		User user = new User("rafael");
			
		MultiplicationResultAttempt attempt1 =
				new MultiplicationResultAttempt(user, multiplication, 3010, false);
		MultiplicationResultAttempt attempt2 =
				new MultiplicationResultAttempt(user, multiplication, 3051, false);		
		
		List<MultiplicationResultAttempt> latestAttempts = Arrays.asList(attempt1, attempt2);
			
		given(userRepository.findByAlias("rafael")).willReturn(Optional.empty());
		given(attemptRepository.findTop5ByUserAliasOrderByIdDesc("rafael")).willReturn(latestAttempts);
			
		List<MultiplicationResultAttempt> latestAttemptsResult = multiplicationServiceImpl.getStatsForUser("rafael");
			
		assertThat(latestAttemptsResult).isEqualTo(latestAttempts);
	}
	
	@Test
	void retrieveAttemptById() {
		Multiplication multiplication = new Multiplication(50, 60);
		User user = new User("rafael");
			
		MultiplicationResultAttempt attempt =
				new MultiplicationResultAttempt(user, multiplication, 3010, false);
		
		given(attemptRepository.findById(any(Long.class))).willReturn(Optional.of(attempt));
		
		MultiplicationResultAttempt foundAttempt = multiplicationServiceImpl.getResultAttemptById(1L);
		
		assertThat(attempt).isEqualTo(foundAttempt);
		verify(attemptRepository).findById(any(Long.class));
	}

}
