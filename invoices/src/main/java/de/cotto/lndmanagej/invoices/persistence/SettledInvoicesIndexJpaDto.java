package de.cotto.lndmanagej.invoices.persistence;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "settled_invoices_index")
public class SettledInvoicesIndexJpaDto {
    @Id
    private long entityId;

    private long allSettledIndexOffset;

    public SettledInvoicesIndexJpaDto() {
        // for JPA
    }

    public long getAllSettledIndexOffset() {
        return allSettledIndexOffset;
    }

    public void setAllSettledIndexOffset(long allSettledIndexOffset) {
        this.allSettledIndexOffset = allSettledIndexOffset;
    }

    public long getEntityId() {
        return entityId;
    }

    public void setEntityId(long entityId) {
        this.entityId = entityId;
    }
}
