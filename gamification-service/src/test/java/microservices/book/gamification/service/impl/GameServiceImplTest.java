package microservices.book.gamification.service.impl;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.any;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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

	@BeforeEach
	void setUp() {
		MockitoAnnotations.initMocks(this);
		gameService = new GameServiceImpl(scoreCardRepository, badgeCardRepository);
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
		given(badgeCardRepository.findByUserIdOrderByBadgeTimestampDesc(userId))
				.willReturn(Collections.emptyList());

		GameStats interation = gameService.newAttemptForUser(userId, attemptId, true);

		assertThat(interation.getScore()).isEqualTo(scoreCard.getScore());
		assertThat(interation.getBadges()).containsOnly(Badge.FIRST_ATTEMPT);
	}

}
