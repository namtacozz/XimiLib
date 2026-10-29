package com.ximilib.application.port;

import com.ximilib.application.dto.BorrowTicketDto;
import java.util.List;

/**
 * CirculationQueryPort: Cổng truy vấn phiếu mượn và lịch sử lưu hành (CQRS Query Port).
 * Trả về BorrowTicketDto phục vụ hiển thị danh sách phiếu mượn của thủ thư và bạn đọc.
 */
public interface CirculationQueryPort {

    List<BorrowTicketDto> findTickets(String keyword, Integer statusFilter);

    List<BorrowTicketDto> findTicketsByReader(String userId);
}
