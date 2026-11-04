package com.ximilib.application.port;

import com.ximilib.application.dto.PatronDto;
import com.ximilib.application.dto.UserAccountDto;
import java.util.List;

public interface UserQueryPort {

    List<PatronDto> searchPatrons(String keyword);

    List<UserAccountDto> getAllUsers(String keyword, String roleCode);
}
