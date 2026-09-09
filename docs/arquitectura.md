# 🧭 Arquitectura

## 🎯 Propósito y criterio de diseño

DaviShop resuelve un MVP de checkout donde el riesgo principal no es la cantidad de pantallas, sino conservar la consistencia entre descuentos secuenciales, inventario, cupón y orden persistida. Por ello se eligió un monorepo con dos aplicaciones autónomas: Angular para una interfaz reactiva y Spring Boot para un núcleo de negocio transaccional.

La solución prioriza cuatro propiedades:

1. **Corrección del cálculo:** las promociones se aplican en secuencia y el límite se evalúa al final.
2. **Consistencia al confirmar:** la orden no confía en una cotización anterior; vuelve a consultar y bloquea los recursos necesarios.
3. **Claridad de responsabilidades:** el backend protege el dominio y el frontend organiza el flujo por capacidades visibles del usuario.
4. **Complejidad proporcional:** no se introducen microservicios, NgRx, Docker, Flyway, Testcontainers ni capas vacías que no aporten al alcance.

## 🗺️ Vista general

```mermaid
flowchart LR
    Customer[Cliente] --> Browser[Angular 22<br/>http://localhost:4200]
    Browser -->|REST JSON tipado| Api[Spring Boot 4.1.1<br/>http://localhost:8080/api/v1]
    Api -->|JPA + transacciones| Database[(PostgreSQL)]
    TestsFrontend[Vitest + TestBed + V8] -. verifica .-> Browser
    TestsBackend[Junit + H2 + JaCoCo] -. verifica .-> Api
    Postman[Postman] -->|REST| Api
```

## 🗂️ Monorepo

```text
.
├── apps/
│   ├── backend/                    # API Spring Boot
│   │   ├── database/                # schema, seed y reinicio de demo
│   │   ├── postman/                 # colección de pruebas HTTP
│   │   └── src/main/java/com/ecommerce/core/
│   │       ├── catalog/
│   │       ├── promotion/
│   │       ├── checkout/
│   │       ├── order/
│   │       └── shared/
│   └── frontend/                   # Aplicación Angular
│       └── src/app/
│           ├── core/api/
│           ├── features/catalog/
│           ├── features/cart/
│           ├── features/checkout/
│           └── features/orders/
├── docs/                            # arquitectura y gobernanza de IA
└── .ai/                             # skill y agente de revisión
```

`packages/` no se creó: el contrato compartido habría agregado una dependencia de build entre Java y TypeScript sin necesidad real. Cada borde HTTP posee sus DTOs tipados y pruebas de contrato en su propia aplicación.

## ☕ Backend: monolito modular con arquitectura hexagonal pragmática

Cada módulo de negocio contiene dominio, aplicación e infraestructura. Se mantienen los puertos donde el desacoplamiento sí cambia el costo de evolución: repositorios, casos de uso y adaptadores web/persistencia.

```mermaid
flowchart TB
    subgraph Web[Adaptador de entrada HTTP]
        Controller[Controllers]
        RequestDto[DTOs de request/response]
        WebMapper[Mappers web]
    end

    subgraph Application[Aplicación]
        UseCase[Puertos de casos de uso]
        Services[Servicios de aplicación]
        RepositoryPort[Puertos de repositorio]
    end

    subgraph Domain[Dominio puro]
        Aggregate[Productos, cupón, orden y dinero]
        Rules[Reglas y estrategias de descuento]
        Policy[Política de descuento máximo]
    end

    subgraph Persistence[Adaptador de salida]
        JpaAdapter[Adaptadores JPA]
        JpaEntities[Entidades JPA]
        PostgreSQL[(PostgreSQL)]
    end

    Controller --> RequestDto --> WebMapper --> UseCase --> Services
    Services --> Aggregate
    Services --> Rules
    Rules --> Policy
    Services --> RepositoryPort --> JpaAdapter --> JpaEntities --> PostgreSQL
```

### 🧩 Módulos de backend

