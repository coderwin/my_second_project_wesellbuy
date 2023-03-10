package shop.wesellbuy.secondproject.service;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import shop.wesellbuy.secondproject.domain.*;
import shop.wesellbuy.secondproject.domain.delivery.DeliveryStatus;
import shop.wesellbuy.secondproject.domain.item.Book;
import shop.wesellbuy.secondproject.domain.item.Furniture;
import shop.wesellbuy.secondproject.domain.item.HomeAppliances;
import shop.wesellbuy.secondproject.domain.item.ItemPicture;
import shop.wesellbuy.secondproject.domain.order.OrderStatus;
import shop.wesellbuy.secondproject.exception.delivery.NotCancelOrderException;
import shop.wesellbuy.secondproject.exception.item.OverflowQuantityException;
import shop.wesellbuy.secondproject.exception.order.NotChangeDeliveryStatusException;
import shop.wesellbuy.secondproject.exception.order.NotCorrectPaidMoneyException;
import shop.wesellbuy.secondproject.repository.item.ItemJpaRepository;
import shop.wesellbuy.secondproject.repository.member.MemberJpaRepository;
import shop.wesellbuy.secondproject.repository.order.OrderJpaRepository;
import shop.wesellbuy.secondproject.repository.order.OrderSearchCond;
import shop.wesellbuy.secondproject.repository.orderitem.OrderItemJpaRepository;
import shop.wesellbuy.secondproject.repository.orderitem.OrderItemSearchCond;
import shop.wesellbuy.secondproject.service.order.OrderService;
import shop.wesellbuy.secondproject.web.item.BookForm;
import shop.wesellbuy.secondproject.web.item.FurnitureForm;
import shop.wesellbuy.secondproject.web.item.HomeAppliancesForm;
import shop.wesellbuy.secondproject.web.member.MemberForm;
import shop.wesellbuy.secondproject.web.order.OrderDetailForm;
import shop.wesellbuy.secondproject.web.order.OrderListForm;
import shop.wesellbuy.secondproject.web.order.OrderListFormForAdmin;
import shop.wesellbuy.secondproject.web.orderitem.OrderItemForm;
import shop.wesellbuy.secondproject.web.orderitem.OrderItemListForm;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@Slf4j
public class OrderServiceTest {

    @Autowired
    EntityManager em;
    @Autowired
    OrderService orderService;
    @Autowired
    OrderJpaRepository orderJpaRepository;
    @Autowired
    OrderItemJpaRepository orderItemJpaRepository;
    @Autowired
    ItemJpaRepository itemJpaRepository;
    @Autowired
    MemberJpaRepository memberJpaRepository;

    private Member member; // test ?????? member
    private Member member2; // test ?????? member
    private Member member3; // test ?????? member

    private Delivery delivery; // test ?????? delivery
    private Delivery delivery2; // test ?????? delivery
    private Delivery delivery3; // test ?????? delivery

    Item item1; // test ?????? ??????
    Item item2; // test ?????? ??????
    Item item3; // test ?????? ??????

    int number = 100; //
    int oCount; // member ?????????
    int oCount2; // member2 ?????????
    int oCount3; // member3 ?????????

    int iaCount = 0; // member(?????????) ???????????????
    int iaCount2 = 0; // member(?????????) ???????????????

    @BeforeEach
    public void init() {

        // ?????? ??????
        MemberForm memberForm1 = new MemberForm("a", "a","123", "a@a", "01012341234", "0511231234", "korea1", "b", "h", "h", "123", null);
        Member member = Member.createMember(memberForm1);
        MemberForm memberForm2 = new MemberForm("a", "b","123", "a@a", "01012341234", "0511231234", "korea12", "b", "h2", "h2", "123", null);
        Member member2 = Member.createMember(memberForm2);
        MemberForm memberForm3 = new MemberForm("b", "cd","123", "a@a", "01012341234", "0511231234", "korea13", "b", "h3", "h3", "123", null);
        Member member3 = Member.createMember(memberForm3);

        memberJpaRepository.save(member);
        memberJpaRepository.save(member2);
        memberJpaRepository.save(member3);

        this.member = member; // x ?????? ??????
        this.member2 = member2; // y ?????? ??????
        this.member3 = member3; // z ?????? ??????
        // ?????? ??????
        // picture ??????
        List<ItemPicture> itemPictureList = new ArrayList<>();
        itemPictureList.add(ItemPicture.createItemPicture("a", "a"));
        itemPictureList.add(ItemPicture.createItemPicture("a1", "a2"));

        BookForm bookFormTest1 = new BookForm(10000, 1000, "x", "x is...", itemPictureList, "ed", "ok");
        this.item1 = Book.createBook(bookFormTest1, member);
        FurnitureForm furnitureFormTest1 = new FurnitureForm(20000, 2000, "y", "y is...", itemPictureList, "ed");
        this.item2 = Furniture.createFurniture(furnitureFormTest1, member2);
        HomeAppliancesForm homeAppliancesFormTest1 = new HomeAppliancesForm(30000, 3000, "z", "z is...", itemPictureList, "ed2");
        this.item3 = HomeAppliances.createHomeAppliances(homeAppliancesFormTest1, member3);

        itemJpaRepository.save(item1);
        itemJpaRepository.save(item2);
        itemJpaRepository.save(item3);

        // ????????????(?????? ??????)
        int number = 100; //
        oCount = 0; // member ?????????
        oCount2 = 0; // member2 ?????????
        oCount3 = 0; // member3 ?????????

        iaCount = 0; // member(?????????) ???????????????
        iaCount2 = 0; // member(?????????) ???????????????

        for(int i = 0; i < number; i++) {
            if(i % 3 == 0) {

                OrderItem orderItem1 = OrderItem.createOrderItem(3, item1.getPrice(), item1); // 1000 * 3 3000
                OrderItem orderItem4 = OrderItem.createOrderItem(3, item1.getPrice(), item1); // 1000 * 3 3000

                // item2 ??????
                OrderItem orderItem2 = OrderItem.createOrderItem(2, item2.getPrice(), item2); // 2000 * 2 4000
                OrderItem orderItem5 = OrderItem.createOrderItem(2, item2.getPrice(), item2); // 2000 * 2 4000
                OrderItem orderItem7 = OrderItem.createOrderItem(2, item2.getPrice(), item2); // 2000 * 2 4000

                // item3 ??????
                OrderItem orderItem3 = OrderItem.createOrderItem(4, item3.getPrice(), item3); // 3000 * 4 12000
                OrderItem orderItem6 = OrderItem.createOrderItem(4, item3.getPrice(), item3); // 3000 * 4 12000



                Delivery delivery11 = Delivery.createDelivery(member);
                Delivery delivery12 = Delivery.createDelivery(member);
                Delivery delivery13 = Delivery.createDelivery(member);

                Order order1 = Order.createOrder(member, delivery11, 7000, orderItem1, orderItem2);  //          // 7000
                Order order5 = Order.createOrder(member, delivery12, 19000, orderItem4, orderItem5, orderItem3);  // 19000
                Order order7 = Order.createOrder(member, delivery13, 16000, orderItem7, orderItem6);              // 16000

                orderJpaRepository.save(order1);
                orderJpaRepository.save(order5);
                orderJpaRepository.save(order7);

                oCount += 3;
                iaCount += 2;

                // ?????? ?????? -> ?????????
                order1.getDelivery().changeStatusRT();
                // ?????? ??????
                order5.cancel();

            } else if(i % 3 == 1) {
                // item1 ??????
                OrderItem orderItem1 = OrderItem.createOrderItem(3, item1.getPrice(), item1); // 1000 * 3 3000
                OrderItem orderItem4 = OrderItem.createOrderItem(3, item1.getPrice(), item1); // 1000 * 3 3000
                // item2 ??????
                OrderItem orderItem2 = OrderItem.createOrderItem(2, item2.getPrice(), item2); // 2000 * 2 4000
                // item3 ??????
                OrderItem orderItem3 = OrderItem.createOrderItem(4, item3.getPrice(), item3); // 3000 * 4 12000
                OrderItem orderItem5 = OrderItem.createOrderItem(4, item3.getPrice(), item3); // 3000 * 4 12000

                Delivery delivery21 = Delivery.createDelivery(member2);
                Delivery delivery22 = Delivery.createDelivery(member2);

                Order order2 = Order.createOrder(member2, delivery21, 15000, orderItem1, orderItem3);            // 15000
                Order order4 = Order.createOrder(member2, delivery22, 19000, orderItem4, orderItem2, orderItem5);// 19000

                orderJpaRepository.save(order2);
                orderJpaRepository.save(order4);

                oCount2 += 2;
                iaCount2 += 2;

                // ?????? ??????
                order2.changeStatus();

            } else {

                OrderItem orderItem2 = OrderItem.createOrderItem(2, item2.getPrice(), item2); // 2000 * 2 4000
                OrderItem orderItem3 = OrderItem.createOrderItem(4, item3.getPrice(), item3); // 3000 * 4 12000

                Delivery delivery3 = Delivery.createDelivery(member3);

                Order order3 = Order.createOrder(member3, delivery3, 16000, orderItem2, orderItem3); // 16000

                orderJpaRepository.save(order3);

                oCount3 += 1;
            }
        }

    }

