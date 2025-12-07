package com.matchhub.catconnect.domain.user.service;

import com.matchhub.catconnect.domain.user.model.dto.UserRequestDTO;
import com.matchhub.catconnect.domain.user.model.dto.UserResponseDTO;
import com.matchhub.catconnect.domain.user.model.entity.User;
import com.matchhub.catconnect.domain.user.model.enums.Role;
import com.matchhub.catconnect.domain.user.repository.UserRepository;
import com.matchhub.catconnect.global.exception.AppException;
import com.matchhub.catconnect.global.exception.Domain;
import com.matchhub.catconnect.global.exception.ErrorCode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Validator validator;

    // 생성자를 통한 의존성 주입
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, Validator validator) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.validator = validator;
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
     * 새 사용자 생성
     * @param requestDTO 사용자 요청 데이터
     * @return 생성된 사용자 DTO
     */
    public UserResponseDTO createUser(UserRequestDTO requestDTO) {
        log.debug("사용자 생성 요청: username={}, email={}", requestDTO.getUsername(), requestDTO.getEmail());
        // 사용자 이름 중복 확인
        if (userRepository.findByUsername(requestDTO.getUsername()).isPresent()) {
            log.warn("사용자 이름 중복: username={}", requestDTO.getUsername());
            throw new AppException(Domain.USER, ErrorCode.USER_ALREADY_EXISTS);
        }
        // 이메일 중복 확인
        if (userRepository.findByEmail(requestDTO.getEmail()).isPresent()) {
            log.warn("이메일 중복: email={}", requestDTO.getEmail());
            throw new AppException(Domain.USER, ErrorCode.USER_EMAIL_ALREADY_EXISTS);
        }
        // 사용자 엔티티 생성 및 비밀번호 암호화
        User user = new User(
                requestDTO.getUsername(),
                requestDTO.getEmail(),
                passwordEncoder.encode(requestDTO.getPassword()),
                Role.USER
        );
        // 생성된 엔티티 유효성 검증
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            log.warn("사용자 엔티티 유효성 검증 실패: errors={}", errorMessage);
            throw new AppException(Domain.USER, ErrorCode.INVALID_REQUEST, errorMessage);
        }
        User savedUser = userRepository.save(user);
        log.debug("사용자 생성 완료: id={}", savedUser.getId());
        return toResponseDTO(savedUser);
    }

    /**
     * 사용자 정보 수정
     * @param id 사용자 ID
     * @param requestDTO 수정 요청 데이터
     * @param username 현재 사용자 이름
     * @return 수정된 사용자 DTO
     */
    public UserResponseDTO updateUser(Long id, UserRequestDTO requestDTO, String username) {
        log.debug("사용자 수정 요청: id={}, username={}", id, username);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(Domain.USER, ErrorCode.USER_NOT_FOUND));
        // 권한 확인
        if (!user.getUsername().equals(username)) {
            log.warn("사용자 수정 권한 없음: id={}, username={}", id, username);
            throw new AppException(Domain.AUTH, ErrorCode.ACCESS_DENIED);
        }
        // 중복 확인 (username, email)
        if (!user.getUsername().equals(requestDTO.getUsername()) &&
                userRepository.findByUsername(requestDTO.getUsername()).isPresent()) {
            log.warn("사용자 이름 중복: username={}", requestDTO.getUsername());
            throw new AppException(Domain.USER, ErrorCode.USER_ALREADY_EXISTS);
        }
        if (!user.getEmail().equals(requestDTO.getEmail()) &&
                userRepository.findByEmail(requestDTO.getEmail()).isPresent()) {
            log.warn("이메일 중복: email={}", requestDTO.getEmail());
            throw new AppException(Domain.USER, ErrorCode.USER_EMAIL_ALREADY_EXISTS);
        }
        // 사용자 정보 수정
        // user.update()는 현재 메모리에 로드된 User 엔티티 객체의 상태(필드 값)를 변경
        user.update(
                requestDTO.getUsername(),
                requestDTO.getEmail(),
                passwordEncoder.encode(requestDTO.getPassword())
        );
        // 업데이트 된 엔티티 유효성 검증
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            log.warn("사용자 엔티티 유효성 검증 실패: errors={}", errorMessage);
            throw new AppException(Domain.USER, ErrorCode.INVALID_REQUEST, errorMessage);
        }
        User updatedUser = userRepository.save(user);
        log.debug("사용자 수정 완료: id={}", id);
        return toResponseDTO(updatedUser);
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

    // User 엔티티를 UserResponseDTO로 변환
    private UserResponseDTO toResponseDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole().name());
        dto.setCreatedDttm(user.getCreatedDttm());
        return dto;
    }
}
