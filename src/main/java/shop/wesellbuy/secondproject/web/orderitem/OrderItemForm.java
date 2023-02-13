package shop.wesellbuy.secondproject.web.orderitem;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 주문상품 dto
 * writer : 이호진
 * init : 2023.02.04
 * updated by writer : 이호진
 * update : 2023.02.05
 * description : 클라이언트가 보내온 주문상품 정보를 담아둔다.
 *
 * update : 상품 가격 제거
 *          -> view에서 수정할 가능성이 있음
 */
@Getter
@AllArgsConstructor
public class OrderItemForm {

    int quantity; // 주문 수량
//    int price; // 상품 가격
    int itemNum; // 상품 번호

}