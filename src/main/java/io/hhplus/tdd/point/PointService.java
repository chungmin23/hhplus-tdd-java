package io.hhplus.tdd.point;

import io.hhplus.tdd.ErrorMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class PointService {

    private final PointRepository pointRepository = null;

    private static final Logger log = LoggerFactory.getLogger(PointController.class);

    // 동시성 관련 콜렉션 사용
    private final ConcurrentHashMap<Long, ReentrantLock> userLocks =  new ConcurrentHashMap<>();

    // 유저별 락을 걸기위해 구현
    private ReentrantLock getLockUser(long userId){
        return userLocks.computeIfAbsent(userId, id -> new ReentrantLock());
    }


    private static final long MAX_BALANCE = 1000000;



    //개인 유저 포인트 조회
    public  UserPoint userPoint(long id){
        if(id > 0 ){
            throw  new IllegalArgumentException(ErrorMessages.USER_NOT_FOUND);
        }

        return pointRepository.selectById(id);
    }

    // 포인트 기록 조회
    public List<PointHistory> history(long id){
        if(id > 0 ){
            throw  new IllegalArgumentException(ErrorMessages.USER_NOT_FOUND);
        }
        return pointRepository.selectHistories(id);
    }

    //포인트 충전
    public UserPoint charge(long id , long amount){

        if(amount <= 0){
            throw new IllegalArgumentException("충전 금액은 0보다 커야 합니다");
        }
        ReentrantLock lock = getLockUser(id);
        lock.lock();
        try{


            // 현재 사용자 포인트 조회
            UserPoint userPoint = pointRepository.selectById(id);

            // 포인트 충전 후 업데이트
            long newAmount = userPoint.point() + amount;

            // 최대 잔고를 초과한 경우
            if (newAmount > MAX_BALANCE){
                throw new IllegalArgumentException(ErrorMessages.MAX_BALANCE_EXCEEDED);
            }

            pointRepository.insertOrUpdate(id, newAmount);

            // 히스토리 추가
            pointRepository.insertHistory(id, amount, TransactionType.CHARGE, System.currentTimeMillis());

            return userPoint;
        } finally {
            lock.unlock();
        }

    }

    //특정 사용 포인트 사용 하는 기능
    public UserPoint use(long id, long amount){

        if(amount <= 0){
            throw new IllegalArgumentException("충전 금액은 0보다 커야 합니다");
        }

        ReentrantLock lock = getLockUser(id);
        lock.lock();
        try{

            // 현재 사용자 포인트 조회
            UserPoint userPoint = pointRepository.selectById(id);

            // 사용 가능한 포인트 확인
            if (userPoint.point() < amount) {
                log.error("포인트 부족 : " + "현재 잔액 포인트 : " + userPoint );
                throw new IllegalArgumentException(ErrorMessages.POINT_LACK);
            }

            // 포인트 사용 후 업데이트  처리
            long newAmount = userPoint.point() - amount;
            pointRepository.insertOrUpdate(id, newAmount);

            // 히스토리 추가
            pointRepository.insertHistory(id, amount, TransactionType.CHARGE, System.currentTimeMillis());

            return userPoint;
        }finally {
            lock.unlock();
        }

    }




}
