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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import shop.wesellbuy.secondproject.domain.Item;
import shop.wesellbuy.secondproject.domain.Member;
import shop.wesellbuy.secondproject.domain.Recommendation;
import shop.wesellbuy.secondproject.domain.board.BoardStatus;
import shop.wesellbuy.secondproject.domain.item.Book;
import shop.wesellbuy.secondproject.domain.item.ItemPicture;
import shop.wesellbuy.secondproject.domain.member.SelfPicture;
import shop.wesellbuy.secondproject.domain.recommendation.RecommendationPicture;
import shop.wesellbuy.secondproject.domain.reply.RecommendationReply;
import shop.wesellbuy.secondproject.exception.recommendation.NotExistingItemException;
import shop.wesellbuy.secondproject.repository.item.ItemJpaRepository;
import shop.wesellbuy.secondproject.repository.member.MemberJpaRepository;
import shop.wesellbuy.secondproject.repository.recommendation.RecommendationJpaRepository;
import shop.wesellbuy.secondproject.repository.recommendation.RecommendationSearchCond;
import shop.wesellbuy.secondproject.repository.reply.recommendation.RecommendationReplyJpaRepository;
import shop.wesellbuy.secondproject.service.recommendation.RecommendationService;
import shop.wesellbuy.secondproject.web.item.BookForm;
import shop.wesellbuy.secondproject.web.member.MemberForm;
import shop.wesellbuy.secondproject.web.member.MemberOriginForm;
import shop.wesellbuy.secondproject.web.recommendation.*;
import shop.wesellbuy.secondproject.web.reply.ReplyForm;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@Slf4j
public class RecommendationServiceTest {

    @Autowired
    EntityManager em;
    @Autowired
    RecommendationService recommendationService;
    @Autowired
    RecommendationJpaRepository recommendationJpaRepository;
    @Autowired
    MemberJpaRepository memberJpaRepository;
    @Autowired
    ItemJpaRepository itemJpaRepository;
    @Autowired
    RecommendationReplyJpaRepository replyRepository;

    // ????????? ?????????
    Member member; // test ??????
    Member member2; // test ??????
    Member member3; // test ??????
    Item item; // test ??????
    Item item2; // test ??????
    Item item3; // test ??????
    List<MultipartFile> files; // test ????????? ?????? ??????


    @BeforeEach
    public void init() throws IOException {

        // member ??????
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

        BookForm bookForm = new BookForm(10, 1000, "book1", "x is...", itemPictureList, "ed", "ok");
        BookForm bookForm2 = new BookForm(10, 1000, "book123", "x is...", itemPictureList, "ed", "ok");
        BookForm bookForm3 = new BookForm(10, 1000, "book2", "x is...", itemPictureList, "ed", "ok");
        Item item = Book.createBook(bookForm, member);
        Item item2 = Book.createBook(bookForm2, member);
        Item item3 = Book.createBook(bookForm3, member2);

        itemJpaRepository.save(item);
        itemJpaRepository.save(item2);
        itemJpaRepository.save(item3);
        this.item = item;
        this.item2 = item2;
        this.item3 = item3;

        // ?????????????????? ??????
        RecommendationForm recommendationForm = new RecommendationForm("book1", "a1", "???????????????", null);

        // ????????? ??????
        // MockMultipartFile ??????
        String fileName = "book"; // ?????????
        String contentType = "jpg"; // ?????? ?????????
        String originFileName = fileName + "." + contentType;
        String filePath = "src/test/resources/testImages/" + fileName + "." + contentType;
        FileInputStream fileInputStream = new FileInputStream(filePath);

        MockMultipartFile file = new MockMultipartFile("book_image", originFileName, contentType, fileInputStream);

        String fileName2 = "book2"; // ?????????
        String contentType2 = "jpg"; // ?????? ?????????
        String originFileName2 = fileName + "." + contentType;
        String filePath2 = "src/test/resources/testImages/" + fileName + "." + contentType;
        FileInputStream fileInputStream2 = new FileInputStream(filePath);

        MockMultipartFile file2 = new MockMultipartFile("book_image2", originFileName2, contentType2, fileInputStream2);

        // ????????? ??????
        List<MultipartFile> files = new ArrayList<>();
        files.add(file);
        files.add(file2);

        this.files = files;

    }

