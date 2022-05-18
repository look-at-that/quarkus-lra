package org.example.ordini.utils;


import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("pagamenti")
@RegisterRestClient
public interface PagamentiServiceClient {

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    Pagamento approvaPagamento(Pagamento pagamento);


    class Pagamento {
        public String codice;
        public Double totale;
    }



}
