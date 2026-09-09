# E-commerce Checkout

Backend de una prueba técnica para un checkout de e-commerce. Calcula descuentos secuenciales, aplica un límite configurable, valida inventario y cupones, y persiste órdenes en PostgreSQL.

## Tecnologías

- Java 21
- Spring Boot 4.1.1
- Spring Web MVC, Validation y Data JPA
- PostgreSQL
- Maven Wrapper y JaCoCo

## Requisitos previos

- JDK 21 instalado y disponible en `PATH`.
- PostgreSQL en ejecución.
- Una base de datos local llamada `ecommerce_db`.

La configuración local por defecto está en `apps/backend/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/ecommerce_db
spring.datasource.username=postgres
spring.datasource.password=root
```

Estos valores se dejaron de forma explícita para la prueba técnica. No deben usarse como patrón para producción.

## Preparar la base de datos

En el gestor de PostgreSQL, conectado como un usuario con permiso para crear bases, ejecutar una sola vez:

```sql
CREATE DATABASE ecommerce_db;
```

Después, al arrancar el backend, Spring Boot ejecuta automáticamente `database/schema.sql` y `database/data.sql`. Ambos scripts son idempotentes: crean el esquema y los datos semilla si todavía no existen.

Si se prefiere ejecutar los scripts manualmente en el gestor, abrir y ejecutar primero [schema.sql](database/schema.sql) y luego [data.sql](database/data.sql), conectándose a `ecommerce_db`.

Para restaurar el estado de demostración tras crear órdenes o consumir el cupón, ejecutar [reset-database.sql](database/reset-database.sql). Este script elimina únicamente las órdenes de la demo y restablece inventario, cupón y política.

## Ejecutar la API

Desde la raíz del repositorio:

```powershell
cd apps/backend
.\mvnw.cmd spring-boot:run
```

La API queda disponible en `http://localhost:8080/api/v1`.

## Endpoints principales

| Método | Ruta | Descripción |
| --- | --- | --- |
| `GET` | `/products` | Catálogo de productos activos. |
| `POST` | `/checkout/quote` | Calcula una cotización sin modificar datos. |
| `POST` | `/checkout` | Confirma una compra, descuenta inventario, consume cupón y guarda la orden. |
| `GET` | `/orders` | Historial de órdenes, de más reciente a más antigua. |
| `GET` | `/orders/{orderId}` | Detalle de una orden con ítems y descuentos. |

Ejemplo de cuerpo para cotizar o confirmar:

```json
{
  "items": [
    { "productId": 1, "quantity": 1 }
  ],
  "couponCode": "WELCOME2026"
}
```

El endpoint de cotización no consume inventario, cupones ni crea órdenes. La confirmación reconsulta los recursos dentro de una transacción y usa bloqueos pesimistas para evitar que un producto o cupón se confirme con un estado obsoleto.

## Reglas de descuento

1. Los productos de tecnología reciben 10%.
2. Si el total restante es estrictamente mayor a $100, recibe 5% adicional.
3. Un cupón válido se aplica sobre el total restante.
4. El descuento total queda limitado por la política `MAX_TOTAL_DISCOUNT_PERCENTAGE`, inicialmente en 35%.

Los descuentos son secuenciales; nunca se suman porcentajes directamente.

## Pruebas y cobertura

```powershell
cd apps/backend
.\mvnw.cmd clean verify
```

El comando ejecuta las pruebas unitarias e integración, genera el reporte JaCoCo en `apps/backend/target/site/jacoco/index.html` y exige como mínimo 80% de cobertura de líneas para la lógica crítica de checkout. La verificación actual alcanza 95,2% en ese alcance.

## Postman

Importar [ecommerce-checkout.postman_collection.json](postman/ecommerce-checkout.postman_collection.json) en Postman. La colección guarda automáticamente el `orderId` de la compra confirmada para consultar su detalle.

Antes de volver a ejecutar el caso que consume `WELCOME2026`, restablecer la demo con [reset-database.sql](database/reset-database.sql).

## Documentación adicional

- [Arquitectura](docs/arquitectura.md)
- [Gobernanza y bitácora de IA](docs/ia.md)
