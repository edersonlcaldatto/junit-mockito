package com.example.librarywithmockito.model.repository;

import com.example.librarywithmockito.model.Book;
import com.example.librarywithmockito.repository.BookRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class BookRepositoryTest {

    //Testes de integração

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    BookRepository repository;

    @Test
    @DisplayName("Deve retornar verdadeiro quando existir um livro com isbn cadastrado")
    public void returnTrueWhenIsbnExists(){

        var isbn = "123";
        var book = createNewBook(isbn);
        entityManager.persist(book);

        var exists = repository.existsByIsbn(isbn);

        assertThat(exists).isTrue();

    }


    @Test
    @DisplayName("Deve retornar verdadeiro quando existir um livro com isbn cadastrado")
    public void returnFalseWhenIsbnDosentExists(){
        var isbn = "123";
        var exists = repository.existsByIsbn(isbn);
        assertThat(exists).isTrue();

    }

    @Test
    @DisplayName("Deve retornar livro pelo ID")
    public void returnTrueWhenIdExists(){
        Book book = createNewBook("123");
        entityManager.persist(book);

        var foundBook = repository.findById(book.getId());

        assertThat( foundBook.isPresent() ).isTrue();

    }

    @Test
    @DisplayName("deve deletar um livro")
    public void deleteBookTest(){
        Book book = createNewBook("123");
        entityManager.persist(book);
        Book foundBook = entityManager.find( Book.class, book.getId() );

        repository.delete(foundBook);

        Book deletedBook = entityManager.find(Book.class, book.getId());
        Assertions.assertThat(deletedBook).isNull();
    }

    private Book createNewBook(String isbn) {
        return Book.builder().author("autor").title("titulo").isbn(isbn).build();
    }

}
