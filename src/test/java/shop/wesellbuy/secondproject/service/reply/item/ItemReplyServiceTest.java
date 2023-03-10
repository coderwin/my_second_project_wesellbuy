package shop.wesellbuy.secondproject.service.reply.item;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import shop.wesellbuy.secondproject.domain.Item;
import shop.wesellbuy.secondproject.domain.Member;
import shop.wesellbuy.secondproject.domain.item.Book;
import shop.wesellbuy.secondproject.domain.item.Furniture;
import shop.wesellbuy.secondproject.domain.item.HomeAppliances;
import shop.wesellbuy.secondproject.domain.item.ItemPicture;
import shop.wesellbuy.secondproject.domain.member.SelfPicture;
import shop.wesellbuy.secondproject.domain.reply.ItemReply;
import shop.wesellbuy.secondproject.domain.reply.ReplyStatus;
import shop.wesellbuy.secondproject.repository.item.ItemJpaRepository;
import shop.wesellbuy.secondproject.repository.member.MemberJpaRepository;
import shop.wesellbuy.secondproject.repository.reply.item.ItemReplyJpaRepository;
import shop.wesellbuy.secondproject.repository.reply.item.ItemReplySearchCond;
import shop.wesellbuy.secondproject.web.item.BookForm;
import shop.wesellbuy.secondproject.web.item.FurnitureForm;
import shop.wesellbuy.secondproject.web.item.HomeAppliancesForm;
import shop.wesellbuy.secondproject.web.member.MemberForm;
import shop.wesellbuy.secondproject.web.reply.ReplyDetailForm;
import shop.wesellbuy.secondproject.web.reply.ReplyForm;
import shop.wesellbuy.secondproject.web.reply.ReplyUpdateForm;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Slf4j
public class ItemReplyServiceTest {

    @Autowired
    MemberJpaRepository memberJpaRepository;
    @Autowired
    ItemJpaRepository itemJpaRepository;
    @Autowired
    ItemReplyService itemReplyService;
    @Autowired
    ItemReplyJpaRepository itemReplyJpaRepository;

    Member member; // test ??????
    Member member2; // test ??????
    Member member3; // test ??????

    Item item; // test ??????
    Item item2; // test ??????
    Item item3; // test ??????
    @BeforeEach
    public void init() {

        log.info("test init ??????");

        // ?????? ??????
        SelfPicture selfPicture = SelfPicture.createSelfPicture("test1", "test2");
        SelfPicture selfPicture3 = SelfPicture.createSelfPicture("test1", "test2");
        MemberForm memberForm1 = new MemberForm("a", "a1", "123", "a", "a@a", "01012341234", "0511231234", "korea", "b", "h", "h", selfPicture);
        Member member = Member.createMember(memberForm1);
        MemberForm memberForm2 = new MemberForm("a", "b1", "123", "a", "a@a", "01012341234", "0511231234", "korea", "b", "h", "h", null);
        Member member2 = Member.createMember(memberForm2);
        MemberForm memberForm3 = new MemberForm("a", "c1", "123", "a", "a@a", "01012341234", "0511231234", "korea", "b", "h", "h", selfPicture3);
        Member member3 = Member.createMember(memberForm3);

        memberJpaRepository.save(member);
        memberJpaRepository.save(member2);
        memberJpaRepository.save(member3);
        this.member = member;
        this.member2 = member2;
        this.member3 = member3;

        // ?????? ??????
        List<ItemPicture> itemPictureList = new ArrayList<>();
        itemPictureList.add(ItemPicture.createItemPicture("a", "a"));
        itemPictureList.add(ItemPicture.createItemPicture("a1", "a2"));

        HomeAppliancesForm homeAppliancesForm = new HomeAppliancesForm(20, 5000, "?????????", "?????? ?????? ??????", new ArrayList<>(), "samsung");
        Item item = HomeAppliances.createHomeAppliances(homeAppliancesForm, member2);
        FurnitureForm furnitureForm = new FurnitureForm(10, 2000, "??????", "??? ??????????????????~", itemPictureList, "hansem");
        Item item2 = Furniture.createFurniture(furnitureForm, member);
        BookForm bookForm = new BookForm(10, 1000, "book1", "x is...", itemPictureList, "ed", "ok");
        Item item3 = Book.createBook(bookForm, member);

        itemJpaRepository.save(item);
        itemJpaRepository.save(item2);
        itemJpaRepository.save(item3);

        this.item = item;
        this.item2 = item2;
        this.item3 = item3;

        log.info("test init ???");

    }

    /**
     * ?????? ?????? ??????
     */
    @Test
//    @Rollback(value = false)
    public void ??????_??????_??????() {
        // given
        ReplyForm replyForm = new ReplyForm("????????????~");
        // when
        // ????????????
        int replyNum = itemReplyService.save(replyForm, member.getNum(), item.getNum());

        // then
        ItemReply result = itemReplyJpaRepository.findById(replyNum).orElseThrow();

        assertThat(result.getNum()).isEqualTo(replyNum);
        assertThat(result.getContent()).isEqualTo(replyForm.getContent());
    }

