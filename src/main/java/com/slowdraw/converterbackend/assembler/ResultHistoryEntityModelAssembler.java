package com.slowdraw.converterbackend.assembler;

import com.slowdraw.converterbackend.controller.ResultHistoryController;
import com.slowdraw.converterbackend.domain.ResultHistory;
import com.slowdraw.converterbackend.exception.ResultHistoryException;
import com.slowdraw.converterbackend.exception.UserException;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ResultHistoryEntityModelAssembler
        implements RepresentationModelAssembler<ResultHistory, EntityModel<ResultHistory>> {

    private final String RESULT_HISTORY_NOT_FOUND = "Given result not found.";

    @Override
    public EntityModel<ResultHistory> toModel(ResultHistory entity) {

        //sanity check: received valid entity
        if(entity == null)
            throw new ResultHistoryException(RESULT_HISTORY_NOT_FOUND);

        return new EntityModel<>(entity,
                linkTo(methodOn(ResultHistoryController.class)
                        .getSpecificResultHistory(entity.getUsername(), entity.getId()))
                        .withSelfRel(),
                linkTo(methodOn(ResultHistoryController.class)
                        .getUsernameResultHistory(entity.getUsername()))
                        .withRel("getAllUsernameResultHistory"),
                linkTo(methodOn(ResultHistoryController.class)
                        .deleteSingleResultHistory(entity.getUsername(), entity.getId()))
                        .withRel("deleteSpecificResultHistory"),
                linkTo(methodOn(ResultHistoryController.class)
                        .deleteUsernameResultHistory(null))
                        .withRel("deleteAllUsernameResultHistory")
        );
    }

    @Override
    public CollectionModel<EntityModel<ResultHistory>> toCollectionModel(Iterable<? extends ResultHistory> entities) {
        return null;
    }
}