    /**
     * ?????? ?????? ?????? v1
     */
    @Test
//    @Rollback(value = false)
    public void ??????_??????_v1_??????() {
        // given
        // OrderItemFrom ?????? * 3
        OrderItemForm orderItemForm1 = new OrderItemForm(2, item1.getNum());
        OrderItemForm orderItemForm2 = new OrderItemForm(6, item1.getNum());
        OrderItemForm orderItemForm3 = new OrderItemForm(10, item2.getNum());
        // ???????????? ??????
        int paidMoneyO = item1.getPrice() * orderItemForm1.getQuantity() + item1.getPrice() * orderItemForm2.getQuantity() + item2.getPrice() * orderItemForm3.getQuantity();
        // ?????????????????? ?????? ???
        int paidMoneyMoreThan = item1.getPrice() * orderItemForm1.getQuantity() + item1.getPrice() * orderItemForm2.getQuantity() + item2.getPrice() * orderItemForm3.getQuantity() + 1;
        // ?????????????????? ?????? ???
        int paidMoneyLessThan = item1.getPrice() * orderItemForm1.getQuantity() + item1.getPrice() * orderItemForm2.getQuantity() + item2.getPrice() * orderItemForm3.getQuantity() - 1;

        List<OrderItemForm> orderItemFormList = new ArrayList<>();

        orderItemFormList.add(orderItemForm1);
        orderItemFormList.add(orderItemForm2);
        orderItemFormList.add(orderItemForm3);
        // when // then
        // ????????????(?????? ????????????)
        // ???????????? ?????? ???
        int orderNum = orderService.save(orderItemFormList, member.getNum(), paidMoneyO);

        // Order ????????????
        Order findOrder = orderJpaRepository.findById(orderNum).orElseThrow();

        assertThat(findOrder.getMember().getId()).isEqualTo(member.getId());
        assertThat(findOrder.getDelivery().getAddress().getCity()).isEqualTo(member.getAddress().getCity());
        assertThat(findOrder.getDelivery().getAddress().getCity()).isEqualTo(member.getAddress().getCity());
        assertThat(findOrder.getOrderItemList().size()).isEqualTo(orderItemFormList.size());

        // ?????????????????? ?????? ???
        assertThrows(NotCorrectPaidMoneyException.class,
                () -> orderService.save(orderItemFormList, member.getNum(), paidMoneyMoreThan),
                () -> "1sdfas ????????? ????????? ?????? ????????? ?????????????????????."
                );

        // ?????????????????? ?????? ???
        assertThrows(NotCorrectPaidMoneyException.class,
                () -> orderService.save(orderItemFormList, member.getNum(), paidMoneyLessThan),
                () -> "????????? ????????? ???????????????."
        );

    }

