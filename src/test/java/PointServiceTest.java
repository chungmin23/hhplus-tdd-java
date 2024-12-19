import io.hhplus.tdd.ErrorMessages;
import io.hhplus.tdd.point.PointService;
import io.hhplus.tdd.point.UserPoint;
import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class PointServiceTest {

    /*
        1. 포인트 충전시
            1) 최대 잔고를 넘은 경우

        2. 포인트사용시
            1) 현재 잔액보다 큰 금액을 사용한경우

        3. 사용자가 없는 경우
        
        4. 동시성 테스트
     */

    private final PointService pointService = new PointService();


    @Test
    void 충전시_최대잔고_초과를_한경우(){
        //given
        long userId = 1L;

        // when

        // 현재 잔고 충전
        pointService.charge(userId,900000);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            pointService.charge(userId, 200000);
        });

        //then
        assertEquals(ErrorMessages.MAX_BALANCE_EXCEEDED, exception.getMessage());

    }

    @Test
    void 포인트사용시_부족한경우(){
        //given
        long userId= 1L;

        //when

        pointService.charge(userId, 100000);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            pointService.use(userId, 200000);
        });

        //then
        assertEquals(ErrorMessages.POINT_LACK, exception.getMessage());

    }

    @Test
    void 사용자가_없는경우() {
        //given
        long userId = 20L;

        //when
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            pointService.userPoint(userId);
        });

        //then
        assertEquals(ErrorMessages.USER_NOT_FOUND, exception.getMessage());

    }

    @Test
    void 동시성테스트() throws InterruptedException {
        //given
        long userId = 1L;
        pointService.charge(userId, 1000000);

        // 쓰레드 풀 생성
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);

        //when
        //10 개 동시 요청
        for( int i = 0; i< 10; i++){
            executor.submit(() -> {
                try{
                    pointService.use(userId, 50000);
                }catch (Exception e){
                    e.printStackTrace();
                }
            });
        }
        // 쓰레드풀 종료 
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // 잔고 조회및 확인
        UserPoint userPoint = pointService.userPoint(userId);

        //then
        assertEquals(50000, userPoint.point());

    }








}
