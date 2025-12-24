package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * xToOne(ManyToOne, OneToOne)
 * Order
 * Order -> Member
 * Order -> Delivery
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

    /**
     * 우선 엔티티를 DTO로 변환하는 방법을 선택한다. v2
     * 필요하면 페치 조인으로 성능을 최적화 한다. 대부분의 성능 이슈가 해결된다. v3
     * 그래도 안되면 DTO로 직접 조회하는 방법을 사용한다. v4
     * 최후의 방법은 JPA가 제공하는 네이티브 SQL이나 스프링 JDBC Template을 사용해서 SQL을 직접 사용한다
     */

    /**
     * 엔티티 직접 반환 하지말자
     * 무한 로딩 걸려서 JsonIgnore해주고
     * Hibernate5JakartaModule 라이브러리 추가해서 bean 등록후 설정 해줘야하는데
     * 이 방법은 좋지 않음.
     */
    @GetMapping("api/v1/simple-orders")
    public List<Order> orderV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        return all;
    }

    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> orderV2() {
        // ORDER 2개라면
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        // 2번 루프를 돈다 ORDER 조회 -> SimpleOrderDto가서 member -> delivery -> member -> delivery 총 5번의 조회쿼리를 날림 (N+1문제)
        // 지연로딩은 영속성컨텍스트에서 조회하므로, 이미 조회된 경우 쿼리를 생략한다.
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());
        return result;
    }

    /**
     * fetch join 사용
     * 쿼리 1번에 조회
     * 재사용성이 좋음
     */
    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> orderV3(){
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        return orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());
    }

    /**
     * 딱 필요한 것만 Dto로 만들어서 써서 재사용성이 안좋음
     */
    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> orderV4() {
        return orderSimpleQueryRepository.findOrderDtos();
    }

    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName(); // LAZY 초기화 : 영속성 컨텍스트가 memberId를 가지고 영속성컨텍스트를 찾아보고 없으면 DB쿼리를 날림
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress(); // LAZY 초기화
        }
    }
}