package shop.wesellbuy.secondproject.service.reply.recommendation;

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
import shop.wesellbuy.secondproject.domain.Member;
import shop.wesellbuy.secondproject.domain.Recommendation;
import shop.wesellbuy.secondproject.domain.member.SelfPicture;
import shop.wesellbuy.secondproject.domain.recommendation.RecommendationPicture;
import shop.wesellbuy.secondproject.domain.reply.RecommendationReply;
import shop.wesellbuy.secondproject.repository.member.MemberJpaRepository;
import shop.wesellbuy.secondproject.repository.recommendation.RecommendationJpaRepository;
import shop.wesellbuy.secondproject.repository.reply.recommendation.RecommendationReplyJpaRepository;
import shop.wesellbuy.secondproject.repository.reply.recommendation.RecommendationReplySearchCond;
import shop.wesellbuy.secondproject.service.recommendation.RecommendationService;
import shop.wesellbuy.secondproject.web.member.MemberForm;
import shop.wesellbuy.secondproject.web.recommendation.RecommendationDetailForm;
import shop.wesellbuy.secondproject.web.recommendation.RecommendationForm;
import shop.wesellbuy.secondproject.web.reply.ReplyDetailForm;
import shop.wesellbuy.secondproject.web.reply.ReplyForm;
import shop.wesellbuy.secondproject.web.reply.ReplyUpdateForm;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Slf4j
public class RecommendationReplyServiceTest {

    @Autowired
    MemberJpaRepository memberJpaRepository;
    @Autowired
    RecommendationJpaRepository recommendationJpaRepository;
    @Autowired
    RecommendationReplyService recommendationReplyService;
    @Autowired
    RecommendationReplyJpaRepository recommendationReplyJpaRepository;




    Member member; // test ??????
    Member member2; // test ??????
    Member member3; // test ??????

    Recommendation recommendation; // test ??????????????????
    Recommendation recommendation2; // test ??????????????????
    Recommendation recommendation3; // test ??????????????????

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

        /// ?????????????????? ??????
        // ????????? ??????
        List<RecommendationPicture> rpList = new ArrayList<>();
        rpList.add(RecommendationPicture.createRecommendationPicture("x", "x"));
        rpList.add(RecommendationPicture.createRecommendationPicture("y", "y"));
        List<RecommendationPicture> rpList2 = new ArrayList<>();
        rpList2.add(RecommendationPicture.createRecommendationPicture("x", "x"));
        rpList2.add(RecommendationPicture.createRecommendationPicture("y", "y"));
        // ?????????????????? ?????????
        RecommendationForm recommendationForm = new RecommendationForm("y1", "b", "ok", rpList);
        Recommendation recommendation = Recommendation.createRecommendation(recommendationForm, member);
        RecommendationForm recommendationForm2 = new RecommendationForm("y1", "b", "ok", new ArrayList<>());
        Recommendation recommendation2 = Recommendation.createRecommendation(recommendationForm2, member2);
        RecommendationForm recommendationForm3 = new RecommendationForm("y1", "b", "ok", rpList2);
        Recommendation recommendation3 = Recommendation.createRecommendation(recommendationForm3, member3);

        recommendationJpaRepository.save(recommendation);
        recommendationJpaRepository.save(recommendation2);
        recommendationJpaRepository.save(recommendation3);

        this.recommendation = recommendation;
        this.recommendation2 = recommendation2;
        this.recommendation3 = recommendation3;

        log.info("test init ???");

    }

    /**
     * ?????? ?????? ??????
     */
    @Test
