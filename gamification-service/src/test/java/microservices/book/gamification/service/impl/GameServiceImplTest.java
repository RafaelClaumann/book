package microservices.book.gamification.service.impl;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import microservices.book.gamification.domain.GameStats;
import microservices.book.gamification.domain.enums.Badge;

class GameServiceImplTest {

	private GameServiceImpl gameService;

	@BeforeEach
	void setUp() {
		gameService = new GameServiceImpl();
	}

	@Test
	void processFirstCorrectAttemptTest() {
		Long userId = 1L;
		Long attemptId = 1L;
		GameStats firstGameStats = gameService.newAttemptForUser(userId, attemptId, true);
		
		assertThat(firstGameStats.getUserId()).isEqualTo(userId);
		assertThat(firstGameStats.getScore()).isEqualTo(10);
		assertThat(firstGameStats.getBadges()).contains(Badge.FIRST_ATTEMPT);
	}

}