    /**
     * ?????? ?????? ?????? ?????? ?????? v1
     */
    @Test
//    @Rollback(value = false)
    public void ????????????_??????_??????_??????() {
        // given
        // OrderItemFrom ?????? * 3
        OrderItemForm orderItemForm1 = new OrderItemForm(10000, item1.getNum());
        OrderItemForm orderItemForm2 = new OrderItemForm(1, item1.getNum());
        OrderItemForm orderItemForm3 = new OrderItemForm(20000, item2.getNum());
        // ???????????? ??????
        int paidMoneyO = item1.getPrice() * orderItemForm1.getQuantity() + item1.getPrice() * orderItemForm2.getQuantity() + item2.getPrice() * orderItemForm3.getQuantity();
        // ?????????????????? ?????? ???
        int paidMoneyMoreThan = item1.getPrice() * orderItemForm1.getQuantity() + item1.getPrice() * orderItemForm2.getQuantity() + item2.getPrice() * orderItemForm3.getQuantity() + 1;
        // ?????????????????? ?????? ???
        int paidMoneyLessThan = item1.getPrice() * orderItemForm1.getQuantity() + item1.getPrice() * orderItemForm2.getQuantity() + item2.getPrice() * orderItemForm3.getQuantity() - 1;

        List<OrderItemForm> orderItemFormList = new ArrayList<>();

        orderItemFormList.add(orderItemForm1);
        orderItemFormList.add(orderItemForm2);
        orderItemFormList.add(orderItemForm3);
        // when // then
        // ????????????(?????? ????????????)
        // ???????????? ?????? ???
//        orderService.save(orderItemFormList, member.getNum(), paidMoneyO);
        assertThrows(OverflowQuantityException.class, () -> orderService.save(orderItemFormList, member.getNum(), paidMoneyO));
//        fail(item1.getName() + "????????? ???????????? ?????? ??????", NotCorrectPaidMoneyException.class);

        /// item2 ????????? 0??? ??? ???

        // item2 ?????? 0?????? ?????????
//        OrderItemForm orderItemForm5 = new OrderItemForm(20000, item2.getNum());
        List<OrderItemForm> orderItemFormList3 = new ArrayList<>();
//        orderItemFormList3.add(orderItemForm5);
        orderItemFormList3.add(orderItemForm3);
        orderService.save(orderItemFormList3, member.getNum(), item2.getPrice() * orderItemForm3.getQuantity());

        // -> 0?????? ????????????
        OrderItemForm orderItemForm4 = new OrderItemForm(0, item2.getNum());

        List<OrderItemForm> orderItemFormList2 = new ArrayList<>();
        orderItemFormList2.add(orderItemForm4);

        assertThrows(OverflowQuantityException.class, () -> orderService.save(orderItemFormList2, member.getNum(), paidMoneyO));
//        assertThrows(NotCorrectPaidMoneyException.class, () -> orderService.save(orderItemFormList2, member.getNum(), paidMoneyO));// ?????? ??????
    }

    /**
     * ?????? ?????? ?????? v2
     */
    @Test
//    @Rollback(value = false)
    public void ??????_??????_v2_??????() {
        // given
        // OrderItemFrom ?????? * 3
        OrderItemForm orderItemForm1 = new OrderItemForm(2, item1.getNum());
        OrderItemForm orderItemForm2 = new OrderItemForm(6, item1.getNum());
        OrderItemForm orderItemForm3 = new OrderItemForm(10, item2.getNum());
        // ???????????? ??????
        int paidMoneyO = item1.getPrice() * orderItemForm1.getQuantity() + item1.getPrice() * orderItemForm2.getQuantity() + item2.getPrice() * orderItemForm3.getQuantity();
        // ?????????????????? ?????? ???
        int paidMoneyMoreThan = item1.getPrice() * orderItemForm1.getQuantity() + item1.getPrice() * orderItemForm2.getQuantity() + item2.getPrice() * orderItemForm3.getQuantity() + 1;
        // ?????????????????? ?????? ???
        int paidMoneyLessThan = item1.getPrice() * orderItemForm1.getQuantity() + item1.getPrice() * orderItemForm2.getQuantity() + item2.getPrice() * orderItemForm3.getQuantity() - 1;

        // when // then
        // ????????????(?????? ????????????)
        // ???????????? ?????? ???
        int orderNum = orderService.save(member.getNum(), paidMoneyO, orderItemForm1, orderItemForm2, orderItemForm3);

        // Order ????????????
        Order findOrder = orderJpaRepository.findById(orderNum).orElseThrow();

        assertThat(findOrder.getMember().getId()).isEqualTo(member.getId());
        assertThat(findOrder.getDelivery().getAddress().getCity()).isEqualTo(member.getAddress().getCity());
        assertThat(findOrder.getDelivery().getAddress().getCity()).isEqualTo(member.getAddress().getCity());
        assertThat(findOrder.getOrderItemList().size()).isEqualTo(3);

        // ?????????????????? ?????? ???
        assertThrows(NotCorrectPaidMoneyException.class,
                () -> orderService.save(member.getNum(), paidMoneyMoreThan, orderItemForm1, orderItemForm2, orderItemForm3),
                () -> "1sdfas ????????? ????????? ?????? ????????? ?????????????????????."
        );

        // ?????????????????? ?????? ???
        assertThrows(NotCorrectPaidMoneyException.class,
                () -> orderService.save(member.getNum(), paidMoneyLessThan, orderItemForm1, orderItemForm2, orderItemForm3),
                () -> "????????? ????????? ???????????????."
        );

    }

    /**
     * ?????? ?????? ?????? ?????? ?????? v2
     */
    @Test
//    @Rollback(value = false)
    public void ????????????_??????_??????_??????_v2() {
        // given
        // OrderItemFrom ?????? * 3
        OrderItemForm orderItemForm1 = new OrderItemForm(10000, item1.getNum());
        OrderItemForm orderItemForm2 = new OrderItemForm(1, item1.getNum());
        OrderItemForm orderItemForm3 = new OrderItemForm(20000, item2.getNum());
        // ???????????? ??????
        int paidMoneyO = item1.getPrice() * orderItemForm1.getQuantity() + item1.getPrice() * orderItemForm2.getQuantity() + item2.getPrice() * orderItemForm3.getQuantity();
        // ?????????????????? ?????? ???
        int paidMoneyMoreThan = item1.getPrice() * orderItemForm1.getQuantity() + item1.getPrice() * orderItemForm2.getQuantity() + item2.getPrice() * orderItemForm3.getQuantity() + 1;
        // ?????????????????? ?????? ???
        int paidMoneyLessThan = item1.getPrice() * orderItemForm1.getQuantity() + item1.getPrice() * orderItemForm2.getQuantity() + item2.getPrice() * orderItemForm3.getQuantity() - 1;

        // when // then
        // ????????????(?????? ????????????)
        // ???????????? ?????? ???
//        orderService.save(orderItemFormList, member.getNum(), paidMoneyO);
        assertThrows(OverflowQuantityException.class, () -> orderService.save(member.getNum(), paidMoneyO, orderItemForm1, orderItemForm2, orderItemForm3));
//        fail(item1.getName() + "????????? ???????????? ?????? ??????", NotCorrectPaidMoneyException.class);

        /// item2 ????????? 0??? ??? ???

        // item2 ?????? 0?????? ?????????
        orderService.save(member.getNum(), item2.getPrice() * orderItemForm3.getQuantity(), orderItemForm3);

        // -> 0?????? ????????????
        OrderItemForm orderItemForm4 = new OrderItemForm(0, item2.getNum());

        assertThrows(OverflowQuantityException.class, () -> orderService.save(member.getNum(), paidMoneyO, orderItemForm4));
//        assertThrows(NotCorrectPaidMoneyException.class, () -> orderService.save(member.getNum(), paidMoneyO, orderItemForm4));// ?????? ??????
    }