| Módulo | Responsabilidad |
| --- | --- |
| `catalog` | Consulta de productos activos e inventario. |
| `promotion` | Consulta de cupones y política máxima configurable. |
| `checkout` | Cotización, confirmación, motor de descuentos y coordinación transaccional. |
| `order` | Agregado persistido, snapshots, listado y detalle. |
| `shared` | Errores de negocio y mapeo HTTP común. |

### 🧠 Patrones aplicados explícitamente

| Patrón | Aplicación | Beneficio |
| --- | --- | --- |
| **Strategy** | `CategoryDiscountStrategy`, `VolumeDiscountStrategy` y `CouponDiscountStrategy`. | Añadir una regla no obliga a condicionar un servicio central ni cambiar reglas existentes. |
| **Pipeline** | `DiscountEngine` ejecuta las estrategias por secuencia. | Conserva el cálculo multiplicativo sobre el saldo y deja evidencia ordenada de cada descuento. |
| **Policy** | `MaximumDiscountPolicy` se aplica después del pipeline. | Evita tratar el tope global como una promoción y mezcla de responsabilidades. |
| **Ports and Adapters** | Casos de uso y repositorios dependen de puertos; HTTP/JPA los adaptan. | El dominio no depende de Spring, JPA ni DTOs web. |

## 🅰️ Frontend: arquitectura modular por feature

Angular se organiza por funcionalidad de producto y no replica las capas del backend. Es una elección deliberada: la aplicación tiene pocas pantallas y una lógica local acotada; una arquitectura limpia completa o NgRx agregarían archivos y ceremonias sin aumentar la corrección.

```mermaid
flowchart LR
    subgraph Core[core/api]
        BaseUrl[API_BASE_URL]
        Http[HttpClient]
        ApiError[Contrato ApiError]
    end

    subgraph Catalog[features/catalog]
        CatalogPage[CatalogPage]
        ProductApi[ProductApiService]
        ProductCard[ProductCard]
    end

    subgraph Cart[features/cart]
        CartStore[CartStore<br/>Signals]
    end

    subgraph Checkout[features/checkout]
        QuoteStore[CheckoutQuoteStore<br/>Signals + switchMap]
        ProcessStore[CheckoutProcessStore]
        CheckoutApi[CheckoutApiService]
        Success[CheckoutSuccessPage]
    end

    subgraph Orders[features/orders]
        OrdersPage[OrdersPage]
        DetailPage[OrderDetailPage]
        OrdersApi[OrderApiService]
    end

    CatalogPage --> ProductApi --> Http
    CatalogPage --> ProductCard
    CatalogPage --> CartStore
    CartStore --> QuoteStore
    QuoteStore --> CheckoutApi --> Http
    CatalogPage --> ProcessStore
    ProcessStore --> CheckoutApi
    ProcessStore --> Success
    OrdersPage --> OrdersApi --> Http
    DetailPage --> OrdersApi
    BaseUrl --> ProductApi
    BaseUrl --> CheckoutApi
    BaseUrl --> OrdersApi
    ApiError --> QuoteStore
    ApiError --> ProcessStore
```

### ⚡ Estado y reactividad

```mermaid
sequenceDiagram
    participant User as Cliente
    participant Catalog as CatalogPage
    participant Cart as CartStore (Signals)
    participant Quote as CheckoutQuoteStore
    participant API as POST /checkout/quote

    User->>Catalog: agrega, disminuye o elimina un producto
    Catalog->>Cart: actualiza cantidad
    Cart-->>Catalog: subtotal y items computados
    Catalog->>Quote: refreshQuote(items, coupon)
    Quote->>API: solicita cotización
    API-->>Quote: descuentos, total y flag de cap
    Quote-->>Catalog: Signals de quote, error o loading
    Catalog-->>User: desglose y alerta persistente si aplica
```

- **Signals** almacenan el estado local y síncrono: carrito, estado de cotización, confirmación y listas de las páginas.
- **RxJS** se usa solo en bordes asíncronos. `switchMap` cancela el resultado obsoleto cuando el usuario modifica el carrito antes de que responda la cotización.
- **Servicios API** encapsulan `HttpClient` y contratos; las páginas no construyen URLs ni interpretan respuestas HTTP directamente.
- **TypeScript estricto y plantillas estrictas** están habilitados mediante `strict: true` y `strictTemplates: true`; no se usan `any` genéricos.

