# 🧾 Especificación inicial y prompt maestro · DaviShop

> **Propósito:** este documento representa la instrucción inicial, completa y versionable que se entrega al agente de desarrollo antes de escribir código. Es la fuente de verdad para planear, implementar, validar y documentar el MVP.

## 🎯 Prompt para iniciar el proyecto

Actúa como arquitecto y desarrollador Full Stack. Construye un MVP de checkout para un e-commerce llamado **DaviShop**, con backend, frontend, base de datos relacional y pruebas automatizadas. Prioriza criterio de ingeniería, reglas de negocio correctas, modularidad, tipado estricto y evidencia verificable sobre cantidad de código o sobreingeniería.

**No empieces a implementar inmediatamente.** Primero analiza esta especificación y entrega un plan por fases, separado en backend y frontend, indicando:

1. objetivos, archivos o módulos a crear y dependencias de cada fase;
2. decisiones de arquitectura y trade-offs;
3. criterios de aceptación y pruebas que validarán cada fase;
4. riesgos o ambigüedades de negocio que deban aclararse antes de alterar reglas;
5. orden de ramas, commits y validaciones.

Espera aprobación explícita antes de implementar cada fase material. Después de una fase aprobada, valida; solicita aprobación para hacer commit; y solicita otra aprobación independiente antes de hacer push. Nunca integres cambios a `main`: el dueño del repositorio hará el PR desde `develop`.

---

## 🌟 Objetivo de producto

Construir una sección de checkout responsiva que permita consultar un catálogo preconfigurado, administrar un carrito, cotizar descuentos acumulativos y confirmar una compra persistida. La aplicación debe mostrar con transparencia el subtotal, cada descuento aplicado, ahorro total, porcentaje efectivo y total final.

La solución se entrega como monorepo público de GitHub y debe incluir:

```text
proyecto/
├── apps/
│   ├── backend/
│   └── frontend/
├── docs/
│   ├── arquitectura.md
│   └── ia.md
├── .ai/                         # Prompt/skill y agente documentados
└── README.md
```

La carpeta `packages/` es opcional. No se debe crear si no resuelve una necesidad compartida real.

---

## 📌 Reglas funcionales transversales

### Catálogo y carrito

- El catálogo contiene productos semilla con identificador, nombre, precio unitario en USD, categoría, estado y stock inicial.
- El cliente puede agregar, disminuir y eliminar productos del carrito; las cantidades y el subtotal cambian en tiempo real.
- Nunca se permite una cantidad menor que uno ni mayor que el stock disponible.
- Si un producto queda sin stock, la interfaz debe comunicar **“Agotado”** y no permitir agregarlo.
- Un carrito vacío, productos repetidos, inexistentes, inactivos o con cantidad inválida son solicitudes inválidas.

### Motor de descuentos acumulativos

Los descuentos **no se suman**. Se aplican en este orden, calculando cada regla sobre el saldo dejado por la anterior:

1. **Categoría:** si el carrito contiene productos de `TECNOLOGIA`, aplicar 10% únicamente al importe de esos productos.
2. **Volumen:** si el subtotal resultante después de la regla de categoría es estrictamente mayor a `100.00 USD`, aplicar 5% a todo el carrito.
3. **Cupón:** si se recibe el cupón activo y vigente `WELCOME2026`, aplicar 15% al saldo posterior a volumen.
4. **Límite absoluto:** el descuento consolidado no puede superar 35% del subtotal original. El límite es una política global aplicada una única vez al final, no otra promoción acumulable.

Todos los importes deben usar precisión decimal; el redondeo monetario es a dos decimales con `HALF_UP`. La respuesta debe incluir el importe de cada descuento, el descuento calculado antes del cap, el descuento final, porcentaje efectivo, total final, porcentaje máximo configurado y la bandera `discountCapApplied`.

> **Validación de consistencia requerida:** antes de implementar una alerta que deba activarse en una demo, comprueba que los datos semilla puedan generar el escenario. Con 10%, 5% y 15% aplicados en cascada, el máximo matemático es `1 - (0.90 × 0.95 × 0.85) = 27.325%`. Si el cap de 35% no puede activarse con reglas fijas, no alteres porcentajes para fabricar la demostración: documenta el hallazgo, prueba el cap con una política determinista y conserva la UI preparada para la bandera del backend.

