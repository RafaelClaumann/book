package microservices.book.gamification.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import microservices.book.gamification.domain.LeaderBoardRow;
import microservices.book.gamification.repository.ScoreCardRepository;
import microservices.book.gamification.service.LeaderBoardService;

@Service
public class LeaderBoardServiceImpl implements LeaderBoardService {

	private ScoreCardRepository scoreCardRepository;
	
	@Autowired
	public LeaderBoardServiceImpl(final ScoreCardRepository scoreCardRepository) {
		this.scoreCardRepository = scoreCardRepository;
	}
	
	@Override
	public List<LeaderBoardRow> getCurrentLeaderBoard() {
		List<LeaderBoardRow> findFirst10 = scoreCardRepository.findFirst10();
		return findFirst10;
	}

}
