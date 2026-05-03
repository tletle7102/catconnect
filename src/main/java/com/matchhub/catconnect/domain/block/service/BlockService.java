package com.matchhub.catconnect.domain.block.service;

import com.matchhub.catconnect.domain.block.model.dto.BlockResponseDTO;
import com.matchhub.catconnect.domain.block.model.entity.UserBlock;
import com.matchhub.catconnect.domain.block.repository.BlockRepository;
import com.matchhub.catconnect.domain.user.model.entity.User;
import com.matchhub.catconnect.domain.user.repository.UserRepository;
import com.matchhub.catconnect.global.exception.AppException;
import com.matchhub.catconnect.global.exception.Domain;
import com.matchhub.catconnect.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlockService {

    private final BlockRepository blockRepository;
    private final UserRepository userRepository;

    @Transactional
    public void blockUser(String blockerUsername, Long targetUserId) {
        User blocker = findUserByUsername(blockerUsername);
        User blocked = findUserById(targetUserId);

        if (blocker.getId().equals(blocked.getId())) {
            throw new AppException(Domain.USER, ErrorCode.INVALID_REQUEST, "자기 자신을 차단할 수 없습니다.");
        }

        if (blockRepository.existsByBlockerIdAndBlockedId(blocker.getId(), blocked.getId())) {
            throw new AppException(Domain.USER, ErrorCode.INVALID_REQUEST, "이미 차단한 사용자입니다.");
        }

        blockRepository.save(new UserBlock(blocker, blocked));
    }

    @Transactional
    public void unblockUser(String blockerUsername, Long targetUserId) {
        User blocker = findUserByUsername(blockerUsername);
        UserBlock block = blockRepository.findByBlockerIdAndBlockedId(blocker.getId(), targetUserId)
                .orElseThrow(() -> new AppException(Domain.USER, ErrorCode.INVALID_REQUEST, "차단하지 않은 사용자입니다."));
        blockRepository.delete(block);
    }

    public List<BlockResponseDTO> getBlockList(String username) {
        User user = findUserByUsername(username);
        return blockRepository.findAllByBlockerId(user.getId()).stream()
                .map(BlockResponseDTO::from)
                .toList();
    }

    public boolean isBlocked(String username, Long targetUserId) {
        User user = findUserByUsername(username);
        return blockRepository.existsByBlockerIdAndBlockedId(user.getId(), targetUserId);
    }

    public boolean isBlockedBetween(Long userId1, Long userId2) {
        return blockRepository.existsBlockBetween(userId1, userId2);
    }

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(Domain.USER, ErrorCode.USER_NOT_FOUND));
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new AppException(Domain.USER, ErrorCode.USER_NOT_FOUND));
    }
}