### Cotización y confirmación

- **Cotizar** calcula y devuelve el resultado, pero no descuenta inventario, no consume cupón y no crea una orden.
- **Confirmar checkout** revalida catálogo, stock, cupón y políticas; calcula de nuevo el precio; bloquea los recursos necesarios; descuenta inventario; consume el cupón cuando corresponda; persiste la orden y devuelve el resumen definitivo.
- La confirmación debe ser transaccional: una falla no puede dejar una orden parcial, inventario decrementado sin orden o cupón consumido sin compra.
- Las órdenes deben almacenar *snapshots* de ítems y descuentos para que su historial no cambie si el catálogo o las reglas se modifican posteriormente.

---

## ☕ Especificación de backend

### Stack y restricciones

- Java 21 y Spring Boot **4.1.1**, gestionado con Maven Wrapper.
- PostgreSQL para ejecución local.
- H2 aislado para pruebas backend; las pruebas no deben modificar la base local de demostración.
- Configuración de base de datos en `application.properties`:

  ```properties
  spring.datasource.url=jdbc:postgresql://localhost:5432/ecommerce_db
  spring.datasource.username=postgres
  spring.datasource.password=${DB_PASSWORD:root}
  ```

  `root` es una contraseña de respaldo exclusivamente académica. Debe documentarse que puede sustituirse con `DB_PASSWORD` sin obligar al evaluador a crear una variable de entorno.
- No agregar Docker, Flyway, Testcontainers, microservicios, autenticación, pagos reales ni mensajería. No aportan valor al alcance actual.

### Arquitectura

Implementar un monolito modular con **arquitectura hexagonal pragmática**, organizado por capacidad de negocio:

```text
core/
├── catalog/
├── checkout/
├── order/
├── promotion/
└── shared/
```

Cada módulo separa:

- `domain`: modelos y reglas de negocio sin dependencia de Spring, JPA o DTOs HTTP;
- `application`: casos de uso, comandos, servicios y puertos de entrada/salida;
- `infrastructure`: adaptadores web, persistencia JPA, configuración, DTOs y mappers.

Los controllers solo validan, mapean y delegan en casos de uso. No contienen reglas de descuento ni acceso directo a repositorios JPA.

### Patrones obligatorios y propósito

1. **Strategy:** una implementación por regla (`CategoryDiscountStrategy`, `VolumeDiscountStrategy`, `CouponDiscountStrategy`) para extender promociones sin condicionales centrales.
2. **Ports and Adapters:** puertos para repositorios y casos de uso; adaptadores HTTP y JPA en infraestructura. El dominio desconoce el framework y la persistencia.

El `DiscountEngine` ordena las reglas y genera una lista extensible de `DiscountDetail`. `MaximumDiscountPolicy` se evalúa una sola vez después del motor.

### Persistencia mínima

Crear scripts SQL dentro de `apps/backend/database/` para esquema, seed y reinicio de demo. El modelo debe contemplar:

- `products`: datos de catálogo, disponibilidad y stock;
- `coupons`: código, porcentaje, vigencia, estado y consumo;
- `discount_policies`: porcentaje máximo configurable;
- `orders`: fecha, subtotal original, totales, cap y porcentaje efectivo;
- `order_items`: snapshot de producto, nombre, precio y cantidad;
- `order_discounts`: tipo, nombre, porcentaje e importe aplicado.

No almacenar `coupon_code` duplicado en `orders`: la evidencia del cupón pertenece al detalle histórico de descuentos.

### API REST versionada

Exponer JSON bajo `/api/v1` con validación de entrada y errores tipados consistentes:

| Método | Ruta | Contrato funcional |
| --- | --- | --- |
| `GET` | `/products` | Retorna productos activos con stock disponible para el catálogo. |
| `POST` | `/checkout/quote` | Recibe `items` y `couponCode`; calcula sin efectos secundarios. |
| `POST` | `/checkout` | Recibe el mismo contrato; revalida, persiste y confirma. |
| `GET` | `/orders` | Retorna resúmenes de órdenes persistidas. |
| `GET` | `/orders/{orderId}` | Retorna detalle con ítems y descuentos históricos. |

