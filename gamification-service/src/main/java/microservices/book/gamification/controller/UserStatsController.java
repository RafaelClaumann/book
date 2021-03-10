package microservices.book.gamification.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import microservices.book.gamification.domain.GameStats;
import microservices.book.gamification.service.GameService;

@RestController
@RequestMapping(value = "/stats")
public class UserStatsController {

	private GameService gameService;

	@Autowired
	public UserStatsController(final GameService gameService) {
		this.gameService = gameService;
	}

	@GetMapping
	public ResponseEntity<GameStats> getUserStats(@RequestParam("userId") Long userId) {
		GameStats retrieveStatsForUser = gameService.retrieveStatsForUser(userId);
		return ResponseEntity.ok(retrieveStatsForUser);
	}

}
