package org.example.ordini.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import org.example.ordini.model.Ordine;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.Optional;

@ApplicationScoped
public class OrdiniRepository implements PanacheRepositoryBase<Ordine, String> {

    @Inject
    EntityManager em;

    @Transactional
    public Ordine save(Ordine o) {
        if(isPersistent(o)) {
            persist(o);
            return o;
        } else {
            return em.merge(o);
        }
    }


    public Optional<Ordine> findByTransactionId(String transactionId) {
        return find("transactionId = ?1", transactionId).firstResultOptional();
    }
}
