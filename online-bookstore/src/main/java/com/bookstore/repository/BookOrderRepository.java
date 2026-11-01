
package com.bookstore.repository;

import com.bookstore.model.BookOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BookOrderRepository extends JpaRepository<BookOrder, Long> {

    List<BookOrder> findByCustomer_EmailOrderByOrderDateDesc(String email);
}
