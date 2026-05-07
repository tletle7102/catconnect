package com.matchhub.catconnect.global.configuration;

import com.matchhub.catconnect.domain.board.model.entity.Board;
import com.matchhub.catconnect.domain.board.repository.BoardRepository;
import com.matchhub.catconnect.domain.boardcategory.model.entity.BoardPermission;
import com.matchhub.catconnect.domain.boardcategory.repository.BoardPermissionRepository;
import com.matchhub.catconnect.domain.comment.model.entity.Comment;
import com.matchhub.catconnect.domain.comment.repository.CommentRepository;
import com.matchhub.catconnect.domain.like.model.entity.Like;
import com.matchhub.catconnect.domain.like.repository.LikeRepository;
import com.matchhub.catconnect.domain.user.model.entity.User;
import com.matchhub.catconnect.domain.user.model.enums.Role;
import com.matchhub.catconnect.domain.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 시연용 더미 데이터 시더 — local 프로필(H2)에서만 실행
 */
@Component
@Profile("local")
@Order(2) // DataInitializer(Order 기본=0) 이후에 실행
public class DemoDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final BoardPermissionRepository permissionRepository;
    private final Random random = new Random();

    public DemoDataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder,
                          BoardRepository boardRepository, CommentRepository commentRepository,
                          LikeRepository likeRepository, BoardPermissionRepository permissionRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.boardRepository = boardRepository;
        this.commentRepository = commentRepository;
        this.likeRepository = likeRepository;
        this.permissionRepository = permissionRepository;
    }

    @Override
    public void run(String... args) {
        if (boardRepository.count() > 0) {
            log.info("시연 데이터 이미 존재 — 시딩 건너뜀");
            return;
        }

        List<User> users = createDemoUsers();
        List<Board> boards = createDemoBoards(users);
        createDemoComments(boards, users);
        createDemoLikes(boards, users);
        createDemoPermissions();

        log.info("시연 더미 데이터 시딩 완료: 유저 {}명, 게시글 {}개", users.size(), boards.size());
    }

    private List<User> createDemoUsers() {
        String[] names = {"catmom", "kittyking", "meowlover", "pawfriend", "nyanmaster",
                "catdad", "whiskers", "furrball", "tabbycat", "purring"};
        List<User> users = new ArrayList<>();

        for (String name : names) {
            if (userRepository.findByUsername(name).isEmpty()) {
                User u = new User(name, name + "@email.com", passwordEncoder.encode("password"), Role.USER);
                users.add(userRepository.save(u));
            }
        }

        // admin, user도 리스트에 포함
        userRepository.findByUsername("admin").ifPresent(users::add);
        userRepository.findByUsername("user").ifPresent(users::add);

        log.info("더미 유저 생성: {}명", users.size());
        return users;
    }

    private List<Board> createDemoBoards(List<User> users) {
        List<Board> boards = new ArrayList<>();

        // 공지사항 (NOTICE) - 관리자 작성
        boards.add(createBoard("5월 시스템 점검 안내", "<p>5월 10일 오전 2시~6시 시스템 점검이 예정되어 있습니다. 이용에 불편을 드려 죄송합니다.</p>", "admin", "NOTICE", "공지사항"));
        boards.add(createBoard("커뮤니티 이용 규칙 안내", "<p>건전한 커뮤니티 운영을 위해 다음 규칙을 준수해주세요.</p><ul><li>욕설/비방 금지</li><li>광고 금지</li><li>개인정보 노출 주의</li></ul>", "admin", "NOTICE", "공지사항"));

        // 가입인사 (GREETING)
        boards.add(createBoard("안녕하세요! 새로 가입했습니다 🐱", "<p>고양이 2마리 키우고 있는 집사입니다. 잘 부탁드려요!</p>", "catmom", "GREETING", null));
        boards.add(createBoard("반갑습니다~ 3년차 집사예요", "<p>러시안블루 키우고 있습니다. 좋은 정보 많이 얻어가고 싶어요!</p>", "kittyking", "GREETING", null));
        boards.add(createBoard("가입인사 드립니다!", "<p>길고양이 급식 봉사를 하고 있는 냥이파파입니다.</p>", "catdad", "GREETING", null));

        // 고양이 자랑 (CAT_SHOW)
        boards.add(createBoard("우리 집 막내 치즈태비 소개합니다", "<p>3개월 된 치즈태비예요. 너무 귀엽지 않나요? 이름은 '치즈'입니다.</p>", "catmom", "CAT_SHOW", null));
        boards.add(createBoard("오늘 찍은 우리 고양이 사진!", "<p>아침에 창가에서 일광욕하는 모습이에요. 너무 평화롭죠?</p>", "meowlover", "CAT_SHOW", null));
        boards.add(createBoard("턱시도 고양이 자랑합니다", "<p>턱시도 패턴이 너무 예쁜 우리 '턱시'입니다. 3살이에요!</p>", "whiskers", "CAT_SHOW", null));
        boards.add(createBoard("삼색이 냥이 보러오세요~", "<p>삼색이 고양이 '무지개'예요. 성격이 정말 좋아요.</p>", "furrball", "CAT_SHOW", null));
        boards.add(createBoard("스코티시폴드 집사입니다", "<p>접힌 귀가 매력 포인트! 우리 '쿠키'를 소개합니다.</p>", "pawfriend", "CAT_SHOW", null));

        // 자유게시판 (FREE)
        boards.add(createBoard("고양이 이름 추천해주세요!", "<p>새로 입양한 아이 이름을 못 정했어요. 회색 러시안블루 수컷입니다. 추천 부탁드려요!</p>", "nyanmaster", "FREE", null));
        boards.add(createBoard("오늘 있었던 웃긴 일 ㅋㅋ", "<p>고양이가 박스에 들어가려고 하는데 몸이 안 맞아서 한참 끼어있었어요 ㅋㅋㅋ</p>", "kittyking", "FREE", null));
        boards.add(createBoard("집사들의 일상 공유해요", "<p>오늘 퇴근하니 고양이가 문 앞에서 기다리고 있었어요. 감동...</p>", "tabbycat", "FREE", null));
        boards.add(createBoard("고양이 관련 유튜브 추천", "<p>고양이 행동학 관련 유튜브 채널 추천합니다. 정말 유익해요!</p>", "purring", "FREE", null));

        // 길고양이 이야기 (STRAY_CAT)
        boards.add(createBoard("우리 동네 길고양이 급식소 운영 후기", "<p>3개월째 급식소를 운영하고 있습니다. 현재 6마리가 정기적으로 오고 있어요.</p>", "catdad", "STRAY_CAT", null));
        boards.add(createBoard("TNR 경험 공유합니다", "<p>처음으로 TNR 참여했는데, 과정과 주의사항을 공유합니다.</p>", "catmom", "STRAY_CAT", null));

        // 질문과 답변 (QNA)
        boards.add(createBoard("고양이 구토가 잦아요, 병원 가야할까요?", "<p>최근 일주일에 2-3번씩 구토를 합니다. 사료를 바꿔봤는데도 계속 그래요.</p>", "meowlover", "QNA", null));
        boards.add(createBoard("캣타워 추천 부탁드려요", "<p>다묘가정(3마리)인데 튼튼한 캣타워 추천해주세요!</p>", "pawfriend", "QNA", null));
        boards.add(createBoard("처음 고양이 키우는데 필수 용품이 뭐가 있나요?", "<p>다음 주에 입양 예정인데 미리 준비할 것들이 궁금합니다.</p>", "nyanmaster", "QNA", null));

        // 건강·의료 정보 (HEALTH)
        boards.add(createBoard("고양이 예방접종 스케줄 정리", "<p>나이별 필수 예방접종 목록을 정리해봤습니다.</p><ul><li>6주: 1차</li><li>9주: 2차</li><li>12주: 3차</li></ul>", "catmom", "HEALTH", null));
        boards.add(createBoard("여름철 고양이 열사병 예방법", "<p>여름에 특히 주의해야 할 열사병 증상과 예방법을 알려드립니다.</p>", "kittyking", "HEALTH", null));

        // 용품 후기 (REVIEW)
        boards.add(createBoard("자동 급식기 3종 비교 후기", "<p>직접 사용해본 자동 급식기 3종을 비교해봅니다.</p>", "furrball", "REVIEW", null));
        boards.add(createBoard("두부모래 vs 벤토나이트 사용 후기", "<p>두 종류 모래를 3개월씩 사용한 솔직 후기입니다.</p>", "whiskers", "REVIEW", null));

        // 임시보호·구조 (RESCUE)
        boards.add(createBoard("서울 마포구 유기묘 임보 구합니다", "<p>마포구에서 구조한 치즈태비(추정 6개월)의 임시보호자를 찾습니다.</p>", "catdad", "RESCUE", "서울"));
        boards.add(createBoard("경기 수원시 다친 길고양이 구조 후기", "<p>다리를 다친 길고양이를 구조하여 치료 중입니다.</p>", "catmom", "RESCUE", "경기"));
        boards.add(createBoard("부산 해운대 유기묘 입양 완료!", "<p>2주간 임보 후 좋은 가정에 입양 보냈습니다. 감사합니다!</p>", "meowlover", "RESCUE", "완료"));

        // 무료나눔 (FREE_SHARE)
        boards.add(createBoard("사료 샘플 나눔합니다 (서울)", "<p>여러 사료 샘플이 남아서 나눔합니다. 직거래 희망.</p>", "tabbycat", "FREE_SHARE", "서울"));
        boards.add(createBoard("캣타워 무료나눔 (경기 성남)", "<p>이사 가면서 캣타워를 나눔합니다. 사용감 있습니다.</p>", "purring", "FREE_SHARE", "경기"));

        return boards;
    }

    private Board createBoard(String title, String content, String author, String category, String prefix) {
        Board board = new Board(title, content, author, category);
        if (prefix != null) board.setPrefix(prefix);
        // 조회수 랜덤
        board.setViewCount(random.nextInt(200) + 10);
        return boardRepository.save(board);
    }

    private void createDemoComments(List<Board> boards, List<User> users) {
        String[][] commentTexts = {
            {"정말 귀엽네요! 🐱", "사진 더 보여주세요~", "우와 너무 예뻐요!"},
            {"좋은 정보 감사합니다!", "저도 같은 경험이 있어요", "도움이 많이 됐어요 👍"},
            {"공감합니다 ㅋㅋ", "저희 집 고양이도 그래요!", "웃겨요 ㅋㅋㅋ"},
            {"응원합니다!", "대단하세요", "좋은 일 하시네요 💪"},
            {"감사합니다~", "참고할게요!", "잘 보고 갑니다"},
        };

        int commentCount = 0;
        for (Board board : boards) {
            // 게시글당 0~8개 랜덤 댓글
            int numComments = random.nextInt(9);
            for (int i = 0; i < numComments; i++) {
                User commenter = users.get(random.nextInt(users.size()));
                // 자기 글에 댓글 달지 않도록 (가끔 가능하게)
                String[] pool = commentTexts[random.nextInt(commentTexts.length)];
                String text = pool[random.nextInt(pool.length)];
                Comment comment = new Comment(text, commenter.getUsername(), board);
                commentRepository.save(comment);
                commentCount++;
            }
        }
        log.info("더미 댓글 생성: {}개", commentCount);
    }

    private void createDemoLikes(List<Board> boards, List<User> users) {
        int likeCount = 0;
        for (Board board : boards) {
            // 게시글당 0~7명이 좋아요
            int numLikes = random.nextInt(8);
            List<User> shuffled = new ArrayList<>(users);
            java.util.Collections.shuffle(shuffled, random);
            for (int i = 0; i < Math.min(numLikes, shuffled.size()); i++) {
                User liker = shuffled.get(i);
                if (!liker.getUsername().equals(board.getAuthor())) {
                    if (!likeRepository.existsByBoardIdAndUsername(board.getId(), liker.getUsername())) {
                        likeRepository.save(new Like(liker.getUsername(), board));
                        likeCount++;
                    }
                }
            }
        }
        log.info("더미 좋아요 생성: {}개", likeCount);
    }

    private void createDemoPermissions() {
        if (permissionRepository.count() > 0) {
            log.info("게시판 권한 이미 존재 — 시딩 건너뜀");
            return;
        }

        String[] categories = {"NOTICE", "GREETING", "CAT_SHOW", "FREE", "STRAY_CAT",
                "QNA", "HEALTH", "REVIEW", "RESCUE", "FREE_SHARE"};

        int count = 0;
        for (String code : categories) {
            for (Role role : Role.values()) {
                boolean canRead = true;
                boolean canWrite = true;

                if (role == Role.USER && "NOTICE".equals(code)) {
                    canWrite = false; // 공지사항: USER는 읽기만
                }

                permissionRepository.save(new BoardPermission(code, role, canRead, canWrite));
                count++;
            }
        }
        log.info("게시판 권한 시딩 완료: {}개", count);
    }
}
