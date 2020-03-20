package com.slowdraw.converterbackend.assembler;

import com.slowdraw.converterbackend.controller.SiteUserController;
import com.slowdraw.converterbackend.domain.SiteUser;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class SiteUserEntityModelAssembler
        implements RepresentationModelAssembler<SiteUser, EntityModel<SiteUser>> {

    private static final String API_BASE_URL = "http://localhost:9191";

    @Override
    public EntityModel<SiteUser> toModel(SiteUser entity) {

        return new EntityModel<>(entity,
                linkTo(methodOn(SiteUserController.class).getSiteUserProfile(entity.getUsername()))
                        .withRel("getSiteUserProfile")
                );
    }

    @Override
    public CollectionModel<EntityModel<SiteUser>> toCollectionModel(Iterable<? extends SiteUser> entities) {
        return null;
    }
}
