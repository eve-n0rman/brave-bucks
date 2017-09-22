package com.buyback.eve.web.rest;

import java.util.Optional;
import java.util.stream.Collectors;

import com.buyback.eve.domain.Killmail;
import com.buyback.eve.domain.PlayerStats;
import com.buyback.eve.domain.User;
import com.buyback.eve.repository.KillmailRepository;
import com.buyback.eve.repository.UserRepository;
import com.buyback.eve.security.SecurityUtils;
import com.buyback.eve.service.PlayerStatsService;
import com.buyback.eve.web.dto.KillmailDto;

import static com.buyback.eve.service.KillmailParser.calculateCoins;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PlayerStatsResource {

    private final PlayerStatsService playerStatsService;
    private final KillmailRepository killmailRepository;
    private final UserRepository userRepository;

    public PlayerStatsResource(final PlayerStatsService playerStatsService,
                               final KillmailRepository killmailRepository,
                               final UserRepository userRepository) {
        this.playerStatsService = playerStatsService;
        this.killmailRepository = killmailRepository;
        this.userRepository = userRepository;
    }

    @GetMapping(path = "/stats/my")
    public ResponseEntity getPlayerStats() {
        PlayerStats statsForCurrentUser = playerStatsService.getStatsForCurrentUser();
        return ResponseEntity.ok(statsForCurrentUser);
    }

    @GetMapping(path = "/stats/potentialPayout")
    public ResponseEntity getPotentialPayout() {
        long potentialPayout = playerStatsService.getStatsForCurrentUser().getPotentialPayout();
        return ResponseEntity.ok(potentialPayout);
    }

    @GetMapping(path = "/killmails")
    public ResponseEntity getKillmails() {
        Optional<User> oneByLogin = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin());
        if (!oneByLogin.isPresent()) {
            return ResponseEntity.badRequest().body("Could not resolve user.");
        }
        return ResponseEntity.ok(killmailRepository.findByCharacterId(oneByLogin.get().getCharacterId())
                                                   .stream().map(this::createMailDto).collect(Collectors.toList()));
    }

    private KillmailDto createMailDto(final Killmail mail) {
        KillmailDto dto = new KillmailDto();
        dto.setCoins(calculateCoins(mail));
        dto.setKillmailId(mail.getKillId());
        dto.setKillTime(mail.getKillTime());
        dto.setVictimAlliance(mail.getVictimAlliance());
        return dto;
    }
}