    /**
     * ?????????????????? ?????? ??????
     */
    @Test
//    @Rollback(value = false)
    public void ??????_??????() throws IOException {
        // given
        // member ??????
        SelfPicture selfPicture = SelfPicture.createSelfPicture("test1", "test2");
        MemberForm memberForm1 = new MemberForm("a", "ab1", "123", "a", "a@a", "01012341234", "0511231234", "korea", "b", "h", "h", selfPicture);
        Member member = Member.createMember(memberForm1);

        memberJpaRepository.save(member);

        // ?????? ??????
        List<ItemPicture> itemPictureList = new ArrayList<>();
        itemPictureList.add(ItemPicture.createItemPicture("a", "a"));
        itemPictureList.add(ItemPicture.createItemPicture("a1", "a2"));

        BookForm bookForm = new BookForm(10, 1000, "book1", "x is...", itemPictureList, "ed", "ok");
        Item item = Book.createBook(bookForm, member);

        itemJpaRepository.save(item);

        // ?????????????????? ??????
        RecommendationForm recommendationForm = new RecommendationForm("book1", "ab1", "???????????????", null);
        // ????????? ??????
        // MockMultipartFile ??????
        String fileName = "book"; // ?????????
        String contentType = "jpg"; // ?????? ?????????
        String originFileName = fileName + "." + contentType;
        String filePath = "src/test/resources/testImages/" + fileName + "." + contentType;
        FileInputStream fileInputStream = new FileInputStream(filePath);

        MockMultipartFile file = new MockMultipartFile("book_image", originFileName, contentType, fileInputStream);

        String fileName2 = "book2"; // ?????????
        String contentType2 = "jpg"; // ?????? ?????????
        String originFileName2 = fileName + "." + contentType;
        String filePath2 = "src/test/resources/testImages/" + fileName2 + "." + contentType2;
        FileInputStream fileInputStream2 = new FileInputStream(filePath2);

        MockMultipartFile file2 = new MockMultipartFile("book_image2", originFileName2, contentType2, fileInputStream2);

        // ????????? ??????
        List<MultipartFile> files = new ArrayList<>();
        files.add(file);
        files.add(file2);

        // ?????????????????? ????????????
        int savedNum = recommendationService.save(recommendationForm, files, member.getNum());

        // when
        Recommendation findRecommendation = recommendationJpaRepository.findById(savedNum).orElseThrow();

        // then
        assertThat(findRecommendation.getNum()).isEqualTo(savedNum);
        assertThat(findRecommendation.getContent()).isEqualTo(recommendationForm.getContent());
        assertThat(findRecommendation.getItemName()).isEqualTo(recommendationForm.getItemName());
        assertThat(findRecommendation.getSellerId()).isEqualTo(recommendationForm.getSellerId());
        assertThat(findRecommendation.getHits()).isEqualTo(0);
        assertThat(findRecommendation.getMember()).isEqualTo(member);
        assertThat(findRecommendation.getRecommendationPictureList().size()).isEqualTo(2);
    }

    /**
     * ?????????????????? ?????? ??????
     * -> ?????? ?????? ????????? ?????? ?????? ????????? ???
     */
    @Test
//    @Rollback(value = false)
    public void ??????_??????_??????_??????() {
        // given
        // ?????????????????? ??????
        RecommendationForm failedItemNameForm = new RecommendationForm("book1234", "a1", "???????????????", null);
        RecommendationForm failedSellerIdForm = new RecommendationForm("book1", "a123", "???????????????", null);

        // when // then
        assertThrows(NotExistingItemException.class, () -> recommendationService.save(failedItemNameForm, files, member.getNum()));
        assertThrows(NotExistingItemException.class, () -> recommendationService.save(failedSellerIdForm, files, member.getNum()));
    }

