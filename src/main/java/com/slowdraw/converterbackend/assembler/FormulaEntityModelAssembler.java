package com.slowdraw.converterbackend.assembler;

import com.slowdraw.converterbackend.controller.FormulaController;
import com.slowdraw.converterbackend.controller.SiteUserController;
import com.slowdraw.converterbackend.domain.Formula;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class FormulaEntityModelAssembler
        implements RepresentationModelAssembler<Formula, EntityModel<Formula>> {
    @Override
    public EntityModel<Formula> toModel(Formula entity) {

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
