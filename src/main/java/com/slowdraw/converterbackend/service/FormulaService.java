package com.slowdraw.converterbackend.service;

import com.slowdraw.converterbackend.domain.Formula;
import com.slowdraw.converterbackend.exception.FormulaException;
import com.slowdraw.converterbackend.repository.FormulasRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FormulaService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FormulaService.class);

    private static final String FORMULA_NOT_FOUND = "Formula not found.";

    private final FormulasRepository formulasRepository;

    //constructor injection
    public FormulaService(@Lazy FormulasRepository formulasRepository) {
        this.formulasRepository = formulasRepository;
    }

    public Set<Formula> getAllFormulas() {

        return formulasRepository.findAll().stream().collect(Collectors.toSet());
    }

    public Formula getSingleFormulaInfo(String name) {

        LOGGER.info("Hit formulaService get");

        //sanity check: formula exists
        if(!formulasRepository.findById(name).isPresent())

            throw new FormulaException(FORMULA_NOT_FOUND);

        return formulasRepository.findById(name)
                .orElseThrow(() ->
                        new FormulaException(FORMULA_NOT_FOUND));
    }

    public Boolean formulaExists(String name) {

        return formulasRepository.existsById(name);
    }
}
