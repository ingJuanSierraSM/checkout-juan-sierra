# Arquitectura

## Enfoque

El backend es un monolito modular con una aplicación hexagonal pragmática. Cada módulo separa el dominio, los casos de uso y los adaptadores de persistencia o HTTP.

```text
HTTP / Postman
      |
      v
Controllers + DTOs + Mappers
      |
      v
Casos de uso (application)
      |
      v
Dominio (reglas, modelos, puertos)
      |
      v
Adaptadores JPA ---- PostgreSQL
```

Los módulos principales son:

- `catalog`: productos e inventario.
- `promotion`: cupones y políticas configurables.
- `checkout`: cotización, motor de descuentos y confirmación transaccional.
- `order`: agregado de orden, persistencia e historial.
- `shared`: errores de negocio y preocupaciones HTTP comunes.

## Flujo de checkout

```text
Solicitud
  -> validar carrito
  -> consultar productos/cupón/política
  -> calcular descuentos secuenciales
  -> [solo quote] responder sin cambios
  -> [checkout] bloquear productos y cupón
  -> recalcular y validar de nuevo
  -> disminuir inventario + marcar cupón usado + guardar orden
  -> confirmar transacción
```

`POST /checkout/quote` utiliza el mismo servicio de cálculo que la compra, pero consulta sin bloqueo y no persiste cambios. `POST /checkout` usa `@Transactional`, vuelve a consultar los recursos y los bloquea en PostgreSQL antes de modificar el estado.

## Reglas de descuentos

El motor usa Strategy y Pipeline:

1. `CategoryDiscountStrategy`: 10% a los productos de `TECHNOLOGY`.
2. `VolumeDiscountStrategy`: 5% si el total acumulado es estrictamente superior a $100.
3. `CouponDiscountStrategy`: porcentaje del cupón válido sobre el saldo restante.
4. `MaximumDiscountPolicy`: recorta el descuento final al porcentaje máximo configurado.

El límite máximo no es una estrategia de descuento. Es una política transversal que se aplica al final del pipeline; esa separación permite añadir reglas sin mezclar el control global con las promociones.

## Modelo de persistencia

```text
products 1 --- N order_items N --- 1 orders 1 --- N order_discounts
coupons                         (se consume una sola vez)
discount_policies               (límite de descuento configurable)
```

La orden conserva snapshots de nombre, precio y categoría de los productos. Los detalles de descuentos se guardan con secuencia, porcentaje y monto. No se duplica `coupon_code` en `orders`: el descuento de cupón ya proporciona la evidencia histórica necesaria.

## Decisiones relevantes

| Decisión | Motivo |
| --- | --- |
| `Money` como value object | Centraliza redondeo `HALF_UP` a dos decimales. |
| Descuentos secuenciales | Cada regla opera sobre el saldo restante y evita sumar porcentajes incorrectamente. |
| Límite como política | Separa una restricción global de las reglas promocionales. |
| Bloqueos pesimistas al confirmar | Evitan el doble consumo del cupón y sobreventa bajo concurrencia. |
| Scripts SQL idempotentes | Simplifican la evaluación local sin agregar Flyway o Docker fuera del alcance. |
| H2 aislado para pruebas | La suite no modifica inventario, cupones ni secuencias de la base PostgreSQL local. |
| JaCoCo enfocado en checkout | El umbral cubre el núcleo de cálculo y confirmación, no DTOs o mapeo de infraestructura. |

## Alcance deliberadamente excluido

No se añadieron Docker, Flyway, Testcontainers, ArchUnit, MapStruct ni Spring Modulith. Son herramientas válidas, pero no eran necesarias para resolver el alcance de la prueba y habrían aumentado la complejidad operativa sin mejorar el comportamiento funcional requerido.
