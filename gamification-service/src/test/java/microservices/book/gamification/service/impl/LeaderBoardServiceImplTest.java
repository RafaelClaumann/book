package microservices.book.gamification.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.BDDMockito.given;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import microservices.book.gamification.domain.LeaderBoardRow;
import microservices.book.gamification.repository.ScoreCardRepository;

class LeaderBoardServiceImplTest {

	private LeaderBoardServiceImpl leaderBoardService;

	@Mock
	private ScoreCardRepository scoreCardRepository;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.initMocks(this);
		leaderBoardService = new LeaderBoardServiceImpl(scoreCardRepository);
	}

	@Test
	void retrieveLeaderBoardTest() {
		Long userId = 1L;
		Long userTotalScore = 350L;

		LeaderBoardRow row = new LeaderBoardRow(userId, userTotalScore);
		List<LeaderBoardRow> expectedLeaderBoard = Collections.singletonList(row);

		given(scoreCardRepository.findFirst10()).willReturn(expectedLeaderBoard);

		List<LeaderBoardRow> currentLeaderBoard = leaderBoardService.getCurrentLeaderBoard();

		assertThat(currentLeaderBoard).isEqualTo(expectedLeaderBoard);
		assertThat(currentLeaderBoard.get(0).getTotalScore()).isEqualTo(userTotalScore);
	}

}
