package com.example.librarywithmockito.api.resource.dto;

import com.example.librarywithmockito.model.Loan;
import lombok.*;

import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookDto {

    private Long id;
    @NotEmpty
    private String title;
    @NotEmpty
    private String author;
    @NotEmpty
    private String isbn;

    @OneToMany( mappedBy = "book", fetch = FetchType.LAZY)
    private List<Loan> loans;

}