    /**
     * ?????? ?????? ??????
     * -> order status : O(ORDER) -> C(CANCEL) ??????????????? ??????
     */
    @Test
    @Rollback(value = false)
    public void ??????_??????_??????() {
        // given
        // ?????? ??????
        // picture ??????
        List<ItemPicture> itemPictureList = new ArrayList<>();
        itemPictureList.add(ItemPicture.createItemPicture("a", "a"));
        itemPictureList.add(ItemPicture.createItemPicture("a1", "a2"));

        BookForm bookFormTest1 = new BookForm(10000, 1000, "x", "x is...", itemPictureList, "ed", "ok");
        Item item1 = Book.createBook(bookFormTest1, member);

        itemJpaRepository.save(item1);

        // ?????? ??????
        // delivery ??????
        Delivery delivery = Delivery.createDelivery(member);

        // ???????????? ??????
        OrderItem orderItem1 = OrderItem.createOrderItem(3, item1.getPrice(), item1);
        OrderItem orderItem2 = OrderItem.createOrderItem(2, item1.getPrice(), item1);

        // ????????????
        Order order = Order.createOrder(member,
                delivery,
                item1.getPrice() * orderItem1.getQuantity() + item1.getPrice() * orderItem2.getQuantity()
                , orderItem1, orderItem2);

        orderJpaRepository.save(order);

        log.info("?????? ?????? ???, item??? stock : {}", item1.getStock());

        // when
        // ?????? ????????????
        orderService.cancel(order.getNum());

        // ?????? order ????????????
        em.flush();
        em.clear();

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.C);

        log.info("?????? ?????? ???, item??? stock : {}", item1.getStock());

    }

    /**
     * ?????? ?????? ?????? ??????
     */
    @Test
//    @Rollback(value = false)
    public void ??????_??????_??????_??????() {
        // given
        // ?????? ??????
        // picture ??????
        List<ItemPicture> itemPictureList = new ArrayList<>();
        itemPictureList.add(ItemPicture.createItemPicture("a", "a"));
        itemPictureList.add(ItemPicture.createItemPicture("a1", "a2"));

        BookForm bookFormTest1 = new BookForm(10000, 1000, "x", "x is...", itemPictureList, "ed", "ok");
        Item item1 = Book.createBook(bookFormTest1, member);

        itemJpaRepository.save(item1);

        // ?????? ??????
        // delivery ??????
        Delivery delivery = Delivery.createDelivery(member);

        // ???????????? ??????
        OrderItem orderItem1 = OrderItem.createOrderItem(3, item1.getPrice(), item1);
        OrderItem orderItem2 = OrderItem.createOrderItem(2, item1.getPrice(), item1);

        // ????????????
        Order order = Order.createOrder(member,
                delivery,
                item1.getPrice() * orderItem1.getQuantity() + item1.getPrice() * orderItem2.getQuantity()
                , orderItem1, orderItem2);

        orderJpaRepository.save(order);

        // when // then
        // ???????????? -> ??????????????? ?????????
        order.getDelivery().changeStatusRT();
        // ?????? ????????????
        assertThrows(NotCancelOrderException.class, () -> orderService.cancel(order.getNum()));
//        assertThrows(IllegalStateException.class, () -> orderService.cancel(order.getNum()));

        log.info("????????? ??? ??? ?????? v1 item??? stock : {}", item1.getStock());

        // ??????????????? ?????????
        order.getDelivery().changeStatusTC();
//        // ?????? ????????????
        assertThrows(NotCancelOrderException.class, () -> orderService.cancel(order.getNum()));
//        assertThrows(IllegalStateException.class, () -> orderService.cancel(order.getNum()));

        log.info("????????? ??? ??? ?????? v2 item??? stock : {}", item1.getStock());


    }

    /**
     * ?????? ?????? ????????????
     */
    @Test
    @Rollback(value = false)
    public void ??????_??????_????????????() {
        // given
        // ?????? ??????
        // picture ??????
        List<ItemPicture> itemPictureList = new ArrayList<>();
        itemPictureList.add(ItemPicture.createItemPicture("a", "a"));
        itemPictureList.add(ItemPicture.createItemPicture("a1", "a2"));

        BookForm bookFormTest1 = new BookForm(10000, 1000, "x", "x is...", itemPictureList, "ed", "ok");
        Item item1 = Book.createBook(bookFormTest1, member);
        FurnitureForm furnitureFormTest1 = new FurnitureForm(20000, 2000, "y", "y is...", null, "ed");
        Item item2 = Furniture.createFurniture(furnitureFormTest1, member2);

        itemJpaRepository.save(item1);
        itemJpaRepository.save(item2);

        // ?????? ??????
        // delivery ??????
        Delivery delivery = Delivery.createDelivery(member);

        // ???????????? ??????
        OrderItem orderItem1 = OrderItem.createOrderItem(3, item1.getPrice(), item1);
        OrderItem orderItem2 = OrderItem.createOrderItem(2, item1.getPrice(), item1);
        OrderItem orderItem3 = OrderItem.createOrderItem(2, item2.getPrice(), item2);

        // ????????????
        Order order = Order.createOrder(member,
                delivery,
                item1.getPrice() * orderItem1.getQuantity() + item1.getPrice() * orderItem2.getQuantity() + item2.getPrice() * orderItem3.getQuantity(),
                orderItem1, orderItem2, orderItem3);

        orderJpaRepository.save(order);
        // when
        // ?????? ????????????
        OrderDetailForm detailForm = orderService.watchDetail(order.getNum());

        // then
        assertThat(detailForm.getNum()).isEqualTo(order.getNum());
        assertThat(detailForm.getOrderStatus()).isEqualTo("ORDER");
        assertThat(detailForm.getId()).isEqualTo(order.getMember().getId());
        assertThat(detailForm.getMemberPhone().getSelfPhone()).isEqualTo(order.getMember().getPhones().getSelfPhone());
        assertThat(detailForm.getDeliveryStatus()).isEqualTo("READY");
        assertThat(detailForm.getOrderItemDetailList().stream()
                .map(oid -> oid.getNum())
                .collect(toList()))
                .containsExactly(orderItem1.getNum(), orderItem2.getNum(), orderItem3.getNum());
        assertThat(detailForm.getTotalPrice()).isEqualTo(order.getTotalPrice());

    }

    /**
     * ?????? ?????? ?????? ?????? ??????
     * -> ???????????? ???
     * -> R(READY) -> T(TRANSIT)
     */
    @Test
    @Rollback(value = false)
    public void ??????_??????_??????_??????_??????_For_Seller() {
        // given
        // ?????? ??????
        // picture ??????
        List<ItemPicture> itemPictureList = new ArrayList<>();
        itemPictureList.add(ItemPicture.createItemPicture("a", "a"));
        itemPictureList.add(ItemPicture.createItemPicture("a1", "a2"));

        BookForm bookFormTest1 = new BookForm(10000, 1000, "x", "x is...", itemPictureList, "ed", "ok");
        Item item1 = Book.createBook(bookFormTest1, member);
        FurnitureForm furnitureFormTest1 = new FurnitureForm(20000, 2000, "y", "y is...", null, "ed");
        Item item2 = Furniture.createFurniture(furnitureFormTest1, member2);

        itemJpaRepository.save(item1);
        itemJpaRepository.save(item2);

        // ?????? ??????
        // delivery ??????
        Delivery delivery = Delivery.createDelivery(member);

        // ???????????? ??????
        OrderItem orderItem1 = OrderItem.createOrderItem(3, item1.getPrice(), item1);
        OrderItem orderItem2 = OrderItem.createOrderItem(2, item1.getPrice(), item1);
        OrderItem orderItem3 = OrderItem.createOrderItem(2, item2.getPrice(), item2);

        // ????????????
        Order order = Order.createOrder(member,
                delivery,
                item1.getPrice() * orderItem1.getQuantity() + item1.getPrice() * orderItem2.getQuantity() + item2.getPrice() * orderItem3.getQuantity(),
                orderItem1, orderItem2, orderItem3);

        orderJpaRepository.save(order);

        // when
        // ???????????? ??????????????? ??????????????? ?????????
        orderService.changeDeliveryStatusForSeller(order.getNum());

        // then
        assertThat(order.getDelivery().getStatus()).isEqualTo(DeliveryStatus.T);

    }

    /**
     * ?????? ?????? ?????? ?????? ??????
     * -> ???????????? ???
     * -> ???????????? ???????????? ??????
     *    -> NotChangeDeliveryStatusException ?????? ??????
     */
    @Test
