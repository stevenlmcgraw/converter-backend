package com.slowdraw.converterbackend.assembler;

import com.slowdraw.converterbackend.controller.ResultHistoryController;
import com.slowdraw.converterbackend.domain.ResultHistory;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ResultHistoryEntityModelAssembler
        implements RepresentationModelAssembler<ResultHistory, EntityModel<ResultHistory>> {


    @Override
    public EntityModel<ResultHistory> toModel(ResultHistory entity) {
        return new EntityModel<>(entity,
                linkTo(methodOn(ResultHistoryController.class).getSpecificResultHistory(entity.getUsername(), entity.getId()))
                        .withSelfRel(),
                linkTo(methodOn(ResultHistoryController.class).getUsernameResultHistory(entity.getUsername()))
                        .withRel("getAllUsernameResultHistory"),
                linkTo(methodOn(ResultHistoryController.class)
                        .deleteSingleResultHistory(entity.getUsername(), entity.getId()))
                        .withRel("deleteSpecificResultHistory")
//                new Link("http://localhost:9191/resultHistory/{username}/{id}" + entity.getId(),
//                        entity.getUsername()).withRel("updateSpecificUsernameResult"),
//                new Link("http://localhost:9191/resultHistory" + entity).withRel("saveResultHistory")
        );
    }

    @Override
    public CollectionModel<EntityModel<ResultHistory>> toCollectionModel(Iterable<? extends ResultHistory> entities) {
        return null;
    }
}
