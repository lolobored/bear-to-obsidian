package org.lolobored.bear2memos.repository;

import org.lolobored.bear2memos.dao.sync.Sync;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;
import java.util.Optional;

public interface SyncRepository extends JpaRepository<Sync, BigInteger> {
    Optional<Sync> findByMemosId(String memosId);
}