//    @Rollback(value = false)
    public void ????????????_??????????????????_??????_??????_??????_??????_??????_??????_For_Seller() {
        // given
        // ?????? ??????
        // picture ??????
        List<ItemPicture> itemPictureList = new ArrayList<>();
        itemPictureList.add(ItemPicture.createItemPicture("a", "a"));
        itemPictureList.add(ItemPicture.createItemPicture("a1", "a2"));

        BookForm bookFormTest1 = new BookForm(10000, 1000, "x", "x is...", itemPictureList, "ed", "ok");
        Item item1 = Book.createBook(bookFormTest1, member);
        FurnitureForm furnitureFormTest1 = new FurnitureForm(20000, 2000, "y", "y is...", null, "ed");
        Item item2 = Furniture.createFurniture(furnitureFormTest1, member2);

        itemJpaRepository.save(item1);
        itemJpaRepository.save(item2);

        // ?????? ??????
        // delivery ??????
        Delivery delivery = Delivery.createDelivery(member);

        // ???????????? ??????
        OrderItem orderItem1 = OrderItem.createOrderItem(3, item1.getPrice(), item1);
        OrderItem orderItem2 = OrderItem.createOrderItem(2, item1.getPrice(), item1);
        OrderItem orderItem3 = OrderItem.createOrderItem(2, item2.getPrice(), item2);

        // ????????????
        Order order = Order.createOrder(member,
                delivery,
                item1.getPrice() * orderItem1.getQuantity() + item1.getPrice() * orderItem2.getQuantity() + item2.getPrice() * orderItem3.getQuantity(),
                orderItem1, orderItem2, orderItem3);

        orderJpaRepository.save(order);

        // when // then
        // ???????????? ????????????
        orderService.cancel(order.getNum());

        // ???????????? ??????????????? ??????????????? ?????????
        assertThrows(NotChangeDeliveryStatusException.class, () -> orderService.changeDeliveryStatusForSeller(order.getNum()));

    }


    /**
     * ?????? ?????? ?????? ?????? ??????
     * -> ???????????? ???
     * -> T(TRANSIT) -> C(COMPLETE)
     */
    @Test
    @Rollback(value = false)
    public void ??????_??????_??????_??????_??????_For_Deliver() {
// given
        // ?????? ??????
        // picture ??????
        List<ItemPicture> itemPictureList = new ArrayList<>();
        itemPictureList.add(ItemPicture.createItemPicture("a", "a"));
        itemPictureList.add(ItemPicture.createItemPicture("a1", "a2"));

        BookForm bookFormTest1 = new BookForm(10000, 1000, "x", "x is...", itemPictureList, "ed", "ok");
        Item item1 = Book.createBook(bookFormTest1, member);
        FurnitureForm furnitureFormTest1 = new FurnitureForm(20000, 2000, "y", "y is...", null, "ed");
        Item item2 = Furniture.createFurniture(furnitureFormTest1, member2);

        itemJpaRepository.save(item1);
        itemJpaRepository.save(item2);

        // ?????? ??????
        // delivery ??????
        Delivery delivery = Delivery.createDelivery(member);

        // ???????????? ??????
        OrderItem orderItem1 = OrderItem.createOrderItem(3, item1.getPrice(), item1);
        OrderItem orderItem2 = OrderItem.createOrderItem(2, item1.getPrice(), item1);
        OrderItem orderItem3 = OrderItem.createOrderItem(2, item2.getPrice(), item2);

        // ????????????
        Order order = Order.createOrder(member,
                delivery,
                item1.getPrice() * orderItem1.getQuantity() + item1.getPrice() * orderItem2.getQuantity() + item2.getPrice() * orderItem3.getQuantity(),
                orderItem1, orderItem2, orderItem3);

        orderJpaRepository.save(order);

        // when
        // ???????????? ??????????????? ??????????????? ?????????
        orderService.changeDeliveryStatusForSeller(order.getNum());

        // ???????????? ???????????? ??????????????? ?????????
        orderService.changeDeliveryStatusForDeliver(order.getNum());

        // then
        assertThat(order.getDelivery().getStatus()).isEqualTo(DeliveryStatus.C);

    }


    /**
     * ?????? ?????? ?????? ?????? ??????
     * -> ???????????? ???
     * -> ???????????? ???????????? ??????
     *    -> NotChangeDeliveryStatusException ?????? ??????
     *
     * -> ????????? ??? ?????? ????????? ????????????!!
     */
