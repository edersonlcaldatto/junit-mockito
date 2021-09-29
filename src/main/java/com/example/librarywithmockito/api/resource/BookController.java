package com.example.librarywithmockito.api.resource;

import com.example.librarywithmockito.api.resource.dto.BookDto;
import com.example.librarywithmockito.api.resource.exception.ApiErros;
import com.example.librarywithmockito.exception.BusinessException;
import com.example.librarywithmockito.model.Book;
import com.example.librarywithmockito.service.BookService;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private BookService bookService;
    private ModelMapper mapper;

    public BookController(BookService bookService, ModelMapper mapper) {
        this.bookService = bookService;
        this.mapper = mapper;
    }


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

    //*******/////

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErros handleValidationException(MethodArgumentNotValidException ex){
        var bindingResult = ex.getBindingResult();
        return new ApiErros(bindingResult);
    }


    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErros handleBusinessException(BusinessException ex){
        return new ApiErros(ex);
    }

}