Ejemplo de solicitud de quote/checkout:

```json
{
  "items": [
    { "productId": 1, "quantity": 1 },
    { "productId": 2, "quantity": 1 }
  ],
  "couponCode": "WELCOME2026"
}
```

La API debe informar errores de validación, carrito, producto, stock, cupón y orden mediante una estructura `ApiErrorResponse` estable, con código HTTP semántico y mensaje apto para la interfaz.

### Criterios de aceptación y calidad backend

- Pruebas unitarias del motor: secuencia, categoría, umbral exacto `100.00`, un centavo por encima, cupón válido/vencido/usado/inactivo/inexistente, cap y redondeo.
- Pruebas de aplicación/integración: carrito vacío, productos duplicados, producto inexistente/inactivo, stock insuficiente, quote sin efectos secundarios y confirmación atómica.
- Las pruebas de integración usan H2.
- JaCoCo exige mínimo **80%** de líneas en el núcleo lógico de checkout; se mide la lógica decisoria y no DTOs triviales ni configuración.
- El comando de validación debe ser `./mvnw verify` o su equivalente para Windows `./mvnw.cmd verify`.

---

## 🅰️ Especificación de frontend

### Stack y principios

- Angular 22 standalone, TypeScript estricto y `strictTemplates`.
- SCSS, componentes pequeños y accesibles.
- Signals para estado local y síncrono; RxJS para comunicación HTTP y cancelación de cotizaciones obsoletas.
- Vitest, TestBed y cobertura V8 para pruebas.
- No usar NgRx, una capa de dominio artificial, librerías de estado global ni componentes de UI pesados. El estado del MVP es acotado y esas herramientas añadirían ceremonia sin resolver un problema actual.

### Organización modular por feature

```text
src/app/
├── core/api/                    # Configuración, errores y contratos transversales
└── features/
    ├── catalog/                 # Catálogo, tarjeta de producto y servicio API
    ├── cart/                    # CartStore basado en Signals
    ├── checkout/                # Quote, confirmación, éxito y servicios API
    └── orders/                  # Listado, detalle y servicio API
```

Las páginas no construyen URLs ni interpretan directamente respuestas HTTP. Los servicios API encapsulan `HttpClient` y los modelos TypeScript representan contratos explícitos, sin `any`.

### Vistas y comportamiento esperado

1. **Catálogo / checkout principal (`/`):** productos en tarjetas con categoría, precio, stock y acción contextual; carrito lateral o panel de resumen con controles de cantidad, cupón, descuentos y total.
2. **Resultado de compra (`/checkout/success`):** confirmación con identificador de orden, importes, descuentos y enlace a historial.
3. **Mis órdenes (`/orders`):** listado de órdenes confirmadas con fecha, subtotal, ahorro y total.
4. **Detalle de orden (`/orders/:orderId`):** ítems y descuentos persistidos como snapshots.

La experiencia visual debe inspirarse en la identidad Davivienda sin copiar las referencias literalmente: tonos rojos, fondos claros, tarjetas redondeadas, contraste legible, jerarquía tipográfica, estados de éxito/error inequívocos y diseño responsivo para escritorio, tableta y móvil.

### Estados UX obligatorios

- Carrito vacío, cantidad máxima, producto agotado, carga y error de red.
- Código de cupón aplicado, removido, inválido, vencido o ya usado.
- Desglose visible de categoría, volumen y cupón, con ahorro total y porcentaje efectivo correctos.
- Botón de compra deshabilitado durante la confirmación para evitar doble envío.
- Alerta persistente y distintiva mientras `discountCapApplied` sea `true`, indicando el límite máximo que devolvió el backend.
- Errores de negocio traducidos a mensajes claros sin perder el código técnico para depuración.

### Criterios de aceptación y calidad frontend

