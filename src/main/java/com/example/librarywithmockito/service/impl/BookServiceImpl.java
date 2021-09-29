package com.example.librarywithmockito.service.impl;

import com.example.librarywithmockito.exception.BusinessException;
import com.example.librarywithmockito.model.Book;
import com.example.librarywithmockito.repository.BookRepository;
import com.example.librarywithmockito.service.BookService;
import lombok.SneakyThrows;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class BookServiceImpl implements BookService {

    private BookRepository bookRepository;

    public BookServiceImpl(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public Book save(Book entity) {
        if(bookRepository.existsByIsbn(entity.getIsbn())){
            throw new BusinessException("Isbn já cadastrado");
        }
        return bookRepository.save(entity);
    }

    @Override
    public Optional<Book> getById(Long id) {
        return this.bookRepository.findById(id);
    }

    @SneakyThrows
    @Override
    public Book update(Book book)  {
        if(book == null || book.getId() == null){
            throw new IllegalArgumentException("Book id cant be null");
        }
        return bookRepository.save(book);
    }

    @Override
    public Page<Book> find(Book filter, Pageable pageRequest) {
        Example<Book> example = Example.of(filter,
                ExampleMatcher
                        .matching()
                        .withIgnoreCase()
                        .withIgnoreNullValues()
                        .withStringMatcher( ExampleMatcher.StringMatcher.CONTAINING )
        ) ;
        return bookRepository.findAll(example, pageRequest);
    }

    @Override
    public void delete(Book book) {
        if(book == null || book.getId() == null){
            throw new IllegalArgumentException("Book id cant be null");
        }
        bookRepository.delete(book);
    }
}
