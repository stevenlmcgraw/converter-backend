package com.slowdraw.converterbackend.controller;

import com.slowdraw.converterbackend.assembler.ResultHistoryEntityModelAssembler;
import com.slowdraw.converterbackend.domain.ResultHistory;
import com.slowdraw.converterbackend.exception.ResultHistoryException;
import com.slowdraw.converterbackend.security.CurrentSiteUser;
import com.slowdraw.converterbackend.security.UserPrincipal;
import com.slowdraw.converterbackend.service.ResultHistoryService;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/resultHistory")
@CrossOrigin
public class ResultHistoryController {

    private ResultHistoryService resultHistoryService;
    private ResultHistoryEntityModelAssembler resultHistoryEntityModelAssembler;

    public ResultHistoryController(ResultHistoryService resultHistoryService,
                                   ResultHistoryEntityModelAssembler resultHistoryEntityModelAssembler) {
        this.resultHistoryService = resultHistoryService;
        this.resultHistoryEntityModelAssembler = resultHistoryEntityModelAssembler;
    }

    @GetMapping("/{username}/{id}")
    public EntityModel<?> getSpecificResultHistory(@PathVariable String username, @PathVariable String id) {

        return new EntityModel<>(resultHistoryEntityModelAssembler
                .toModel(resultHistoryService.findById(id)));
    }

    @GetMapping("/{username}")
    public CollectionModel<EntityModel<ResultHistory>> getUsernameResultHistory(@PathVariable String username) {

        if(resultHistoryService.findAllByUsername(username).isEmpty()) {
            throw new ResultHistoryException("No calculation/conversion result history for user.");
        }

        return new CollectionModel<>(resultHistoryService.findAllByUsername(username)
                .stream().map(result -> resultHistoryEntityModelAssembler.toModel(result))
                .collect(Collectors.toList()),
                new Link("http://localhost:9191/resultHistory").withSelfRel()
                );
    }

    @PostMapping
    public Object saveResultHistory(@CurrentSiteUser UserPrincipal currentUser, @Valid @RequestBody ResultHistory resultHistory, BindingResult bindingResult) {

        if(bindingResult.hasErrors())
            return resultHistoryService.errorMap(bindingResult);

        return new EntityModel<>(resultHistoryEntityModelAssembler
                .toModel(resultHistoryService.persistResultHistory(resultHistory)),
                new Link("http://localhost:9191/resultHistory").withRel("saveSingleResultHistory"));
    }

    @PutMapping("/{username}/{id}")
    public Object updateSingleResultHistory(@PathVariable String username,
                                            @PathVariable String id,
                                            @Valid @RequestBody ResultHistory resultHistory,
                                            BindingResult bindingResult) {

        if(bindingResult.hasErrors())
            return resultHistoryService.errorMap(bindingResult);

        return new EntityModel<>(resultHistoryEntityModelAssembler
                .toModel(resultHistoryService.updateResultHistory(resultHistory, username, id)));
    }


    @DeleteMapping("/delete/{username}/{id}")
    public ResponseEntity<?> deleteSingleResultHistory(@PathVariable String username, @PathVariable String id) {

        resultHistoryService.deleteSingleResultHistory(id);

        return new ResponseEntity<String>("Results from conversion/calculation deleted.", HttpStatus.OK);
    }

    @DeleteMapping("/delete/{username}")
    public ResponseEntity<?> deleteUsernameResultHistory(@PathVariable String username) {

        resultHistoryService.deleteUsernameAllResultHistory(username);

        return new ResponseEntity<String>("All conversion/calculation history deleted.", HttpStatus.OK);
    }
}
