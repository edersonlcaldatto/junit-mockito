package com.example.librarywithmockito.service;

import com.example.librarywithmockito.exception.BusinessException;
import com.example.librarywithmockito.model.Book;
import com.example.librarywithmockito.repository.BookRepository;
import com.example.librarywithmockito.service.impl.BookServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class BookServiceTest {

    BookService service;
    @MockBean
    BookRepository repository;

    @BeforeEach
    public void setUp(){
        this.service = new BookServiceImpl(repository);
    }

    @Test
    @DisplayName("Deve salvar um livro")
    public void shouldSaveBook(){
        //cenário
        var book = createValidBook();
        var bookSaved = Book.builder().id(11L).author("fulano").title("teste").isbn("123").build();
        when( repository.existsByIsbn(anyString())).thenReturn(false);
        when( repository.save(book) ).thenReturn(bookSaved);

        //execucao

        var savedBook = service.save(book);

        //verificacao
        assertThat(savedBook.getId()).isNotNull();
        assertThat(savedBook.getIsbn()).isEqualTo("123");
        assertThat(savedBook.getTitle()).isEqualTo("teste");
        assertThat(savedBook.getAuthor()).isEqualTo("fulano");
    }

    private Book createValidBook() {
        return Book.builder().isbn("123").author("fulano").title("teste").build();
    }


    @Test
    @DisplayName("Não deve salvar livro com isbn duplicado")
    public void shouldNotCreateBookWithDuplicatedIsbn(){
        //cenario
        var book = createValidBook();
        when(repository.existsByIsbn(anyString())).thenReturn(true);

        //execucao
        Throwable exception = catchThrowable(() -> service.save(book));

        //verificacoes
        assertThat(exception)
                .isInstanceOf(BusinessException.class)
                .hasMessage("Isbn já cadastrado");

        //Nunca deve chamar o save do repostory book
        verify(repository, never()).save(book);

    }


    @Test
    @DisplayName("Deve retornar o livro pelo id")
    public void getBookByIdTest(){
        Long id = 1l;
        Book book = createValidBook();
        book.setId(id);

        when(repository.findById(id)).thenReturn(Optional.of(book));

        //execucao
        Optional<Book> foundBook = service.getById(id);

        //verificacoes
        assertThat( foundBook.isPresent() ).isTrue();
        assertThat( foundBook.get().getId()).isEqualTo(id);
        assertThat( foundBook.get().getAuthor()).isEqualTo(book.getAuthor());
        assertThat( foundBook.get().getIsbn()).isEqualTo(book.getIsbn());
        assertThat( foundBook.get().getTitle()).isEqualTo(book.getTitle());
    }

    @Test
    @DisplayName("Deve retornar vazio ao obter livro pelo id quando ele não existir")
    public void getNotFoundBookByIdTest(){
        var id = 1L;
        when( repository.findById(id) ).thenReturn(Optional.empty());

        var foundBook = service.getById(id);

        assertThat( foundBook.isPresent()).isFalse();
    }

    @Test
    @DisplayName("Deve deletar um livro")
    public void deleteBookTest() throws IllegalAccessException {

        Book book = Book.builder().id(1L).build();

        service.delete(book);

        Mockito.verify( repository, Mockito.times(1)).delete(book);

    }

    @Test
    @DisplayName("Deve ocorrer erro ao deletar um livro inexistente")
    public void deleteInvalidBookTest() {
        Book book = new Book();

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> service.delete(book));

        Mockito.verify( repository, Mockito.never() ).delete(book);
    }


    @Test
    @DisplayName("Deve ocorrer erro ao tentar atualizar um livro inexistente.")
    public void updateInvalidBookTest(){
        Book book = new Book();

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> service.update(book));

        Mockito.verify( repository, Mockito.never() ).save(book);
    }

    @Test
    @DisplayName("Deve atualizar um livro.")
    public void updateBookTest(){
        //cenário
        long id = 1l;

        //livro a atualizar
        Book updatingBook = Book.builder().id(id).build();

        //simulacao
        Book updatedBook = createValidBook();
        updatedBook.setId(id);
        when(repository.save(updatingBook)).thenReturn(updatedBook);

        //exeucao
        Book book = service.update(updatingBook);

        //verificacoes
        assertThat(book.getId()).isEqualTo(updatedBook.getId());
        assertThat(book.getTitle()).isEqualTo(updatedBook.getTitle());
        assertThat(book.getIsbn()).isEqualTo(updatedBook.getIsbn());
        assertThat(book.getAuthor()).isEqualTo(updatedBook.getAuthor());

    }

    @Test
    @DisplayName("Deve filtrar livros pelas propriedades")
    public void findBookTest(){
        //cenario
        Book book = createValidBook();

        PageRequest pageRequest = PageRequest.of(0, 10);

        List<Book> lista = Arrays.asList(book);
        Page<Book> page = new PageImpl<Book>(lista, pageRequest, 1);
        when( repository.findAll(Mockito.any(Example.class), Mockito.any(PageRequest.class)))
                .thenReturn(page);

        //execucao
        Page<Book> result = service.find(book, pageRequest);

        //verificacoes
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).isEqualTo(lista);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);
    }

    @Test
    public void getBookByIsbnTest(){

        String isbn = "1230"
                ;
        when( repository.findByIsbn(isbn) ).thenReturn( Optional.of(Book.builder().id(1l).isbn(isbn).build() ));

        var book = service.getBookByIsbn(isbn);

        assertThat(book.isPresent()).isTrue();
        assertThat(book.get().getId()).isEqualTo(1l);
        assertThat(book.get().getIsbn()).isEqualTo(isbn);

        verify(repository, times(1)).findByIsbn(isbn);

    }

}