//    @Test
////    @Rollback(value = false)
//    public void ????????????_??????????????????_??????_??????_??????_??????_??????_??????_For_Deliver() {
//        // given
//        // ?????? ??????
//        // picture ??????
//        List<ItemPicture> itemPictureList = new ArrayList<>();
//        itemPictureList.add(ItemPicture.createItemPicture("a", "a"));
//        itemPictureList.add(ItemPicture.createItemPicture("a1", "a2"));
//
//        BookForm bookFormTest1 = new BookForm(10000, 1000, "x", "x is...", itemPictureList, "ed", "ok");
//        Item item1 = Book.createBook(bookFormTest1, member);
//        FurnitureForm furnitureFormTest1 = new FurnitureForm(20000, 2000, "y", "y is...", null, "ed");
//        Item item2 = Furniture.createFurniture(furnitureFormTest1, member2);
//
//        itemJpaRepository.save(item1);
//        itemJpaRepository.save(item2);
//
//        // ?????? ??????
//        // delivery ??????
//        Delivery delivery = Delivery.createDelivery(member);
//
//        // ???????????? ??????
//        OrderItem orderItem1 = OrderItem.createOrderItem(3, item1.getPrice(), item1);
//        OrderItem orderItem2 = OrderItem.createOrderItem(2, item1.getPrice(), item1);
//        OrderItem orderItem3 = OrderItem.createOrderItem(2, item2.getPrice(), item2);
//
//        // ????????????
//        Order order = Order.createOrder(member,
//                delivery,
//                item1.getPrice() * orderItem1.getQuantity() + item1.getPrice() * orderItem2.getQuantity() + item2.getPrice() * orderItem3.getQuantity(),
//                orderItem1, orderItem2, orderItem3);
//
//        orderJpaRepository.save(order);
//
//        // when // then
//        // ???????????? ????????????
//        orderService.cancel(order.getNum());
//
//        // ????????? root??????.
//        // ???????????? ???????????? ??????????????? ?????????
//        delivery.changeStatusRT();
//
//        // ???????????? ??????????????? ??????????????? ?????????
//        assertThrows(NotChangeDeliveryStatusException.class, () -> orderService.changeDeliveryStatusForSeller(order.getNum()));
//
//    }


    /**
     * ???????????? ????????? ?????? ?????? ??????
     */
    @Test
    @Rollback(value = false)
    public void ????????????_?????????_??????_??????_??????() {
        // given
        // ?????????, ????????? ?????????
        Pageable pageablePage0Size100 = PageRequest.of(0, 100);
        Pageable pageablePage0Size10 = PageRequest.of(0, 10);
        Pageable pageablePage1Size10 = PageRequest.of(1, 10);
        Pageable pageablePage3Size5 = PageRequest.of(3, 5);
        Pageable pageablePage2Size2 = PageRequest.of(2, 2);

        // ?????? condition
        String today = "2023-02-05";
        String otherDay = "2023-02-06";

        // when
        // ???????????? ??????
        // ????????? member ??? ???
        // ?????? 0
        OrderSearchCond cond0 = new OrderSearchCond("", "", "", "");
        // ?????? 1
        OrderSearchCond cond11 = new OrderSearchCond("", "o", "", "");
        OrderSearchCond cond12 = new OrderSearchCond("", "", "R", "");
        OrderSearchCond cond13 = new OrderSearchCond("", "", "", today);
        // ?????? 2
        OrderSearchCond cond21 = new OrderSearchCond("", "o", "R", "");
        OrderSearchCond cond22 = new OrderSearchCond("", "", "t", today);
        OrderSearchCond cond23 = new OrderSearchCond("", "C", "", today);
        // ?????? 3
        OrderSearchCond cond31 = new OrderSearchCond("", "o", "R", today);

        // ????????? ?????? result??? ?????? ??????
        OrderSearchCond cond14 = new OrderSearchCond("", "ab", "", "");
        OrderSearchCond cond24 = new OrderSearchCond("", "", "Rsdaf", otherDay);
        OrderSearchCond cond25 = new OrderSearchCond("", "sdfsdf", "", today);
        OrderSearchCond cond32 = new OrderSearchCond("", "o", "R", otherDay);

        // then
        // ?????? 1??? ??????
        int remove = 100 / 3 + 1;
        // ?????? ??????(?????????)
        // ?????? 0
        orderTestResult(pageablePage0Size100, cond0, member.getId(), oCount);

        // ?????? 1
        orderTestResult(pageablePage0Size10, cond11, member.getId(), oCount - remove);
        orderTestResult(pageablePage0Size10, cond12, member.getId(), oCount - remove);
        orderTestResult(pageablePage0Size10, cond13, member.getId(), oCount);

        // ?????? 2
        orderTestResult(pageablePage0Size10, cond21, member.getId(), oCount - remove * 2);
        orderTestResult(pageablePage0Size10, cond22, member.getId(), oCount - remove * 2);
        orderTestResult(pageablePage0Size10, cond23, member.getId(), oCount - remove * 2);

        // ?????? 3
        orderTestResult(pageablePage3Size5, cond31, member.getId(), oCount - remove * 2);


        // ????????? ?????? result??? ?????? ??????
        orderTestResult(pageablePage0Size10, cond14, member.getId(), oCount);
        orderTestResult(pageablePage0Size10, cond24, member.getId(), 0);
        orderTestResult(pageablePage0Size10, cond25, member.getId(), oCount);
        orderTestResult(pageablePage0Size10, cond32, member.getId(), 0);

    }

    /**
     * ????????????_??????_??????_??????_?????????
     *  -> test ?????? ??????
     */
    private void orderTestResult(Pageable pageable, OrderSearchCond cond, String memberId, int count) {
        // page ????????????
        Page<OrderListForm> orderPage = orderService.selectList(cond, pageable, memberId);
        // ?????? ????????? ??????
        assertThat(orderPage.getTotalElements()).isEqualTo(count);
    }


