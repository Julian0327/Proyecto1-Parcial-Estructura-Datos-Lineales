package estructuras.grafo;

/**
 * Conexión dirigida hacia una estación, con su tiempo de viaje en minutos.
 * Es inmutable. El destino se guarda como índice interno del {@link Grafo}
 * para que los algoritmos accedan a los vecinos en O(1).
 */
public final class Arista {

    private final int destino;
    private final int minutos;

    public Arista(int destino, int minutos) {
        if (destino < 0) {
            throw new IllegalArgumentException("El índice de destino no puede ser negativo.");
        }
        if (minutos <= 0) {
            throw new IllegalArgumentException("El tiempo de viaje debe ser mayor que cero.");
        }
        this.destino = destino;
        this.minutos = minutos;
    }

    /** Índice interno de la estación a la que lleva esta arista. */
    public int getDestino() {
        return destino;
    }

    public int getMinutos() {
        return minutos;
    }

    @Override
    public boolean equals(Object otro) {
        if (this == otro) {
            return true;
        }
        if (otro == null || getClass() != otro.getClass()) {
            return false;
        }
        Arista que = (Arista) otro;
        return destino == que.destino && minutos == que.minutos;
    }

    @Override
    public int hashCode() {
        return 31 * destino + minutos;
    }

    @Override
    public String toString() {
        return "->" + destino + " (" + minutos + " min)";
    }
}
