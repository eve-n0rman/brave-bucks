package com.buyback.eve.repository;

import java.util.List;
import java.util.Optional;

import com.buyback.eve.domain.Killmail;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

/**
 * Spring Data MongoDB repository for the Killmail entity.
 */
public interface KillmailRepository extends MongoRepository<Killmail, String> {
    @Query("{ attackerIds: ?0 }")
    List<Killmail> findByAttackerId(long attackerId);

    Optional<Killmail> findByKillId(long killmailId);

    @Query("{ payoutCalculated: false }")
    List<Killmail> findPending();
}
