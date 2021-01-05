package microservices.book.gamification.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import microservices.book.gamification.domain.GameStats;
import microservices.book.gamification.domain.enums.Badge;
import microservices.book.gamification.service.GameService;

@Service
public class GameServiceImpl implements GameService {

	@Override
	public GameStats newAttemptForUser(Long userId, Long attemptId, boolean correct) {
		List<Badge> badges = new ArrayList<>();
		badges.add(Badge.FIRST_ATTEMPT);
		return new GameStats(userId, 10, badges);
	}

	@Override
	public GameStats retrieveStatsForUser(Long userId) {
		// TODO Auto-generated method stub
		return null;
	}

}
