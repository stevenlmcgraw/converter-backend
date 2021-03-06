package com.slowdraw.converterbackend.assembler;

import com.slowdraw.converterbackend.controller.SiteUserController;
import com.slowdraw.converterbackend.domain.SiteUser;
import com.slowdraw.converterbackend.exception.UserException;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class SiteUserEntityModelAssembler
        implements RepresentationModelAssembler<SiteUser, EntityModel<SiteUser>> {

    private static final String USERNAME_NOT_FOUND = "Username not found.";

    @Override
    public EntityModel<SiteUser> toModel(SiteUser entity) {

        //sanity check: received valid entity
        if(entity == null)
            throw new UserException(USERNAME_NOT_FOUND);

        return new EntityModel<>(entity,
                linkTo(methodOn(SiteUserController.class).getSiteUserProfile(entity.getUsername()))
                        .withRel("getSiteUserProfile"),
                linkTo(methodOn(SiteUserController.class)
                        .addFormulaToSiteUserFavoritesList(entity.getUsername(), null))
                        .withRel("addFormulaToFavorites"),
                linkTo(methodOn(SiteUserController.class)
                        .deleteFormulaFromUsernameFavorites(entity.getUsername(), null))
                        .withRel("deleteFormulaFromFavorites"),
                linkTo(methodOn(SiteUserController.class)
                        .deleteAllUsernameFavorites(entity.getUsername()))
                        .withRel("deleteAllFormulasFromFavorites")
                );
    }

    @Override
    public CollectionModel<EntityModel<SiteUser>> toCollectionModel(Iterable<? extends SiteUser> entities) {
        return null;
    }
}
