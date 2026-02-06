# Análisis app vs backend (Swagger)

Resumen de lo que conviene **corregir**, **agregar** o **mejorar** en la app para alinearla con el backend (Swagger).

---

## 1. DTOs e IDs numéricos

- **RecuerdoDto** y **EmocionDto** tienen `id: String`. Si el backend devuelve `"id": 0` (número), Gson puede fallar o comportarse distinto según versión.
- **Recomendación:** Usar el mismo patrón que en `CofreDto` y `UserDto`: campo `idRaw: JsonElement?` y función `idValue(): String` que acepte número o string. Así se evitan errores de parsing.
- **PersonaArbolDto** también tiene `id: String`; mismo criterio si el backend devuelve id numérico.

---

## 2. Auth y usuario actual

- **auth/me:** La app espera `Response<UserDto>`. Si el backend devuelve un wrapper tipo `{ "usuario": { "id": 1, "persona": {...}, ... } }`, el parsing falla o devuelve null.
- **Recomendación:** Confirmar en Swagger el contrato de `GET auth/me`. Si es wrapper, definir algo como `MeResponse(@SerializedName("usuario") val usuario: UsuarioDto)` y en `AuthRepository.getMe()` usar `body()?.usuario?.toUserDto()`.
- **Login/Signup:** Ya se usa `body?.user ?: body?.usuario?.toUserDto()` y fallback a `getMe()`; está bien cubierto.

---

## 3. Recuerdos

- **RecuerdoDto:** Ya tiene `rutaArchivo`, `tipoArchivo`, `emocionPersonalizada` y el mapeo a UI con `resolveEmotionalTag` y tipo según `tipoArchivo`/`tipo`/`type`. OK.
- **Crear recuerdo:** Multipart con `file`, `titulo`, `descripcion`, `fecha`, `emocionId`, `emocionPersonalizada` está alineado con lo que se comentó del backend.
- **PATCH recuerdos/{id}:** Se usa `RecuerdoCreateRequest` (JSON). Si el backend acepta solo JSON y no multipart para update, está correcto. Si en Swagger el update es multipart, habría que cambiar a `@Multipart` y Part como en create.

---

## 4. Cofres

- **Mis cofres:** `MisCofresResponse(usuario, cofres)` y uso de `response.body()?.cofres` está bien.
- **Crear cofre con foto e invitados:** Se usa `createFull` con `nombre`, `parentesco`, `fraseDescripcion`, `fotoPortada`, `invitadosEmails` (RequestBody JSON array). En Swagger suele ser `invitadosEmails` o `emailsUsuariosInvitados`; confirmar nombre exacto del campo.
- **Invitar:** `CofreInviteRequest(cofreRecuerdoId: Int, emailsUsuariosInvitados: List<String>)` y `invitar(cofreId)` con `cofreId.toIntOrNull()` está bien si el backend espera número en el body.
- **Familia (pestaña):** Hoy se muestra solo el usuario actual (`buildFamiliaMembers(cofre, currentUser)`). Si el backend expone algo tipo `GET cofre-recuerdos/{id}/miembros` o `GET cofre-recuerdos/{id}/usuarios`, usar esa lista para mostrar todos los miembros del cofre en lugar de solo el usuario actual.

---

## 5. Usuario y perfil

- **PATCH usuario/{id}:** Multipart con `nombre`, `apellido`, `nombreUsuario`, `contrasena`, `email`, `activo`, `fotoPerfil`, `urlFotoPerfil` está alineado con lo descrito del Swagger. No se envía `activo` desde Editar perfil; correcto.
- **Errores al actualizar perfil:** Si el backend devuelve JSON con `message` o `errorDetails.message`, conviene parsear el `errorBody()` y mostrar ese mensaje en lugar del JSON crudo. Se puede reutilizar la misma lógica de `AuthRepository.parseErrorBody` (o un parser compartido).

---

## 6. Errores HTTP y mensajes al usuario

- **Repositorios:** Cofre, Recuerdos, Auth ya manejan `HttpException`, `IOException` y en muchos casos `response.errorBody()?.string()`. RecuerdosRepository tiene mensaje amigable para 413. OK.
- **ProfileViewModel:** Al fallar `usuarioApi.update()`, se asigna `response.errorBody()?.string()` directo a `_errorMessage`. Mejorar parseando el JSON del error (p. ej. `message` o `errorDetails.message`) para mostrar un texto legible.
- **Consistencia:** Tener un único lugar (p. ej. `NetworkErrorParser` o método en `ApiResult`) para parsear el body de error y usarlo en Auth, Profile y, si aplica, en otros repositorios.

---

## 7. Paginación y respuestas de listas

- **PagedResponse:** Ya contempla `content`, `data`, `items` y `list()`. Si el backend usa otro nombre (p. ej. `results`), agregar `@SerializedName("results") val results: List<T>?` y usarlo en `list()`.
- **Recuerdos por cofre:** Se llama a `recuerdos/search?cofreRecuerdosId=...` sin paginación. Si el backend exige `page`/`size`, añadir parámetros por defecto (p. ej. `page=0`, `size=100`) para no depender de “todo en una página”.

---

## 8. Endpoints que la app no usa (según Swagger)

- Revisar en Swagger si hay:
  - **Cambio de contraseña:** La app tiene `AuthApi.changePassword` y `ChangePasswordRequest`; falta la pantalla o flujo que lo llame.
  - **DELETE usuario:** `UsuarioApi.delete(id)` existe; ver si hay flujo “eliminar cuenta”.
  - **Árbol familiar / personas:** ArbolFamiliarApi y DTOs están; verificar que las pantallas (FamilyTreeScreen, AddPersonScreen) usen los endpoints correctos y que los IDs (árbol, persona) coincidan con el backend (número vs string).
  - **Listado de miembros de un cofre:** Si existe en Swagger, añadir llamada y usar en la pestaña Familia.

---

## 9. Resumen de acciones sugeridas

| Prioridad | Acción |
|----------|--------|
| Alta | Hacer que **RecuerdoDto** y **EmocionDto** acepten `id` numérico o string (p. ej. `idRaw: JsonElement?` + `idValue()`). |
| Alta | En **ProfileViewModel**, parsear el body de error de `PATCH usuario` (p. ej. con el mismo criterio que `AuthRepository.parseErrorBody`) y mostrar mensaje legible. |
| Media | Confirmar contrato de **GET auth/me** (objeto directo vs wrapper `usuario`) y ajustar DTO y AuthRepository si hace falta. |
| Media | Si el backend tiene **GET cofre-recuerdos/{id}/miembros** (o similar), usarlo en la pestaña Familia para mostrar todos los miembros. |
| Media | Unificar **parseo de errores** en un util compartido (Auth, Profile, y opcionalmente otros repos). |
| Baja | Revisar **PersonaArbolDto.id** si el backend devuelve número. |
| Baja | Ver en Swagger nombres exactos de campos en **create cofre** (invitados) y en **PagedResponse** (content/data/items/results). |
| Baja | Añadir **page/size** a búsqueda de recuerdos por cofre si el backend lo requiere. |

---

*Documento generado a partir del estado actual del código y del resumen de integración con el backend/Swagger.*
