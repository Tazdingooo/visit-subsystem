package com.example.visitsubsystem.repository;

import com.example.visitsubsystem.domain.VisitStatus;

public interface StatusCountRow {
    VisitStatus getStatus();

    long getVisits();
}
