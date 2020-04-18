package com.slowdraw.converterbackend.assembler;

import com.slowdraw.converterbackend.controller.FormulaController;
import com.slowdraw.converterbackend.controller.SiteUserController;
import com.slowdraw.converterbackend.domain.Formula;
import com.slowdraw.converterbackend.exception.FormulaException;
import com.slowdraw.converterbackend.exception.FormulaNotFoundResponse;
import com.slowdraw.converterbackend.exception.UserException;
import com.slowdraw.converterbackend.repository.FormulasRepository;
import com.slowdraw.converterbackend.repository.SiteUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class FormulaEntityModelAssembler
        implements RepresentationModelAssembler<Formula, EntityModel<Formula>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FormulaEntityModelAssembler.class);

    private final String FORMULA_NOT_FOUND = "Formula not found.";

    private final FormulasRepository formulasRepository;

    public FormulaEntityModelAssembler(FormulasRepository formulasRepository) {
        this.formulasRepository = formulasRepository;
    }

    @Override
    public EntityModel<Formula> toModel(Formula entity) {

        LOGGER.info("Hit formula entity assembler toModel");

        //sanity check: received valid entity
        if(entity == null)
            throw new FormulaException(FORMULA_NOT_FOUND);

        return new EntityModel<>(entity,
                linkTo(methodOn(FormulaController.class).getSingleFormulaInfo(entity.getFormulaName()))
                        .withRel("getFormulaInfo"),
                linkTo(methodOn(FormulaController.class).getAllFormulas())
                        .withRel("getAllFormulas"));
    }

    @Override
    public CollectionModel<EntityModel<Formula>> toCollectionModel(Iterable<? extends Formula> entities) {
        return null;
    }
}
