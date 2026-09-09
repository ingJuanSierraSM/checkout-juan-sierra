# 🤖 Gobernanza de IA

## 🎯 Propósito

La IA se usó como apoyo para explorar alternativas, estructurar código repetitivo, proponer pruebas y revisar el resultado. No decide reglas de negocio ni se acepta una sugerencia sin contraste con el alcance, el código, las pruebas y la ejecución local.

Estimación del proceso: **60% asistencia de IA y 40% definición, revisión y validación manual**. El porcentaje expresa apoyo durante la producción; la responsabilidad de las decisiones y de la entrega final es humana.

## 🛡️ Controles aplicados

- El alcance, los porcentajes de descuento, los contratos REST y las decisiones de arquitectura se definieron y aprobaron antes de integrar cada cambio.
- Toda recomendación que afectó persistencia, concurrencia o reglas se contrastó con pruebas automatizadas y/o una llamada HTTP real.
- La IA no recibió secretos externos. `DB_PASSWORD` admite una credencial local y conserva `root` solo como respaldo académico explícitamente documentado.
- Los artefactos generados se revisaron contra el código actual: no se declara como implementado un comportamiento que no exista o no esté probado.
- Los commits se hicieron por etapas funcionales y después de aprobación humana.

## 🧰 Artefactos reutilizables

| Artefacto | Función | Límites importantes |
| --- | --- | --- |
| [Skill `discount-test-designer`](../.ai/skills/discount-test-designer.md) | Diseña una matriz de casos de borde para reglas de descuento. | No modifica producción, no inventa reglas, no suma porcentajes y exige verificar la secuencia y el tope. |
| [Agente `checkout-code-reviewer`](../.ai/agents/checkout-code-reviewer.md) | Revisa separación de capas, consistencia de checkout, persistencia y cobertura. | Solo reporta hallazgos; no cambia el código, reglas del negocio ni datos. |

### 🧪 Skill: `discount-test-designer`

**Entrada requerida:** regla o estrategia, subtotal inicial, productos/categorías/cantidades, cupón, fecha y política máxima.

**Procedimiento:** expresa cada escenario como precondición, operación y resultado; calcula cada descuento sobre el saldo previo; incorpora bordes de umbral, decimales, cupón vencido/usado, stock y tope.

**Salida:** una propuesta de pruebas no triviales que verifica importes por etapa, descuento total, porcentaje efectivo, total final y activación del cap.

La especificación completa permanece en el archivo del skill para que pueda repetirse sin depender de esta documentación.

### 🔎 Agente: `checkout-code-reviewer`

**Alcance:** módulos de catálogo, promociones, checkout y órdenes antes de integrar una modificación.

**Lista de control:** lógica de negocio fuera de controllers, dominio sin dependencias de framework, cálculo secuencial y cap final, cotización sin efectos secundarios, confirmación transaccional con revalidación, snapshots históricos, errores tipados y cobertura de rutas críticas.

**Formato de salida:** severidad, archivo/clase, evidencia, impacto y corrección sugerida. Si no hay hallazgos bloqueantes, declara verificaciones realizadas y riesgos residuales.

## ✅ Ejecución de los artefactos sobre el código final

La siguiente revisión se realizó sobre los archivos actuales y se contrastó con las suites del proyecto. No produjo cambios automáticos.

### 📋 Resultado del skill de pruebas

| Riesgo solicitado por el skill | Evidencia implementada | Resultado |
| --- | --- | --- |
| Aplicación secuencial de categoría, volumen y cupón. | `DiscountEngineTest.shouldApplyCategoryVolumeAndCouponDiscountsSequentially` verifica importes `12.00`, `5.40` y `15.39` sobre un subtotal de `120.00`. | Conforme. |
| Tope máximo y cálculo del porcentaje efectivo. | `DiscountEngineTest` prueba un cap de 25% y un caso aislado que recorta exactamente 40% a 35%. | Conforme. |
| Borde del descuento por volumen. | Las pruebas separan `100.00` (no aplica) de `100.01` (sí aplica). | Conforme. |
| Cupón vencido, usado, inactivo o inexistente. | `QuoteCheckoutServiceTest` cubre los cuatro rechazos y una cotización válida sin consumir el cupón. | Conforme. |
| Carrito inválido e inventario. | `QuoteCheckoutServiceTest` cubre carrito vacío, producto duplicado, producto inexistente/inactivo y stock insuficiente. | Conforme. |
| Confirmación atómica. | `ProcessCheckoutIntegrationTest` verifica orden, snapshots, consumo del cupón y disminución de stock en H2. | Conforme. |

### 🧾 Resultado del agente de revisión

