package com.matchhub.catconnect.domain.user.service;

import com.matchhub.catconnect.domain.user.model.dto.UserResponseDTO;
import com.matchhub.catconnect.domain.user.model.dto.UserUpdateRequestDTO;
import com.matchhub.catconnect.domain.user.model.entity.User;
import com.matchhub.catconnect.domain.user.repository.UserRepository;
import com.matchhub.catconnect.global.exception.AppException;
import com.matchhub.catconnect.global.exception.Domain;
import com.matchhub.catconnect.global.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 생성자를 통한 의존성 주입
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 전체 사용자 목록 조회
     * @return 사용자 DTO 목록
     */
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        log.debug("사용자 목록 조회 요청");
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * 전체 사용자 목록 조회 (페이지네이션)
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 페이지네이션된 사용자 DTO 목록
     */
    @Transactional(readOnly = true)
    public Page<UserResponseDTO> getAllUsers(int page, int size) {
        log.debug("페이지네이션 사용자 조회 요청: page={}, size={}", page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDttm").descending());
        Page<User> userPage = userRepository.findAll(pageable);
        return userPage.map(this::toResponseDTO);
    }

    /**
     * 특정 사용자 상세 조회
     * @param id 사용자 ID
     * @return 사용자 DTO
     */
    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long id) {
        log.debug("사용자 상세 조회 요청: id={}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(Domain.USER, ErrorCode.USER_NOT_FOUND));
        return toResponseDTO(user);
    }

    /**
     * 여러 사용자 삭제
     * @param ids 삭제할 사용자 ID 목록
     */
    public void deleteUsers(List<Long> ids) {
        log.debug("사용자 다중 삭제 요청: ids={}", ids);
        if (ids == null || ids.isEmpty()) {
            log.debug("삭제할 사용자 없음, 처리 생략");
            return;
        }
        userRepository.deleteAllByIdInBatch(ids);
        log.debug("사용자 다중 삭제 완료: count={}", ids.size());
    }

    /**
     * 단일 사용자 삭제
     * @param id 삭제할 사용자 ID
     */
    public void deleteUser(Long id) {
        log.debug("사용자 개별 삭제 요청: id={}", id);
        if (!userRepository.existsById(id)) {
            log.warn("삭제 대상 사용자 없음: id={}", id);
            throw new AppException(Domain.USER, ErrorCode.USER_NOT_FOUND);
        }
        userRepository.deleteById(id);
        log.debug("사용자 개별 삭제 완료: id={}", id);
    }

    /**
     * 사용자 정보 수정
     * @param id 수정할 사용자 ID
     * @param request 수정 요청 DTO
     * @return 수정된 사용자 DTO
     */
    public UserResponseDTO updateUser(Long id, UserUpdateRequestDTO request) {
        log.debug("사용자 수정 요청: id={}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(Domain.USER, ErrorCode.USER_NOT_FOUND));

        // 수정할 값 결정 (null이면 기존 값 유지)
        String newUsername = request.getUsername() != null ? request.getUsername() : user.getUsername();
        String newEmail = request.getEmail() != null ? request.getEmail() : user.getEmail();
        String newPhoneNumber = request.getPhoneNumber() != null ? request.getPhoneNumber() : user.getPhoneNumber();
        String newPassword = request.getPassword() != null
                ? passwordEncoder.encode(request.getPassword())
                : user.getPassword();

        // 중복 체크 (변경된 경우에만)
        if (!newUsername.equals(user.getUsername()) && userRepository.existsByUsername(newUsername)) {
            throw new AppException(Domain.USER, ErrorCode.USER_DUPLICATE_USERNAME, "이미 사용 중인 사용자 이름입니다.");
        }
        if (!newEmail.equals(user.getEmail()) && userRepository.existsByEmail(newEmail)) {
            throw new AppException(Domain.USER, ErrorCode.USER_DUPLICATE_EMAIL, "이미 사용 중인 이메일입니다.");
        }

        user.update(newUsername, newEmail, newPhoneNumber, newPassword);
        userRepository.save(user);

        log.debug("사용자 수정 완료: id={}", id);
        return toResponseDTO(user);
    }

    // User 엔티티를 UserResponseDTO로 변환
    private UserResponseDTO toResponseDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setRole(user.getRole().name());
        dto.setCreatedDttm(user.getCreatedDttm());
        return dto;
    }
}
