package Modelo;

/**
 * Ordena estaciones por nombre con Merge Sort recursivo.
 * Complejidad: O(n log n) en tiempo, O(n) en memoria auxiliar. Es estable.
 */
public final class OrdenadorEstaciones {

    private OrdenadorEstaciones() {
    }

    public static void ordenarPorNombre(Estacion[] estaciones) {
        if (estaciones.length < 2) {
            return;
        }
        Estacion[] auxiliar = new Estacion[estaciones.length];
        mergeSort(estaciones, auxiliar, 0, estaciones.length - 1);
    }

    private static void mergeSort(Estacion[] datos, Estacion[] auxiliar, int izq, int der) {
        if (izq >= der) {
            return;
        }
        int medio = izq + (der - izq) / 2;
        mergeSort(datos, auxiliar, izq, medio);
        mergeSort(datos, auxiliar, medio + 1, der);
        mezclar(datos, auxiliar, izq, medio, der);
    }

    private static void mezclar(Estacion[] datos, Estacion[] auxiliar, int izq, int medio, int der) {
        for (int i = izq; i <= der; i++) {
            auxiliar[i] = datos[i];
        }
        int i = izq;
        int j = medio + 1;
        int k = izq;
        while (i <= medio && j <= der) {
            if (auxiliar[i].getNombre().compareTo(auxiliar[j].getNombre()) <= 0) {
                datos[k++] = auxiliar[i++];
            } else {
                datos[k++] = auxiliar[j++];
            }
        }
        while (i <= medio) {
            datos[k++] = auxiliar[i++];
        }
        // La mitad derecha restante ya está en su lugar.
    }
}