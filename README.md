# 👨‍💻 Autor: Juan Sierra M.

### Software Engineer

# 🛒 DaviShop · Core E-Commerce Checkout

Aplicación Full Stack para un checkout de e-commerce con descuentos acumulativos, límite máximo de ahorro, control de inventario, cupón de un solo uso e historial de órdenes. El repositorio es un monorepo con Angular en el frontend y Spring Boot en el backend.

## ✨ Capacidades principales

- Catálogo reactivo con productos, categoría, precio y stock disponible.
- Carrito con cantidades, subtotal original y validación de stock en la interfaz.
- Cotización sin efectos secundarios mediante descuentos secuenciales: categoría, volumen y cupón.
- Límite absoluto de descuento y alerta visual persistente cuando el backend lo informa.
- Confirmación transaccional que vuelve a validar los recursos, disminuye inventario, consume el cupón y persiste snapshots de la orden.
- Historial y detalle de órdenes.
- Tipado estricto de TypeScript y contratos HTTP explícitos.

La arquitectura, los diagramas y los trade-offs están en [docs/arquitectura.md](docs/arquitectura.md). La evidencia de uso responsable de IA está en [docs/ia.md](docs/ia.md).

## 🧰 Tecnologías

| Capa | Tecnologías |
| --- | --- |
| Frontend | Angular 22, TypeScript estricto, Signals, RxJS, SCSS, Vitest y cobertura V8 |
| Backend | Java 21, Spring Boot 4.1.1, Spring Web MVC, Validation, Spring Data JPA y Maven Wrapper |
| Persistencia | PostgreSQL para ejecución local; H2 aislado para pruebas backend |
| Calidad | JaCoCo para el núcleo backend y cobertura V8 para el frontend |

## ✅ Requisitos previos

- JDK 21 disponible en `PATH`.
- Node.js compatible con npm 11.
- PostgreSQL en ejecución en `localhost:5432`.
- Opcional: Postman para importar la colección de pruebas HTTP.

## 🚀 Inicio rápido

### 1. 🗃️ Crear la base de datos

Conéctate a PostgreSQL como un usuario con permiso para crear bases y ejecuta una sola vez:

```sql
CREATE DATABASE ecommerce_db;
```

La configuración local intencionalmente explícita para la prueba está en [application.properties](apps/backend/src/main/resources/application.properties):

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/ecommerce_db
spring.datasource.username=postgres
spring.datasource.password=root
```

> La contraseña `root` es solo una comodidad de desarrollo solicitada para esta prueba; no representa una configuración de producción.

Al iniciar el backend desde `apps/backend`, Spring ejecuta automáticamente [schema.sql](apps/backend/database/schema.sql) y [data.sql](apps/backend/database/data.sql). Los scripts son idempotentes, por lo que crean el esquema y los datos semilla sin duplicarlos.

Si prefieres ejecutarlos desde tu gestor de base de datos, conéctate a `ecommerce_db` y ejecuta primero `apps/backend/database/schema.sql` y después `apps/backend/database/data.sql`.

### 2. ⚙️ Iniciar el backend

Desde la raíz del repositorio:

```powershell
cd apps/backend
.\mvnw.cmd spring-boot:run
```

La API quedará disponible en `http://localhost:8080/api/v1`. El CORS local permite `http://localhost:4200`.

### 3. 🖥️ Iniciar el frontend

En otra terminal, desde la raíz:

```powershell
cd apps/frontend
npm ci
npm run start
```

Abre `http://localhost:4200` en el navegador.

## 🎬 Flujo de demostración

1. Agrega productos desde el catálogo; el carrito actualiza cantidades y subtotal.
2. Combina productos de tecnología y supera $100 para visualizar categoría y volumen.
3. Ingresa `WELCOME2026` y pulsa **Aplicar** para agregar el descuento de cupón.
4. Confirma la compra. El backend devuelve una orden persistida y el frontend muestra su resultado.
5. Abre **Mis órdenes** para consultar el listado y el detalle.

`WELCOME2026` se consume al confirmar una orden. Para restaurar inventario, cupón, política y órdenes de demostración, ejecuta [reset-database.sql](apps/backend/database/reset-database.sql) sobre `ecommerce_db`.

> Con las reglas fijas de 10%, 5% y 15% aplicadas en cascada, el máximo matemático de los datos estándar es 27.325%. Por esa razón el tope de 35% no se activa con el seed por defecto; el backend y el frontend sí cubren el caso cuando una política o respuesta lo indique.

## 🔌 Endpoints

| Método | Ruta | Efecto |
| --- | --- | --- |
| `GET` | `/products` | Obtiene el catálogo activo. |
| `POST` | `/checkout/quote` | Calcula descuentos sin consumir stock, cupón ni crear órdenes. |
| `POST` | `/checkout` | Revalida, bloquea recursos, confirma la compra y devuelve el resumen. |
| `GET` | `/orders` | Consulta las órdenes persistidas. |
| `GET` | `/orders/{orderId}` | Consulta ítems y descuentos de una orden. |

Ejemplo para cotizar o confirmar:

```json
{
  "items": [
    { "productId": 1, "quantity": 1 },
    { "productId": 2, "quantity": 1 }
  ],
  "couponCode": "WELCOME2026"
}
```

## 🧪 Pruebas y cobertura

### ☕ Backend

```powershell
cd apps/backend
.\mvnw.cmd verify
```

La suite utiliza H2 aislado para no modificar PostgreSQL local y JaCoCo exige al menos 80% de cobertura de líneas para el núcleo de checkout. Si el backend está ejecutándose en Windows, usa `verify` como arriba; para `clean verify`, primero detén el proceso porque puede mantener bloqueado un archivo de `target`.

El reporte se genera en `apps/backend/target/site/jacoco/index.html`.

### 🅰️ Frontend

```powershell
cd apps/frontend
npm run test:coverage
npm run build
```

La suite usa Vitest, TestBed y cobertura V8. El comando de cobertura actual supera el 80% requerido en la lógica esencial de carrito y checkout.

## 📮 Colección Postman

Importa [ecommerce-checkout.postman_collection.json](apps/backend/postman/ecommerce-checkout.postman_collection.json). Incluye catálogo, cotización, confirmación, consulta de órdenes y casos de error. La colección guarda el `orderId` creado para consultar su detalle.

## 🗂️ Estructura del repositorio

```text
.
├── apps/
│   ├── backend/                 # Spring Boot: módulos hexagonales pragmáticos
│   │   ├── database/            # Esquema, seed y reinicio de demo PostgreSQL
│   │   └── postman/             # Colección de validación HTTP
│   └── frontend/                # Angular: organización modular por feature
├── docs/                        # Arquitectura y gobernanza de IA
└── .ai/                         # Skill y agente definidos para la revisión
```

## 📚 Documentación

- [Arquitectura, diagramas y decisiones](docs/arquitectura.md)
- [Gobernanza, skill, agente y bitácora de IA](docs/ia.md)
- [Colección Postman](apps/backend/postman/ecommerce-checkout.postman_collection.json)
