package com.ximilib.application.port;

import com.ximilib.application.dto.BookSearchCriteria;
import com.ximilib.application.dto.BookSummaryDto;
import com.ximilib.application.dto.PagedResult;
import java.util.List;

/**
 * BookQueryPort: Cổng truy vấn thông tin đầu sách và danh mục (CQRS Query Port).
 * Tách rời các truy vấn phân trang / tìm kiếm DTO đa bảng khỏi Domain BookRepository.
 */
public interface BookQueryPort {

    PagedResult<BookSummaryDto> searchBooks(BookSearchCriteria criteria, int page, int pageSize);

    List<BookSummaryDto> getDeletedBooks();
}