- Pruebas de `CartStore`: agregado, eliminación, límite por stock, subtotal y vaciado.
- Pruebas de cotización: carga, cancelación de solicitud obsoleta, errores, cupón y alerta de cap.
- Pruebas de confirmación y pantallas de órdenes: éxito, error y navegación esencial.
- Cobertura V8 mínima **80%** en la lógica de carrito y checkout.
- Los comandos obligatorios son `npm run test:coverage` y `npm run build`.
- Validar visualmente cada vista contra sus mockups antes de aprobarla.

---

## 🔗 Contratos, consistencia y seguridad de cambios

- Backend y frontend se integran únicamente mediante REST tipado. No duplicar reglas de descuento en Angular: el backend es la fuente de verdad monetaria.
- El frontend puede calcular subtotales locales de UX, pero usa `/checkout/quote` como resultado autoritativo de descuentos y totales.
- Mantener CORS limitado al origen local de Angular durante desarrollo.
- Las operaciones destructivas de base de datos solo se permiten mediante los scripts de reinicio explícitos y aprobados.
- Proteger las decisiones críticas con pruebas antes de simplificar o refactorizar.

---

## 🤖 Gobernanza de IA y documentación

Durante el desarrollo registra decisiones para terminar `docs/ia.md`. Debe incluir:

- porcentaje aproximado de asistencia de IA y responsabilidad humana;
- al menos un prompt estructurado o skill para diseñar casos de prueba de descuentos;
- al menos un agente revisor, con rol, alcance, formato de salida y límites de no modificar reglas ni datos;
- mínimo dos sugerencias de IA corregidas o rechazadas con justificación técnica;
- evidencia de que las recomendaciones se validaron contra código, pruebas o ejecución HTTP.

El agente nunca debe inventar porcentajes, modificar reglas aprobadas, cambiar `main`, aceptar su propio trabajo ni ejecutar operaciones destructivas. La documentación de IA puede consolidarse al final, pero la bitácora debe mantenerse desde el inicio.

El `README.md` se completa al final de la implementación; debe contener requisitos previos, PostgreSQL, scripts SQL, variable `DB_PASSWORD`, comandos de inicio, pruebas, cobertura y colección Postman. La colección se ubica en `apps/backend/postman/` e incluye flujos exitosos y de error.

---

## 🗺️ Plan de trabajo solicitado al agente

Antes de crear código, responde con un plan conciso en este orden:

1. **Fundación backend:** generar el proyecto Spring, estructura modular, configuración PostgreSQL/H2 y scripts de base.
2. **Dominio y descuentos:** modelos de dinero, reglas Strategy, motor secuencial, política cap y pruebas unitarias iniciales.
3. **Casos de uso y persistencia:** puertos, adaptadores JPA, quote sin efectos secundarios y checkout transaccional.
4. **API y calidad backend:** DTOs dentro de carpetas `dto`, mappers separados, manejo global de errores, integración H2 y JaCoCo.
5. **Fundación frontend:** Angular standalone, configuración estricta, estructura por feature, tema visual y modelos REST.
6. **Carrito y cotización:** catálogo, `CartStore`, controles de stock, cupón, stores de quote y desglose reactivo.
7. **Confirmación e historial:** compra, éxito, listado y detalle de órdenes.
8. **Calidad y experiencia:** pruebas frontend, cobertura, build, responsividad y comparación visual con mockups.
9. **Cierre:** colección Postman, `README.md`, arquitectura Mermaid, gobernanza IA y auditoría final contra los requisitos.

Para cada fase, informa qué se hará y espera aprobación antes de continuar. Tras implementar, ejecuta las validaciones proporcionales al cambio e informa el resultado. Solo después de aprobación humana crea el commit y, con una segunda aprobación, hace push a `develop`.

---

## ✅ Definición de terminado

El trabajo estará terminado cuando el repositorio público tenga commits incrementales y descriptivos; backend y frontend funcionen de extremo a extremo; los descuentos sean secuenciales, precisos y auditables; checkout sea consistente y persistido; las vistas sean responsivas; las suites superen 80% en lógica esencial; y `README.md`, `docs/arquitectura.md`, `docs/ia.md`, scripts de base y colección Postman permitan a un evaluador reproducir y defender la solución.