    /**
     * ?????????????????? ???????????? ??????
     *
     * comment : db ????????? ?????? 6?????????????????? ???????????? flush ??? ???, ?????? ??????
     */
    @Test
//    @Rollback(value = false)
    public void ????????????_??????() {
        // given
        // ?????????????????? ??????
        List<RecommendationPicture> rpList = new ArrayList<>();
        rpList.add(RecommendationPicture.createRecommendationPicture("x", "x"));
        rpList.add(RecommendationPicture.createRecommendationPicture("y1", "y2"));
        RecommendationForm recommendationForm = new RecommendationForm("book1", "a1", "ok", rpList);

        Recommendation recommendation = Recommendation.createRecommendation(recommendationForm, member);
        // ??????
        recommendationJpaRepository.save(recommendation);

        // ?????? ??????
        ReplyForm replyForm1 = new ReplyForm("?????????~");
        ReplyForm replyForm2 = new ReplyForm("?????????2~");
        ReplyForm replyForm3 = new ReplyForm("?????????3~");
        RecommendationReply recommendationReply1 = RecommendationReply.createRecommendationReply(replyForm1, member2, recommendation);
        RecommendationReply recommendationReply2 = RecommendationReply.createRecommendationReply(replyForm2, member3, recommendation);
        RecommendationReply recommendationReply3 = RecommendationReply.createRecommendationReply(replyForm3, member2, recommendation);
        // ??????
        replyRepository.save(recommendationReply1);
        replyRepository.save(recommendationReply2);
        replyRepository.save(recommendationReply3);

        // betch size ?????? ??????
        // ?????? ??????
//        em.flush();
//        em.clear();

        // when
        // ????????????
        RecommendationDetailForm detailForm = recommendationService.watchDetail(recommendation.getNum());

        // then
        assertThat(detailForm.getNum()).isEqualTo(recommendation.getNum());
        assertThat(detailForm.getContent()).isEqualTo(recommendation.getContent());
        assertThat(detailForm.getItemName()).isEqualTo(recommendation.getItemName());
        assertThat(detailForm.getSellerId()).isEqualTo(recommendation.getSellerId());
//        assertThat(detailForm.getHits()).isEqualTo(0);// ?????? ??????
        assertThat(detailForm.getHits()).isEqualTo(1);
//        assertThat(detailForm.getCreateDate()).isEqualTo(recommendation.getCreatedDate());// db ????????? ?????? 6?????????????????? ?????????????
        // ????????? ???????????? ????????? ?????? ??????
        assertThat(detailForm.getRecommendationPictureFormList().stream()
                .map(p -> p.getOriginalFileName())
                .collect(toList()))
                .containsExactly("x", "y1");
        // ?????? ????????? ??????
        assertThat(detailForm.getReplyFormList().stream()
                .map(r -> r.getMemberId())
                .collect(toList()))
                .containsExactly(member2.getId(), member3.getId(), member2.getId());
    }

