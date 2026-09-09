# Gobernanza de IA

## Uso en el proyecto

La IA se utilizó como apoyo de diseño, generación de código repetitivo, casos de prueba y revisión. Las reglas de negocio, el alcance y la validación final fueron revisados manualmente sobre la implementación, las pruebas y la API ejecutada.

Estimación de contribución: 60% asistencia de IA y 40% definición, revisión y validación manual. El porcentaje describe apoyo de producción, no delegación de responsabilidad técnica.

## Bitácora

| Fecha | Tarea | Propuesta o hallazgo de IA | Decisión | Justificación |
| --- | --- | --- | --- | --- |
| 2026-09-08 | Diseño del motor de descuentos | Modelar el límite máximo como otra estrategia de descuento. | Corregida. | El límite es una política global aplicada después de las promociones, no una promoción adicional. |
| 2026-09-08 | Diseño del esquema de órdenes | Guardar `coupon_code` directamente en `orders`. | Rechazada. | `order_discounts` ya conserva el cupón aplicado; duplicarlo rompe normalización y abre inconsistencias. |
| 2026-09-08 | Mapeo JPA de órdenes | Cargar dos listas con `EntityGraph`. | Corregida tras una prueba real. | Hibernate lanzó `MultipleBagFetchException`; los adaptadores usan conjuntos JPA y exponen listas ordenadas en el dominio. |
| 2026-09-08 | Validación de checkout | Confiar en la cotización previa al confirmar. | Rechazada. | La compra debe reconsultar, validar y bloquear inventario/cupón dentro de la transacción para evitar estado obsoleto. |
| 2026-09-08 | Cobertura | Medir DTOs y entidades como lógica principal. | Corregida. | JaCoCo exige 80% en el núcleo de checkout, donde viven las decisiones de negocio. |

## Controles aplicados

- Las sugerencias no se aceptan por autoridad; se contrastan contra reglas de negocio y pruebas.
- La IA no accede ni expone credenciales nuevas; la contraseña local de demostración fue indicada expresamente en el alcance.
- Los cambios de base de datos se probaron y el estado de demostración se restableció con un script versionado.
- La ejecución final se verificó con `mvnw.cmd clean verify` y llamadas HTTP reales.

## Artefactos reutilizables

- [Skill de diseño de pruebas de descuentos](../.ai/skills/discount-test-designer.md)
- [Agente de revisión de checkout](../.ai/agents/checkout-code-reviewer.md)
