package org.example.pagamenti.rest;

import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.Complete;
import org.eclipse.microprofile.lra.annotation.ParticipantStatus;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;
import org.example.pagamenti.model.Pagamento;
import org.example.pagamenti.repository.PagamentiRepository;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.File;
import java.net.URI;

import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_CONTEXT_HEADER;

@Path("pagamenti")
public class PagamentiRisorsa {

    @Inject
    Logger logger;

    @Inject
    PagamentiRepository repository;


    private static final Double MAX_PAGAMENTO = 1000.0;


    @LRA(value = LRA.Type.SUPPORTS, end = false)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @POST
    public Response approvaPagamento(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI lraId, Pagamento p) {
        logger.infov("Creazione pagamento {0} (lra-id {1})", p.getCodice(), lraId.toASCIIString());

        p.setTransactionId(getTransactionId(lraId));
        p.setStatus(Pagamento.Status.PENDING);

        Pagamento creato = repository.save(p);

        if(p.getTotale() > MAX_PAGAMENTO) {
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode(), "Superati limiti pagamento").build();
        }

        return Response.ok(creato).build();
    }


    @Compensate
    @PUT
    @Path("rollback")
    public Response rollback(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI lraId) {
        logger.infov("Avvio rollback pagamento (lra-id {0})", lraId.toASCIIString());
        cambiaStatoPagamentoDaIdTransazione(getTransactionId(lraId), Pagamento.Status.REJECTED);
        return Response.ok(ParticipantStatus.Compensated.name()).build();
    }


    @Complete
    @PUT
    @Path("complete")
    public Response complete(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI lraId) {
        logger.infov("Avvio completamento pagamento (lra-id {0})", lraId.toASCIIString());
        cambiaStatoPagamentoDaIdTransazione(getTransactionId(lraId), Pagamento.Status.ACCEPTED);
        return Response.ok(ParticipantStatus.Completed.name()).build();
    }

    private void cambiaStatoPagamentoDaIdTransazione(String transactionId, Pagamento.Status newStatus) {
        repository.findByTransactionId(transactionId)
                .ifPresent((p) -> {
                    logger.infov("Salvataggio pagamento in stato {0} (txn-id {1})", newStatus, transactionId);
                    p.setStatus(newStatus);
                    repository.save(p);
                });
    }


    private String getTransactionId(URI lraId) {
        return lraId.getPath().split("/")[2];
    }




}
