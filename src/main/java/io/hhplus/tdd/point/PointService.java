package io.hhplus.tdd.point;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class PointService {

    private final PointRepository pointRepository = null;

    private static final Logger log = LoggerFactory.getLogger(PointController.class);

    private final ReentrantLock lock = new ReentrantLock(); // 동시성 처리를 위한 Lock



    //개인 유저 포인트 조회
    public  UserPoint userPoint(long id){
        if(id > 0 ){
            throw  new IllegalArgumentException("id 가 없습니다 ");
        }

        return pointRepository.selectById(id);
    }

    // 포인트 기록 조회
    public List<PointHistory> history(long id){
        if(id > 0 ){
            throw  new IllegalArgumentException("id 가 없습니다 ");
        }
        return pointRepository.selectHistories(id);
    }

    //포인트 충전
    public UserPoint charge(long id , long amount){

        lock.lock();
        try{
            // 현재 사용자 포인트 조회
            UserPoint userPoint = pointRepository.selectById(id);

            // 포인트 충전 후 업데이트
            long newAmount = userPoint.point() + amount;
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

        lock.lock();
        try{
            // 현재 사용자 포인트 조회
            UserPoint userPoint = pointRepository.selectById(id);

            // 사용 가능한 포인트 확인
            if (userPoint.point() < amount) {
                log.error("포인트 부족 : " + "현재 잔액 포인트 : " + userPoint );
                throw new IllegalArgumentException("포인트가 부족합니다");
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
