package jpabook.jpashop.repository.order.simplequery;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OrderSimpleQueryRepository {

    EntityManager em;

    /**
     * new 명령어를 사용해서 JPQL의 결과를 DTO로 즉시 변환
     * 리포지토리 재사용성 떨어짐, API 스펙에 맞춘 코드가 리포지토리에 들어가는 단점
     * select절 필드 몇 개 덜 조회한다고 성능차이가 크게 나지 않음
     ***** 리포지토리는 순수한 order엔티티만 조회하는데 쓰고 리포지토리 분리
     */
    public List<OrderSimpleQueryDto> findOrderDtos() {
        return em.createQuery(
                "select new jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto(o.id, m.name, o.orderDate, o.status, d.address)" +
                        " from Order o" +
                        " join o.member m" +
                        " join o.delivery d", OrderSimpleQueryDto.class
        ).getResultList();
    }
}
