package org.lolobored.repository;

import org.lolobored.dao.sync.Sync;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;

public interface SyncRepository extends JpaRepository<Sync, BigInteger> {
}
