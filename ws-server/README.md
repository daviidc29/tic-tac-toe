# TicTac – WebSockets Rooms 👾

**TicTac** es una extensión *sencilla y divertida* del clásico **tic-tac-toe** para jugar **en tiempo real** usando **WebSockets nativos**.
La idea: yo creo (o ingreso) a una **sala** por ID, otra persona entra a la misma sala, y los dos jugamos en el **mismo tablero** con turnos sincronizados al instante.

> 🎯 Objetivo: mostrar un patrón mínimo y claro de tiempo real con WebSockets en Java 21 + Spring Boot 3, manteniendo el proyecto original y solo **añadiendo** un submódulo con la parte de tiempo real.

---

## ✨ ¿Qué ofrece?

* **Salas por ID**: crea o entra a `sala-1`, `profe`, lo que quieras.
* **2 jugadores**: símbolo automático (`X` y `O`).
* **Reglas de tic-tac-toe**: turnos válidos, casillas bloqueadas, ganador y empate.
* **Feedback inmediato**: si tu rival se desconecta, te enteras al instante.
* **UI minimalista**: estilo tipo tutorial — **X en negro** y **O en rojo**.

---

## 🧩 Cómo funciona (alto nivel)

1. **Cliente** (React UMD + Babel en el navegador) abre un **WebSocket** hacia
   `ws(s)://<host>/parcial/tictac`.
2. Al unirse (`type: "join"`), el servidor asigna símbolo y **emite estado** de la sala.
3. Cada jugada válida (`type: "move"`) **actualiza tablero** y se **broadcast** a la sala.
4. El servidor **valida** turnos, casillas ocupadas y detecta **ganador/empate**.

> No hay base de datos: el registro de salas y sesiones vive en memoria para máxima simplicidad del ejemplo.

---

## 🏗️ Arquitectura (mini)

```
tic-tac-toe/
 └─ ws-server/                              # Submódulo añadido
    ├─ src/main/java/edu/eci/arsw/parcial/
    │  ├─ ParcialApplication.java           # Spring Boot (Java 21)
    │  ├─ config/WebSocketConfig.java       # ServerEndpointExporter
    │  ├─ endpoints/TTTEndpoint.java        # @ServerEndpoint("/parcial/tictac")
    │  └─ service/
    │     ├─ Game.java                      # Reglas de juego y ganador
    │     └─ RoomsRegistry.java             # Salas y sesiones (in-memory)
    └─ src/main/resources/static/parcial/
       ├─ tictac.html                       # Página estática para pruebas
       └─ js/tictac.jsx                     # Cliente React (sin build)
```

* **Endpoint WebSocket**: `/parcial/tictac` (Jakarta WebSocket + Spring Boot)
* **Por qué nativo y no STOMP?** Buscamos el **mínimo** viable para entender el patrón. STOMP es genial para routing/topic, pero aquí priorizamos claridad y cero dependencias adicionales.

---

## 🚀 Arranque rápido

> Requisitos: **Java 21** y **Maven**

```bash
cd tic-tac-toe/ws-server
mvn spring-boot:run
# o:
mvn -q -DskipTests package
java -jar target/ws-server-1.0.0.jar
```

Abre en el navegador:
`http://localhost:8080/parcial/tictac.html`

* En “id de sala” escribe, por ejemplo, **sala-1** y pulsa **Crear/Entrar**.
* Abre la **misma URL en otra pestaña** (o en otro navegador) y entra a la misma sala.
* ¡Juega! `X` y `O` comparten el tablero en tiempo real.

---

## 🔌 Protocolo (mensajes JSON)

**Cliente → Servidor**

```json
{"type":"join","room":"sala-1"}
{"type":"move","room":"sala-1","index":0}
{"type":"leave"}
```

**Servidor → Cliente**

```json
{"type":"joined","room":"sala-1","symbol":"X|O","board":[...],"turn":"X|O"}
{"type":"state","board":[...],"turn":"X|O","winner":"X|O|DRAW"}
{"type":"opponent_left"}
{"type":"error","error":"mensaje descriptivo"}
```

---

## 🎨 Estilo minimalista

* **X** en **gris** (por defecto).
* **O** en **rojo** via clase `.square.o` (añadida dinámicamente en el botón).

```css
.square {
  background:#fff; border:1px solid #999; float:left;
  font-size:24px; font-weight:bold; line-height:34px; height:34px;
  margin-right:-1px; margin-top:-1px; padding:0; text-align:center; width:34px;
  cursor:pointer;
}
.square.o { color:#d00; } 
```

---

## 🛠️ Tips & Troubleshooting

* **HTTP vs HTTPS**: el cliente detecta automáticamente `ws://` o `wss://` según `window.location`. En local, usa **http** para evitar líos de certificados.
* **Babel**: el cliente evita sintaxis moderna que Babel 6 no transpila (`?.`, `??`), para que funcione directo en navegador sin build.

---

## 📌 Qué me llevo de este ejemplo

* Cómo **levantar un WebSocket nativo** en Spring Boot 3 con Java 21.
* Un patrón claro de **salas** con **broadcast selectivo**.
* Un cliente React **ligero** y listo para *probar la señal en vivo* sin pipeline de build.
* Un esqueleto perfecto para extender a **persistencia**, **reconexión**, **espectadores**, o **historial de jugadas**.
