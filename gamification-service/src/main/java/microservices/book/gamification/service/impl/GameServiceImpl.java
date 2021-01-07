package microservices.book.gamification.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import microservices.book.gamification.domain.BadgeCard;
import microservices.book.gamification.domain.GameStats;
import microservices.book.gamification.domain.ScoreCard;
import microservices.book.gamification.domain.enums.Badge;
import microservices.book.gamification.repository.BadgeCardRepository;
import microservices.book.gamification.repository.ScoreCardRepository;
import microservices.book.gamification.service.GameService;

@Service
public class GameServiceImpl implements GameService {

	private ScoreCardRepository scoreCardRepository;

	private BadgeCardRepository badgeCardRepository;

	@Autowired
	public GameServiceImpl(final ScoreCardRepository scoreCardRepository,
			final BadgeCardRepository badgeCardRepository) {
		this.scoreCardRepository = scoreCardRepository;
		this.badgeCardRepository = badgeCardRepository;
	}

	@Override
	public GameStats newAttemptForUser(Long userId, Long attemptId, boolean correct) {

		
		if (correct) {
			// vai criar um ScoreCard com 10pts; usando System.currentTimeMillis()
			ScoreCard scoreCard = new ScoreCard(userId, attemptId);
			scoreCardRepository.save(scoreCard);

			// atualizar os badges
			List<BadgeCard> badgeCards = processBadges(userId);
			List<Badge> badges = badgeCards.stream().map(BadgeCard::getBadge).collect(Collectors.toList());
			
			return new GameStats(userId, scoreCard.getScore(), badges);
		}

		return GameStats.emptyStats(userId);
	}

	public List<BadgeCard> processBadges(Long userId) {

		List<BadgeCard> badgeCards = new ArrayList<>();
		int totalScoreForUser = scoreCardRepository.getTotalScoreForUser(userId);
		List<ScoreCard> scoreCards = scoreCardRepository.findByUserIdOrderByScoreTimestampDesc(userId);
		
		if(scoreCards.size() == 1 && !containsBadge(badgeCards, Badge.FIRST_ATTEMPT)) {
			badgeCards.add(new BadgeCard(userId, Badge.FIRST_ATTEMPT));
			badgeCardRepository.save(new BadgeCard(userId, Badge.FIRST_ATTEMPT));
		}

		if(totalScoreForUser >= 100 && !containsBadge(badgeCards, Badge.BRONZE_MULTIPLICATOR)) {
			badgeCards.add(new BadgeCard(userId, Badge.BRONZE_MULTIPLICATOR));
			badgeCardRepository.save(new BadgeCard(userId, Badge.BRONZE_MULTIPLICATOR));
		}
		
		if(totalScoreForUser >= 500 && !containsBadge(badgeCards, Badge.SILVER_MULTIPLICATOR)) {
			badgeCards.add(new BadgeCard(userId, Badge.SILVER_MULTIPLICATOR));
			badgeCardRepository.save(new BadgeCard(userId, Badge.SILVER_MULTIPLICATOR));
		}
		
		if(totalScoreForUser >= 999 && !containsBadge(badgeCards, Badge.GOLD_MULTIPLICATOR)) {
			badgeCards.add(new BadgeCard(userId, Badge.GOLD_MULTIPLICATOR));
			badgeCardRepository.save(new BadgeCard(userId, Badge.GOLD_MULTIPLICATOR));
		}

		return badgeCards;
	}

	public boolean containsBadge(List<BadgeCard> badgeCards, Badge badge) {
		return badgeCards.stream().anyMatch(card -> card.getBadge().equals(badge));
	}

	@Override
	public GameStats retrieveStatsForUser(Long userId) {
		// TODO Auto-generated method stub
		return null;
	}

}
;