//    -------------------------methods using for user(seller) start ----------------------------------

    /**
     * ???????????? ????????? ?????? ?????? ??????
     */
    @Test
    @Rollback(value = false)
    public void ????????????_?????????_??????_??????_??????() {
        // given
        // ?????????, ????????? ?????????
        Pageable pageablePage0Size100 = PageRequest.of(0, 100);
        Pageable pageablePage0Size10 = PageRequest.of(0, 10);
        Pageable pageablePage1Size10 = PageRequest.of(1, 10);
        Pageable pageablePage3Size5 = PageRequest.of(3, 5);
        Pageable pageablePage2Size2 = PageRequest.of(2, 2);

        // ?????? condition
        String today = "2023-02-05";
        String otherDay = "2023-02-06";

        // when
        // when
        // ?????? ??????
        // ???????????? member ??? ???
        // ?????? 0
        OrderItemSearchCond cond0 = new OrderItemSearchCond(0, "", "", "", "");
        // ?????? 1
        OrderItemSearchCond cond11 = new OrderItemSearchCond(0, "b", "", "", "");
        OrderItemSearchCond cond12 = new OrderItemSearchCond(0, "", "o", "", "");
        OrderItemSearchCond cond13 = new OrderItemSearchCond(0, "", "", "R", "");
        OrderItemSearchCond cond14 = new OrderItemSearchCond(0, "", "", "", today);
        // ?????? 2
        OrderItemSearchCond cond21 = new OrderItemSearchCond(0, "a", "O", "", "");
        OrderItemSearchCond cond22 = new OrderItemSearchCond(0, "a", "", "R", "");
        OrderItemSearchCond cond23 = new OrderItemSearchCond(0, "a", "", "", otherDay);
        OrderItemSearchCond cond24 = new OrderItemSearchCond(0, "", "C", "R", "");
        OrderItemSearchCond cond25 = new OrderItemSearchCond(0, "", "c", "", today);
        OrderItemSearchCond cond26 = new OrderItemSearchCond(0, "", "", "t", today);
        // ?????? ??????
        OrderItemSearchCond cond27 = new OrderItemSearchCond(0, "", "", "c", today);

        // ?????? 3
        OrderItemSearchCond cond31 = new OrderItemSearchCond(0, "b", "o", "T", "");
        OrderItemSearchCond cond32 = new OrderItemSearchCond(0, "b", "O", "", today);
        OrderItemSearchCond cond33 = new OrderItemSearchCond(0, "a", "", "t", today);
        OrderItemSearchCond cond34 = new OrderItemSearchCond(0, "", "O", "R", today);
        // ?????? ??? ??? ?????????
        OrderItemSearchCond cond36 = new OrderItemSearchCond(0, "cd", "O", "", today);

        // ?????? 4
        OrderItemSearchCond cond41 = new OrderItemSearchCond(0, "a", "o", "T", today);

        // ?????? ?????? ????????? ???
        OrderItemSearchCond cond1 = new OrderItemSearchCond(0, "ok12", "", "", "");
        OrderItemSearchCond cond15 = new OrderItemSearchCond(0, "", "asdf", "", today);
        OrderItemSearchCond cond28 = new OrderItemSearchCond(0, "", "asdf", "sdfas", "");
        OrderItemSearchCond cond35 = new OrderItemSearchCond(0, "", "O", "Tasd", today);
        OrderItemSearchCond cond42 = new OrderItemSearchCond(0, "aefe", "o", "T", today);


        // then
        // ???????????? member ??? ???  ????????? : 0 item1 C(CANCEL) 1???, T(TRANSIT) 1???,
        //                      ????????? : 1 item1 C(CANCEL) 1???
        // ?????? 0
        sellerTestResult(cond0, pageablePage0Size10, iaCount + iaCount2);
        // ?????? 1
        sellerTestResult(cond11, pageablePage1Size10, iaCount2);
        sellerTestResult(cond12, pageablePage1Size10, iaCount / 2 + iaCount2 / 2);
        sellerTestResult(cond13, pageablePage1Size10, iaCount / 2 + iaCount2);
        sellerTestResult(cond14, pageablePage1Size10, iaCount + iaCount2);
        // ?????? 2
        sellerTestResult(cond21, pageablePage0Size100, iaCount / 2);
        sellerTestResult(cond22, pageablePage0Size100, iaCount / 2);
        sellerTestResult(cond23, pageablePage0Size100, 0);
        sellerTestResult(cond24, pageablePage0Size100, iaCount / 2 + iaCount2 / 2);
        sellerTestResult(cond25, pageablePage0Size100, iaCount / 2 + iaCount2 / 2);
        sellerTestResult(cond26, pageablePage0Size100, iaCount / 2);
        // ?????? ??????
        sellerTestResult(cond27, pageablePage0Size100, 0);

        // ?????? 3
        sellerTestResult(cond31, pageablePage0Size100, 0);
        sellerTestResult(cond32, pageablePage0Size100, iaCount2 / 2);
        sellerTestResult(cond33, pageablePage0Size100, iaCount / 2);
        sellerTestResult(cond34, pageablePage0Size100, iaCount2 / 2);
        // ?????? ??? ??? ?????????
        sellerTestResult(cond36, pageablePage0Size100, 0);

        // ?????? 4
        sellerTestResult(cond41, pageablePage2Size2, iaCount / 2);

        // ?????? ?????? ????????? ???
        sellerTestResult(cond1, pageablePage0Size10, 0);
        sellerTestResult(cond15, pageablePage1Size10, iaCount + iaCount2); // status??? ?????? ????????? ??? null ?????? ?????? ??????????
        sellerTestResult(cond28, pageablePage1Size10, iaCount + iaCount2); // status??? ?????? ????????? ??? null ?????? ?????? ??????????
        sellerTestResult(cond35, pageablePage1Size10, iaCount / 2 + iaCount2 / 2); // status??? ?????? ????????? ??? null ?????? ?????? ??????????
        sellerTestResult(cond42, pageablePage2Size2, 0); // status??? ?????? ????????? ??? null ?????? ?????? ??????????
    }

    /**
     * ????????????_?????????_??????_??????_?????????
     *  -> test ?????? ??????
     */
    private void sellerTestResult(OrderItemSearchCond cond, Pageable pageable, int count) {
        // page ????????????
        Page<OrderItemListForm> orderItemPage = orderService.selectOrderItemList(cond, pageable, member.getNum());
        // ?????? ????????? ??????
        assertThat(orderItemPage.getTotalElements()).isEqualTo(count);
    }

