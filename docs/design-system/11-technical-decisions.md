# 11 - Technical Decisions

## Registro de decisiones

### 2026-05-14 - Dark mode

- Contexto: hoy no es prioridad funcional.
- Decision: no implementar dark mode en esta fase.
- Compromiso: dejar foundation/theme listos para activarlo en una etapa futura sin reestructurar APIs.

### 2026-05-14 - Nivel de tokenizacion inicial

- Contexto: se necesita una base solida desde el inicio.
- Decision: adoptar `full foundation` desde la primera etapa.
- Implicancia: primero se normalizan tokens y contratos antes de migrar masivamente pantallas.

### 2026-05-14 - Alcance de migracion

- Contexto: la app ya esta funcional y visualmente lograda.
- Decision: migracion incremental por feature (enfoque recomendado), evitando cambios big-bang.
- Implicancia: menor riesgo de regresion y mejor control de calidad por iteracion.

### 2026-05-14 - Naming de componentes base

- Contexto: los prompts usan prefijo `App*`, pero el proyecto ya usa `Nonna*`.
- Decision: mantener prefijo `Nonna*` para consistencia y evitar churn innecesario.
- Implicancia: el contrato conceptual de prompts se cumple mapeando `App*` -> `Nonna*`.

### 2026-05-14 - Primera migracion Route/Screen

- Contexto: varias pantallas aun mezclan Route y Screen.
- Decision: iniciar migracion por `CofresList` como referencia para el patron MVVM + UDF.
- Implicancia: se habilita plantilla concreta para migrar el resto por iteraciones.

### 2026-05-15 - Integración de token push para notificaciones

- Contexto: se implementó backend de notificaciones con endpoint de registro de token, pero el proyecto aún no tiene integración Firebase activa.
- Decision: incorporar `NotificationTokenSyncManager` con estrategia de token pendiente + sync al detectar sesión activa.
- Implicancia: el backend queda integrado y operativo; la fuente automática del token FCM se conecta en una iteración posterior.

## Regla para nuevas decisiones ambiguas

Si surge ambiguedad de diseno/requerimiento:

1. declarar supuesto explicito,
2. proponer decision razonable con trade-off,
3. registrar decision en este archivo antes del merge.
