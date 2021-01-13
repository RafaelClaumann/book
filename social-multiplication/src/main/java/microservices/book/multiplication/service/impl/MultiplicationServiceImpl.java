package microservices.book.multiplication.service.impl;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import microservices.book.multiplication.domain.Multiplication;
import microservices.book.multiplication.domain.MultiplicationResultAttempt;
import microservices.book.multiplication.domain.User;
import microservices.book.multiplication.event.EventDispatcher;
import microservices.book.multiplication.event.MultiplicationSolvedEvent;
import microservices.book.multiplication.repository.MultiplicationResultAttemptRepository;
import microservices.book.multiplication.repository.UserRepository;
import microservices.book.multiplication.service.MultiplicationService;
import microservices.book.multiplication.service.RandomGeneratorService;

@Service
class MultiplicationServiceImpl implements MultiplicationService {

	private RandomGeneratorService randomGeneratorService;
	private MultiplicationResultAttemptRepository attemptRepository;
	private UserRepository userRepository;
	private EventDispatcher eventDispatcher;

	@Autowired
	public MultiplicationServiceImpl(
			final RandomGeneratorService randomGeneratorService,
			final MultiplicationResultAttemptRepository attemptRepository,
			final UserRepository userRepository,
			final EventDispatcher eventDispatcher) {
		this.randomGeneratorService = randomGeneratorService;
		this.attemptRepository = attemptRepository;
		this.userRepository = userRepository;
		this.eventDispatcher = eventDispatcher;
	}

	@Override
	public Multiplication createRandomMultiplication() {
		int factorA = randomGeneratorService.generateRandomFactor();
		int factorB = randomGeneratorService.generateRandomFactor();
		return new Multiplication(factorA, factorB);
	}

	@Transactional
	@Override
	public boolean checkAttempt(final MultiplicationResultAttempt attempt) {
		Optional<User> user = userRepository.findByAlias(attempt.getUser().getAlias());
		
		// Avoids 'hack' attempts
		Assert.isTrue(!attempt.isCorrect(), "You can't send an attempt marked as correct!!");
		
		// Checks if it's correct
		boolean isCorrect = attempt.getResultAttempt() == attempt.getMultiplication().getFactorA()
				* attempt.getMultiplication().getFactorB();
		
		// Creates a copy, now setting the 'correct' field accordingly
		MultiplicationResultAttempt checkedAttempt = new MultiplicationResultAttempt(
				user.orElse(attempt.getUser()),
				attempt.getMultiplication(),
				attempt.getResultAttempt(),
				isCorrect);
		
		attemptRepository.save(checkedAttempt);
		
		// Communicates the result via Event
		eventDispatcher.send(new MultiplicationSolvedEvent(
						checkedAttempt.getId(),
						checkedAttempt.getUser().getId(),
						checkedAttempt.isCorrect()));
		
		// Returns the result
		return isCorrect;
	}
	
	@Override
	public List<MultiplicationResultAttempt> getStatsForUser(String userAlias) {
		return attemptRepository.findTop5ByUserAliasOrderByIdDesc(userAlias);
	}

	@Override
	public MultiplicationResultAttempt getResultAttemptById(final Long attemptId) {
		Optional<MultiplicationResultAttempt> attempt = attemptRepository.findById(attemptId);
		return attempt.orElseThrow(() -> new IllegalArgumentException("Attempt for id:" + attemptId + ", not found!"));
	}
}
