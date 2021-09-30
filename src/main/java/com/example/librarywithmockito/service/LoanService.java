package com.example.librarywithmockito.service;

import com.example.librarywithmockito.api.resource.dto.LoanFilterDTO;
import com.example.librarywithmockito.model.Book;
import com.example.librarywithmockito.model.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface LoanService {
    Loan save( Loan loan );

    Optional<Loan> getById(Long id);

    Loan update(Loan loan);

    Page<Loan> getLoansByBook(Book book, Pageable pageable);

    List<Loan> getAllLateLoans();

    Page<Loan> find(LoanFilterDTO dto, Pageable pageRequest);
}
