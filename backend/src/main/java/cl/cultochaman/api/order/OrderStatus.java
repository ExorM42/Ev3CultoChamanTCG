package cl.cultochaman.api.order;

public enum OrderStatus {
    PENDING, PAID, SHIPPED, CANCELLED;

    /** PENDING -> PAID | CANCELLED ; PAID -> SHIPPED | CANCELLED ; SHIPPED y CANCELLED son finales. */
    public boolean canMoveTo(OrderStatus next) {
        return switch (this) {
            case PENDING -> next == PAID || next == CANCELLED;
            case PAID -> next == SHIPPED || next == CANCELLED;
            case SHIPPED, CANCELLED -> false;
        };
    }
}
