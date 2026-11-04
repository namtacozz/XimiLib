package com.ximilib.application.port;

import com.ximilib.application.dto.BorrowTicketDto;
import java.util.List;

public interface CirculationQueryPort {

    List<BorrowTicketDto> findTickets(String keyword, Integer statusFilter);

    List<BorrowTicketDto> findTicketsByReader(String userId);
}
