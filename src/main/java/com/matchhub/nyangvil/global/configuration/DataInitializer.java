package com.matchhub.nyangvil.global.configuration;

import com.matchhub.nyangvil.domain.boardcategory.model.entity.BoardCategoryGroup;
import com.matchhub.nyangvil.domain.boardcategory.model.entity.BoardCategoryItem;
import com.matchhub.nyangvil.domain.boardcategory.repository.BoardCategoryGroupRepository;
import com.matchhub.nyangvil.domain.boardcategory.repository.BoardCategoryItemRepository;
import com.matchhub.nyangvil.domain.user.model.entity.User;
import com.matchhub.nyangvil.domain.user.model.enums.Role;
import com.matchhub.nyangvil.domain.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BoardCategoryGroupRepository groupRepository;
    private final BoardCategoryItemRepository itemRepository;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder,
                           BoardCategoryGroupRepository groupRepository, BoardCategoryItemRepository itemRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.groupRepository = groupRepository;
        this.itemRepository = itemRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        initUsers();
        initBoardCategories();
    }

    private void initUsers() {
        if (userRepository.findByUsername("admin").isEmpty()) {
            userRepository.save(new User("admin", "admin@email.com", passwordEncoder.encode("password"), Role.ADMIN));
            log.info("admin 계정 생성 완료");
        }
        if (userRepository.findByUsername("user").isEmpty()) {
            userRepository.save(new User("user", "user@email.com", passwordEncoder.encode("password"), Role.USER));
            log.info("user 계정 생성 완료");
        }
    }

    private void initBoardCategories() {
        if (groupRepository.count() > 0) {
            log.info("게시판 카테고리 이미 존재 — 시딩 건너뜀");
            return;
        }

        // 운영
        BoardCategoryGroup g1 = groupRepository.save(new BoardCategoryGroup("운영", "📢", 1));
        createItemIfNotExists("NOTICE", "공지사항", g1, 1);
        createItemIfNotExists("GREETING", "가입인사", g1, 2);

        // 소통
        BoardCategoryGroup g2 = groupRepository.save(new BoardCategoryGroup("소통", "💬", 2));
        createItemIfNotExists("CAT_SHOW", "고양이 자랑", g2, 1);
        createItemIfNotExists("FREE", "자유게시판", g2, 2);
        createItemIfNotExists("STRAY_CAT", "길고양이 이야기", g2, 3);

        // 정보
        BoardCategoryGroup g3 = groupRepository.save(new BoardCategoryGroup("정보", "📚", 3));
        createItemIfNotExists("QNA", "질문과 답변", g3, 1);
        createItemIfNotExists("HEALTH", "건강·의료 정보", g3, 2);
        createItemIfNotExists("REVIEW", "용품 후기", g3, 3);

        // 나눔
        BoardCategoryGroup g4 = groupRepository.save(new BoardCategoryGroup("나눔", "🐱", 4));
        createItemIfNotExists("RESCUE", "임시보호·구조", g4, 1);
        createItemIfNotExists("FREE_SHARE", "무료나눔", g4, 2);

        log.info("게시판 카테고리 초기 시딩 완료 (4 그룹, 10 게시판)");
    }

    private void createItemIfNotExists(String code, String label, BoardCategoryGroup group, int order) {
        if (!itemRepository.existsByCategoryCode(code)) {
            itemRepository.save(new BoardCategoryItem(code, label, group, order));
        }
    }
}
