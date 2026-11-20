# Proyecto 1 — Rutas de Metro

Sistema de metro desarrollado en **Java** que modela estaciones, conexiones entre ellas y pasajeros. Permite registrar estaciones, conectar líneas con tiempos de viaje en minutos, simular pasajeros esperando en el andén y calcular la **ruta más corta** entre dos estaciones, mostrando el camino, el tiempo total y un historial de consultas recientes.

> **Importante:** todas las estructuras de datos están implementadas desde cero. No se usan las colecciones de Java (`List`, `Stack`, `Queue`, `HashMap`, `Hashtable`, `TreeMap`, `PriorityQueue`, etc.).

## Integrantes

| Integrante | Responsabilidad |
|---|---|
| Julian Aldemar Perez | modelo, excepciones, datos demo, persistencia, README |
| Maria de los Angeles Gomez | estructuras lineales (lista enlazada, cola, pila) |
| Nelson Escobar | tabla hash y árbol binario de búsqueda |
| Ximena Talaga | grafo, cola de prioridad y algoritmos de ruta |
| David Santiago Pachecho | servicio, interfaz (consola y Swing) y Main |

## Video de demostración

_Pegar aquí el enlace de YouTube (mínimo 7 minutos)_

## Estructuras de datos y problema que resuelven

| Estructura | Elemento | Problema que resuelve |
|---|---|---|
| Lista enlazada | `Estacion` en una línea | Recorrer estaciones sin arreglo fijo; insertar y eliminar paradas |
| Cola | `Pasajero` | Orden FIFO de quienes esperan subir al próximo tren |
| Pila | `ConsultaRuta` | Historial de rutas pedidas; ver la última consulta o deshacer |
| Tabla hash (encadenamiento) | `idPasajero` → pasajero | Alta y búsqueda de pasajeros en O(1) promedio |
| Árbol binario de búsqueda | `codigoLinea` → `LineaMetro` | Buscar, insertar y listar líneas en orden |
| Grafo (lista de adyacencia) | Estaciones y minutos | Red del metro y cálculo de ruta mínima |
| Cola de prioridad (min-heap) | Estaciones por distancia | Selección eficiente del siguiente nodo en Dijkstra |

## Algoritmos de ruta

- **BFS:** se usa cuando todos los pesos son 1.
- **Dijkstra:** se usa cuando los tiempos entre estaciones son distintos.

Ambos implementan la interfaz `AlgoritmoRuta`, por lo que `MetroService` puede cambiar de algoritmo sin modificarse.

## Estructura del proyecto

```
src/
├── modelo/              Persona, Pasajero, Estacion, EstacionTransbordo,
│                        LineaMetro, ConsultaRuta, ResultadoRuta
├── excepciones/         ExcepcionMetro, ColaVaciaException,
│                        EstacionNoExisteException
├── estructuras/
│   ├── lineales/        NodoLista, ListaEnlazada, ICola, NodoCola, Cola,
│   │                    IPila, NodoPila, Pila
│   ├── hash/            IMapa, NodoHash, TablaHash
│   ├── arbol/           IArbol, NodoArbol, ArbolBinario
│   └── grafo/           Arista, Grafo, ColaPrioridad
├── algoritmos/          AlgoritmoRuta, AlgoritmoRutaBase, BfsRuta, DijkstraRuta
├── servicio/            MetroService, SimuladorTurno, DatosDemo, PersistenciaGrafo
├── ui/                  MenuConsola, VentanaMetro
└── Main.java
```

## Cómo ejecutar

1. Clonar el repositorio:
   ```bash
   git clone <URL-DEL-REPOSITORIO>
   ```
2. Abrir el proyecto en **NetBeans** (o cualquier IDE con Java 17 o superior).
3. Ejecutar la clase `Main`.
4. Elegir una opción del menú de consola o usar la interfaz gráfica.

## Manual de uso

**Menú de consola**

| Opción | Qué demuestra |
|---|---|
| 1 | Lista enlazada de estaciones (agregar, eliminar, recorrer) |
| 2 | Cola de pasajeros (encolar, desencolar, andén vacío) |
| 3 | Pila de consultas (apilar, ver la última, deshacer) |
| 4 | Tabla hash (altas, búsqueda y colisión de claves) |
| 5 | Árbol de líneas (insertar, buscar, listar en orden) |
| 6 | Calcular ruta más corta entre dos estaciones |
| 7 | Simular un turno de pasajeros |

Cada opción incluye al menos un **caso real** y un **caso borde** (estación inexistente, andén vacío).

**Interfaz gráfica (Swing):** permite elegir origen y destino, muestra la ruta y el tiempo total en un `JTextArea` y lista las estaciones en una tabla.

## Buenas prácticas aplicadas

- **POO:** encapsulamiento, abstracción, herencia (`Persona` → `Pasajero`) y polimorfismo (`AlgoritmoRuta`).
- **SOLID:** una responsabilidad por clase, algoritmos intercambiables por interfaz e inyección de dependencias en `MetroService`.
- **Recursividad:** recorridos del árbol, reconstrucción del camino y operaciones auxiliares en la lista.
- **Clean Code:** métodos cortos, nombres claros, sin lógica en el `Main` y código comentado con Javadoc.

## Complejidad de las operaciones principales

| Operación | Complejidad |
|---|---|
| Encolar / desencolar | O(1) |
| Apilar / desapilar | O(1) |
| Hash: insertar y buscar | O(1) promedio |
| Árbol: insertar y buscar | O(log n) promedio |
| BFS | O(V + A) |
| Dijkstra con min-heap | O((V + A) log V) |

## Uso de IA

Se utilizó **DeepSeek** después de terminar la Fase 1, y para generar la interfaz gráfica. Los prompts y capturas están en la carpeta `docs/ia/`.

## Flujo de trabajo en Git

- `main`: versión estable.
- `develop`: integración.
- `feature/<nombre>`: una rama por integrante y por paquete, con *Pull Request* hacia `develop`.

## Fases del proyecto

- [ ] **Fase 1:** estructuras básicas y menú de prueba
- [ ] **Fase 2:** grafo, árbol, `MetroService` y simulación de turno
- [ ] **Fase 3:** interfaz Swing y persistencia en `.txt`