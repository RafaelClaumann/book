package microservices.book.gamification.service.impl;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.BDDMockito.any;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import microservices.book.gamification.client.dto.MultiplicationResultAttempt;
import microservices.book.gamification.client.impl.MultiplicationResultAttemptClientImpl;
import microservices.book.gamification.domain.BadgeCard;
import microservices.book.gamification.domain.GameStats;
import microservices.book.gamification.domain.ScoreCard;
import microservices.book.gamification.domain.enums.Badge;
import microservices.book.gamification.repository.BadgeCardRepository;
import microservices.book.gamification.repository.ScoreCardRepository;

class GameServiceImplTest {

	private GameServiceImpl gameService;

	@Mock
	private ScoreCardRepository scoreCardRepository;

	@Mock
	private BadgeCardRepository badgeCardRepository;
	
	@Mock
	private MultiplicationResultAttemptClientImpl multiplicationClient;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.initMocks(this);
		gameService = new GameServiceImpl(scoreCardRepository, badgeCardRepository, multiplicationClient);
		
		MultiplicationResultAttempt attempt = new MultiplicationResultAttempt("rafael", 50, 2, 100, true);
		given(multiplicationClient.retrieveMultiplicationResultAttemptbyId(any(Long.class))).willReturn(attempt);
	}

	@Test
	void processFirstCorrectAttemptTest() {
		Long userId = 1L;
		Long attemptId = 1L;
		int userTotalScore = 10;
		ScoreCard scoreCard = new ScoreCard(userId, attemptId);

		given(scoreCardRepository.getTotalScoreForUser(userId)).willReturn(userTotalScore);

		given(scoreCardRepository.findByUserIdOrderByScoreTimestampDesc(userId))
				.willReturn(Collections.singletonList(scoreCard));
		given(badgeCardRepository.findByUserIdOrderByBadgeTimestampDesc(userId)).willReturn(Collections.emptyList());

		GameStats interation = gameService.newAttemptForUser(userId, attemptId, true);

		assertThat(interation.getScore()).isEqualTo(scoreCard.getScore());
		assertThat(interation.getBadges()).containsOnly(Badge.FIRST_WON);
	}

	@Test
	void processCorrectAttemptForScoreBadgeTest() {
		Long userId = 1L;
		Long attemptId = 1L;
		int userTotalScore = 100;

		BadgeCard firstWonBadge = new BadgeCard(userId, Badge.FIRST_WON);
		given(scoreCardRepository.getTotalScoreForUser(userId)).willReturn(userTotalScore);

		given(scoreCardRepository.findByUserIdOrderByScoreTimestampDesc(userId))
				.willReturn(createNScoreCards(10, userId));
		given(badgeCardRepository.findByUserIdOrderByBadgeTimestampDesc(userId))
				.willReturn(Collections.singletonList(firstWonBadge));

		// when
		GameStats iteration = gameService.newAttemptForUser(userId, attemptId, true);

		// assert - should score one card and win the badge BRONZE
		assertThat(iteration.getScore()).isEqualTo(ScoreCard.DEFAULT_SCORE);
		assertThat(iteration.getBadges()).containsOnly(Badge.BRONZE_MULTIPLICATOR);
	}

	@Test
	void processWrongAttemptTest() {
		//given
		Long userId = 1L;
		Long attemptId = 1L;
		int userTotalScore = 0;
		ScoreCard scoreCard = new ScoreCard(userId, attemptId);
		given(scoreCardRepository.getTotalScoreForUser(userId)).willReturn(userTotalScore);

		given(scoreCardRepository.findByUserIdOrderByScoreTimestampDesc(userId))
				.willReturn(Collections.singletonList(scoreCard));
		given(badgeCardRepository.findByUserIdOrderByBadgeTimestampDesc(userId))
				.willReturn(Collections.emptyList());

		// when
		GameStats iteration = gameService.newAttemptForUser(userId, attemptId, false);

		// assert - shouldn't score anything
		assertThat(iteration.getScore()).isEqualTo(0);
		assertThat(iteration.getBadges()).isEmpty();

	}
	
	@Test
	void retrieveStatsForUserTest() {
		// given
		Long userId = 1L;
		int userTotalScore = 30;
		BadgeCard card = new BadgeCard(userId, Badge.FIRST_WON);
		
		given(scoreCardRepository.getTotalScoreForUser(userId))
				.willReturn(userTotalScore);
		given(badgeCardRepository.findByUserIdOrderByBadgeTimestampDesc(userId))
				.willReturn(Collections.singletonList(card));
		
		// when
		GameStats iteration = gameService.retrieveStatsForUser(userId);
		
		// assert - should score one card and win the badge FIRST_WON
		assertThat(iteration.getScore()).isEqualTo(userTotalScore);
		assertThat(iteration.getBadges()).containsOnly(Badge.FIRST_WON);
	}
	
	@Test
	void processCorrectAttemptForLuckyNumberBadgeTest() {
		Long userId = 1L;
		Long attemptId = 1L;
		
		ScoreCard scoreCard = new ScoreCard(userId, attemptId);
		MultiplicationResultAttempt attempt = new MultiplicationResultAttempt("Rafael", 42, 1, 42, true);
		
		given(scoreCardRepository.findByUserIdOrderByScoreTimestampDesc(userId))
			.willReturn(Collections.singletonList(scoreCard));
		given(badgeCardRepository.findByUserIdOrderByBadgeTimestampDesc(userId))
			.willReturn(Collections.emptyList());		
		given(multiplicationClient.retrieveMultiplicationResultAttemptbyId(attemptId)).willReturn(attempt);
		
		GameStats iteration = gameService.newAttemptForUser(userId, attemptId, true);
		
		verify(multiplicationClient).retrieveMultiplicationResultAttemptbyId(attemptId);
		assertThat(iteration.getBadges()).isEqualTo(Arrays.asList(Badge.FIRST_WON, Badge.LUCKY_NUMBER));
	}

	private List<ScoreCard> createNScoreCards(int n, Long userId) {
		return IntStream.range(0, n).mapToObj(i -> new ScoreCard(userId, (long) i)).collect(Collectors.toList());
	}

}