## 🔄 Flujo de checkout consistente

```mermaid
sequenceDiagram
    actor Customer as Cliente
    participant UI as Angular
    participant Controller as CheckoutController
    participant Pricing as CheckoutPricingService
    participant Products as ProductRepository
    participant Coupons as CouponRepository
    participant Engine as DiscountEngine
    participant Orders as OrderRepository

    Customer->>UI: confirma carrito y cupón
    UI->>Controller: POST /checkout
    Controller->>Pricing: process(command)
    Pricing->>Products: consulta con bloqueo pesimista
    Pricing->>Coupons: consulta cupón con bloqueo pesimista
    Pricing->>Engine: calcula descuentos secuenciales y cap
    Engine-->>Pricing: breakdown inmutable
    Pricing->>Products: descuenta inventario
    Pricing->>Coupons: marca cupón usado
    Pricing->>Orders: persiste orden, ítems y descuentos
    Pricing-->>Controller: orden confirmada
    Controller-->>UI: 201 con resumen exacto
    UI-->>Customer: confirmación y detalle
```

`POST /checkout/quote` reutiliza el mismo cálculo, pero no bloquea ni persiste. `POST /checkout` vuelve a consultar dentro de una transacción y no confía en la cotización mostrada previamente.

## 💸 Motor de descuentos

```mermaid
flowchart TD
    Start[Subtotal original] --> Category{¿Hay tecnología?}
    Category -->|Sí| CategoryDiscount[10% sobre productos de tecnología]
    Category -->|No| Volume
    CategoryDiscount --> Volume{¿Saldo restante > USD 100?}
    Volume -->|Sí| VolumeDiscount[5% sobre todo el carrito]
    Volume -->|No| Coupon
    VolumeDiscount --> Coupon{¿Cupón válido?}
    Coupon -->|Sí| CouponDiscount[Porcentaje del cupón sobre saldo]
    Coupon -->|No| Cap
    CouponDiscount --> Cap{¿Descuento efectivo > política máxima?}
    Cap -->|Sí| Clamp[Recortar exactamente al máximo]
    Cap -->|No| Result[Desglose y total final]
    Clamp --> Result
```

Las estrategias trabajan sobre el saldo dejado por la anterior; los porcentajes no se suman. La política máxima no es una estrategia: es una restricción transversal aplicada una sola vez al final.

## 🗃️ Modelo de datos

```mermaid
erDiagram
    PRODUCTS ||--o{ ORDER_ITEMS : "referenciado por"
    ORDERS ||--|{ ORDER_ITEMS : "contiene"
    ORDERS ||--o{ ORDER_DISCOUNTS : "conserva"

    PRODUCTS {
        bigint id PK
        varchar name
        numeric unit_price
        varchar category
        int stock
        boolean active
        varchar image_url
    }
    COUPONS {
        bigint id PK
        varchar code UK
        numeric percentage
        boolean active
        timestamptz expires_at
        timestamptz used_at
    }
    DISCOUNT_POLICIES {
        bigint id PK
        varchar code UK
        numeric value
        timestamptz updated_at
    }
    ORDERS {
        bigint id PK
        numeric original_subtotal
        numeric calculated_discount_before_cap
        numeric total_discount
        numeric effective_discount_percentage
        numeric final_total
        boolean discount_cap_applied
        timestamptz created_at
    }
    ORDER_ITEMS {
        bigint id PK
        bigint order_id FK
        bigint product_id FK
        varchar product_name
        numeric unit_price
        varchar category
        int quantity
    }
    ORDER_DISCOUNTS {
        bigint id PK
        bigint order_id FK
        varchar type
        varchar name
        numeric percentage
        numeric amount
        int sequence
    }
```