//    -------------------------methods using for user(seller) end ----------------------------------


//    -------------------------methods using for admin, deliver start----------------------------------

    /**
     * ?????????, ???????????? ????????? ?????? ?????? ??????
     */
    @Test
    @Rollback(value = false)
    public void ????????????_?????????_??????_??????_??????() {
        // given
        // ?????????, ????????? ?????????
        Pageable pageablePage0Size100 = PageRequest.of(0, 100);
        Pageable pageablePage0Size10 = PageRequest.of(0, 10);
        Pageable pageablePage1Size10 = PageRequest.of(1, 10);
        Pageable pageablePage3Size5 = PageRequest.of(3, 5);
        Pageable pageablePage2Size2 = PageRequest.of(2, 2);

        // ?????? condition
        String today = "2023-02-05";
        String otherDay = "2023-02-06";

        // when
        // ???????????? ??????
        // ?????? 0
        OrderSearchCond cond0 = new OrderSearchCond("", "", "", "");
        // ?????? 1
        OrderSearchCond cond11 = new OrderSearchCond("cd", "", "", "");
        OrderSearchCond cond12 = new OrderSearchCond("", "o", "", "");
        OrderSearchCond cond13 = new OrderSearchCond("", "", "R", "");
        OrderSearchCond cond14 = new OrderSearchCond("", "", "", today);
        // ?????? 2
        OrderSearchCond cond21 = new OrderSearchCond("b", "o", "", "");
        OrderSearchCond cond22 = new OrderSearchCond("b", "", "T", "");
        OrderSearchCond cond23 = new OrderSearchCond("b", "", "", today);
        OrderSearchCond cond24 = new OrderSearchCond("", "c", "R", "");
        OrderSearchCond cond25 = new OrderSearchCond("", "O", "", today);
        OrderSearchCond cond26 = new OrderSearchCond("", "", "R", today);
        // ?????? 3
        OrderSearchCond cond31 = new OrderSearchCond("a", "o", "R", "");
        OrderSearchCond cond32 = new OrderSearchCond("a", "O", "", today);
        OrderSearchCond cond33 = new OrderSearchCond("a", "", "R", today);
        OrderSearchCond cond34 = new OrderSearchCond("", "o", "C", today);
        // ?????? 4
        OrderSearchCond cond41 = new OrderSearchCond("a", "o", "R", today);

        // ????????? ?????? result??? ?????? ??????
        OrderSearchCond cond15 = new OrderSearchCond("", "ab", "", "");
        OrderSearchCond cond27 = new OrderSearchCond("", "", "Rsdaf", otherDay);
        OrderSearchCond cond35 = new OrderSearchCond("asd", "sdfsdf", "", today);
        OrderSearchCond cond42 = new OrderSearchCond("a", "o", "Rsdf", today);

        // then
        // ?????? 1??? ??????
        int remove1 = 100 / 3 + 1;
        int remove2 = 100 / 3;
        int remove3 = 100 / 3;
        // ?????? ??????(?????????)
        // ?????? 0
        adminTestResult(pageablePage0Size100, cond0, oCount + oCount2 + oCount3);

        // ?????? 1
        adminTestResult(pageablePage0Size10, cond11, oCount3);
        adminTestResult(pageablePage0Size10, cond12, oCount + oCount2 + oCount3 - remove1 - remove2);
        adminTestResult(pageablePage0Size10, cond13, oCount + oCount2 + oCount3 - remove1);
        adminTestResult(pageablePage0Size10, cond14, oCount + oCount2 + oCount3);

        // ?????? 2
        adminTestResult(pageablePage0Size10, cond21, oCount2 - remove2);
        adminTestResult(pageablePage0Size10, cond22, 0);
        adminTestResult(pageablePage0Size10, cond23, oCount2);
        adminTestResult(pageablePage0Size10, cond24, oCount - remove1 - remove1 + oCount2 - remove2);
        adminTestResult(pageablePage0Size10, cond25, oCount + oCount2 + oCount3 - remove1 - remove2);
        adminTestResult(pageablePage0Size10, cond26, oCount + oCount2 + oCount3 - remove1);

        // ?????? 3
        adminTestResult(pageablePage3Size5, cond31, oCount - remove1 - remove1);
        adminTestResult(pageablePage3Size5, cond32, oCount - remove1);
        adminTestResult(pageablePage3Size5, cond33, oCount - remove1);
        adminTestResult(pageablePage3Size5, cond34, 0);

        // ?????? 4
        adminTestResult(pageablePage3Size5, cond41, oCount - remove1 * 2);

        // ????????? ?????? result??? ?????? ??????
        adminTestResult(pageablePage0Size10, cond15, oCount + oCount2 + oCount3);
        adminTestResult(pageablePage0Size10, cond27, 0);
        adminTestResult(pageablePage0Size10, cond35, 0);
        adminTestResult(pageablePage0Size10, cond42, oCount - remove1);

    }

    /**
     * ?????????, ????????????_??????_??????_??????_?????????
     *  -> test ?????? ??????
     */
    private void adminTestResult(Pageable pageable, OrderSearchCond cond, int count) {
        // page ????????????
        Page<OrderListFormForAdmin> orderPage = orderService.selectListForAdmin(cond, pageable);
        // ?????? ????????? ??????
        assertThat(orderPage.getTotalElements()).isEqualTo(count);
    }

//    -------------------------methods using for admin, deliver end ----------------------------------


}
