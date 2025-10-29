package com.flooring.flooringmastery.dao;

import org.springframework.stereotype.Repository;

@Repository
public class AuditDaoFileImpl implements AuditDao {
    @Override
    public void writeAuditEntry(String entry) {
        // Minimal implementation: currently a no-op.
        // Can be extended to write to a file (e.g. FileData/Backup/audit.txt) or logging.
    }
}
