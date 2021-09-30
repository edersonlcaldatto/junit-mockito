package com.example.librarywithmockito.api.resource;

import com.example.librarywithmockito.api.resource.dto.BookDto;
import com.example.librarywithmockito.api.resource.dto.LoanDto;
import com.example.librarywithmockito.api.resource.dto.LoanFilterDTO;
import com.example.librarywithmockito.api.resource.dto.ReturnedLoanDTO;
import com.example.librarywithmockito.model.Book;
import com.example.librarywithmockito.model.Loan;
import com.example.librarywithmockito.service.BookService;
import com.example.librarywithmockito.service.LoanService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanService service;
    private final BookService bookService;
    private final ModelMapper modelMapper;

    public LoanController(LoanService service, BookService bookService, ModelMapper modelMapper) {
        this.service = service;
        this.bookService = bookService;
        this.modelMapper = modelMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long create(@RequestBody LoanDto dto) {
        Book book = bookService
                .getBookByIsbn(dto.getIsbn())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, "Book not found for passed isbn"));
        Loan entity = Loan.builder()
                .book(book)
                .customer(dto.getCustomer())
                .loanDate(LocalDate.now())
                .build();

        entity = service.save(entity);
        return entity.getId();
    }

    @PatchMapping("{id}")
    public void returnBook(
            @PathVariable Long id,
            @RequestBody ReturnedLoanDTO dto) {
        Loan loan = service.getById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        loan.setReturned(dto.getReturned());
        service.update(loan);
    }

    @GetMapping
    public Page<LoanDto> find(LoanFilterDTO dto, Pageable pageRequest) {
        Page<Loan> result = service.find(dto, pageRequest);
        List<LoanDto> loans = result
                .getContent()
                .stream()
                .map(entity -> {

                    Book book = entity.getBook();
                    BookDto bookDTO = modelMapper.map(book, BookDto.class);
                    LoanDto loanDTO = modelMapper.map(entity, LoanDto.class);
                    loanDTO.setBook(bookDTO);
                    return loanDTO;

                }).collect(Collectors.toList());
        return new PageImpl<LoanDto>(loans, pageRequest, result.getTotalElements());
    }

}
