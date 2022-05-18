package org.example.ordini.rest;


import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.Complete;
import org.eclipse.microprofile.lra.annotation.ParticipantStatus;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.example.ordini.model.Ordine;
import org.example.ordini.repository.OrdiniRepository;
import org.example.ordini.utils.PagamentiServiceClient;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import java.io.File;
import java.net.URI;

import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_CONTEXT_HEADER;

@Path("ordini")
public class OrdiniRisorsa {


    @Inject
    Logger logger;

    @Inject
    OrdiniRepository repository;


    @Inject
    @RestClient
    PagamentiServiceClient pagamentiServiceClient;



    @LRA(LRA.Type.REQUIRES_NEW)
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response creaNuovoOrdine(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI lraId, Ordine o) {
        logger.infov("Creazione ordine {0} (lra-id {1})", o.getCodice(), lraId.toASCIIString());

        o.setTransactionId(getTransactionId(lraId));
        o.setStatus(Ordine.Status.PENDING);

        Ordine creato = repository.save(o);

        try {
            avviaPagamento(creato);
        } catch(Exception ex) {
            logger.warnv("Pagamento fallito {0}", ex.getMessage());
            return Response.serverError().build();
        }

        return Response.ok(creato).build();
    }


    @Compensate
    @PUT
    @Path("rollback")
    public Response rollback(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI lraId) {
        logger.infov("Avvio rollback ordine (lra-id {0})", lraId.toASCIIString());
        cambiaStatoOrdineDaIdTransazione(getTransactionId(lraId), Ordine.Status.REJECTED);
        return Response.ok(ParticipantStatus.Compensated.name()).build();
    }


    @Complete
    @Path("/complete")
    @PUT
    public Response complete(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI lraId) {
        logger.infov("Avvio completamento ordine (lra-id {0})", lraId.toASCIIString());
        cambiaStatoOrdineDaIdTransazione(getTransactionId(lraId), Ordine.Status.ACCEPTED);
        return Response.ok(ParticipantStatus.Completed.name()).build();
    }



    private PagamentiServiceClient.Pagamento avviaPagamento(Ordine ordine) {
        PagamentiServiceClient.Pagamento pagamento = new PagamentiServiceClient.Pagamento();
        pagamento.codice = ordine.getCodice();
        pagamento.totale = ordine.getTotale();

        return pagamentiServiceClient.approvaPagamento(pagamento);
    }


    private void cambiaStatoOrdineDaIdTransazione(String transactionId, Ordine.Status newStatus) {
        repository.findByTransactionId(transactionId)
                .ifPresent((o) -> {
                    logger.infov("Salvataggio ordine in stato {0} (txn-id {1})", newStatus, transactionId);
                    o.setStatus(newStatus);
                    repository.save(o);
                });
    }


    private String getTransactionId(URI lraId) {
        return lraId.getPath().split("/")[2];
    }


}
