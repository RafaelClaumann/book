package microservices.book.gamification.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import microservices.book.gamification.client.dto.MultiplicationResultAttempt;
import microservices.book.gamification.client.impl.MultiplicationResultAttemptClientImpl;
import microservices.book.gamification.domain.BadgeCard;
import microservices.book.gamification.domain.GameStats;
import microservices.book.gamification.domain.ScoreCard;
import microservices.book.gamification.domain.enums.Badge;
import microservices.book.gamification.repository.BadgeCardRepository;
import microservices.book.gamification.repository.ScoreCardRepository;
import microservices.book.gamification.service.GameService;

@Service
@Slf4j
public class GameServiceImpl implements GameService {

	private final int POINTS_TO_BRONZE_BADGE = 100;
	private final int POINTS_TO_SILVER_BADGE = 500;
	private final int POINTS_TO_GOLD_BADGE = 999;
	private final int LUCKY_NUMBER = 42;

	private ScoreCardRepository scoreCardRepository;

	private BadgeCardRepository badgeCardRepository;
	
	private MultiplicationResultAttemptClientImpl attemptClient;

	@Autowired
	public GameServiceImpl(final ScoreCardRepository scoreCardRepository,
			final BadgeCardRepository badgeCardRepository,
			final MultiplicationResultAttemptClientImpl multiplicationClientRestTemplate) {
		this.scoreCardRepository = scoreCardRepository;
		this.badgeCardRepository = badgeCardRepository;
		this.attemptClient = multiplicationClientRestTemplate;
	}

	@Override
	public GameStats newAttemptForUser(Long userId, Long attemptId, boolean correct) {

		if (correct) {
			ScoreCard scoreCard = new ScoreCard(userId, attemptId);
			scoreCardRepository.save(scoreCard);
			log.info("User with id {} scored {} points for attempt id {}", userId, scoreCard.getScore(), attemptId);
			List<BadgeCard> badgeCards = processForBadges(userId, attemptId);
			return new GameStats(userId, scoreCard.getScore(),
					badgeCards.stream().map(BadgeCard::getBadge).collect(Collectors.toList()));
		}

		return GameStats.emptyStats(userId);
	}
	
	@Override
	public GameStats retrieveStatsForUser(Long userId) {
		int totalScoreForUser = scoreCardRepository.getTotalScoreForUser(userId);
		List<BadgeCard> badgesCards = badgeCardRepository.findByUserIdOrderByBadgeTimestampDesc(userId);
		List<Badge> badges = badgesCards.stream().map(badge -> badge.getBadge()).collect(Collectors.toList());
		
		GameStats gameStats = new GameStats(userId, totalScoreForUser, badges);
		return gameStats;
	}

	private List<BadgeCard> processForBadges(Long userId, Long attemptId) {

		List<BadgeCard> badgeCards = new ArrayList<>();

		int totalScore = scoreCardRepository.getTotalScoreForUser(userId);
		log.info("New score for user {} is {}", userId, totalScore);

		List<ScoreCard> scoreCardList = scoreCardRepository.findByUserIdOrderByScoreTimestampDesc(userId);
		List<BadgeCard> badgeCardList = badgeCardRepository.findByUserIdOrderByBadgeTimestampDesc(userId);

		// Badges depending on score
		checkAndGiveBadgeBasedOnScore(badgeCardList, Badge.BRONZE_MULTIPLICATOR, totalScore, POINTS_TO_BRONZE_BADGE,
				userId).ifPresent(badgeCards::add);
		checkAndGiveBadgeBasedOnScore(badgeCardList, Badge.SILVER_MULTIPLICATOR, totalScore, POINTS_TO_SILVER_BADGE,
				userId).ifPresent(badgeCards::add);
		checkAndGiveBadgeBasedOnScore(badgeCardList, Badge.GOLD_MULTIPLICATOR, totalScore, POINTS_TO_GOLD_BADGE,
				userId).ifPresent(badgeCards::add);

		// First won badge
		if (scoreCardList.size() == 1 && !containsBadge(badgeCardList, Badge.FIRST_WON)) {
			BadgeCard firstWonBadge = giveBadgeToUser(Badge.FIRST_WON, userId);
			badgeCards.add(firstWonBadge);
		}
		
		// Lucky number badge
		MultiplicationResultAttempt attempt = attemptClient.retrieveMultiplicationResultAttemptbyId(attemptId);
		checkAndGiveLuckyBadge(userId, attempt, badgeCardList).ifPresent(badgeCards::add);
		
		return badgeCards;
	}

	private Optional<BadgeCard> checkAndGiveBadgeBasedOnScore(final List<BadgeCard> badgeCards, final Badge badge,
			final int score, final int scoreThreshold, final Long userId) {
		if (score >= scoreThreshold && !containsBadge(badgeCards, badge)) {
			return Optional.of(giveBadgeToUser(badge, userId));
		}
		return Optional.empty();
	}
	
	private Optional<BadgeCard> checkAndGiveLuckyBadge(final Long userId, final MultiplicationResultAttempt attempt,
			final List<BadgeCard> badgeCardList) {
		if(containsLuckyFactor(attempt) && !containsBadge(badgeCardList, Badge.LUCKY_NUMBER)) {
			BadgeCard luckyBadge = giveBadgeToUser(Badge.LUCKY_NUMBER, userId);
			return Optional.of(luckyBadge);
		}
		return Optional.empty();
	}

	private BadgeCard giveBadgeToUser(final Badge badge, final Long userId) {
		BadgeCard badgeCard = new BadgeCard(userId, badge);
		badgeCardRepository.save(badgeCard);
		log.info("User with id {} won a new badge: {}", userId, badge);
		return badgeCard;
	}
	
	private boolean containsLuckyFactor(final MultiplicationResultAttempt attempt) {
		if(attempt.getMultiplicationFactorA() == LUCKY_NUMBER || attempt.getMultiplicationFactorB() == LUCKY_NUMBER) {
			return true;
		}
		return false;
	}
	
	private boolean containsBadge(final List<BadgeCard> badgeCards, final Badge badge) {
		return badgeCards.stream().anyMatch(card -> card.getBadge().equals(badge));
	}

}