    /**
     * ?????? ??????
     */
    @Test
//    @Rollback(value = false)
    public void ??????_??????() throws IOException {
        // given
        // ?????????????????? ??????
        List<RecommendationPicture> rpList = new ArrayList<>();
        rpList.add(RecommendationPicture.createRecommendationPicture("x", "x"));
        rpList.add(RecommendationPicture.createRecommendationPicture("y1", "y2"));
        RecommendationForm recommendationForm = new RecommendationForm("book1", "a1", "?????????~", rpList);

        Recommendation recommendation = Recommendation.createRecommendation(recommendationForm, member);
        // ??????
        recommendationJpaRepository.save(recommendation);

        // ?????? form ?????????
        RecommendationUpdateForm updateForm = new RecommendationUpdateForm(recommendation.getNum(), "book123", "a1", "?????? ?????????~");

        // when
        // ?????? ?????? ???
//        recommendationService.update(updateForm, files);
        // ?????? ?????? ???
        recommendationService.update(updateForm, new ArrayList<>());

        // then
        assertThat(recommendation.getItemName()).isEqualTo(updateForm.getItemName());
        assertThat(recommendation.getSellerId()).isEqualTo(updateForm.getSellerId());
        assertThat(recommendation.getContent()).isEqualTo(updateForm.getContent());
        // ?????? ?????? ???
//        assertThat(recommendation.getRecommendationPictureList().size()).isEqualTo(2 + 2);
        // ?????? ?????? ???
        assertThat(recommendation.getRecommendationPictureList().size()).isEqualTo(2);
    }

    /**
     * ?????? ??????
     * -> ?????? ?????? or ????????? ???????????? ???????????? ?????? ??? ?????? ??????
     */
    @Test
    public void ??????_??????_??????_??????() throws IOException {
        // given
        // ?????????????????? ??????
        List<RecommendationPicture> rpList = new ArrayList<>();
        rpList.add(RecommendationPicture.createRecommendationPicture("x", "x"));
        rpList.add(RecommendationPicture.createRecommendationPicture("y1", "y2"));
        RecommendationForm recommendationForm = new RecommendationForm("book1", "a1", "?????????~", rpList);

        Recommendation recommendation = Recommendation.createRecommendation(recommendationForm, member);
        // ??????
        recommendationJpaRepository.save(recommendation);

        // ?????? form ?????????
        RecommendationUpdateForm falutItemNameUpdateForm = new RecommendationUpdateForm(recommendation.getNum(), "book12", "a1", "?????? ?????????~");
        RecommendationUpdateForm falutSellerIdUpdateForm = new RecommendationUpdateForm(recommendation.getNum(), "book123", "a1342", "?????? ?????????~");

        // when
        assertThrows(NotExistingItemException.class, () -> recommendationService.update(falutItemNameUpdateForm, files));;
        assertThrows(NotExistingItemException.class, () -> recommendationService.update(falutSellerIdUpdateForm, new ArrayList<>()));
//        assertThrows(NotExistingIdException.class, () -> recommendationService.update(falutSellerIdUpdateForm, new ArrayList<>())); // ?????? ??????

    }

    /**
     * ?????? ??????
     * comment: status R -> D??? ????????????.
     */
    @Test
    public void ??????_??????() {
        // given
        // ?????????????????? ??????
        List<RecommendationPicture> rpList = new ArrayList<>();
        rpList.add(RecommendationPicture.createRecommendationPicture("x", "x"));
        rpList.add(RecommendationPicture.createRecommendationPicture("y1", "y2"));
        RecommendationForm recommendationForm = new RecommendationForm("book1", "a1", "?????????~", new ArrayList<>());

        Recommendation recommendation = Recommendation.createRecommendation(recommendationForm, member);
        // ??????
        recommendationJpaRepository.save(recommendation);
        // when
        // ????????????
        recommendationService.delete(recommendation.getNum());

        // then
        assertThat(recommendation.getStatus()).isEqualTo(BoardStatus.D);
    }