`order_items` conserva snapshots de nombre, precio y categoría. Así una orden histórica no cambia si el catálogo evoluciona. El cupón no se duplica como `coupon_code` en `orders`: el registro en `order_discounts` constituye la evidencia aplicada y evita inconsistencias. `discount_policies` y `coupons` se resuelven al procesar una compra, pero no se convierten en claves históricas de la orden.

## 🔌 API y contratos

| Método | Ruta | Consumidor | Responsabilidad |
| --- | --- | --- | --- |
| `GET` | `/api/v1/products` | Catálogo Angular | Productos activos con stock. |
| `POST` | `/api/v1/checkout/quote` | `CheckoutQuoteStore` | Cotización pura para reaccionar al carrito. |
| `POST` | `/api/v1/checkout` | `CheckoutProcessStore` | Confirmación transaccional y respuesta `201`. |
| `GET` | `/api/v1/orders` | `OrdersPageComponent` | Historial de órdenes. |
| `GET` | `/api/v1/orders/{id}` | `OrderDetailPageComponent` | Ítems y descuentos persistidos. |

Los contratos del frontend son interfaces inmutables (`readonly`). Los errores de negocio conservan `status`, `code`, `message`, `path` y validaciones para que la UI muestre el mensaje del backend sin acoplarse a excepciones Java.

## 🧪 Pruebas y calidad

```mermaid
flowchart TB
    UnitFrontend[Frontend unitario<br/>CartStore, servicios HTTP, quote, checkout, alertas y páginas]
    UnitBackend[Backend unitario<br/>Money, estrategias, motor, cupón y servicios]
    IntegrationBackend[Backend integración H2<br/>persistencia, confirmación y promociones]
    Http[Postman<br/>flujo REST manual]

    UnitFrontend --> CoverageV8[Reporte V8 >= 80%]
    UnitBackend --> JaCoCo[JaCoCo checkout >= 80%]
    IntegrationBackend --> JaCoCo
    Http --> Demo[Demostración de extremo a extremo]
```

- Frontend: Vitest/TestBed/V8 valida carrito, stock, solicitud de quote, errores, cap, confirmación, listado y detalle.
- Backend: JUnit y H2 aislado cubren secuencia, umbrales, cupones, stock, cap y confirmación persistida.
- JaCoCo se enfoca en `checkout` y excluye infraestructura; se mide la lógica donde se toman decisiones de negocio, no DTOs triviales.
- La aplicación se verificó mediante `npm run test:coverage`, `npm run build` y `.\mvnw.cmd verify`.

## ⚖️ Decisiones y trade-offs

| Decisión | Beneficio | Trade-off aceptado |
| --- | --- | --- |
| Spring Boot modular y hexagonal pragmática | Dominio protegido y pruebas aisladas. | Más archivos que un CRUD directo. |
| Angular por features | Navegación y capacidades fáciles de ubicar. | No existe una capa compartida de dominio frontend, porque hoy no es necesaria. |
| Signals locales + RxJS en HTTP | Estado simple y cancelación de quotes obsoletos. | No se incorpora time-travel ni devtools de un store global. |
| PostgreSQL local + scripts SQL | Inicio reproducible y fácil de evaluar. | Migraciones versionadas avanzadas se dejan fuera del MVP. |
| H2 solo en pruebas | Pruebas rápidas sin alterar la demo PostgreSQL. | No sustituye un entorno de integración con PostgreSQL real. |
| Sin Docker, Flyway, NgRx, Testcontainers ni microservicios | Menor costo operativo y foco en reglas críticas. | Esas herramientas serían candidatas si el sistema creciera. |

## 🔭 Riesgos conocidos y evolución

- Las reglas semilla actuales no llegan por sí solas al cap de 35%; se prueba el comportamiento del motor con una política que lo activa y se prueba la alerta de UI con una respuesta tipada que marca `discountCapApplied`.
- Si aparecen más promociones, se agregan nuevas estrategias y se mantienen sus secuencias sin modificar las existentes.
- Si se requieren autenticación, pagos externos o alta concurrencia distribuida, se incorporarían identidad, idempotencia de pagos y observabilidad antes de dividir el monolito.