    /**
     * ?????? ?????? ??????
     */
    @Test
//    @Rollback(value = false)
    public void ??????_??????_??????() {
        // given
        // ?????? ??????
        ReplyForm replyForm = new ReplyForm("????????????~");
        int replyNum = itemReplyService.save(replyForm, member.getNum(), item.getNum());

        // when
        // ?????? ??????
        ReplyUpdateForm replyUpdateForm = new ReplyUpdateForm(replyNum, "?????? ????????????~");

        itemReplyService.update(replyUpdateForm);
        // then
        ItemReply findReply = itemReplyJpaRepository.findById(replyNum).orElseThrow();

        // ?????? ?????? content
        assertThat(findReply.getContent()).isEqualTo(replyUpdateForm.getContent());
        // ?????? ?????? content
        assertThat(findReply.getContent()).isNotEqualTo(replyForm.getContent());
    }

    /**
     * ?????? ?????? ??????
     * -> status : R -> D??? ??????
     * -> ?????? ????????? ???????????? ?????? ??????(??? ???????????????) : ItemService
     */
    @Test
//    @Rollback(value = false)
    public void ??????_??????_??????() {
        // given
        ReplyForm replyForm = new ReplyForm("????????????~");
        int replyNum = itemReplyService.save(replyForm, member3.getNum(), item2.getNum());

        // ?????? ?????? ??????(R)
        ItemReply findItemReply1 = itemReplyJpaRepository.findById(replyNum).orElseThrow();

        assertThat(findItemReply1.getStatus()).isEqualTo(ReplyStatus.R);

        // when
        itemReplyService.delete(replyNum);

        // then
        ItemReply findItemReply2 = itemReplyJpaRepository.findById(replyNum).orElseThrow();

        assertThat(findItemReply2.getStatus()).isEqualTo(ReplyStatus.D);
    }

//    -------------------------methods using for admin start----------------------------------

    /**
     * ????????? ?????? ?????? ?????? ???????????? ??????
     * -> ?????????(admin)??? ??????
     */
    @Test
//    @Rollback(value = false)
    public void ?????????_??????_??????_????????????() {
        // given
        // ?????? ????????? ??????
        int amount = 100;
        var rCount = 0; // ?????? ??????
        var r2Count = 0; // ?????? ??????
        var r3Count = 0; // ?????? ??????
        ReplyForm replyForm = null;
        ItemReply itemReply = null;
        for(int i = 0; i < amount; i++) {
            if(i % 3 == 1) {
                replyForm = new ReplyForm("????????????");
                itemReply = ItemReply.createItemReply(replyForm, member, item);
                rCount += 1;
            } else if(i * 3 == 2) {
                replyForm = new ReplyForm("?????????~~~~~");
                itemReply = ItemReply.createItemReply(replyForm, member2, item);
                r2Count += 1;
            } else {
                replyForm = new ReplyForm("????????????~~~~~");
                itemReply = ItemReply.createItemReply(replyForm, member3, item2);
                r3Count += 1;
            }

            itemReplyJpaRepository.save(itemReply);

            // ????????????
            if(i % 4 == 0) {
                itemReply.delete();
            }
        }

        // ????????? ??????
        Pageable page0size10 = PageRequest.of(0, 10);
        Pageable page2size10 = PageRequest.of(0, 10);

        // ?????? ??????
        String today = "2023-02-02";
        String otherDay = "2023-02-03";

        // when
        // ?????? 0
        ItemReplySearchCond cond0 = new ItemReplySearchCond("", "", "");
        // ?????? 1
        ItemReplySearchCond cond11 = new ItemReplySearchCond("a1", "", "");
        ItemReplySearchCond cond12 = new ItemReplySearchCond("", "???~~", "");
        ItemReplySearchCond cond13 = new ItemReplySearchCond("", "", today);
        // ?????? 2
        ItemReplySearchCond cond21 = new ItemReplySearchCond("b1", "??????", "");
        ItemReplySearchCond cond22 = new ItemReplySearchCond("b1", "", today);
        ItemReplySearchCond cond23 = new ItemReplySearchCond("", "???~", today);
        // ?????? 3
        ItemReplySearchCond cond31 = new ItemReplySearchCond("c1", "????????????", today);


        // ????????? ?????? ????????? ???
        ItemReplySearchCond cond14 = new ItemReplySearchCond("a123", "", "");
        ItemReplySearchCond cond24 = new ItemReplySearchCond("b123", "", today);
        ItemReplySearchCond cond32 = new ItemReplySearchCond("c1", "????????????!", today);

        // then
        // ?????? 0
        testResultForAdmin(cond0, page0size10, rCount + r2Count + r3Count);
        // ?????? 1
        testResultForAdmin(cond11, page2size10, rCount);
        testResultForAdmin(cond12, page2size10, r2Count + r3Count);
        testResultForAdmin(cond13, page2size10, rCount + r2Count + r3Count);
        // ?????? 2
        testResultForAdmin(cond21, page0size10,  r2Count);
        testResultForAdmin(cond22, page0size10, r2Count);
        testResultForAdmin(cond23, page0size10, r2Count + r3Count);
        // ?????? 3
        testResultForAdmin(cond31, page2size10,  r3Count);

        // ????????? ?????? ????????? ???
        testResultForAdmin(cond14, page2size10, 0);
        testResultForAdmin(cond24, page0size10, 0);
        testResultForAdmin(cond32, page2size10,  0);

    }

    /**
     * test ????????? ??????
     */
    private void testResultForAdmin(ItemReplySearchCond cond, Pageable pageable, int count) {
        Page<ReplyDetailForm> result = itemReplyService.selectListForAdmin(cond, pageable);
        assertThat(result.getTotalElements()).isEqualTo(count);
    }


//    -------------------------methods using for admin admin----------------------------------

}
