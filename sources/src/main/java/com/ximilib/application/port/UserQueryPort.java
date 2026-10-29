package com.ximilib.application.port;

import com.ximilib.application.dto.PatronDto;
import com.ximilib.application.dto.UserAccountDto;
import java.util.List;

/**
 * UserQueryPort: Cổng truy vấn thông tin bạn đọc và tài khoản người dùng (CQRS Query Port).
 * Tách rời các truy vấn chiếu Read-Model / DTO khỏi Domain UserRepository.
 */
public interface UserQueryPort {

    List<PatronDto> searchPatrons(String keyword);

    List<UserAccountDto> getAllUsers(String keyword, String roleCode);
}
