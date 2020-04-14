package com.slowdraw.converterbackend.controller;

import com.slowdraw.converterbackend.assembler.FormulaEntityModelAssembler;
import com.slowdraw.converterbackend.domain.Formula;
import com.slowdraw.converterbackend.exception.FormulaException;
import com.slowdraw.converterbackend.service.FormulaService;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/formulas")
@CrossOrigin
public class FormulaController {

    private final FormulaService formulaService;
    private final FormulaEntityModelAssembler formulaEntityModelAssembler;

    //constructor injection
    public FormulaController(FormulaService formulaService, FormulaEntityModelAssembler formulaEntityModelAssembler) {
        this.formulaService = formulaService;
        this.formulaEntityModelAssembler = formulaEntityModelAssembler;
    }

    @GetMapping
    public CollectionModel<EntityModel<Formula>> getAllFormulas() {

        return new CollectionModel<>(formulaService.getAllFormulas()
                .stream().map(result -> formulaEntityModelAssembler.toModel(result))
                .collect(Collectors.toSet()));
    }

    @GetMapping("/{name}")
    public EntityModel<?> getSingleFormulaInfo(@PathVariable(value = "name") String name) {

        if(!formulaService.formulaExists(name)) {
            throw new FormulaException("I am sorry to report that formula name exists not, my friend.");
        }

        return new EntityModel<>(formulaEntityModelAssembler
                .toModel(formulaService.getSingleFormulaInfo(name)));
    }
}