| Severidad | Verificación | Evidencia | Resultado |
| --- | --- | --- | --- |
| Bloqueante | Lógica fuera de los controllers. | `CheckoutController` valida, mapea y delega en puertos de entrada; no usa repositorios ni entidades JPA. | Sin hallazgo. |
| Bloqueante | Cotización sin efectos secundarios. | `QuoteCheckoutService` usa `prepareQuote`; la prueba verifica cero invocaciones a `CouponRepository.save`. | Sin hallazgo. |
| Bloqueante | Revalidación y concurrencia al confirmar. | `ProcessCheckoutService` es `@Transactional`; `CheckoutPricingService` usa consultas `findByIdForUpdate` y `findByCodeForUpdate`. | Sin hallazgo. |
| Alto | Descuentos y cap correctos. | `DiscountEngine` ordena las reglas por secuencia y `MaximumDiscountPolicy` se ejecuta una sola vez al final. | Sin hallazgo. |
| Alto | Historial estable y sin duplicación de cupón. | La orden persiste snapshots de ítems y `order_discounts`; `orders` no tiene `coupon_code`. | Sin hallazgo. |
| Medio | Error HTTP consistente. | `GlobalExceptionHandler` convierte reglas y validaciones a un contrato `ApiErrorResponse` tipado. | Sin hallazgo. |

**Riesgo residual conocido:** con las reglas fijas de 10%, 5% y 15% aplicadas en cascada, el seed estándar solo alcanza 27.325%; por tanto no activa visualmente el cap de 35%. El comportamiento sí se cubre de manera determinista en el motor y la UI acepta la señal `discountCapApplied`. La limitación, su cálculo y una ruta de evolución están registrados en [arquitectura.md](arquitectura.md).

## 📝 Bitácora de decisiones, correcciones y rechazos

| Fecha | Tarea | Propuesta u observación asistida por IA | Decisión humana | Justificación y evidencia |
| --- | --- | --- | --- | --- |
| 2026-09-08 | Diseño del motor de descuentos | Modelar el límite máximo como una estrategia más. | **Corregida.** | El límite es una política transversal posterior a las promociones. Se implementó `MaximumDiscountPolicy` y se probó con recorte exacto al 35%. |
| 2026-09-08 | Modelo de órdenes | Guardar `coupon_code` directamente en `orders`. | **Rechazada.** | `order_discounts` ya deja la evidencia del cupón y su importe. Duplicarlo abriría inconsistencias y no mejora la consulta. |
| 2026-09-08 | Confirmación de checkout | Reutilizar sin validación la cotización previa. | **Rechazada.** | Una cotización puede quedar obsoleta. La confirmación reconsulta recursos con bloqueo, recalcula, descuenta stock y consume cupón dentro de la transacción. |
| 2026-09-08 | Mapeo JPA de órdenes | Cargar dos colecciones como listas mediante un único `EntityGraph`. | **Corregida tras ejecutar la prueba.** | Hibernate generó `MultipleBagFetchException`; los adaptadores persisten conjuntos JPA y entregan listas ordenadas al dominio. |
| 2026-09-08 | Métrica de backend | Medir DTOs y entidades como cobertura esencial. | **Corregida.** | JaCoCo se concentra en `checkout`, donde están las decisiones; DTOs e infraestructura no distorsionan la métrica. |
| 2026-09-09 | Arquitectura frontend | Incorporar NgRx y una capa de dominio adicional para un estado pequeño. | **Rechazada.** | Signals locales y servicios por feature cubren el estado actual. Agregar un store global aumentaría archivos y ceremonias sin resolver un problema presente. |
| 2026-09-09 | Robustez TypeScript | Mantener las opciones de compilación por defecto. | **Corregida.** | Se habilitaron `strict: true` y `strictTemplates: true` para cumplir el requisito de contratos y plantillas tipadas. |
| 2026-09-09 | Validación visual del cap | Mostrar un aviso temporal que desaparezca con el siguiente cambio. | **Corregida.** | El requisito pide una alerta persistente mientras el backend reporte el límite; la UI usa el flag tipado de la cotización. |

## 🔁 Cómo repetir la validación

```powershell
# Backend: H2, pruebas y umbral JaCoCo del núcleo de checkout
cd apps/backend
.\mvnw.cmd verify

# Frontend: Vitest/TestBed, cobertura V8 y compilación estricta
cd ../frontend
npm run test:coverage
npm run build
```

La guía operativa y los comandos de inicio se encuentran en el [README](../README.md). La arquitectura completa, los diagramas Mermaid y los trade-offs están en [arquitectura.md](arquitectura.md).
