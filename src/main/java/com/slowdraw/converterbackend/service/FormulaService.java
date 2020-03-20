package com.slowdraw.converterbackend.service;

import com.slowdraw.converterbackend.domain.Formula;
import com.slowdraw.converterbackend.exception.FormulaException;
import com.slowdraw.converterbackend.repository.FormulasRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FormulaService {

    private FormulasRepository formulasRepository;

    //constructor injection
    public FormulaService(@Lazy FormulasRepository formulasRepository) {
        this.formulasRepository = formulasRepository;
    }

    public Set<Formula> getAllFormulas() {

        return formulasRepository.findAll().stream().collect(Collectors.toSet());
    }

    public Formula getSingleFormulaInfo(String name) {

        return formulasRepository.findById(name)
                .orElseThrow(() ->
                        new FormulaException("No formula by that name exists around he-yump."));
    }

    public Boolean formulaExists(String name) {

        return formulasRepository.existsById(name);
    }
}
