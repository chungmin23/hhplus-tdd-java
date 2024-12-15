package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/point")
public class PointController {

    private static final Logger log = LoggerFactory.getLogger(PointController.class);

    private final UserPointTable userPointTable = new UserPointTable();
    private final PointHistoryTable pointHistoryTable = new PointHistoryTable();


    /**
     * TODO - 특정 유저의 포인트를 조회하는 기능을 작성해주세요.
     */
    @GetMapping("{id}")
    public UserPoint point(
            @PathVariable long id
    ) {

        UserPoint selectUserPoint =  userPointTable.selectById(id);

        return selectUserPoint;
    }

    /**
     * TODO - 특정 유저의 포인트 충전/이용 내역을 조회하는 기능을 작성해주세요.
     */
    @GetMapping("{id}/histories")
    public List<PointHistory> history(
            @PathVariable long id
    ) {

        return pointHistoryTable.selectAllByUserId(id);
        //return List.of();
    }

    /**
     * TODO - 특정 유저의 포인트를 충전하는 기능을 작성해주세요.
     */
    @PatchMapping("{id}/charge")
    public UserPoint charge(
            @PathVariable long id,
            @RequestBody long amount
    ) {

        // 현재 사용자 포인트 조회 (없으면 기본값 반환)
        UserPoint userPoint = userPointTable.selectById(id);

        // 포인트 충전 후 업데이트
        long newAmount = userPoint.point() + amount;
        UserPoint updatedUserPoint = userPointTable.insertOrUpdate(id, newAmount);

        // 히스토리 추가
        pointHistoryTable.insert(id, amount, TransactionType.CHARGE, System.currentTimeMillis());

        return updatedUserPoint;
    }

    /**
     * TODO - 특정 유저의 포인트를 사용하는 기능을 작성해주세요.
     */
    @PatchMapping("{id}/use")
    public UserPoint use(
            @PathVariable long id,
            @RequestBody long amount
    ) {
        // 현재 사용자 포인트 조회 (없으면 기본값 반환)
        UserPoint userPoint = userPointTable.selectById(id);

        // 사용 가능한 포인트 확인
        if (userPoint.point() < amount) {
            log.error("Insufficient points for user id: {} (requested: {}, available: {})", id, amount, userPoint.point());
            throw new IllegalArgumentException("Insufficient points.");
        }

        // 포인트 사용 후 업데이트
        long newAmount = userPoint.point() - amount;
        UserPoint updatedUserPoint = userPointTable.insertOrUpdate(id, newAmount);

        // 히스토리 추가
        pointHistoryTable.insert(id, -amount, TransactionType.USE, System.currentTimeMillis());

        return updatedUserPoint;
    }
}
