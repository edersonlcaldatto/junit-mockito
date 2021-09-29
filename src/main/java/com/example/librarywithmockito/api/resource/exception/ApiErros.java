package com.example.librarywithmockito.api.resource.exception;

import com.example.librarywithmockito.exception.BusinessException;
import org.springframework.validation.BindingResult;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ApiErros {

    private List<String> erros;

    public ApiErros(BindingResult bindingResult) {
        this.erros = new ArrayList<>();

        bindingResult.getAllErrors().forEach( error -> this.erros.add(error.getDefaultMessage()));
    }

    public ApiErros(BusinessException ex) {
        this.erros = Arrays.asList(ex.getMessage());
    }

    public ApiErros(ResponseStatusException ex) {
        this.erros = Arrays.asList(ex.getReason());
    }

    public List<String> getErros() {
        return erros;
    }
}