    /**
     * ????????? ?????? ??????
     */
    @Test
//    @Rollback(value = false)
    public void ?????????_??????_??????() {
        // given
        // ?????????????????? ??????
        List<RecommendationPicture> rpList = new ArrayList<>();
        RecommendationPicture picture1 = RecommendationPicture.createRecommendationPicture("x", "x");
        RecommendationPicture picture2 = RecommendationPicture.createRecommendationPicture("y1", "y2");
        RecommendationPicture picture3 = RecommendationPicture.createRecommendationPicture("y1", "y2");
        rpList.add(picture1);
        rpList.add(picture2);
        rpList.add(picture3);
        RecommendationForm recommendationForm = new RecommendationForm("book1", "a1", "?????????~", rpList);

        Recommendation recommendation = Recommendation.createRecommendation(recommendationForm, member);
        // ??????
        recommendationJpaRepository.save(recommendation);

        // when
        // ????????? ????????????
        recommendationService.deletePicture(recommendation.getNum(), picture1.getNum());

        // then
        // ?????????????????? ????????? ???????????? ??????
        RecommendationDetailForm detailForm = recommendationService.watchDetail(recommendation.getNum());

        assertThat(detailForm.getRecommendationPictureFormList().size()).isEqualTo(2);
    }

    /**
     * ???????????? ?????? ?????? ?????????????????? ?????? ???????????? ??????
     */
    @Test
//    @Rollback(value = false)
    public void ?????????_??????_??????_??????_????????????_??????() {
        // given
        // ?????????????????? ??????
        List<RecommendationPicture> rpList = new ArrayList<>();
        List<RecommendationPicture> rpList2 = new ArrayList<>();
        RecommendationPicture picture1 = RecommendationPicture.createRecommendationPicture("x", "x");
        RecommendationPicture picture2 = RecommendationPicture.createRecommendationPicture("y1", "y2");
        RecommendationPicture picture3 = RecommendationPicture.createRecommendationPicture("y1", "y2");
        RecommendationPicture picture4 = RecommendationPicture.createRecommendationPicture("y1", "y2");
        rpList.add(picture1);
        rpList.add(picture2);
        rpList.add(picture3);

        rpList2.add(picture4);

        // ?????? ????????? ??????
        Recommendation recommendation = null;
        var rCount = 0;
        var r2Count = 0;
        var r3Count = 0;
        var amount = 100;
        for(int i = 0; i < amount; i++) {
            if(i % 3 == 0) {
                RecommendationForm recommendationForm = new RecommendationForm("book1", "a1", "?????????~", rpList);
                recommendation = Recommendation.createRecommendation(recommendationForm, member);
                rCount += 1;
            } else if(i % 3 == 1) {
                RecommendationForm recommendationForm = new RecommendationForm("book1", "a1", "?????????~", new ArrayList<>());
                recommendation = Recommendation.createRecommendation(recommendationForm, member2);
                r2Count += 1;

            } else {
                RecommendationForm recommendationForm = new RecommendationForm("book3", "b2", "?????????~", rpList2);
                recommendation = Recommendation.createRecommendation(recommendationForm, member3);
                r3Count += 1;
            }
            // ??????
            recommendationJpaRepository.save(recommendation);
        }

        // when
        // ????????? ??????
        Pageable page0size10 = PageRequest.of(0, 10);
        Pageable page2size5 = PageRequest.of(2, 5);

        // ??????
        String today = "2023-02-01";
        String otherDay = "2023-02-02";

        // ?????? ??????
        // ?????? 0
        RecommendationSearchCond cond0 = new RecommendationSearchCond("", "", "", "");
        // ?????? 1
        RecommendationSearchCond cond11 = new RecommendationSearchCond("book1", "", "", "");
        RecommendationSearchCond cond12 = new RecommendationSearchCond("", "a1", "", "");
        RecommendationSearchCond cond13 = new RecommendationSearchCond("", "", "b1", "");
        RecommendationSearchCond cond14 = new RecommendationSearchCond("", "", "", today);
        // ?????? 2
        RecommendationSearchCond cond21 = new RecommendationSearchCond("book1", "a1", "", "");
        RecommendationSearchCond cond22 = new RecommendationSearchCond("book1", "", "b1", "");
        RecommendationSearchCond cond23 = new RecommendationSearchCond("book1", "", "", today);
        RecommendationSearchCond cond24 = new RecommendationSearchCond("", "a1", "a1", "");
        RecommendationSearchCond cond25 = new RecommendationSearchCond("", "a1", "", today);
        RecommendationSearchCond cond26 = new RecommendationSearchCond("", "", "c1", today);
        // ?????? 3
        RecommendationSearchCond cond31 = new RecommendationSearchCond("book1", "a1", "b1", "");
        RecommendationSearchCond cond32 = new RecommendationSearchCond("book1", "a1", "", today);
        RecommendationSearchCond cond33 = new RecommendationSearchCond("book3", "", "c1", today);
        RecommendationSearchCond cond34 = new RecommendationSearchCond("", "a1", "b1", today);
        // ??????4
        RecommendationSearchCond cond41 = new RecommendationSearchCond("book1", "a1", "b1", today);

        // ????????? ?????? ??? ?????? ???
        RecommendationSearchCond cond15 = new RecommendationSearchCond("", "a3", "", "");
        RecommendationSearchCond cond27 = new RecommendationSearchCond("", "123456", "c1", "");
        RecommendationSearchCond cond35 = new RecommendationSearchCond("book1", "a1", "b12", "");
        RecommendationSearchCond cond42 = new RecommendationSearchCond("book1123", "a1", "b1", otherDay);


        // then
        // ??????0
        searchListTestResult(cond0, page0size10, rCount + r2Count + r3Count);
        // ??????1
        searchListTestResult(cond11, page0size10, rCount + r2Count);
        searchListTestResult(cond12, page0size10, rCount + r2Count);
        searchListTestResult(cond13, page0size10, r2Count);
        searchListTestResult(cond14, page0size10, rCount + r2Count + r3Count);
        // ??????2
        searchListTestResult(cond21, page2size5, rCount + r2Count);
        searchListTestResult(cond22, page2size5, r2Count);
        searchListTestResult(cond23, page2size5, rCount + r2Count);
        searchListTestResult(cond24, page2size5, rCount);
        searchListTestResult(cond25, page2size5, rCount + r2Count);
        searchListTestResult(cond26, page2size5, r3Count);
        // ??????3
        searchListTestResult(cond31, page2size5, r2Count);
        searchListTestResult(cond32, page2size5, rCount + r2Count);
        searchListTestResult(cond33, page2size5, r3Count);
        searchListTestResult(cond34, page2size5, r2Count);
        // ??????4
        searchListTestResult(cond41, page0size10, r2Count);

        // ????????? ?????? ??? ?????? ???
        searchListTestResult(cond15, page0size10, 0);
        searchListTestResult(cond27, page2size5, 0);
        searchListTestResult(cond35, page2size5, 0);
        searchListTestResult(cond42, page0size10, 0);
    }

