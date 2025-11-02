package org.lolobored.bear2memos.dao.sync;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigInteger;

@Entity
@Table(name = "sync")
@Data
public class Sync {
    @Id
    private BigInteger bearId;
    private String bearChecksum;
    @Column(unique = true)
    private String memosId;
    private String memosChecksum;
    private boolean deleted;
}
