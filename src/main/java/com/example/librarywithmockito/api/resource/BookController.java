package com.example.librarywithmockito.api.resource;

import com.example.librarywithmockito.api.resource.dto.BookDto;
import com.example.librarywithmockito.api.resource.dto.LoanDto;
import com.example.librarywithmockito.api.resource.exception.ApiErros;
import com.example.librarywithmockito.exception.BusinessException;
import com.example.librarywithmockito.model.Book;
import com.example.librarywithmockito.model.Loan;
import com.example.librarywithmockito.service.BookService;
import com.example.librarywithmockito.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private BookService bookService;
    private ModelMapper mapper;
    private LoanService loanService;



    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookDto create(@Valid @RequestBody BookDto dto){
        Book entity = mapper.map(dto, Book.class);
        entity = bookService.save(entity);
        return mapper.map(entity, BookDto.class);
    }

    @GetMapping("{id}")
    public BookDto get(@PathVariable Long id){
        return bookService
                .getById(id)
                .map( book -> mapper.map(book, BookDto.class)  )
                .orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND) );
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) throws IllegalAccessException {
        var book = bookService.getById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        bookService.delete(book);
    }


    @PutMapping("{id}")
    public BookDto update(@PathVariable Long id, BookDto dto){
        return bookService.getById(id).map( book -> {
                book.setAuthor(dto.getAuthor());
                book.setTitle(dto.getTitle());
                var bookUpdate = bookService.update(book);
                return mapper.map(bookUpdate, BookDto.class);
                }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }


    @GetMapping("{id}/loans")
    public Page<LoanDto> loansByBook(@PathVariable Long id, Pageable pageable){
        var book = bookService.getById(id).orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Page<Loan> result = loanService.getLoansByBook(book, pageable);

        var list = result.getContent()
                                    .stream()
                                    .map(loan -> {
                                        var loanBook = loan.getBook();
                                        var bookDto = mapper.map(loanBook, BookDto.class);
                                        var loanDto = mapper.map(bookDto, LoanDto.class);
                                        loanDto.setBook(bookDto);
                                        return loanDto;
                                    }).collect(Collectors.toList()) ;
        return new PageImpl<LoanDto>(list, pageable, result.getTotalElements());


    }

}