    // ??????_????????????_?????? test??? ??????
    private void searchListTestResult(RecommendationSearchCond cond, Pageable pageable, int count) {
        // ?????????????????? ??????
        Page<RecommendationListForm> result = recommendationService.selectList(cond, pageable);
        // ?????? ??????
        assertThat(result.getTotalElements()).isEqualTo(count);
    }

    /**
     * ?????????????????? ?????? ???????????? ??????
     * -> ?????? ????????? ?????? ??????
     */
    @Test
//    @Rollback(value = false)
    public void ??????_????????????_??????_?????????_????????????_??????() {
        // given
        // ?????????????????? ??????
        List<RecommendationPicture> rpList = new ArrayList<>();
        List<RecommendationPicture> rpList2 = new ArrayList<>();
        RecommendationPicture picture1 = RecommendationPicture.createRecommendationPicture("x", "x");
        RecommendationPicture picture2 = RecommendationPicture.createRecommendationPicture("y1", "y2");
        RecommendationPicture picture3 = RecommendationPicture.createRecommendationPicture("y1", "y2");
        RecommendationPicture picture4 = RecommendationPicture.createRecommendationPicture("y1", "y2");
        rpList.add(picture1);
        rpList.add(picture2);
        rpList.add(picture3);

        rpList2.add(picture4);

        RecommendationForm recommendationForm = new RecommendationForm("book1", "a1", "?????????~", rpList);
        Recommendation recommendation = Recommendation.createRecommendation(recommendationForm, member);

        RecommendationForm recommendationForm2 = new RecommendationForm("book1", "a1", "?????????~", new ArrayList<>());
        Recommendation recommendation2 = Recommendation.createRecommendation(recommendationForm, member2);

        RecommendationForm recommendationForm3 = new RecommendationForm("book3", "b2", "?????????~", rpList2);
        Recommendation recommendation3 = Recommendation.createRecommendation(recommendationForm, member3);
        // ??????
        recommendationJpaRepository.save(recommendation);
        recommendationJpaRepository.save(recommendation2);
        recommendationJpaRepository.save(recommendation3);

        //when
        // ?????????????????? ????????????
        recommendationService.delete(recommendation2.getNum());

        //then
        // ????????? ?????? ????????? ??? ??????
        Pageable page0size10 = PageRequest.of(0, 10);
        RecommendationSearchCond cond0 = new RecommendationSearchCond("", "", "", "");

        // ????????? ?????? ??????
        searchListTestResult(cond0, page0size10, 2);
//        searchListTestResult(cond0, page0size10, 3);// ?????? ??????
        // ???????????? ??????
        searchListForAdminTestResult(cond0, page0size10, 3);
    }