//    @Rollback(value = false)
    public void ??????_??????_??????() {
        // given
        // given
        ReplyForm replyForm = new ReplyForm("????????????~");
        // when
        int replyNum = recommendationReplyService.save(replyForm, member.getNum(), recommendation3.getNum());
        // then
        RecommendationReply findReply = recommendationReplyJpaRepository.findById(replyNum).orElseThrow();

        assertThat(findReply.getNum()).isEqualTo(replyNum);
        assertThat(findReply.getContent()).isEqualTo(replyForm.getContent());
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
        int replyNum = recommendationReplyService.save(replyForm, member.getNum(), recommendation3.getNum());

        // when
        // ?????? ??????
        ReplyUpdateForm replyUpdateForm = new ReplyUpdateForm(replyNum, "????????????!");

        recommendationReplyService.update(replyUpdateForm);

        // then
        RecommendationReply findReply = recommendationReplyJpaRepository.findById(replyNum).orElseThrow();

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
    public void ??????_??????_??????(@Autowired RecommendationService recommendationService,
                         @Autowired EntityManager em) {

        // given
        // ?????? ??????
        ReplyForm replyForm = new ReplyForm("????????????~");
        int replyNum = recommendationReplyService.save(replyForm, member.getNum(), recommendation3.getNum());
        ReplyForm replyForm2 = new ReplyForm("????????????~");
        int replyNum2 = recommendationReplyService.save(replyForm2, member2.getNum(), recommendation3.getNum());
        ReplyForm replyForm3 = new ReplyForm("????????????~");
        int replyNum3 = recommendationReplyService.save(replyForm3, member.getNum(), recommendation3.getNum());
        // when
        recommendationReplyService.delete(replyNum);

        // betch size ??????????????? ????????????
        em.flush();
        em.clear();

        // then
        // RecommendationService?????? ??????
        RecommendationDetailForm detailForm = recommendationService.watchDetail(recommendation3.getNum());

        // ????????? R -> D??? ???????????????
        // ????????? 3????????? 2?????? ????????????.
        assertThat(detailForm.getReplyFormList().size()).isEqualTo(2);
        assertThat(detailForm.getReplyFormList().size()).isNotEqualTo(3);
    }

//    -------------------------methods using for admin start----------------------------------
    /**
     * ????????? ?????? ?????? ?????? ???????????? ??????
     * -> ?????????(admin)??? ??????
     */
    @Test
    @Rollback(value = false)
    public void ?????????_??????_??????_????????????() {
        // given
        // ?????? ????????? ??????
        int amount = 100;
        var rCount = 0; // ?????? ??????
        var r2Count = 0; // ?????? ??????
        var r3Count = 0; // ?????? ??????
        ReplyForm replyForm = null;
        RecommendationReply recommendationReply = null;

        for(int i = 0; i < amount; i++) {
            if(i % 3 == 1) {
                replyForm = new ReplyForm("????????????");
                recommendationReply = RecommendationReply.createRecommendationReply(replyForm, member, recommendation);
                rCount += 1;
            } else if(i * 3 == 2) {
                replyForm = new ReplyForm("?????????~~~~~");
                recommendationReply = RecommendationReply.createRecommendationReply(replyForm, member2, recommendation2);
                r2Count += 1;
            } else {
                replyForm = new ReplyForm("????????????~~~~~");
                recommendationReply = RecommendationReply.createRecommendationReply(replyForm, member3, recommendation3);
                r3Count += 1;
            }
            // ????????????
            recommendationReplyJpaRepository.save(recommendationReply);

            // ????????????
            if(i % 4 == 0) {
                recommendationReply.delete();
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
        RecommendationReplySearchCond cond0 = new RecommendationReplySearchCond("", "", "");
        // ?????? 1
        RecommendationReplySearchCond cond11 = new RecommendationReplySearchCond("a1", "", "");
        RecommendationReplySearchCond cond12 = new RecommendationReplySearchCond("", "???~~", "");
        RecommendationReplySearchCond cond13 = new RecommendationReplySearchCond("", "", today);
        // ?????? 2
        RecommendationReplySearchCond cond21 = new RecommendationReplySearchCond("b1", "??????", "");
        RecommendationReplySearchCond cond22 = new RecommendationReplySearchCond("b1", "", today);
        RecommendationReplySearchCond cond23 = new RecommendationReplySearchCond("", "???~", today);
        // ?????? 3
        RecommendationReplySearchCond cond31 = new RecommendationReplySearchCond("c1", "????????????", today);


        // ????????? ?????? ????????? ???
        RecommendationReplySearchCond cond14 = new RecommendationReplySearchCond("a123", "", "");
        RecommendationReplySearchCond cond24 = new RecommendationReplySearchCond("b123", "", today);
        RecommendationReplySearchCond cond32 = new RecommendationReplySearchCond("c1", "????????????!", today);

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
    private void testResultForAdmin(RecommendationReplySearchCond cond, Pageable pageable, int count) {
        Page<ReplyDetailForm> result = recommendationReplyService.selectListForAdmin(cond, pageable);
        assertThat(result.getTotalElements()).isEqualTo(count);
    }


//    -------------------------methods using for admin admin----------------------------------

}