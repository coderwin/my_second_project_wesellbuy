package shop.wesellbuy.secondproject.web.resultBox;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 클라이언트 응답 data
 * writer : 이호진
 * init : 2023.02.13
 * updated by writer :
 * update :
 * description : Item 처리 후, 클라이언트에게 보낼 데이터를 담아둔다.
 *               > data + itemNum
 */
@Getter
@AllArgsConstructor
public class ResultForItem<T> {

    private final T data;
    private int itemNum; // 상품 번호 보내주기
}
