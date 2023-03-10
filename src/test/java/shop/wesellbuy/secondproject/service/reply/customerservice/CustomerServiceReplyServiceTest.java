package shop.wesellbuy.secondproject.service.reply.customerservice;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import shop.wesellbuy.secondproject.domain.CustomerService;
import shop.wesellbuy.secondproject.domain.Member;
import shop.wesellbuy.secondproject.domain.member.SelfPicture;
import shop.wesellbuy.secondproject.domain.reply.CustomerServiceReply;
import shop.wesellbuy.secondproject.repository.customerservice.CustomerServiceJpaRepository;
import shop.wesellbuy.secondproject.repository.member.MemberJpaRepository;
import shop.wesellbuy.secondproject.repository.reply.customerservice.CustomerServiceReplyJpaRepository;
import shop.wesellbuy.secondproject.repository.reply.customerservice.CustomerServiceReplySearchCond;
import shop.wesellbuy.secondproject.service.customerservice.CustomerServiceService;
import shop.wesellbuy.secondproject.web.customerservice.CustomerServiceDetailForm;
import shop.wesellbuy.secondproject.web.member.MemberForm;
import shop.wesellbuy.secondproject.web.reply.ReplyDetailForm;
import shop.wesellbuy.secondproject.web.reply.ReplyForm;
import shop.wesellbuy.secondproject.web.reply.ReplyUpdateForm;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Slf4j
public class CustomerServiceReplyServiceTest {

    @Autowired
    MemberJpaRepository memberJpaRepository;
    @Autowired
    CustomerServiceJpaRepository customerServiceJpaRepository;
    @Autowired
    CustomerServiceReplyService customerServiceReplyService;
    @Autowired
    CustomerServiceReplyJpaRepository customerServiceReplyJpaRepository;

    Member member; // test ??????
    Member member2; // test ??????
    Member member3; // test ??????

    CustomerService customerService;// test ???????????????
    CustomerService customerService2;// test ???????????????
    CustomerService customerService3;// test ???????????????

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

        // ??????????????? ??????
        CustomerService customerService1 = CustomerService.createCustomerService("b1", "????????? ????????????!", member);
        CustomerService customerService2 = CustomerService.createCustomerService("c1", "????????????!", member2);
        CustomerService customerService3 = CustomerService.createCustomerService("a1", "????????? ?????? ?????????", member3);

        customerServiceJpaRepository.save(customerService1);
        customerServiceJpaRepository.save(customerService2);
        customerServiceJpaRepository.save(customerService3);

        this.customerService = customerService1;
        this.customerService2 = customerService2;
        this.customerService3 = customerService3;

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
        int replyNum = customerServiceReplyService.save(replyForm, member.getNum(), customerService2.getNum());
        // then
        CustomerServiceReply result = customerServiceReplyJpaRepository.findById(replyNum).orElseThrow();

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
        int replyNum = customerServiceReplyService.save(replyForm, member.getNum(), customerService2.getNum());

        // when
        // ?????? ??????
        ReplyUpdateForm replyUpdateForm = new ReplyUpdateForm(replyNum, "????????????!");

        customerServiceReplyService.update(replyUpdateForm);
        // then
        CustomerServiceReply findReply = customerServiceReplyJpaRepository.findById(replyNum).orElseThrow();

        // ?????? ??? content
        assertThat(findReply.getContent()).isEqualTo(replyUpdateForm.getContent());
        // ?????? ??? content
        assertThat(findReply.getContent()).isNotEqualTo(replyForm.getContent());
    }

    /**
     * ?????? ?????? ??????
     * -> status : R -> D??? ??????
     * -> ??????????????? ????????? ???????????? ?????? ??????(??? ???????????????) : CustomerServiceService
     */
    @Test
//    @Rollback(value = false)
    public void ??????_??????_??????(@Autowired CustomerServiceService customerServiceService,
                         @Autowired EntityManager em) {
        // given
        // ?????? ??????
        ReplyForm replyForm = new ReplyForm("????????????~");
        int replyNum = customerServiceReplyService.save(replyForm, member.getNum(), customerService3.getNum());
        ReplyForm replyForm2 = new ReplyForm("????????????~");
        int replyNum2 = customerServiceReplyService.save(replyForm2, member2.getNum(), customerService3.getNum());
        ReplyForm replyForm3 = new ReplyForm("????????????~");
        int replyNum3 = customerServiceReplyService.save(replyForm3, member.getNum(), customerService3.getNum());
        // when
        customerServiceReplyService.delete(replyNum);

        // betch size ??????????????? ????????????
        em.flush();
        em.clear();

        // then
        // CustomerServiceService?????? ??????
        CustomerServiceDetailForm detailForm = customerServiceService.watchDetail(customerService3.getNum());

        // ????????? R -> D??? ???????????????
        // ????????? 3????????? 2?????? ????????????.
        assertThat(detailForm.getReplyList().size()).isEqualTo(2);
        assertThat(detailForm.getReplyList().size()).isNotEqualTo(3);
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
        CustomerServiceReply customerServiceReply = null;

        for(int i = 0; i < amount; i++) {
            if(i % 3 == 1) {
                replyForm = new ReplyForm("????????????");
                customerServiceReply = CustomerServiceReply.createCustomerServiceReply(replyForm, member, customerService);
                rCount += 1;
            } else if(i * 3 == 2) {
                replyForm = new ReplyForm("?????????~~~~~");
                customerServiceReply = CustomerServiceReply.createCustomerServiceReply(replyForm, member2, customerService);
                r2Count += 1;
            } else {
                replyForm = new ReplyForm("????????????~~~~~");
                customerServiceReply = CustomerServiceReply.createCustomerServiceReply(replyForm, member3, customerService2);
                r3Count += 1;
            }
            // ????????????
            customerServiceReplyJpaRepository.save(customerServiceReply);

            // ????????????
            if(i % 4 == 0) {
                customerServiceReply.delete();
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
        CustomerServiceReplySearchCond cond0 = new CustomerServiceReplySearchCond("", "", "");
        // ?????? 1
        CustomerServiceReplySearchCond cond11 = new CustomerServiceReplySearchCond("a1", "", "");
        CustomerServiceReplySearchCond cond12 = new CustomerServiceReplySearchCond("", "???~~", "");
        CustomerServiceReplySearchCond cond13 = new CustomerServiceReplySearchCond("", "", today);
        // ?????? 2
        CustomerServiceReplySearchCond cond21 = new CustomerServiceReplySearchCond("b1", "??????", "");
        CustomerServiceReplySearchCond cond22 = new CustomerServiceReplySearchCond("b1", "", today);
        CustomerServiceReplySearchCond cond23 = new CustomerServiceReplySearchCond("", "???~", today);
        // ?????? 3
        CustomerServiceReplySearchCond cond31 = new CustomerServiceReplySearchCond("c1", "????????????", today);


        // ????????? ?????? ????????? ???
        CustomerServiceReplySearchCond cond14 = new CustomerServiceReplySearchCond("a123", "", "");
        CustomerServiceReplySearchCond cond24 = new CustomerServiceReplySearchCond("b123", "", today);
        CustomerServiceReplySearchCond cond32 = new CustomerServiceReplySearchCond("c1", "????????????!", today);

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
    private void testResultForAdmin(CustomerServiceReplySearchCond cond, Pageable pageable, int count) {
        Page<ReplyDetailForm> result = customerServiceReplyService.selectListForAdmin(cond, pageable);
        assertThat(result.getTotalElements()).isEqualTo(count);
    }

//    -------------------------methods using for admin admin----------------------------------

}
