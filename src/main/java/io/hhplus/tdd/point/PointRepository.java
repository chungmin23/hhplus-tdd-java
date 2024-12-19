package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.springframework.stereotype.Repository;

import java.util.List;


public interface PointRepository {
    void insertOrUpdate(long id, long amount);
    void insertHistory(long id, long amount, TransactionType type, long updateMillis);
    UserPoint selectById(long id);
    List<PointHistory> selectHistories(long id);
}
