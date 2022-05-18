package org.example.pagamenti.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import org.example.pagamenti.model.Pagamento;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.Optional;

@ApplicationScoped
public class PagamentiRepository implements PanacheRepositoryBase<Pagamento, String> {

    @Inject
    EntityManager em;

    @Transactional
    public Pagamento save(Pagamento p) {
        if(isPersistent(p)) {
            persist(p);
            return p;
        } else {
            return em.merge(p);
        }
    }


    public Optional<Pagamento> findByTransactionId(String transactionId) {
        return find("transactionId = ?1", transactionId).firstResultOptional();
    }

}
