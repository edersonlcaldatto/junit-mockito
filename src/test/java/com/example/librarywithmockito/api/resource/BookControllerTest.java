package com.example.librarywithmockito.api.resource;

import com.example.librarywithmockito.api.resource.dto.BookDto;
import com.example.librarywithmockito.exception.BusinessException;
import com.example.librarywithmockito.model.Book;
import com.example.librarywithmockito.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest
@AutoConfigureMockMvc
public class BookControllerTest {

    private static String BOOK_API = "/api/books";

    @Autowired
    MockMvc mvc;

    @MockBean
    BookService bookService;

    @Test
    @DisplayName("Deve criar um livro com suceso")
    public void createBookTest() throws Exception {

        BookDto bookDto = getBookDto();

        var savedBook = Book.builder().id(10L).author("Ederson").title("Lalalala").isbn("001").build();

        BDDMockito.given(bookService.save(any(Book.class))).willReturn(savedBook);
        String json = new ObjectMapper().writeValueAsString(bookDto);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);
        mvc
                .perform(request)
                .andExpect( status().isCreated() )
                .andExpect( jsonPath("id").value(10L) )
                .andExpect( jsonPath("title").value(bookDto.getTitle()) )
                .andExpect( jsonPath("author").value(bookDto.getAuthor()) )
                .andExpect( jsonPath("isbn").value(bookDto.getIsbn()) );

    }

    private BookDto getBookDto() {
        BookDto bookDto = BookDto.builder().author("Ederson").title("Lalalala").isbn("001").build();
        return bookDto;
    }

    @Test
    @DisplayName("Deve lançar erro de validação quando não houver dados suficientes para criação")
    public void createInvalidBookTest() throws Exception {
        String json = new ObjectMapper().writeValueAsString(new BookDto());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);
        mvc
                .perform(request)
                .andExpect( status().isBadRequest())
                .andExpect( jsonPath("erros", hasSize(3)));
    }


    @Test
    @DisplayName("Deve lançar erro ao tentar cadastrar livro com isbn já informado.")
    public void shouldValidateBookWithDuplicatedIsbn() throws Exception {
        var bookDto = getBookDto();

        String json = new ObjectMapper().writeValueAsString(bookDto);
        String messagemErro = "Isbn já cadastrado";

        BDDMockito.given( bookService.save(any(Book.class)))
                .willThrow(new BusinessException(messagemErro));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform( request )
                .andExpect( status().isBadRequest() )
                .andExpect( jsonPath("erros", hasSize(1)))
                .andExpect( jsonPath("erros[0]").value(messagemErro));

    }

    @Test
    @DisplayName("Deve obter informaçoes de um livro")
    public void getBookDetailstest() throws Exception {

        Long id = 1l;

        var book = Book.builder()
                .id(id)
                .title(getBookDto().getTitle())
                .author(getBookDto().getAuthor())
                .isbn(getBookDto().getIsbn())
                .build();

        BDDMockito.given( bookService.getById(id)).willReturn(Optional.of(book));

        //
        var requestBuilder = MockMvcRequestBuilders
                .get(BOOK_API.concat("/"+id))
                .accept(MediaType.APPLICATION_JSON);

        mvc
                .perform( requestBuilder)
                .andExpect( status().isOk() )
                .andExpect( jsonPath("id").value(id) )
                .andExpect( jsonPath("title").value(getBookDto().getTitle()) )
                .andExpect( jsonPath("author").value(getBookDto().getAuthor()) )
                .andExpect( jsonPath("isbn").value(getBookDto().getIsbn()) );

    }

    @Test
    @DisplayName("Deve retornar resource not found quando o livro procurado não existir")
    public void bookNotFoundTest() throws Exception {

        BDDMockito.given( bookService.getById(anyLong())).willReturn(Optional.empty());

        //
        var requestBuilder = MockMvcRequestBuilders
                .get(BOOK_API.concat("/" + 1))
                .accept(MediaType.APPLICATION_JSON);

        mvc
                .perform( requestBuilder)
                .andExpect( status().isNotFound() );
    }

    @Test
    @DisplayName("Deve deletar um livro")
    public void shouldDeleteBook() throws Exception {

        BDDMockito.given( bookService.getById(anyLong())).willReturn( Optional.of(Book.builder().id(1L).build()));

        var requestBuilder = MockMvcRequestBuilders
                .delete(BOOK_API.concat("/" + 1));

        mvc
                .perform(requestBuilder)
                .andExpect( status().isNoContent());
    }

    @Test
    @DisplayName("Deve retornar resource not found quando nao encotnrar um livro para deletar")
    public void shouldNotFoundBookToDelete() throws Exception {

        BDDMockito.given( bookService.getById(anyLong())).willReturn( Optional.empty() );

        var requestBuilder = MockMvcRequestBuilders
                .delete(BOOK_API.concat("/" + 1));

        mvc
                .perform(requestBuilder)
                .andExpect( status().isNotFound());
    }

    @Test
    @DisplayName("Deve atualizar um livro")
    public void updateBookTest() throws Exception {
        var id = 1L;
        String json = new ObjectMapper().writeValueAsString(getBookDto());

        Book updatingBook = Book.builder().id(1L).author("nao sei").title("menos ainda").isbn("001").build();
        BDDMockito.given( bookService.getById( id )).willReturn( Optional.of(updatingBook) );
        Book updateBook = Book.builder().id(1L).author("Ederson").title("Lalalala").isbn("001").build();
        BDDMockito.given( bookService.update( updatingBook )).willReturn( updateBook );

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(BOOK_API.concat("/" + 1))
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);
        mvc
                .perform(request)
                .andExpect( status().isOk() )
                .andExpect( jsonPath("id").value(id) )
                .andExpect( jsonPath("title").value(getBookDto().getTitle()) )
                .andExpect( jsonPath("author").value(getBookDto().getAuthor()) )
                .andExpect( jsonPath("isbn").value("001") );
    }

    @Test
    @DisplayName("Deve retornar 404 ao tentar atualizar o livro")
    public void updateInexistentBookTest() throws Exception {
        String json = new ObjectMapper().writeValueAsString(getBookDto());

        BDDMockito.given( bookService.getById( anyLong() )).willReturn( Optional.empty() );


        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(BOOK_API.concat("/" + 1))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);
        mvc
                .perform(request)
                .andExpect( status().isNotFound() );
    }



}