    /**
     * ???????????? ?????? ?????????????????? ?????? ???????????? ??????
     */
    @Test
    public void ????????????_??????_??????_????????????_??????() {
// given
        // ?????????????????? ??????
        List<RecommendationPicture> rpList = new ArrayList<>();
        List<RecommendationPicture> rpList2 = new ArrayList<>();
        RecommendationPicture picture1 = RecommendationPicture.createRecommendationPicture("x", "x");
        RecommendationPicture picture2 = RecommendationPicture.createRecommendationPicture("y1", "y2");
        RecommendationPicture picture3 = RecommendationPicture.createRecommendationPicture("y1", "y2");
        RecommendationPicture picture4 = RecommendationPicture.createRecommendationPicture("y1", "y2");
        rpList.add(picture1);
        rpList.add(picture2);
        rpList.add(picture3);

        rpList2.add(picture4);

        // ?????? ????????? ??????
        Recommendation recommendation = null;
        var rCount = 0;
        var r2Count = 0;
        var r3Count = 0;
        var amount = 100;
        for(int i = 0; i < amount; i++) {
            if(i % 3 == 0) {
                RecommendationForm recommendationForm = new RecommendationForm("book1", "a1", "?????????~", rpList);
                recommendation = Recommendation.createRecommendation(recommendationForm, member);
                rCount += 1;
            } else if(i % 3 == 1) {
                RecommendationForm recommendationForm = new RecommendationForm("book1", "a1", "?????????~", new ArrayList<>());
                recommendation = Recommendation.createRecommendation(recommendationForm, member2);
                r2Count += 1;

            } else {
                RecommendationForm recommendationForm = new RecommendationForm("book3", "b2", "?????????~", rpList2);
                recommendation = Recommendation.createRecommendation(recommendationForm, member3);
                r3Count += 1;
            }
            // ??????
            recommendationJpaRepository.save(recommendation);
        }

        // when
        // ????????? ??????
        Pageable page0size10 = PageRequest.of(0, 10);
        Pageable page2size5 = PageRequest.of(2, 5);

        // ??????
        String today = "2023-02-01";
        String otherDay = "2023-02-02";

        // ?????? ??????
        // ?????? 0
//        RecommendationSearchCond cond0 = new RecommendationSearchCond("", "", "", "");
        // ?????? 1
        RecommendationSearchCond cond11 = new RecommendationSearchCond("book1", "", "", "");
        RecommendationSearchCond cond12 = new RecommendationSearchCond("", "a1", "", "");
        RecommendationSearchCond cond13 = new RecommendationSearchCond("", "", "b1", "");
        RecommendationSearchCond cond14 = new RecommendationSearchCond("", "", "", today);
        // ?????? 2
        RecommendationSearchCond cond21 = new RecommendationSearchCond("book1", "a1", "", "");
        RecommendationSearchCond cond22 = new RecommendationSearchCond("book1", "", "b1", "");
        RecommendationSearchCond cond23 = new RecommendationSearchCond("book1", "", "", today);
        RecommendationSearchCond cond24 = new RecommendationSearchCond("", "a1", "a1", "");
        RecommendationSearchCond cond25 = new RecommendationSearchCond("", "a1", "", today);
        RecommendationSearchCond cond26 = new RecommendationSearchCond("", "", "c1", today);
        // ?????? 3
        RecommendationSearchCond cond31 = new RecommendationSearchCond("book1", "a1", "b1", "");
        RecommendationSearchCond cond32 = new RecommendationSearchCond("book1", "a1", "", today);
        RecommendationSearchCond cond33 = new RecommendationSearchCond("book3", "", "c1", today);
        RecommendationSearchCond cond34 = new RecommendationSearchCond("", "a1", "b1", today);
        // ??????4
        RecommendationSearchCond cond41 = new RecommendationSearchCond("book1", "a1", "b1", today);

        // ????????? ?????? ??? ?????? ???
        RecommendationSearchCond cond15 = new RecommendationSearchCond("", "a3", "", "");
        RecommendationSearchCond cond27 = new RecommendationSearchCond("", "123456", "c1", "");
        RecommendationSearchCond cond35 = new RecommendationSearchCond("book1", "a1", "b12", "");
        RecommendationSearchCond cond42 = new RecommendationSearchCond("book1123", "a1", "b1", otherDay);

        // then
        // ??????0
//        searchListForAdminTestResult(cond0, page0size10, rCount + r2Count + r3Count);
        // ??????1
        searchListForAdminTestResult(cond11, page0size10, rCount + r2Count);
        searchListForAdminTestResult(cond12, page0size10, rCount + r2Count);
        searchListForAdminTestResult(cond13, page0size10, r2Count);
        searchListForAdminTestResult(cond14, page0size10, rCount + r2Count + r3Count);
        // ??????2
        searchListForAdminTestResult(cond21, page2size5, rCount + r2Count);
        searchListForAdminTestResult(cond22, page2size5, r2Count);
        searchListForAdminTestResult(cond23, page2size5, rCount + r2Count);
        searchListForAdminTestResult(cond24, page2size5, rCount);
        searchListForAdminTestResult(cond25, page2size5, rCount + r2Count);
        searchListForAdminTestResult(cond26, page2size5, r3Count);
        // ??????3
        searchListForAdminTestResult(cond31, page2size5, r2Count);
        searchListForAdminTestResult(cond32, page2size5, rCount + r2Count);
        searchListForAdminTestResult(cond33, page2size5, r3Count);
        searchListForAdminTestResult(cond34, page2size5, r2Count);
        // ??????4
        searchListForAdminTestResult(cond41, page0size10, r2Count);

        // ????????? ?????? ??? ?????? ???
        searchListForAdminTestResult(cond15, page0size10, 0);
        searchListForAdminTestResult(cond27, page2size5, 0);
        searchListForAdminTestResult(cond35, page2size5, 0);
        searchListForAdminTestResult(cond42, page0size10, 0);
    }

    // ??????_????????????_?????? test??? ?????? for admin
    private void searchListForAdminTestResult(RecommendationSearchCond cond, Pageable pageable, int count) {
        // ?????????????????? ??????
        Page<RecommendationListForAdminForm> result = recommendationService.selectListForAdmin(cond, pageable);
        // ?????? ??????
        assertThat(result.getTotalElements()).isEqualTo(count);
    }
























}
