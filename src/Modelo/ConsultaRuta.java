package Modelo;

public class ConsultaRuta {
    private final Estacion origen;
    private final Estacion destino;
    private final int minutos;

    public ConsultaRuta(Estacion origen, Estacion destino, int minutos) {
        this.origen = origen;
        this.destino = destino;
        this.minutos = minutos;
    }

    public Estacion getOrigen() {
        return origen;
    }

    public Estacion getDestino() {
        return destino;
    }

    public int getMinutos() {
        return minutos;
    }

    @Override
    public String toString() {
        return "Origen: " + origen.getNombre() + " -> Destino: " + destino.getNombre()
                + " | Minutos: " + minutos;
    }

}
