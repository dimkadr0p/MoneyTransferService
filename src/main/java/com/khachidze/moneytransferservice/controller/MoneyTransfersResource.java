package com.khachidze.moneytransferservice.controller;

import com.khachidze.moneytransferservice.dto.TransferMoneyRequestDto;
import com.khachidze.moneytransferservice.entity.MoneyTransfersEntity;
import com.khachidze.moneytransferservice.exception.CommissionOperationException;
import com.khachidze.moneytransferservice.exception.InsufficientMoneyException;
import com.khachidze.moneytransferservice.exception.TransferMoneyFailedException;
import com.khachidze.moneytransferservice.exception.UserNotFoundException;
import com.khachidze.moneytransferservice.service.MoneyTransfersService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;


@Path("/transfer")
public class MoneyTransfersResource {

    @Inject
    private MoneyTransfersService moneyTransfersService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response transferMoney(TransferMoneyRequestDto transferRequestDto) {
        try {
            moneyTransfersService.transferMoney(transferRequestDto);
            return Response.ok("Transaction successful").build();
        } catch (UserNotFoundException | CommissionOperationException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (InsufficientMoneyException e) {
            return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @GET
    @Produces("application/json")
    public Response getHistoryTransaction(@QueryParam("phone") String phoneNumber) {
        List<MoneyTransfersEntity> transactions = moneyTransfersService.getHistoryTransaction(phoneNumber);
        return Response.ok(transactions).build();
    }


    @GET
    @Path("/all")
    @Produces("application/json")
    public Response getAllMoneyTransfers() {
        return Response.ok(moneyTransfersService.getAllMoneyTransfers()).build();
    }

}